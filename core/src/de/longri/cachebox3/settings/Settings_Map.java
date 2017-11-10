/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.settings;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.settings.types.*;

/**
 * Holds all map relevant settings
 * <p>
 * Created by Longri on 21.01.17.
 */
public class Settings_Map extends Settings_Const {

    // NORMAL visible
    public static final SettingFolder MapPackFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("MapPackFolder", SettingCategory.Map, NORMAL, "?/repository/maps", SettingStoreType.Global, SettingUsage.ALL, false));
    public static final SettingInt dynamicZoomLevelMax = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMax", SettingCategory.CarMode, NORMAL, 17, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt dynamicZoomLevelMin = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMin", SettingCategory.CarMode, NORMAL, 15, SettingStoreType.Global, SettingUsage.ACB));
    //    public static final SettingBool dynamicZoom = (SettingBool) SettingsList.addSetting(new SettingBool("dynamicZoom", SettingCategory.CarMode, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt MoveMapCenterMaxSpeed = (SettingInt) SettingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed", SettingCategory.CarMode, NORMAL, 60, SettingStoreType.Global, SettingUsage.ACB));


    // EXPERT visible


    // EXPERT visible
    public static final SettingFloat MapViewDPIFaktor = (SettingFloat) SettingsList.addSetting(new SettingFloat("MapViewDPIFaktor", SettingCategory.Map, EXPERT, 1f, SettingStoreType.Global, SettingUsage.ACB));


    // DEVELOPER visible
    public static final SettingDouble MapInitLatitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapInitLatitude", SettingCategory.Positions, DEVELOPER, -1000, SettingStoreType.Global, SettingUsage.ALL));
    public static final SettingDouble MapInitLongitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapInitLongitude", SettingCategory.Positions, DEVELOPER, -1000, SettingStoreType.Global, SettingUsage.ALL));


    // NEVER visible
    public static final SettingBool ShowAllWaypoints = (SettingBool) SettingsList.addSetting(new SettingBool("ShowAllWaypoints", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapShowRating = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowRating", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapShowDT = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowDT", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapShowTitles = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowTitles", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapShowCompass = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowCompass", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowDirektLine = (SettingBool) SettingsList.addSetting(new SettingBool("ShowDirektLine", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapHideMyFinds = (SettingBool) SettingsList.addSetting(new SettingBool("MapHideMyFinds", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFolder MapPackFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("MapPackFolderLocal", SettingCategory.Map, NEVER, "?/repository/maps", SettingStoreType.Local, SettingUsage.ALL, false));
    public static final SettingStringList CurrentMapLayer = (SettingStringList) SettingsList.addSetting(new SettingStringList("CurrentMapLayer", SettingCategory.Map, NEVER, new String[]{"Mapnik"}, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt lastMapState = (SettingInt) SettingsList.addSetting(new SettingInt("lastMapState", SettingCategory.Map, NEVER, 0, SettingStoreType.Global, SettingUsage.ALL));
    public static final SettingBool ShowAccuracyCircle = (SettingBool) SettingsList.addSetting(new SettingBool("ShowAccuracyCircle", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowMapCenterCross = (SettingBool) SettingsList.addSetting(new SettingBool("ShowMapCenterCross", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFolder TileCacheFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("TileCacheFolder", SettingCategory.Folder, NEVER, "?/repository/cache", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder TileCacheFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("TileCacheFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true));


//    public static final SettingIntArray ZoomCross = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("ZoomCross", SettingCategory.Map, NORMAL, 16, SettingStoreType.Global, SettingUsage.ACB, CrossLevel));
//    public static final SettingString CurrentMapOverlayLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapOverlayLayer", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool PositionMarkerTransparent = (SettingBool) SettingsList.addSetting(new SettingBool("PositionMarkerTransparent", SettingCategory.Map, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB));
//
//    public static final SettingIntArray OsmMinLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("OsmMinLevel", SettingCategory.Map, EXPERT, 7, SettingStoreType.Global, SettingUsage.ACB, Level));
//    public static final SettingIntArray OsmMaxLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("OsmMaxLevel", SettingCategory.Map, EXPERT, 19, SettingStoreType.Global, SettingUsage.ACB, Level));
//    public static final SettingIntArray CompassMapMinZoomLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("CompassMapMinZoomLevel", SettingCategory.Map, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB, Level));
//    public static final SettingIntArray CompassMapMaxZommLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("CompassMapMaxZommLevel", SettingCategory.Map, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB, Level));
//
//    public static final SettingFolder RenderThemesFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("RenderThemesFolder", SettingCategory.Map, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL, false));
//    public static final SettingFile MapsforgeDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
//    public static final SettingFile MapsforgeNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
//    public static final SettingFile MapsforgeCarDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
//    public static final SettingFile MapsforgeCarNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
//    public static final SettingBool MoveMapCenterWithSpeed = (SettingBool) SettingsList.addSetting(new SettingBool("MoveMapCenterWithSpeed", SettingCategory.CarMode, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool DEBUG_MapGrid = (SettingBool) SettingsList.addSetting(new SettingBool("DEBUG_MapGrid", SettingCategory.Debug, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));


}
