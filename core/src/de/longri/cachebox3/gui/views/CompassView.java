/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.skin.styles.AttributesStyle;
import de.longri.cachebox3.gui.skin.styles.CompassViewStyle;
import de.longri.cachebox3.gui.widgets.CacheSizeWidget;
import de.longri.cachebox3.gui.widgets.ColorWidget;
import de.longri.cachebox3.gui.widgets.Compass;
import de.longri.cachebox3.gui.widgets.Stars;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.SkinColor;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Longri on 24.07.16.
 */
public class CompassView extends AbstractView implements PositionChangedListener, OrientationChangedListener {

    private static final Logger log = LoggerFactory.getLogger(CompassView.class);

    private final CompassPanel compassPanel;
    private final VisSplitPane splitPane;
    private final Table topTable, bottomTable;
    private final CompassViewStyle style;
    private final Image backgroundWidget;

    private CoordinateGPS actCoord;
    private float actHeading = 0;
    private Label targetdirectionLabel, ownPositionLabel;

    private boolean resetLayout, showMap, showName, showIcon, showAtt, showGcCode, showCoords, showWpDesc, showSatInfos, showSunMoon, showAnyContent, showTargetDirection, showSDT, showLastFound;

    public CompassView() {
        super("CompassView");

        style = VisUI.getSkin().get("compassViewStyle", CompassViewStyle.class);

        //background
        backgroundWidget = new Image(style.background);
        backgroundWidget.setBounds(0, 0, this.getWidth(), this.getHeight());
        this.addActor(backgroundWidget);


        topTable = new Table();
        bottomTable = new Table();

//        topTable.setDebug(true, true);
//        bottomTable.setDebug(true, true);

        topTable.setBackground(style.splitBackground);
        bottomTable.setBackground(style.splitBackground);


        VisSplitPane.VisSplitPaneStyle visSplitPaneStyle = new VisSplitPane.VisSplitPaneStyle();
        visSplitPaneStyle.handle = style.splitHandle;
        splitPane = new VisSplitPane(topTable, bottomTable, true, visSplitPaneStyle);
        this.addChild(splitPane);

        compassPanel = new CompassPanel(style);
        bottomTable.add(compassPanel).expand().fill().center();

        readSettings();
    }

    private void layoutInfoPanel() {
        topTable.clear();

        Label.LabelStyle infoStyle = new Label.LabelStyle();
        infoStyle.font = style.infoFont;
        infoStyle.fontColor = style.infoColor;

        Cache actCache = null;
        Waypoint actWaypoint = null;

        if (EventHandler.getSelectedWaypoint() == null) {
            actCache = EventHandler.getSelectedCache();
        } else {
            actWaypoint = EventHandler.getSelectedWaypoint();
        }

        //1. line
        if (showName || showIcon) {
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            Image icon = null;
            Label nameLabel = null;

            if (showIcon) {
                icon = new Image();
                icon.setDrawable(actWaypoint == null ? actCache.Type.getDrawable() : actWaypoint.Type.getDrawable());
            }

            if (showName) {
                nameLabel = new Label("", infoStyle);
                nameLabel.setText(actWaypoint == null ? actCache.getName() : actWaypoint.getTitle());
            }
            lineTable.add(icon);
            lineTable.add(nameLabel);
            topTable.add(lineTable).left();
            topTable.row();
        }

        if (showGcCode || showCoords) {
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            if (showCoords) {
                if (actWaypoint == null) {
                    lineTable.add(new Label(actCache.FormatCoordinate(), infoStyle));
                } else {
                    lineTable.add(new Label(actWaypoint.FormatCoordinate(), infoStyle));
                }
            }
            if (showGcCode) {
                lineTable.add(new Label(actCache.getGcCode(), infoStyle));
            }
            topTable.add(lineTable).left();
            topTable.row();
        }

        if (showSatInfos) {
            ownPositionLabel = new Label(Translation.Get("waiting_for_fix"), infoStyle);
            topTable.add(ownPositionLabel).left();
            topTable.row();
        }

        if (showTargetDirection) {
            targetdirectionLabel = new Label(Translation.Get("waiting_for_fix"), infoStyle);
            topTable.add(targetdirectionLabel).left();
            topTable.row();
        }

        if (showSDT) {
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            VisLabel dLabel = new VisLabel("D", infoStyle);
            lineTable.left();
            lineTable.add(dLabel);
            Stars difficultyStars = new Stars((int) (actCache.getDifficulty() * 2));
            lineTable.add(difficultyStars);
            VisLabel sLabel = new VisLabel(actCache.Size.toShortString(), infoStyle);
            lineTable.add(sLabel).padLeft(CB.scaledSizes.MARGIN);
            CacheSizeWidget sizeWidget = new CacheSizeWidget(actCache.Size);
            lineTable.add(sizeWidget).padLeft(CB.scaledSizes.MARGIN_HALF);
            VisLabel tLabel = new VisLabel("T", infoStyle);
            lineTable.left();
            lineTable.add(tLabel);
            Stars terrainStars = new Stars((int) (actCache.getTerrain() * 2));
            lineTable.add(terrainStars);
            VisLabel vLabel = new VisLabel("GcV", infoStyle);
            lineTable.add(vLabel).padLeft(CB.scaledSizes.MARGIN);
            Stars vStars = new Stars((int) Math.min(actCache.Rating * 2, 5 * 2));
            lineTable.add(vStars);
            topTable.add(lineTable).left();
            topTable.row();
        }

        // add Attribute
        if (showAtt) {
            AttributesStyle attStyle = VisUI.getSkin().get("CompassView", AttributesStyle.class);
            ArrayList<Attributes> attList = actCache.getAttributes();
            float iconWidth = 0, iconHeight = 0;
            int lineBreak = 0, lineBreakStep = 0;
            Table lineTable = null;
            for (int i = 0, n = attList.size(); i < n; i++) {
                Drawable attDrawable = attList.get(i).getDrawable(attStyle);
                if (attDrawable != null) {
                    if (iconWidth == 0) {
                        iconWidth = attDrawable.getMinWidth();
                        iconHeight = attDrawable.getMinHeight();
                        lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + CB.scaledSizes.MARGINx2));
                        lineTable = new Table();
                        lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                    }
                    lineTable.add(new Image(attDrawable)).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
                    if (i >= lineBreak) {
                        topTable.add(lineTable).left();
                        topTable.row();
                        lineTable = new Table();
                        lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                        lineBreak += lineBreakStep;
                    }
                }
            }
            topTable.add(lineTable).left();
            topTable.row();

        }
        resetLayout = false;
    }

    @Override
    public void onShow() {
        if (resetLayout)
            readSettings();
        EventHandler.add(this);
    }

    @Override
    public void onHide() {
        EventHandler.remove(this);
    }

    private void readSettings() {
        showMap = Config.CompassShowMap.getValue();
        showName = Config.CompassShowWP_Name.getValue();
        showIcon = Config.CompassShowWP_Icon.getValue();
        showAtt = Config.CompassShowAttributes.getValue();
        showGcCode = Config.CompassShowGcCode.getValue();
        showCoords = Config.CompassShowCoords.getValue();
        showWpDesc = Config.CompassShowWpDesc.getValue();
        showSatInfos = Config.CompassShowSatInfos.getValue();
        showSunMoon = Config.CompassShowSunMoon.getValue();
        showTargetDirection = Config.CompassShowTargetDirection.getValue();
        showSDT = Config.CompassShowSDT.getValue();
        showLastFound = Config.CompassShowLastFound.getValue();

        showAnyContent = showMap || showName || showIcon || showAtt || showGcCode || showCoords || showWpDesc || showSatInfos || showSunMoon || showTargetDirection || showSDT || showLastFound;

        layoutInfoPanel();

        // get last Coord and set values
        actCoord = EventHandler.getMyPosition();
        actHeading = EventHandler.getHeading();
        refreshOrientationInfo();
    }


    @Override
    public void layout() {
        super.layout();
        splitPane.setBounds(0, 0, this.getWidth(), this.getHeight());
    }


    @Override
    public void dispose() {
        EventHandler.remove(this);
    }

    public void resetLayout() {
        resetLayout = true;
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
        actCoord = event.pos;
        refreshOrientationInfo();
    }

    @Override
    public void orientationChanged(OrientationChangedEvent event) {
        actHeading = event.orientation;
        refreshOrientationInfo();
    }

    private void refreshOrientationInfo() {

        if (actCoord == null) {
            actCoord = EventHandler.getMyPosition();
        }

        if (actCoord == null) {
            log.debug("No own position, return refresh Compass ");
            return;
        }

        if (ownPositionLabel != null) {
            ownPositionLabel.setText(actCoord.FormatCoordinate());
        }


        Cache actCache = null;
        Waypoint actWaypoint = null;

        if (EventHandler.getSelectedWaypoint() == null) {
            actCache = EventHandler.getSelectedCache();
        } else {
            actWaypoint = EventHandler.getSelectedWaypoint();
        }
        Coordinate dest = actWaypoint != null ? actWaypoint : actCache;

        float result[] = new float[4];

        try {
            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, actCoord.getLatitude(),
                    actCoord.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);
        } catch (Exception e) {
            log.error("Compute distance and bearing", e);
            return;
        }

        float distance = result[0];
        float bearing = result[1];

        compassPanel.setInfo(distance, actHeading, bearing, actCoord.getAccuracy());

        if (targetdirectionLabel != null) {
            double directionToTarget = 0;
            if (bearing < 0)
                directionToTarget = 360 + bearing;
            else
                directionToTarget = bearing;

            String sBearing = Translation.Get("directionToTarget") + " : " +
                    String.format("%.0f", directionToTarget) + "°";
            targetdirectionLabel.setText(sBearing);
        }

        CB.requestRendering();
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        backgroundWidget.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    // holds the Compass, the distance label and the Sun/Moon drawables
    private static class CompassPanel extends WidgetGroup {

        private final Compass compass;
        private final Label distance, accurate;

        private CompassPanel(CompassViewStyle style) {
            compass = new Compass(style);

            Label.LabelStyle distanceStyle = new Label.LabelStyle();
            Label.LabelStyle accurateStyle = new Label.LabelStyle();

            distanceStyle.background = style.distanceBackground;
            distanceStyle.font = style.distnaceFont;
            distanceStyle.fontColor = style.distanceColor;

            accurateStyle.font = style.accurateFont;
            accurateStyle.fontColor = style.accurateColor;

            distance = new Label("distance", distanceStyle);
            accurate = new Label("accurate", accurateStyle);

            distance.setAlignment(Align.left);
            accurate.setAlignment(Align.right);

            this.addActor(distance);
            this.addActor(accurate);
            this.addActor(compass);
        }

        @Override
        public void sizeChanged() {
            super.sizeChanged();

            float height = distance.getPrefHeight();
            float yPos = this.getHeight() - height - CB.scaledSizes.MARGIN;
            compass.setBounds(0, 0, this.getWidth(), this.getHeight() - height + CB.scaledSizes.MARGINx4);
            distance.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);
            accurate.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);
        }

        public void setInfo(float distance, float heading, float bearing, float accurate) {
            this.distance.setText(UnitFormatter.distanceString(distance, false));
            this.accurate.setText("  +/- " + UnitFormatter.distanceString(accurate, true));
            compass.setBearing(heading);
            compass.setHeading(bearing - (360 - heading));
        }
    }

    public String toString() {
        return "CompassView";
    }
}
