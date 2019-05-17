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

import de.longri.cachebox3.settings.types.*;
import de.longri.cachebox3.translation.Language;

import static de.longri.cachebox3.settings.types.SettingCategory.*;
import static de.longri.cachebox3.settings.types.SettingStoreType.Global;
import static de.longri.cachebox3.settings.types.SettingStoreType.Local;
import static de.longri.cachebox3.settings.types.SettingUsage.ACB;
import static de.longri.cachebox3.settings.types.SettingUsage.ALL;

/**
 * Created by Longri on 31.07.16.
 */
public class Settings extends Settings_Skin {

    // NORMAL visible
    public static final SettingString GcLogin = (SettingString) settingsList.addSetting(new SettingString("GcLogin", Login, NORMAL, "", Global, ALL));
    public static final SettingEnum<Language> localisation = (SettingEnum<Language>) settingsList.addSetting(new SettingEnum("localisation", Locale, NORMAL, Language.en_GB, Global, ALL, Language.en_GB));
    public static final SettingBool showGestureHelp = (SettingBool) settingsList.addSetting(new SettingBool("showGestureHelp", RememberAsk, NORMAL, true, Global, ACB));
    public static final SettingBool UseCorrectedFinal = (SettingBool) settingsList.addSetting(new SettingBool("UseCorrectedFinal", Misc, NORMAL, true, Global, ALL));

    // EXPERT visible


    // EXPERT visible
    public static final SettingInt LongClicktime = (SettingInt) settingsList.addSetting(new SettingInt("LongClicktime", Misc, EXPERT, 600, Global, ACB));
    public static final SettingFolder TrackFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("TrackFolder", Folder, EXPERT, "?/user/tracks", Global, ACB, true));
    public static final SettingIntArray TrackDistance = (SettingIntArray) settingsList.addSetting(new SettingIntArray("TrackDistance", Misc, EXPERT, 3, Global, ACB, TrackDistanceArray));


    // DEVELOPER visible
    public static final SettingEncryptedString AccessToken = (SettingEncryptedString) settingsList.addSetting(new SettingEncryptedString("GcAPI", Login, DEVELOPER, "", Global, ALL));


    // NEVER visible

    public static final SettingEncryptedString AccessTokenForTest = (SettingEncryptedString) settingsList.addSetting(new SettingEncryptedString("GcAPIStaging", Login, NEVER, "", Global, ALL));
    public static final SettingFolder DescriptionImageFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("DescriptionImageFolder", Folder, NEVER, "?/repository/images", Global, ALL, true));
    public static final SettingFolder DescriptionImageFolderLocal = (SettingFolder) settingsList.addSetting(new SettingFolder("DescriptionImageFolderLocal", Folder, NEVER, "", Local, ALL, true));
    public static final SettingFolder SpoilerFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("SpoilerFolder", Folder, NEVER, "?/repository/spoilers", Global, ALL, true));
    public static final SettingFolder SpoilerFolderLocal = (SettingFolder) settingsList.addSetting(new SettingFolder("SpoilerFolderLocal", Folder, NEVER, "", Local, ALL, true));
    public static final SettingFolder PocketQueryFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("PocketQueryFolder", Folder, NEVER, "?/pocketQuery", Global, ALL, true));
    public static final SettingFolder UserImageFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("UserImageFolder", Folder, NEVER, "?/user/media", Global, ALL, true));
    public static final SettingBool UseTestUrl = (SettingBool) settingsList.addSetting(new SettingBool("StagingAPI", Folder, NEVER, false, Global, ALL));

    public static final SettingInt socket_timeout = (SettingInt) settingsList.addSetting(new SettingInt("socket_timeout", Internal, NEVER, 60000, Global, ALL));
    public static final SettingDouble ParkingLatitude = (SettingDouble) settingsList.addSetting(new SettingDouble("ParkingLatitude", Positions, NEVER, 0, Global, ACB));
    public static final SettingDouble ParkingLongitude = (SettingDouble) settingsList.addSetting(new SettingDouble("ParkingLongitude", Positions, NEVER, 0, Global, ACB));
    public static final SettingString Friends = (SettingString) settingsList.addSetting(new SettingString("Friends", Login, SettingMode.Normal, "", Global, ACB));

    public static final SettingBool DirectOnlineLog = (SettingBool) settingsList.addSetting(new SettingBool("DirectOnlineLog", Drafts, NEVER, false, Global, ACB));

    public static final SettingInt memberChipType = (SettingInt) settingsList.addSetting(new SettingInt("memberChipType", API, NEVER, -1, Global, ACB, true));
    public static final SettingInt apiCallLimit = (SettingInt) settingsList.addSetting(new SettingInt("apiCallLimit", API, NEVER, 30, Global, ACB, true));

    // Settings Compass
    public static final SettingBool CompassShowMap = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowMap", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowWP_Name = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowWP_Name", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowWP_Icon = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowWP_Icon", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowAttributes = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowAttributes", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowGcCode = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowGcCode", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowCoords = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowCoords", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowWpDesc = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowWpDesc", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowSatInfos = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowSatInfos", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowSunMoon = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowSunMoon", Compass, NEVER, false, Global, ACB));
    public static final SettingBool CompassShowTargetDirection = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowTargetDirection", Compass, NEVER, false, Global, ACB));
    public static final SettingBool CompassShowSDT = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowSDT", Compass, NEVER, true, Global, ACB));
    public static final SettingBool CompassShowLastFound = (SettingBool) settingsList.addSetting(new SettingBool("CompassShowLastFound", Compass, NEVER, true, Global, ACB));
    public static final SettingString OverrideUrl = (SettingString) settingsList.addSetting(new SettingString("OverrideUrl", Login, NEVER, "", Global, ACB));
    public static final SettingFile DatabaseName = (SettingFile) settingsList.addSetting(new SettingFile("DatabaseName", Folder, NEVER, "?/cachebox.db3", Global, ACB, "db3"));
    public static final SettingBool StartWithAutoSelect = (SettingBool) settingsList.addSetting(new SettingBool("StartWithAutoSelect", Misc, EXPERT, false, Global, ACB));
    public static final SettingBool MultiDBAsk = (SettingBool) settingsList.addSetting(new SettingBool("MultiDBAsk", Internal, NEVER, true, Global, ACB));
    public static final SettingBool ImperialUnits = (SettingBool) settingsList.addSetting(new SettingBool("ImperialUnits", Locale, NORMAL, false, Global, ACB));
    public static final SettingBool SearchWithoutFounds = (SettingBool) settingsList.addSetting(new SettingBool("SearchWithoutFounds", API, NEVER, true, Global, ACB));
    public static final SettingBool SearchWithoutOwns = (SettingBool) settingsList.addSetting(new SettingBool("SearchWithoutOwns", API, NEVER, true, Global, ACB));
    public static final SettingBool SearchOnlyAvailable = (SettingBool) settingsList.addSetting(new SettingBool("SearchOnlyAvailable", API, NEVER, true, Global, ACB));
    public static final SettingBool DescriptionNoAttributes = (SettingBool) settingsList.addSetting(new SettingBool("DescriptionNoAttributes", Misc, NEVER, false, Global, ACB));
    public static final SettingBool quickButtonLastShow = (SettingBool) settingsList.addSetting(new SettingBool("quickButtonLastShow", QuickList, NEVER, false, Global, ACB));
    public static final SettingInt MultiDBAutoStartTime = (SettingInt) settingsList.addSetting(new SettingInt("MultiDBAutoStartTime", Internal, NEVER, 0, Global, ACB));
    public static final SettingInt lastSearchRadius = (SettingInt) settingsList.addSetting(new SettingInt("lastSearchRadius", API, NEVER, 5, Global, ACB));
    public static final SettingString quickButtonList = (SettingString) settingsList.addSetting(new SettingString("quickButtonList", QuickList, NEVER, "5,0,1,3,2", Global, ACB));
    public static final SettingString LastSelectedCache = (SettingString) settingsList.addSetting(new SettingString("LastSelectedCache", Misc, NEVER, "", Local, ALL));
    public static final SettingLongString FilterNew = (SettingLongString) settingsList.addSetting(new SettingLongString("FilterNew", Misc, NEVER, "", Local, ALL));


    // AudioSettings
    public static final SettingInt AppRaterlaunchCount = (SettingInt) settingsList.addSetting(new SettingInt("AppRaterlaunchCount", Internal, NEVER, 0, Global, ACB));

    public static final SettingsAudio GlobalVolume = (SettingsAudio) settingsList.addSetting(new SettingsAudio("GlobalVolume", Sounds, NORMAL, new Audio("sound/Approach.mp3", false, false, 1.0f), Global, ACB));
    public static final SettingsAudio Approach = (SettingsAudio) settingsList.addSetting(new SettingsAudio("Approach", Sounds, NORMAL, new Audio("sound/Approach.mp3", false, false, 1.0f), Global, ACB));
    public static final SettingsAudio GPS_lose = (SettingsAudio) settingsList.addSetting(new SettingsAudio("GPS_lose", Sounds, NORMAL, new Audio("sound/GPS_lose.mp3", false, false, 1.0f), Global, ACB));
    public static final SettingsAudio GPS_fix = (SettingsAudio) settingsList.addSetting(new SettingsAudio("GPS_fix", Sounds, NORMAL, new Audio("sound/GPS_Fix.mp3", false, false, 1.0f), Global, ACB));
    public static final SettingsAudio AutoResortSound = (SettingsAudio) settingsList.addSetting(new SettingsAudio("AutoResortSound", Sounds, NORMAL, new Audio("sound/AutoResort.mp3", false, false, 1.0f), Global, ACB));

    public static final SettingIntArray SoundApproachDistance = (SettingIntArray) settingsList.addSetting(new SettingIntArray("SoundApproachDistance", Misc, NEVER, 50, Global, ACB, approach));
    public static final SettingFolder ImageCacheFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("ImageCacheFolder", Folder, NEVER, "?/repository/cache", Local, ACB, true));
    public static final SettingBool SettingsShowExpert = (SettingBool) settingsList.addSetting(new SettingBool("SettingsShowExpert", Internal, NEVER, false, Global, ACB));
    public static final SettingBool SettingsShowAll = (SettingBool) settingsList.addSetting(new SettingBool("SettingsShowAll", Internal, NEVER, false, Global, ACB));
    public static final SettingFloat CompassViewSplit = (SettingFloat) settingsList.addSetting(new SettingFloat("CompassViewSplit", Compass, NEVER, 0.5f, Global, ACB));


    public static final SettingBool DraftsLoadAll = (SettingBool) settingsList.addSetting(new SettingBool("DraftsLoadAll", Drafts, EXPERT, false, Global, ACB));
    public static final SettingInt DraftsLoadLength = (SettingInt) settingsList.addSetting(new SettingInt("DraftsLoadLength", Drafts, EXPERT, 10, Global, ACB));
    public static final SettingInt FoundOffset = (SettingInt) settingsList.addSetting(new SettingInt("FoundOffset", Misc, NEVER, 0, Global, ACB));
    public static final SettingString FoundTemplate = (SettingString) settingsList.addSetting(new SettingLongString("FoundTemplate", Templates, NORMAL, FOUND, Global, ACB));
    public static final SettingString AttendedTemplate = (SettingString) settingsList.addSetting(new SettingLongString("AttendedTemplate", Templates, NORMAL, ATTENDED, Global, ACB));
    public static final SettingString WebcamTemplate = (SettingString) settingsList.addSetting(new SettingLongString("WebCamTemplate", Templates, NORMAL, WEBCAM, Global, ACB));
    public static final SettingString DNFTemplate = (SettingString) settingsList.addSetting(new SettingLongString("DNFTemplate", Templates, NORMAL, DNF, Global, ACB));
    public static final SettingString NeedsMaintenanceTemplate = (SettingString) settingsList.addSetting(new SettingLongString("NeedsMaintenanceTemplate", Templates, NORMAL, LOG, Global, ACB));
    public static final SettingString AddNoteTemplate = (SettingString) settingsList.addSetting(new SettingLongString("AddNoteTemplate", Templates, NORMAL, LOG, Global, ACB));
    public static final SettingString DiscoverdTemplate = (SettingString) settingsList.addSetting(new SettingLongString("DiscoverdTemplate", Templates, NORMAL, DISCOVERD, Global, ACB));
    public static final SettingString VisitedTemplate = (SettingString) settingsList.addSetting(new SettingLongString("VisitedTemplate", Templates, NORMAL, VISITED, Global, ACB));
    public static final SettingString DroppedTemplate = (SettingString) settingsList.addSetting(new SettingLongString("DroppedTemplate", Templates, NORMAL, DROPPED, Global, ACB));
    public static final SettingString GrabbedTemplate = (SettingString) settingsList.addSetting(new SettingLongString("GrabbedTemplate", Templates, NORMAL, GRABED, Global, ACB));
    public static final SettingString PickedTemplate = (SettingString) settingsList.addSetting(new SettingLongString("PickedTemplate", Templates, NORMAL, PICKED, Global, ACB));
    public static final SettingFile DraftsGarminPath = (SettingFile) settingsList.addSetting(new SettingFile("DraftsGarminPath", Folder, DEVELOPER, "?/user/geocache_visits.txt", Global, ACB));
    public static final SettingEncryptedString GcVotePassword = (SettingEncryptedString) settingsList.addSetting(new SettingEncryptedString("GcVotePassword", Login, NORMAL, "", Global, ALL));

    public static final SettingBool VibrateFeedback = (SettingBool) settingsList.addSetting(new SettingBool("vibrateFeedback", Misc, NORMAL, true, Global, ACB));
    public static final SettingInt VibrateTime = (SettingInt) settingsList.addSetting(new SettingInt("VibrateTime", Misc, EXPERT, 20, Global, ACB));

    public static final SettingLongString UserFilter = (SettingLongString) settingsList.addSetting(new SettingLongString("UserFilter", Misc, NEVER, "", Global, ACB));
    public static final SettingInt HardwareCompassLevel = (SettingInt) settingsList.addSetting(new SettingInt("HardwareCompassLevel", Gps, NORMAL, 5, Global, ACB));
    public static final SettingBool HardwareCompassOnly = (SettingBool) settingsList.addSetting(new SettingBool("HardwareCompassOnly", Gps, NORMAL, true, Global, ACB));


    //        public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, ACB, Live_Cache_Time.h_6);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);


//    public static final SettingBool DisableLiveMap = (SettingBool) settingsList.addSetting(new SettingBool("DisableLiveMap", LiveMap, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingInt LiveMaxCount = (SettingInt) settingsList.addSetting(new SettingInt("LiveMaxCount", LiveMap, EXPERT, 350, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool LiveExcludeFounds = (SettingBool) settingsList.addSetting(new SettingBool("LiveExcludeFounds", LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool LiveExcludeOwn = (SettingBool) settingsList.addSetting(new SettingBool("LiveExcludeOwn", LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool showSandbox = (SettingBool) settingsList.addSetting(new SettingBool("showSandbox", RememberAsk, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingInt gpsUpdateTime = (SettingInt) settingsList.addSetting(new SettingInt("gpsUpdateTime", Gps, NORMAL, 500, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingInt conection_timeout = (SettingInt) settingsList.addSetting(new SettingInt("conection_timeout", Internal, DEVELOPER, 10000, SettingStoreType.Global, SettingUsage.ALL));

//    public static final SettingFile gpxExportFileName = (SettingFile) settingsList.addSetting(new SettingFile("gpxExportFileName", Folder, NEVER,  "?/user/export.gpx", SettingStoreType.Global, SettingUsage.ACB, "gpx"));
//    public static final SettingBool TrackRecorderStartup = (SettingBool) settingsList.addSetting(new SettingBool("TrackRecorderStartup", Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ImportGpx = (SettingBool) settingsList.addSetting(new SettingBool("ImportGpx", API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CacheMapData = (SettingBool) settingsList.addSetting(new SettingBool("CacheMapData", Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CacheImageData = (SettingBool) settingsList.addSetting(new SettingBool("CacheImageData", Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CacheSpoilerData = (SettingBool) settingsList.addSetting(new SettingBool("CacheSpoilerData", Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool SuppressPowerSaving = (SettingBool) settingsList.addSetting(new SettingBool("SuppressPowerSaving", Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool GCAdditionalImageDownload = (SettingBool) settingsList.addSetting(new SettingBool("GCAdditionalImageDownload", API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString CacheHistory = (SettingString) settingsList.addSetting(new SettingString("CacheHistory", Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
//    public static final SettingString NavigationProvider = (SettingString) settingsList.addSetting(new SettingString("NavigationProvider", Internal, DEVELOPER, "http://openls.geog.uni-heidelberg.de/testing2015/route?", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString SpoilersDescriptionTags = (SettingString) settingsList.addSetting(new SettingString("SpoilersDescriptionTags", Internal, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString GcJoker = (SettingString) settingsList.addSetting(new SettingString("GcJoker", Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingStringArray Navis = (SettingStringArray) settingsList.addSetting(new SettingStringArray("Navis", Misc, NORMAL, "Google", SettingStoreType.Global, SettingUsage.ACB, navis));
//    public static final SettingBool ShowFieldnotesCMwithFirstShow = (SettingBool) settingsList.addSetting(new SettingBool("ShowFieldnotesCMwithFirstShow", Drafts, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ShowFieldnotesAsDefaultView = (SettingBool) settingsList.addSetting(new SettingBool("ShowFieldnotesAsDefaultView", Drafts, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool LiveMapEnabeld = (SettingBool) settingsList.addSetting(new SettingBool("LiveMapEnabeld", LiveMap, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool AppRaterDontShowAgain = (SettingBool) settingsList.addSetting(new SettingBool("AppRaterDontShowAgain", RememberAsk, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString AppRaterFirstLunch = (SettingString) settingsList.addSetting(new SettingString("AppRaterFirstLunch", Internal, NEVER, "0", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool GestureOn = (SettingBool) settingsList.addSetting(new SettingBool("GestureOn", Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingColor LiveMapBackgroundColor = (SettingColor) settingsList.addSetting(new SettingColor("LiveMapBackgroundColor", LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingLongString Filter = (SettingLongString) settingsList.addSetting(new SettingLongString("Filter", Misc, NEVER, FilterProperties.presets[0].toString(), SettingStoreType.Local, SettingUsage.ALL));
//
//    public static final SettingLongString UserFilterNew = (SettingLongString) settingsList.addSetting(new SettingLongString("UserFilterNew", Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingTime ScreenLock = (SettingTime) settingsList.addSetting(new SettingTime("ScreenLock", Misc, NEVER, 60000, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ImportRatings = (SettingBool) settingsList.addSetting(new SettingBool("ImportRatings", API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ImportPQsFromGeocachingCom = (SettingBool) settingsList.addSetting(new SettingBool("ImportPQsFromGeocachingCom", API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool switchViewApproach = (SettingBool) settingsList.addSetting(new SettingBool("switchViewApproach", Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool hasCallPermission = (SettingBool) settingsList.addSetting(new SettingBool("hasCallPermission", Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingBool hasPQ_PlugIn = (SettingBool) settingsList.addSetting(new SettingBool("hasPQ_PlugIn", Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool hasFTF_PlugIn = (SettingBool) settingsList.addSetting(new SettingBool("hasFTF_PlugIn", Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool dynamicFilterAtSearch = (SettingBool) settingsList.addSetting(new SettingBool("dynamicFilterAtSearch", Misc, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool DeleteLogs = (SettingBool) settingsList.addSetting(new SettingBool("DeleteLogs", Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CompactDB = (SettingBool) settingsList.addSetting(new SettingBool("CompactDB", Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool AskAgain = (SettingBool) settingsList.addSetting(new SettingBool("AskAgain", RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingBool RememberAsk_Get_API_Key = (SettingBool) settingsList.addSetting(new SettingBool("RememberAsk_Get_API_Key", RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool Ask_Switch_GPS_ON = (SettingBool) settingsList.addSetting(new SettingBool("Ask_Switch_GPS_ON", RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingBool TB_DirectLog = (SettingBool) settingsList.addSetting(new SettingBool("TB_DirectLog", Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingInt LogMaxMonthAge = (SettingInt) settingsList.addSetting(new SettingInt("LogMaxMonthAge", Internal, DEVELOPER, 6, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingInt LogMinCount = (SettingInt) settingsList.addSetting(new SettingInt("LogMinCount", Internal, DEVELOPER, 99999, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingInt installRev = (SettingInt) settingsList.addSetting(new SettingInt("installRev", Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingBool newInstall = (SettingBool) settingsList.addSetting(new SettingBool("newInstall", Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool FieldnotesUploadAll = (SettingBool) settingsList.addSetting(new SettingBool("FieldnotesUploadAll", API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool quickButtonShow = (SettingBool) settingsList.addSetting(new SettingBool("quickButtonShow", QuickList, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));


}
