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
package de.longri.cachebox3.gui.actions.show_vies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.baseMap.AbstractManagedMapLayer;
import de.longri.cachebox3.gui.map.baseMap.BaseMapManager;
import de.longri.cachebox3.gui.map.baseMap.MapsforgeSingleMap;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.locator.track.TrackRecorder;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.settings.types.SettingBool;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_MapView extends Abstract_Action_ShowView {

    private MapView mapViewInstance;

    public Action_Show_MapView() {
        super("Map", MenuID.AID_SHOW_MAP);
    }

    @Override
    public void execute() {
        if (isActVisible()) return;
        mapViewInstance = new MapView(CB.viewmanager.getMain());
        CB.viewmanager.showView(mapViewInstance);
    }

    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.mapIcon;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }


    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof MapView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(MapView.class.getName());
    }


    //#############################################################################################
    // 
    //       Map context menu
    //
    //#############################################################################################

    @Override
    public Menu getContextMenu() {
        Menu icm = new Menu("menu_mapviewgl");

        icm.addItem(MenuID.MI_LAYER, "Layer");
        icm.addItem(MenuID.MI_MAPVIEW_OVERLAY_VIEW, "overlays");
        icm.addCheckableItem(MenuID.MI_ALIGN_TO_COMPSS, "AlignToCompass", mapViewInstance.getAlignToCompass());
        icm.addItem(MenuID.MI_CENTER_WP, "CenterWP");
        // icm.addItem(MenuID.MI_SETTINGS, "settings", Sprites.getSprite(IconName.settings.name()));
        // icm.addItem(MenuID.MI_SEARCH, "search", SpriteCache.Icons.get(27));
        icm.addItem(MenuID.MI_MAPVIEW_VIEW, "view");
        icm.addItem(MenuID.MI_TREC_REC, "RecTrack");
        icm.addItem(MenuID.MI_MAP_DOWNOAD, "MapDownload");

        icm.setOnItemClickListener(onItemClickListener);
        return icm;
    }

    private void showMapLayerMenu() {
        Menu icm = new Menu("MapViewShowLayerContextMenu");

        BaseMapManager.INSTANCE.setMapFolder(Gdx.files.absolute(Settings_Map.MapPackFolder.getValue()));


        int menuID = 0;
        for (int i = 0, n = BaseMapManager.INSTANCE.size(); i < n; i++) {

            AbstractManagedMapLayer baseMap = BaseMapManager.INSTANCE.get(i);

            if (!baseMap.isOverlay) {
                MenuItem mi = icm.addItem(menuID++, "", baseMap.name); // == friendlyName == FileName !!! without translation
                mi.setData(baseMap);
                mi.setCheckable(true);

                //set icon (Online_BMP, Online_Vector, Mapsforge or Freizeitkarte)
                Drawable icon = null;
                MenuIconStyle style = VisUI.getSkin().get(MenuIconStyle.class);

                if (baseMap.isOnline) {
                    if (baseMap.isVector()) {
                        icon = style.baseMapOnlineVector;
                    } else {
                        icon = style.baseMapOnlineBitmap;
                    }
                } else {
                    if (baseMap instanceof MapsforgeSingleMap) {
                        MapsforgeSingleMap map = (MapsforgeSingleMap) baseMap;
                        if (map.isFreizeitKarte()) {
                            icon = style.baseMapFreizeitkarte;
                        } else {
                            icon = style.baseMapMapsforge;
                        }
                    }

                }

                if (icon != null)
                    mi.setIcon(icon);

                String[] currentLayer = Settings_Map.CurrentMapLayer.getValue();

                for (int j = 0, m = currentLayer.length; j < m; j++) {
                    String str = currentLayer[j];
                    if (str.equals(baseMap.name)) {
                        mi.setChecked(true);
                        break;
                    }
                }
            }
        }

        icm.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                final AbstractManagedMapLayer baseMap = (AbstractManagedMapLayer) item.getData();

                // if curent layer a Mapsforge map, it is posible to add the selected Mapsforge map
                // to the current layer. We ask the User!
//                if (MapView.mapTileLoader.getCurrentLayer().isMapsForge() && layer.isMapsForge()) {
//                    GL_MsgBox msgBox = GL_MsgBox.Show("add or change", "Map selection", MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
//
//                        @Override
//                        public boolean onClick(int which, Object data) {
//
//                            switch (which) {
//                                case GL_MsgBox.BUTTON_POSITIVE:
//                                    // add the selected map to the curent layer
//                                    TabMainView.mapView.addToCurrentLayer(layer);
//                                    break;
//                                case GL_MsgBox.BUTTON_NEUTRAL:
//                                    // switch curent layer to selected
//                                    TabMainView.mapView.setCurrentLayer(layer);
//                                    break;
//                                default:
//                                    // do nothing
//                            }
//
//                            return true;
//                        }
//                    });
//                    msgBox.button1.setText("add");
//                    msgBox.button2.setText("select");
//                    return true;
//                }

                mapViewInstance.setBaseMap(baseMap);
                return true;
            }
        });

        icm.show();
    }

    private void showMapOverlayMenu() {
        final Menu icm = new Menu("MapViewShowMapOverlayMenu");

//        int menuID = 0;
//        for (Layer layer : ManagerBase.Manager.getLayers()) {
//            if (layer.isOverlay()) {
//                MenuItem mi = icm.addCheckableItem(menuID++, layer.FriendlyName, layer == MapView.mapTileLoader.getCurrentOverlayLayer());
//                mi.setData(layer);
//            }
//        }
//
//        icm.addOnClickListener(new OnClickListener() {
//            @Override
//            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
//                Layer layer = (Layer) ((MenuItem) v).getData();
//                if (layer == MapView.mapTileLoader.getCurrentOverlayLayer()) {
//                    // switch off Overlay
//                    TabMainView.mapView.SetCurrentOverlayLayer(null);
//                } else {
//                    TabMainView.mapView.SetCurrentOverlayLayer(layer);
//                }
//                // Refresh menu
//                icm.close();
//                showMapOverlayMenu();
//                return true;
//            }
//        });

        icm.show();
    }

    private void showMapViewLayerMenu() {
        Menu icm = new Menu("MapViewShowLayerContextMenu");


        icm.addCheckableItem(MenuID.MI_HIDE_FINDS, "HideFinds", Settings_Map.MapHideMyFinds.getValue());
        icm.addCheckableItem(MenuID.MI_MAP_SHOW_COMPASS, "MapShowCompass", Settings_Map.MapShowCompass.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_ALL_WAYPOINTS, "ShowAllWaypoints", Settings_Map.ShowAllWaypoints.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_RATINGS, "ShowRatings", Settings_Map.MapShowRating.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_DT, "ShowDT", Settings_Map.MapShowDT.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_TITLE, "ShowTitle", Settings_Map.MapShowTitles.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_DIRECT_LINE, "ShowDirectLine", Settings_Map.ShowDirektLine.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_ACCURACY_CIRCLE, "MenuTextShowAccuracyCircle", Settings_Map.ShowAccuracyCircle.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_CENTERCROSS, "ShowCenterCross", Settings_Map.ShowMapCenterCross.getValue());

        icm.setOnItemClickListener(onItemClickListener);
        icm.show();
    }

    private final OnItemClickListener onItemClickListener = new OnItemClickListener() {


        @Override
        public boolean onItemClick(MenuItem item) {
            switch (item.getMenuItemId()) {
                case MenuID.MI_LAYER:
                    showMapLayerMenu();
                    return true;
                case MenuID.MI_MAPVIEW_OVERLAY_VIEW:
                    showMapOverlayMenu();
                    return true;
                case MenuID.MI_MAPVIEW_VIEW:
                    showMapViewLayerMenu();
                    return true;
                case MenuID.MI_ALIGN_TO_COMPSS:
                    mapViewInstance.setAlignToCompass(!mapViewInstance.getAlignToCompass());
                    return true;
                case MenuID.MI_SHOW_ALL_WAYPOINTS:
                    toggleSetting(Settings_Map.ShowAllWaypoints);
                    return true;
                case MenuID.MI_HIDE_FINDS:
                    toggleSettingWithReload(Settings_Map.MapHideMyFinds);
                    return true;
                case MenuID.MI_SHOW_RATINGS:
                    toggleSetting(Settings_Map.MapShowRating);
                    return true;
                case MenuID.MI_SHOW_DT:
                    toggleSetting(Settings_Map.MapShowDT);
                    return true;
                case MenuID.MI_SHOW_TITLE:
                    toggleSetting(Settings_Map.MapShowTitles);
                    return true;
                case MenuID.MI_SHOW_DIRECT_LINE:
                    toggleSetting(Settings_Map.ShowDirektLine);
                    return true;
                case MenuID.MI_SHOW_ACCURACY_CIRCLE:
                    toggleSetting(Settings_Map.ShowAccuracyCircle);
                    return true;
                case MenuID.MI_SHOW_CENTERCROSS:
                    toggleSetting(Settings_Map.ShowMapCenterCross);
                    return true;
                case MenuID.MI_MAP_SHOW_COMPASS:
                    toggleSetting(Settings_Map.MapShowCompass);
                    return true;
                case MenuID.MI_CENTER_WP:
                    if (mapViewInstance != null) {
                        //TODO   mapViewInstance.createWaypointAtCenter();
                    }
                    return true;
                case MenuID.MI_TREC_REC:
                    showMenuTrackRecording();
                    return true;
                case MenuID.MI_MAP_DOWNOAD:
                    //TODO MapDownload.INSTANCE.show();
                    return true;
                default:
                    return false;
            }
        }
    };

    private static final int START = 1;
    private static final int PAUSE = 2;
    private static final int STOP = 3;

    private void showMenuTrackRecording() {
        MenuItem mi;
        Menu cm2 = new Menu("TrackRecordContextMenu");
        cm2.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case START:
                        TrackRecorder.INSTANCE.StartRecording();
                        return true;
                    case PAUSE:
                        TrackRecorder.INSTANCE.PauseRecording();
                        return true;
                    case STOP:
                        TrackRecorder.INSTANCE.StopRecording();
                        return true;
                }
                return false;
            }
        });
        mi = cm2.addItem(START, "start");
        mi.setEnabled(!TrackRecorder.INSTANCE.recording);

        if (TrackRecorder.INSTANCE.pauseRecording)
            mi = cm2.addItem(PAUSE, "continue");
        else
            mi = cm2.addItem(PAUSE, "pause");

        mi.setEnabled(TrackRecorder.INSTANCE.recording);

        mi = cm2.addItem(STOP, "stop");
        mi.setEnabled(TrackRecorder.INSTANCE.recording | TrackRecorder.INSTANCE.pauseRecording);

        cm2.show();
    }

    private void toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
        if (mapViewInstance != null)
            mapViewInstance.setNewSettings();
    }

    private void toggleSettingWithReload(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
        if (mapViewInstance != null)
            mapViewInstance.setNewSettings();
    }

}
