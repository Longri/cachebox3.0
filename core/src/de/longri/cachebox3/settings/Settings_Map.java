/*
 * Copyright (C) 2014-2018 team-cachebox.de
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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.settings.types.*;

import static de.longri.cachebox3.settings.types.SettingCategory.*;
import static de.longri.cachebox3.settings.types.SettingStoreType.Global;
import static de.longri.cachebox3.settings.types.SettingStoreType.Local;
import static de.longri.cachebox3.settings.types.SettingUsage.ACB;
import static de.longri.cachebox3.settings.types.SettingUsage.ALL;

/**
 * Holds all map relevant settings
 * <p>
 * Created by Longri on 21.01.17.
 */
public class Settings_Map extends Settings_Const {

    // NORMAL visible
    public static final SettingFolder MapPackFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("MapPackFolder", Map, NORMAL,
            Gdx.app.getType() == Application.ApplicationType.iOS ? Gdx.files.getExternalStoragePath() : "?/repository/maps",
            Global, ALL, false));


    public static final SettingInt dynamicZoomLevelMax = (SettingInt) settingsList.addSetting(new SettingInt("dynamicZoomLevelMax", CarMode, NORMAL, 15, Global, ACB));
    public static final SettingInt dynamicZoomLevelMin = (SettingInt) settingsList.addSetting(new SettingInt("dynamicZoomLevelMin", CarMode, NORMAL, 14, Global, ACB));
    public static final SettingBool dynamicZoom = (SettingBool) settingsList.addSetting(new SettingBool("dynamicZoom", CarMode, NORMAL, true, Global, ACB));
    public static final SettingInt MoveMapCenterMaxSpeed = (SettingInt) settingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed", CarMode, NORMAL, 40, Global, ACB));

    // EXPERT visible
    public static final SettingFloat MapViewDPIFaktor = (SettingFloat) settingsList.addSetting(new SettingFloat("MapViewDPIFaktor", Map, EXPERT, 1f, Global, ACB));
    public static final SettingFloat MapViewTextFaktor = (SettingFloat) settingsList.addSetting(new SettingFloat("MapViewTextFaktor", Map, EXPERT, 1f, Global, ACB));

    // NEVER visible
    public static final SettingBool ShowAllWaypoints = (SettingBool) settingsList.addSetting(new SettingBool("ShowAllWaypoints", Map, NEVER, true, Global, ACB));
    public static final SettingBool MapShowRating = (SettingBool) settingsList.addSetting(new SettingBool("MapShowRating", Map, NEVER, true, Global, ACB));
    public static final SettingBool MapShowDT = (SettingBool) settingsList.addSetting(new SettingBool("MapShowDT", Map, NEVER, true, Global, ACB));
    public static final SettingBool MapShowTitles = (SettingBool) settingsList.addSetting(new SettingBool("MapShowTitles", Map, NEVER, true, Global, ACB));
    public static final SettingBool MapShowCompass = (SettingBool) settingsList.addSetting(new SettingBool("MapShowCompass", Map, NEVER, true, Global, ACB));
    public static final SettingBool ShowDirektLine = (SettingBool) settingsList.addSetting(new SettingBool("ShowDirektLine", Map, NEVER, false, Global, ACB));
    public static final SettingBool MapHideMyFinds = (SettingBool) settingsList.addSetting(new SettingBool("MapHideMyFinds", Map, NEVER, false, Global, ACB));
    public static final SettingFolder MapPackFolderLocal = (SettingFolder) settingsList.addSetting(new SettingFolder("MapPackFolderLocal", Map, NEVER, "?/repository/maps", Local, ALL, false));
    public static final SettingStringList CurrentMapLayer = (SettingStringList) settingsList.addSetting(new SettingStringList("CurrentMapLayer", Map, NEVER, new String[]{"Mapnik"}, Global, ACB));
    public static final SettingBool ShowAccuracyCircle = (SettingBool) settingsList.addSetting(new SettingBool("ShowAccuracyCircle", Map, NEVER, true, Global, ACB));
    public static final SettingBool ShowMapCenterCross = (SettingBool) settingsList.addSetting(new SettingBool("ShowMapCenterCross", Map, NEVER, true, Global, ACB));
    public static final SettingFolder TileCacheFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("TileCacheFolder", Folder, NEVER, "?/repository/cache", Global, ALL, true));
    public static final SettingFolder TileCacheFolderLocal = (SettingFolder) settingsList.addSetting(new SettingFolder("TileCacheFolderLocal", Folder, NEVER, "", Local, ALL, true));

    public static final SettingFile MapsforgeDayTheme = (SettingFile) settingsList.addSetting(new SettingFile("MapsforgeDayTheme", Map, NEVER, "", Global, ACB, "xml"));
    public static final SettingFile MapsforgeNightTheme = (SettingFile) settingsList.addSetting(new SettingFile("MapsforgeNightTheme", Map, NEVER, "", Global, ACB, "xml"));
    public static final SettingFile MapsforgeCarDayTheme = (SettingFile) settingsList.addSetting(new SettingFile("MapsforgeCarDayTheme", Map, NEVER, "", Global, ACB, "xml"));
    public static final SettingFile MapsforgeCarNightTheme = (SettingFile) settingsList.addSetting(new SettingFile("MapsforgeCarNightTheme", Map, NEVER, "", Global, ACB, "xml"));
    public static final SettingString MapsforgeDayStyle = (SettingString) settingsList.addSetting(new SettingString("MapsforgeDayStyle", Map, NEVER, "", Global, ACB));
    public static final SettingString MapsforgeNightStyle = (SettingString) settingsList.addSetting(new SettingString("MapsforgeNightStyle", Map, NEVER, "", Global, ACB));
    public static final SettingString MapsforgeCarDayStyle = (SettingString) settingsList.addSetting(new SettingString("MapsforgeCarDayStyle", Map, NEVER, "", Global, ACB));
    public static final SettingString MapsforgeCarNightStyle = (SettingString) settingsList.addSetting(new SettingString("MapsforgeCarNightStyle", Map, NEVER, "", Global, ACB));

    public static final SettingsBlob lastMapState = (SettingsBlob) settingsList.addSetting(new SettingsBlob("lastMapState", Map, NEVER, Local, ACB, false, new byte[]{}));
    public static final SettingsBlob lastMapStateBeforeCar = (SettingsBlob) settingsList.addSetting(new SettingsBlob("lastMapStateBeforeCar", Map, NEVER, Local, ACB, false, new byte[]{}));

//    public static final SettingIntArray ZoomCross = (SettingIntArray) settingsList.addSetting(new SettingIntArray("ZoomCross", Map, NORMAL, 16, Global, ACB, CrossLevel));
//    public static final SettingString CurrentMapOverlayLayer = (SettingString) settingsList.addSetting(new SettingString("CurrentMapOverlayLayer", Map, NEVER, "", Global, ACB));
//    public static final SettingBool PositionMarkerTransparent = (SettingBool) settingsList.addSetting(new SettingBool("PositionMarkerTransparent", Map, EXPERT, true, Global, ACB));
//
//    public static final SettingIntArray OsmMinLevel = (SettingIntArray) settingsList.addSetting(new SettingIntArray("OsmMinLevel", Map, EXPERT, 7, Global, ACB, Level));
//    public static final SettingIntArray OsmMaxLevel = (SettingIntArray) settingsList.addSetting(new SettingIntArray("OsmMaxLevel", Map, EXPERT, 19, Global, ACB, Level));
//    public static final SettingIntArray CompassMapMinZoomLevel = (SettingIntArray) settingsList.addSetting(new SettingIntArray("CompassMapMinZoomLevel", Map, EXPERT, 13, Global, ACB, Level));
//    public static final SettingIntArray CompassMapMaxZommLevel = (SettingIntArray) settingsList.addSetting(new SettingIntArray("CompassMapMaxZommLevel", Map, EXPERT, 20, Global, ACB, Level));
//
//    public static final SettingFolder RenderThemesFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("RenderThemesFolder", Map, NORMAL, "", Global, ALL, false));
//    public static final SettingBool MoveMapCenterWithSpeed = (SettingBool) settingsList.addSetting(new SettingBool("MoveMapCenterWithSpeed", CarMode, NORMAL, false, Global, ACB));
//    public static final SettingBool DEBUG_MapGrid = (SettingBool) settingsList.addSetting(new SettingBool("DEBUG_MapGrid", Debug, NORMAL, false, Global, ACB));


}
