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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.menu.*;
import de.longri.cachebox3.gui.skin.styles.AttributesStyle;
import de.longri.cachebox3.gui.skin.styles.CompassViewStyle;
import de.longri.cachebox3.gui.widgets.CacheSizeWidget;
import de.longri.cachebox3.gui.widgets.Compass;
import de.longri.cachebox3.gui.widgets.Stars;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.SettingBool;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final float result[] = new float[4];

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
        bottomTable = new Table() {
            public void sizeChanged() {
                compassPanel.sizeChanged();
            }
        };


        topTable.setBackground(style.splitBackground);
        bottomTable.setBackground(style.splitBackground);


        VisSplitPane.VisSplitPaneStyle visSplitPaneStyle = new VisSplitPane.VisSplitPaneStyle();
        visSplitPaneStyle.handle = style.splitHandle;

        VisScrollPane scrollPane = new VisScrollPane(topTable);

        splitPane = new VisSplitPane(scrollPane, bottomTable, true, visSplitPaneStyle);
        this.addChild(splitPane);

        compassPanel = new CompassPanel(style);
        bottomTable.add(compassPanel).expand().fill().center();

        readSettings();

        splitPane.setMinSplitAmount(0.25f);
        splitPane.setMaxSplitAmount(0.59f);


    }

    private void layoutInfoPanel() {
        topTable.clear();

        Label.LabelStyle infoStyle = new Label.LabelStyle();
        infoStyle.font = style.infoFont;
        infoStyle.fontColor = style.infoColor;

        AbstractCache actAbstractCache = null;
        AbstractWaypoint actWaypoint = null;

        if (EventHandler.getSelectedWaypoint() == null) {
            actAbstractCache = EventHandler.getSelectedCache();
        } else {
            actWaypoint = EventHandler.getSelectedWaypoint();
        }

        boolean actCacheNotNull = actAbstractCache != null;
        boolean actWpNotNull = actWaypoint != null;
        //1. line
        if (actCacheNotNull && (showName || showIcon)) {
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            Image icon = null;
            Label nameLabel = null;

            if (showIcon) {
                icon = new Image();
                icon.setDrawable(actWaypoint == null ? actAbstractCache.getType().getDrawable() : actWaypoint.getType().getDrawable());
            }

            if (showName) {
                nameLabel = new Label("", infoStyle);
                nameLabel.setText(actWaypoint == null ? actAbstractCache.getName() : actWaypoint.getTitle());
            }
            lineTable.add(icon);
            lineTable.add(nameLabel);
            topTable.add(lineTable).left();
            topTable.row();
        }

        if (actCacheNotNull && (showGcCode || showCoords)) {
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            if (showCoords) {
                if (actWaypoint == null) {
                    lineTable.add(new Label(actAbstractCache.FormatCoordinate(), infoStyle));
                } else {
                    lineTable.add(new Label(actWaypoint.FormatCoordinate(), infoStyle));
                }
            }
            if (showGcCode) {
                lineTable.add(new Label(actAbstractCache.getGcCode(), infoStyle));
            }
            topTable.add(lineTable).left();
            topTable.row();
        }

        if (showSatInfos) {
            ownPositionLabel = new Label(Translation.get("waiting_for_fix"), infoStyle);
            topTable.add(ownPositionLabel).left();
            topTable.row();
        }

        if (showTargetDirection) {
            targetdirectionLabel = new Label(Translation.get("waiting_for_fix"), infoStyle);
            topTable.add(targetdirectionLabel).left();
            topTable.row();
        }

        if (actCacheNotNull && showSDT) {
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            VisLabel dLabel = new VisLabel("D", infoStyle);
            lineTable.left();
            lineTable.add(dLabel);
            Stars difficultyStars = new Stars((int) (actAbstractCache.getDifficulty() * 2), style.starStyle);
            lineTable.add(difficultyStars);
            VisLabel sLabel = new VisLabel(actAbstractCache.getSize().toShortString(), infoStyle);
            lineTable.add(sLabel).padLeft(CB.scaledSizes.MARGIN);
            CacheSizeWidget sizeWidget = new CacheSizeWidget(actAbstractCache.getSize(), style.cacheSizeStyle);
            lineTable.add(sizeWidget).padLeft(CB.scaledSizes.MARGIN_HALF);
            VisLabel tLabel = new VisLabel("T", infoStyle);
            lineTable.left();
            lineTable.add(tLabel);
            Stars terrainStars = new Stars((int) (actAbstractCache.getTerrain() * 2), style.starStyle);
            lineTable.add(terrainStars);
            VisLabel vLabel = new VisLabel("GcV", infoStyle);
            lineTable.add(vLabel).padLeft(CB.scaledSizes.MARGIN);
            Stars vStars = new Stars((int) Math.min(actAbstractCache.getRating() * 2, 5 * 2), style.starStyle);
            lineTable.add(vStars);
            topTable.add(lineTable).left();
            topTable.row();
        }

        // add Attribute
        if (actCacheNotNull && showAtt) {
            AttributesStyle attStyle = VisUI.getSkin().get("CompassView", AttributesStyle.class);
            Array<Attributes> attList = actAbstractCache.getAttributes(Database.Data);
            float iconWidth = 0, iconHeight = 0;
            int lineBreak = 0, lineBreakStep = 0;
            Table lineTable = null;
            for (int i = 0, n = attList.size; i < n; i++) {
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
        splitPane.setSplitAmount(Config.CompassViewSplit.getValue());
    }

    @Override
    public void onHide() {
        EventHandler.remove(this);
        Config.CompassViewSplit.setValue(splitPane.getSplit());
        Config.AcceptChanges();
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
        targetdirectionLabel = null;
        ownPositionLabel = null;
        SnapshotArray<Actor> childs = this.getChildren();
        for (int i = 0, n = childs.size - 1; i < n; i++) {
            this.removeChild(childs.get(i));
        }
        childs.clear();
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
        actHeading = event.getOrientation();
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


        AbstractCache actAbstractCache = null;
        AbstractWaypoint actWaypoint = null;

        if (EventHandler.getSelectedWaypoint() == null) {
            actAbstractCache = EventHandler.getSelectedCache();
        } else {
            actWaypoint = EventHandler.getSelectedWaypoint();
        }
        Coordinate dest = actWaypoint != null ? actWaypoint : actAbstractCache;

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

            String sBearing = Translation.get("directionToTarget") + " : " +
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
            compass = new Compass(style, true);

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
            float yPos = this.getParent().getHeight() - (height /*+ CB.scaledSizes.MARGIN*/);

            compass.setBounds(0, CB.scaledSizes.MARGINx4, this.getParent().getWidth()-CB.scaledSizes.MARGINx2, this.getParent().getHeight() - height + CB.scaledSizes.MARGINx4);

            distance.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);
            accurate.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);


            log.debug("yPos:{}  height:{}", yPos, height);
        }

        public void setInfo(float distance, float heading, float bearing, float accurate) {
            this.distance.setText(UnitFormatter.distanceString(distance, false));
            this.accurate.setText("  +/- " + UnitFormatter.distanceString(accurate, true));
            compass.setBearing(heading);
            compass.setHeading(bearing - heading);
        }

        public float getMinHeight() {
            return compass.getMinHeight() + distance.getMinHeight() - CB.scaledSizes.MARGINx4;
        }
    }

    public String toString() {
        return "CompassView";
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    public Menu getContextMenu() {
        Menu icm = new Menu("menu_compassView");
        icm.setOnItemClickListener(onItemClickListener);

        icm.addItem(MenuID.MI_COMPASS_SHOW, "view", CB.getSkin().getMenuIcon.viewSettings);

        return icm;
    }

    private void showOtionMenu() {
        OptionMenu icm = new OptionMenu("menu_compassView");
        icm.setOnItemClickListener(onItemClickListener);
        MenuItem mi;

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_MAP, "CompassShowMap");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowMap.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_NAME, "CompassShowWP_Name");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowWP_Name.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ICON, "CompassShowWP_Icon");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowWP_Icon.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ATTRIBUTES, "CompassShowAttributes");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowAttributes.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_GC_CODE, "CompassShowGcCode");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowGcCode.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_COORDS, "CompassShowCoords");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowCoords.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_WP_DESC, "CompassShowWpDesc");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowWpDesc.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SAT_INFO, "CompassShowSatInfos");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowSatInfos.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SUN_MOON, "CompassShowSunMoon");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowSunMoon.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_TARGET_DIRECTION, "CompassShowTargetDirection");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowTargetDirection.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_S_D_T, "CompassShowSDT");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowSDT.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_LAST_FOUND, "CompassShowLastFound");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowLastFound.getValue());

        icm.show();

    }

    private final OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public boolean onItemClick(MenuItem item) {
            switch (item.getMenuItemId()) {
                case MenuID.MI_COMPASS_SHOW:
                    showOtionMenu();
                    return true;
                case MenuID.MI_COMPASS_SHOW_MAP:
                    toggleSetting(Config.CompassShowMap, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_NAME:
                    toggleSetting(Config.CompassShowWP_Name, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_ICON:
                    toggleSetting(Config.CompassShowWP_Icon, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_ATTRIBUTES:
                    toggleSetting(Config.CompassShowAttributes, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_GC_CODE:
                    toggleSetting(Config.CompassShowGcCode, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_COORDS:
                    toggleSetting(Config.CompassShowCoords, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_WP_DESC:
                    toggleSetting(Config.CompassShowWpDesc, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_SAT_INFO:
                    toggleSetting(Config.CompassShowSatInfos, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_SUN_MOON:
                    toggleSetting(Config.CompassShowSunMoon, item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_TARGET_DIRECTION:
                    toggleSetting(Config.CompassShowTargetDirection, item);
                    return true;
                case MenuID.MI_COMPASS_SHOW_S_D_T:
                    toggleSetting(Config.CompassShowSDT, item);
                    return true;
                case MenuID.MI_COMPASS_SHOW_LAST_FOUND:
                    toggleSetting(Config.CompassShowLastFound, item);
                    return true;
            }
            return false;
        }
    };

    private void toggleSetting(SettingBool setting, MenuItem item) {
        boolean newValue = !setting.getValue();
        setting.setValue(newValue);
        item.setChecked(newValue);
        Config.AcceptChanges();

        resetLayout();
    }
}
