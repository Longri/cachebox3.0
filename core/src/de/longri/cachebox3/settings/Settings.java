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

/**
 * Created by Longri on 31.07.16.
 */
public class Settings extends Settings_Skin {

    // NORMAL visible
    public static final SettingString GcLogin = (SettingString) Config.settingsList.addSetting(new SettingString("GcLogin", SettingCategory.Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL));
    public static final SettingEnum<Language> localisation = (SettingEnum<Language>) Config.settingsList.addSetting(new SettingEnum("localisation", SettingCategory.Locale, NORMAL, Language.en_GB, SettingStoreType.Global, SettingUsage.ALL, Language.en_GB));
    public static final SettingBool showGestureHelp = (SettingBool) Config.settingsList.addSetting(new SettingBool("showGestureHelp", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));


    // EXPERT visible


    // EXPERT visible
    public static final SettingInt LongClicktime = (SettingInt) Config.settingsList.addSetting(new SettingInt("LongClicktime", SettingCategory.Misc, EXPERT, 600, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFolder TrackFolder = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("TrackFolder", SettingCategory.Folder, EXPERT, "?/user/tracks", SettingStoreType.Global, SettingUsage.ACB, true));
    public static final SettingIntArray TrackDistance = (SettingIntArray) Config.settingsList.addSetting(new SettingIntArray("TrackDistance", SettingCategory.Misc, EXPERT, 3, SettingStoreType.Global, SettingUsage.ACB, TrackDistanceArray));


    // DEVELOPER visible
    public static final SettingEncryptedString GcAPI = (SettingEncryptedString) Config.settingsList.addSetting(new SettingEncryptedString("GcAPI", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ALL));


    // NEVER visible

    public static final SettingEncryptedString GcAPIStaging = (SettingEncryptedString) Config.settingsList.addSetting(new SettingEncryptedString("GcAPIStaging", SettingCategory.Login, NEVER, "", SettingStoreType.Global, SettingUsage.ALL));
    public static final SettingFolder DescriptionImageFolder = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("DescriptionImageFolder", SettingCategory.Folder, NEVER, "?/repository/images", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder DescriptionImageFolderLocal = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("DescriptionImageFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true));
    public static final SettingFolder SpoilerFolder = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("SpoilerFolder", SettingCategory.Folder, NEVER, "?/repository/spoilers", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder SpoilerFolderLocal = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("SpoilerFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true));
    public static final SettingFolder PocketQueryFolder = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("PocketQueryFolder", SettingCategory.Folder, NEVER, "?/pocketQuery", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder UserImageFolder = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("UserImageFolder", SettingCategory.Folder, NEVER, "?/user/media", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingBool StagingAPI = (SettingBool) Config.settingsList.addSetting(new SettingBool("StagingAPI", SettingCategory.Folder, NEVER, false, SettingStoreType.Global, SettingUsage.ALL));

    public static final SettingInt socket_timeout = (SettingInt) Config.settingsList.addSetting(new SettingInt("socket_timeout", SettingCategory.Internal, NEVER, 60000, SettingStoreType.Global, SettingUsage.ALL));
    public static final SettingDouble ParkingLatitude = (SettingDouble) Config.settingsList.addSetting(new SettingDouble("ParkingLatitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingDouble ParkingLongitude = (SettingDouble) Config.settingsList.addSetting(new SettingDouble("ParkingLongitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString Friends = (SettingString) Config.settingsList.addSetting(new SettingString("Friends", SettingCategory.Login, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingBool DirectOnlineLog = (SettingBool) Config.settingsList.addSetting(new SettingBool("DirectOnlineLog", SettingCategory.Fieldnotes, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingInt memberChipType = (SettingInt) Config.settingsList.addSetting(new SettingInt("memberChipType", SettingCategory.API, NEVER, -1, SettingStoreType.Global, SettingUsage.ACB, true));
    public static final SettingInt apiCallLimit = (SettingInt) Config.settingsList.addSetting(new SettingInt("apiCallLimit", SettingCategory.API, NEVER, 30, SettingStoreType.Global, SettingUsage.ACB, true));

    // Settings Compass
    public static final SettingBool CompassShowMap = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowMap", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowWP_Name = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowWP_Name", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowWP_Icon = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowWP_Icon", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowAttributes = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowAttributes", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowGcCode = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowGcCode", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowCoords = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowCoords", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowWpDesc = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowWpDesc", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowSatInfos = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowSatInfos", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowSunMoon = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowSunMoon", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowTargetDirection = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowTargetDirection", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowSDT = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowSDT", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowLastFound = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompassShowLastFound", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString OverrideUrl = (SettingString) Config.settingsList.addSetting(new SettingString("OverrideUrl", SettingCategory.Login, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFile DatabaseName = (SettingFile) Config.settingsList.addSetting(new SettingFile("DatabaseName", SettingCategory.Folder, NEVER, "?/cachebox.db3", SettingStoreType.Global, SettingUsage.ACB, "db3"));
    public static final SettingBool StartWithAutoSelect = (SettingBool) Config.settingsList.addSetting(new SettingBool("StartWithAutoSelect", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MultiDBAsk = (SettingBool) Config.settingsList.addSetting(new SettingBool("MultiDBAsk", SettingCategory.Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ImperialUnits = (SettingBool) Config.settingsList.addSetting(new SettingBool("ImperialUnits", SettingCategory.Locale, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SearchWithoutFounds = (SettingBool) Config.settingsList.addSetting(new SettingBool("SearchWithoutFounds", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SearchWithoutOwns = (SettingBool) Config.settingsList.addSetting(new SettingBool("SearchWithoutOwns", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SearchOnlyAvailable = (SettingBool) Config.settingsList.addSetting(new SettingBool("SearchOnlyAvailable", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool DescriptionNoAttributes = (SettingBool) Config.settingsList.addSetting(new SettingBool("DescriptionNoAttributes", SettingCategory.Misc, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool quickButtonLastShow = (SettingBool) Config.settingsList.addSetting(new SettingBool("quickButtonLastShow", SettingCategory.QuickList, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt MultiDBAutoStartTime = (SettingInt) Config.settingsList.addSetting(new SettingInt("MultiDBAutoStartTime", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt lastSearchRadius = (SettingInt) Config.settingsList.addSetting(new SettingInt("lastSearchRadius", SettingCategory.API, NEVER, 5, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString quickButtonList = (SettingString) Config.settingsList.addSetting(new SettingString("quickButtonList", SettingCategory.QuickList, NEVER, "5,0,1,3,2", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString LastSelectedCache = (SettingString) Config.settingsList.addSetting(new SettingString("LastSelectedCache", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
    public static final SettingLongString FilterNew = (SettingLongString) Config.settingsList.addSetting(new SettingLongString("FilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));


    // AudioSettings
    public static final SettingInt AppRaterlaunchCount = (SettingInt) Config.settingsList.addSetting(new SettingInt("AppRaterlaunchCount", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingsAudio GlobalVolume = (SettingsAudio) Config.settingsList.addSetting(new SettingsAudio("GlobalVolume", SettingCategory.Sounds, NORMAL, new Audio("sound/Approach.mp3", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio Approach = (SettingsAudio) Config.settingsList.addSetting(new SettingsAudio("Approach", SettingCategory.Sounds, NORMAL, new Audio("sound/Approach.mp3", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio GPS_lose = (SettingsAudio) Config.settingsList.addSetting(new SettingsAudio("GPS_lose", SettingCategory.Sounds, NORMAL, new Audio("sound/GPS_lose.mp3", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio GPS_fix = (SettingsAudio) Config.settingsList.addSetting(new SettingsAudio("GPS_fix", SettingCategory.Sounds, NORMAL, new Audio("sound/GPS_Fix.mp3", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio AutoResortSound = (SettingsAudio) Config.settingsList.addSetting(new SettingsAudio("AutoResortSound", SettingCategory.Sounds, NORMAL, new Audio("sound/AutoResort.mp3", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingIntArray SoundApproachDistance = (SettingIntArray) Config.settingsList.addSetting(new SettingIntArray("SoundApproachDistance", SettingCategory.Misc, NEVER, 50, SettingStoreType.Global, SettingUsage.ACB, approach));
    public static final SettingFolder ImageCacheFolder = (SettingFolder) Config.settingsList.addSetting(new SettingFolder("ImageCacheFolder", SettingCategory.Folder, NEVER, "?/repository/cache", SettingStoreType.Local, SettingUsage.ACB, true));
    public static final SettingBool SettingsShowExpert = (SettingBool) Config.settingsList.addSetting(new SettingBool("SettingsShowExpert", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SettingsShowAll = (SettingBool) Config.settingsList.addSetting(new SettingBool("SettingsShowAll", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFloat CompassViewSplit = (SettingFloat) Config.settingsList.addSetting(new SettingFloat("CompassViewSplit", SettingCategory.Compass, NEVER, 0.5f, SettingStoreType.Global, SettingUsage.ACB));


    public static final SettingBool DraftsLoadAll = (SettingBool) Config.settingsList.addSetting(new SettingBool("DraftsLoadAll", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt DraftsLoadLength = (SettingInt) Config.settingsList.addSetting(new SettingInt("DraftsLoadLength", SettingCategory.Fieldnotes, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FoundOffset = (SettingInt) Config.settingsList.addSetting(new SettingInt("FoundOffset", SettingCategory.Misc, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString FoundTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("FoundTemplate", SettingCategory.Templates, NORMAL, FOUND, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString AttendedTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("AttendedTemplate", SettingCategory.Templates, NORMAL, ATTENDED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString WebcamTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("WebCamTemplate", SettingCategory.Templates, NORMAL, WEBCAM, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString DNFTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("DNFTemplate", SettingCategory.Templates, NORMAL, DNF, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString NeedsMaintenanceTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("NeedsMaintenanceTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString AddNoteTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("AddNoteTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString DiscoverdTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("DiscoverdTemplate", SettingCategory.Templates, NORMAL, DISCOVERD, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString VisitedTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("VisitedTemplate", SettingCategory.Templates, NORMAL, VISITED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString DroppedTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("DroppedTemplate", SettingCategory.Templates, NORMAL, DROPPED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString GrabbedTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("GrabbedTemplate", SettingCategory.Templates, NORMAL, GRABED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString PickedTemplate = (SettingString) Config.settingsList.addSetting(new SettingLongString("PickedTemplate", SettingCategory.Templates, NORMAL, PICKED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFile DraftsGarminPath = (SettingFile) Config.settingsList.addSetting(new SettingFile("DraftsGarminPath", SettingCategory.Folder, DEVELOPER, "?/user/geocache_visits.txt", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingEncryptedString GcVotePassword = (SettingEncryptedString) Config.settingsList.addSetting(new SettingEncryptedString("GcVotePassword", SettingCategory.Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL));

    public static final SettingInt VibrateTime = (SettingInt) Config.settingsList.addSetting(new SettingInt("VibrateTime", SettingCategory.Misc, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool VibrateFeedback = (SettingBool) Config.settingsList.addSetting(new SettingBool("VibrateFeedback", SettingCategory.Misc, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingLongString UserFilter = (SettingLongString) Config.settingsList.addSetting(new SettingLongString("UserFilter", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt HardwareCompassLevel = (SettingInt) Config.settingsList.addSetting(new SettingInt("HardwareCompassLevel", SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool HardwareCompassOnly = (SettingBool) Config.settingsList.addSetting(new SettingBool("HardwareCompassOnly", SettingCategory.Gps, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));


    //        public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", SettingCategory.LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", SettingCategory.LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);


//    public static final SettingBool DisableLiveMap = (SettingBool) Config.settingsList.addSetting(new SettingBool("DisableLiveMap", SettingCategory.LiveMap, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingInt LiveMaxCount = (SettingInt) Config.settingsList.addSetting(new SettingInt("LiveMaxCount", SettingCategory.LiveMap, EXPERT, 350, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool LiveExcludeFounds = (SettingBool) Config.settingsList.addSetting(new SettingBool("LiveExcludeFounds", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool LiveExcludeOwn = (SettingBool) Config.settingsList.addSetting(new SettingBool("LiveExcludeOwn", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool showSandbox = (SettingBool) Config.settingsList.addSetting(new SettingBool("showSandbox", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingInt gpsUpdateTime = (SettingInt) Config.settingsList.addSetting(new SettingInt("gpsUpdateTime", SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingInt conection_timeout = (SettingInt) Config.settingsList.addSetting(new SettingInt("conection_timeout", SettingCategory.Internal, DEVELOPER, 10000, SettingStoreType.Global, SettingUsage.ALL));

//    public static final SettingFile gpxExportFileName = (SettingFile) Config.settingsList.addSetting(new SettingFile("gpxExportFileName", SettingCategory.Folder, NEVER,  "?/user/export.gpx", SettingStoreType.Global, SettingUsage.ACB, "gpx"));
//    public static final SettingBool TrackRecorderStartup = (SettingBool) Config.settingsList.addSetting(new SettingBool("TrackRecorderStartup", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ImportGpx = (SettingBool) Config.settingsList.addSetting(new SettingBool("ImportGpx", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CacheMapData = (SettingBool) Config.settingsList.addSetting(new SettingBool("CacheMapData", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CacheImageData = (SettingBool) Config.settingsList.addSetting(new SettingBool("CacheImageData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CacheSpoilerData = (SettingBool) Config.settingsList.addSetting(new SettingBool("CacheSpoilerData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool SuppressPowerSaving = (SettingBool) Config.settingsList.addSetting(new SettingBool("SuppressPowerSaving", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool GCAdditionalImageDownload = (SettingBool) Config.settingsList.addSetting(new SettingBool("GCAdditionalImageDownload", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString CacheHistory = (SettingString) Config.settingsList.addSetting(new SettingString("CacheHistory", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
//    public static final SettingString NavigationProvider = (SettingString) Config.settingsList.addSetting(new SettingString("NavigationProvider", SettingCategory.Internal, DEVELOPER, "http://openls.geog.uni-heidelberg.de/testing2015/route?", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString SpoilersDescriptionTags = (SettingString) Config.settingsList.addSetting(new SettingString("SpoilersDescriptionTags", SettingCategory.Internal, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString GcJoker = (SettingString) Config.settingsList.addSetting(new SettingString("GcJoker", SettingCategory.Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingStringArray Navis = (SettingStringArray) Config.settingsList.addSetting(new SettingStringArray("Navis", SettingCategory.Misc, NORMAL, "Google", SettingStoreType.Global, SettingUsage.ACB, navis));
//    public static final SettingBool ShowFieldnotesCMwithFirstShow = (SettingBool) Config.settingsList.addSetting(new SettingBool("ShowFieldnotesCMwithFirstShow", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ShowFieldnotesAsDefaultView = (SettingBool) Config.settingsList.addSetting(new SettingBool("ShowFieldnotesAsDefaultView", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool LiveMapEnabeld = (SettingBool) Config.settingsList.addSetting(new SettingBool("LiveMapEnabeld", SettingCategory.LiveMap, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool AppRaterDontShowAgain = (SettingBool) Config.settingsList.addSetting(new SettingBool("AppRaterDontShowAgain", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingString AppRaterFirstLunch = (SettingString) Config.settingsList.addSetting(new SettingString("AppRaterFirstLunch", SettingCategory.Internal, NEVER, "0", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool GestureOn = (SettingBool) Config.settingsList.addSetting(new SettingBool("GestureOn", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingColor LiveMapBackgroundColor = (SettingColor) Config.settingsList.addSetting(new SettingColor("LiveMapBackgroundColor", SettingCategory.LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingLongString Filter = (SettingLongString) Config.settingsList.addSetting(new SettingLongString("Filter", SettingCategory.Misc, NEVER, FilterProperties.presets[0].toString(), SettingStoreType.Local, SettingUsage.ALL));
//
//    public static final SettingLongString UserFilterNew = (SettingLongString) Config.settingsList.addSetting(new SettingLongString("UserFilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingTime ScreenLock = (SettingTime) Config.settingsList.addSetting(new SettingTime("ScreenLock", SettingCategory.Misc, NEVER, 60000, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ImportRatings = (SettingBool) Config.settingsList.addSetting(new SettingBool("ImportRatings", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool ImportPQsFromGeocachingCom = (SettingBool) Config.settingsList.addSetting(new SettingBool("ImportPQsFromGeocachingCom", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool switchViewApproach = (SettingBool) Config.settingsList.addSetting(new SettingBool("switchViewApproach", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool hasCallPermission = (SettingBool) Config.settingsList.addSetting(new SettingBool("hasCallPermission", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingBool hasPQ_PlugIn = (SettingBool) Config.settingsList.addSetting(new SettingBool("hasPQ_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool hasFTF_PlugIn = (SettingBool) Config.settingsList.addSetting(new SettingBool("hasFTF_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool dynamicFilterAtSearch = (SettingBool) Config.settingsList.addSetting(new SettingBool("dynamicFilterAtSearch", SettingCategory.Misc, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool DeleteLogs = (SettingBool) Config.settingsList.addSetting(new SettingBool("DeleteLogs", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool CompactDB = (SettingBool) Config.settingsList.addSetting(new SettingBool("CompactDB", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool AskAgain = (SettingBool) Config.settingsList.addSetting(new SettingBool("AskAgain", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingBool RememberAsk_Get_API_Key = (SettingBool) Config.settingsList.addSetting(new SettingBool("RememberAsk_Get_API_Key", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool Ask_Switch_GPS_ON = (SettingBool) Config.settingsList.addSetting(new SettingBool("Ask_Switch_GPS_ON", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingBool TB_DirectLog = (SettingBool) Config.settingsList.addSetting(new SettingBool("TB_DirectLog", SettingCategory.Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ALL));
//    public static final SettingInt LogMaxMonthAge = (SettingInt) Config.settingsList.addSetting(new SettingInt("LogMaxMonthAge", SettingCategory.Internal, DEVELOPER, 6, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingInt LogMinCount = (SettingInt) Config.settingsList.addSetting(new SettingInt("LogMinCount", SettingCategory.Internal, DEVELOPER, 99999, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingInt installRev = (SettingInt) Config.settingsList.addSetting(new SettingInt("installRev", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));

//    public static final SettingBool newInstall = (SettingBool) Config.settingsList.addSetting(new SettingBool("newInstall", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool FieldnotesUploadAll = (SettingBool) Config.settingsList.addSetting(new SettingBool("FieldnotesUploadAll", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
//    public static final SettingBool quickButtonShow = (SettingBool) Config.settingsList.addSetting(new SettingBool("quickButtonShow", SettingCategory.QuickList, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));


}
