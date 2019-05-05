/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.location.AccuracyChangedEvent;
import de.longri.cachebox3.events.location.AccuracyChangedListener;
import de.longri.cachebox3.events.location.OrientationChangedListener;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OptionMenu;
import de.longri.cachebox3.gui.skin.styles.AttributesStyle;
import de.longri.cachebox3.gui.skin.styles.CompassViewStyle;
import de.longri.cachebox3.gui.widgets.CacheSizeWidget;
import de.longri.cachebox3.gui.widgets.CompassPanel;
import de.longri.cachebox3.gui.widgets.Stars;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.SettingBool;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.07.16.
 */
public class CompassView extends AbstractView implements
        PositionChangedListener, OrientationChangedListener, AccuracyChangedListener {

    private static final Logger log = LoggerFactory.getLogger(CompassView.class);

    private CompassPanel compassPanel;
    private VisSplitPane splitPane;
    private Table topTable, bottomTable;
    private CompassViewStyle style;
    private Image backgroundWidget;
    private final float result[] = new float[4];

    private Coordinate actCoord;
    private float actHeading = 0;
    private Label targetdirectionLabel, ownPositionLabel;

    private boolean resetLayout, showMap, showName, showIcon, showAtt, showGcCode, showCoords, showWpDesc, showSatInfos, showSunMoon, showAnyContent, showTargetDirection, showSDT, showLastFound;
    private float accuracy;

    public CompassView(BitStore reader) {
        super(reader);
    }

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
                compassPanel.setSize(bottomTable.getWidth(), bottomTable.getHeight());
            }
        };


        topTable.setBackground(style.splitBackground);
        bottomTable.setBackground(style.splitBackground);


        if (CB.isMocked()) return;

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
            Array<Attributes> attList = null;
            try {
                attList = actAbstractCache.getAttributes();
            } catch (Exception e) {
                log.error("can't get attributes from Cache" + actAbstractCache, e);
            }
            if (attList != null) {
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
            }
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
        synchronized (this) {
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
    }


    @Override
    public void layout() {
        super.layout();
        splitPane.setBounds(0, 0, this.getWidth(), this.getHeight());
        splitPane.layout();
        compassPanel.setSize(bottomTable.getWidth(), bottomTable.getHeight());
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
    public void positionChanged(de.longri.cachebox3.events.location.PositionChangedEvent event) {
        synchronized (this) {
            actCoord = event.pos;
            refreshOrientationInfo();
        }
    }

    @Override
    public void orientationChanged(de.longri.cachebox3.events.location.OrientationChangedEvent event) {
        synchronized (this) {
            actHeading = event.getOrientation();
            refreshOrientationInfo();
        }
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
            CB.postOnGlThread(new NamedRunnable("postOnGlThread") {
                @Override
                public void run() {
                    ownPositionLabel.setText(actCoord.FormatCoordinate());
                }
            });
        }

        final float heading = actHeading;
        final Coordinate dest = EventHandler.getSelectedCoord();

        CB.postOnGlThread(new NamedRunnable("Set compass values") {
            @Override
            public void run() {
                try {
                    MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, actCoord.getLatitude(),
                            actCoord.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);
                } catch (Exception e) {
                    log.error("Compute distance and bearing", e);
                    return;
                }

                float distance = result[0];
                float bearing = result[1];
                log.debug("set Compass heading to {}", heading);
                compassPanel.setInfo(distance, heading, bearing, accuracy);

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
            }
        });
        CB.requestRendering();
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        backgroundWidget.setBounds(0, 0, this.getWidth(), this.getHeight());
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
        Menu icm = new Menu("Compass");
        icm.addMenuItem("view", CB.getSkin().getMenuIcon.viewSettings, this::showOtionMenu);
        return icm;
    }

    private void showOtionMenu() {
        OptionMenu icm = new OptionMenu("CompassViewElementsMenuTitle");
        final ObjectMap<String, MenuItem> menuItems = new ObjectMap<>();
        menuItems.put("1", icm.addCheckableMenuItem("CompassShowMap", Config.CompassShowMap.getValue(), () -> toggleSetting(Config.CompassShowMap, menuItems.get("1"))));
        menuItems.put("2", icm.addCheckableMenuItem("CompassShowWP_Name", Config.CompassShowWP_Name.getValue(), () -> toggleSetting(Config.CompassShowWP_Name, menuItems.get("2"))));
        menuItems.put("3", icm.addCheckableMenuItem("CompassShowWP_Icon", Config.CompassShowWP_Icon.getValue(), () -> toggleSetting(Config.CompassShowWP_Icon, menuItems.get("3"))));
        menuItems.put("4", icm.addCheckableMenuItem("CompassShowAttributes", Config.CompassShowAttributes.getValue(), () -> toggleSetting(Config.CompassShowAttributes, menuItems.get("4"))));
        menuItems.put("5", icm.addCheckableMenuItem("CompassShowGcCode", Config.CompassShowGcCode.getValue(), () -> toggleSetting(Config.CompassShowGcCode, menuItems.get("5"))));
        menuItems.put("6", icm.addCheckableMenuItem("CompassShowCoords", Config.CompassShowCoords.getValue(), () -> toggleSetting(Config.CompassShowCoords, menuItems.get("6"))));
        menuItems.put("7", icm.addCheckableMenuItem("CompassShowWpDesc", Config.CompassShowWpDesc.getValue(), () -> toggleSetting(Config.CompassShowWpDesc, menuItems.get("7"))));
        menuItems.put("8", icm.addCheckableMenuItem("CompassShowSatInfos", Config.CompassShowSatInfos.getValue(), () -> toggleSetting(Config.CompassShowSatInfos, menuItems.get("8"))));
        menuItems.put("9", icm.addCheckableMenuItem("CompassShowSunMoon", Config.CompassShowSunMoon.getValue(), () -> toggleSetting(Config.CompassShowSunMoon, menuItems.get("9"))));
        menuItems.put("10", icm.addCheckableMenuItem("CompassShowTargetDirection", Config.CompassShowTargetDirection.getValue(), () -> toggleSetting(Config.CompassShowTargetDirection, menuItems.get("10"))));
        menuItems.put("11", icm.addCheckableMenuItem("CompassShowSDT", Config.CompassShowSDT.getValue(), () -> toggleSetting(Config.CompassShowSDT, menuItems.get("11"))));
        menuItems.put("12", icm.addCheckableMenuItem("CompassShowLastFound", Config.CompassShowLastFound.getValue(), () -> toggleSetting(Config.CompassShowLastFound, menuItems.get("12"))));
        icm.show();
    }

    private void toggleSetting(SettingBool setting, MenuItem item) {
        boolean newValue = !setting.getValue();
        setting.setValue(newValue);
        item.setChecked(newValue);
        Config.AcceptChanges();
        resetLayout();
    }

    @Override
    public void accuracyChanged(AccuracyChangedEvent event) {
        this.accuracy = event.accuracy;
    }
}
