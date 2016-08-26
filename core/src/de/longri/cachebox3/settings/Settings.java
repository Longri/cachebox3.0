package de.longri.cachebox3.settings;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.HSV_Color;

/**
 * Created by Longri on 31.07.16.
 */
public class Settings {

    static {
        new SettingsList();
    }

    public static final SettingMode DEVELOPER = SettingMode.DEVELOPER;
    public static final SettingMode NORMAL = SettingMode.Normal;
    public static final SettingMode EXPERT = SettingMode.Expert;
    public static final SettingMode NEVER = SettingMode.Never;

    public static final Integer Level[] = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
    public static final Integer CrossLevel[] = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

    public static final String FOUND = "<br>###finds##, ##time##, Found it with Cachebox!";
    public static final String ATTENDED = "<br>###finds##, ##time##, Have been there!";
    public static final String WEBCAM = "<br>###finds##, ##time##, Photo taken!";
    public static final String DNF = "<br>##time##. Could not find the cache!";
    public static final String LOG = "Logged it with Cachebox!";
    public static final String DISCOVERD = "<br> ##time##, Discovered it with Cachebox!";
    public static final String VISITED = "<br> ##time##, Visited it with Cachebox!";
    public static final String DROPPED = "<br> ##time##, Dropped off with Cachebox!";
    public static final String PICKED = "<br> ##time##, Picked it with Cachebox!";
    public static final String GRABED = "<br> ##time##, Grabed it with Cachebox!";

    public static final Integer[] approach = new Integer[]{0, 2, 10, 25, 50, 100, 200, 500, 1000};
    public static final Integer[] TrackDistanceArray = new Integer[]{1, 3, 5, 10, 20};
    public static final String[] navis = new String[]{"Navigon", "Google", "Copilot", "OsmAnd", "OsmAnd2", "Waze", "Orux", "Ask"};


    public static final SettingString GcLogin = (SettingString) SettingsList.addSetting(new SettingString("GcLogin", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL));
    public static final SettingEncryptedString GcAPI = (SettingEncryptedString) SettingsList.addSetting(new SettingEncryptedString("GcAPI", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Platform, SettingUsage.ALL));
    public static final SettingEncryptedString GcAPIStaging = (SettingEncryptedString) SettingsList.addSetting(new SettingEncryptedString("GcAPIStaging", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Platform, SettingUsage.ALL));

    // Folder Settings
    public static final SettingFolder DescriptionImageFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("DescriptionImageFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/images", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder DescriptionImageFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("DescriptionImageFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true));
    public static final SettingFolder SpoilerFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("SpoilerFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/spoilers", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder SpoilerFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("SpoilerFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true));
    public static final SettingFolder PocketQueryFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("PocketQueryFolder", SettingCategory.Folder, DEVELOPER, CB.WorkPath + "/pocketQuery", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder UserImageFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("UserImageFolder", SettingCategory.Folder, NORMAL, CB.WorkPath + "/user/media", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingBool StagingAPI = (SettingBool) SettingsList.addSetting(new SettingBool("StagingAPI", SettingCategory.Folder, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ALL));

    public static final SettingInt conection_timeout = (SettingInt) SettingsList.addSetting(new SettingInt("conection_timeout", SettingCategory.Internal, DEVELOPER, 10000, SettingStoreType.Global, SettingUsage.ALL));
    public static final SettingInt socket_timeout = (SettingInt) SettingsList.addSetting(new SettingInt("socket_timeout", SettingCategory.Internal, DEVELOPER, 60000, SettingStoreType.Global, SettingUsage.ALL));
    public static final SettingEncryptedString GcVotePassword = (SettingEncryptedString) SettingsList.addSetting(new SettingEncryptedString("GcVotePassword", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL));
    public static final SettingDouble ParkingLatitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("ParkingLatitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingDouble ParkingLongitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("ParkingLongitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool FieldNotesLoadAll = (SettingBool) SettingsList.addSetting(new SettingBool("FieldNotesLoadAll", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FieldNotesLoadLength = (SettingInt) SettingsList.addSetting(new SettingInt("FieldNotesLoadLength", SettingCategory.Fieldnotes, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString Friends = (SettingString) SettingsList.addSetting(new SettingString("Friends", SettingCategory.Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowAllWaypoints = (SettingBool) SettingsList.addSetting(new SettingBool("ShowAllWaypoints", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool DisableLiveMap = (SettingBool) SettingsList.addSetting(new SettingBool("DisableLiveMap", SettingCategory.LiveMap, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt LiveMaxCount = (SettingInt) SettingsList.addSetting(new SettingInt("LiveMaxCount", SettingCategory.LiveMap, EXPERT, 350, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool LiveExcludeFounds = (SettingBool) SettingsList.addSetting(new SettingBool("LiveExcludeFounds", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool LiveExcludeOwn = (SettingBool) SettingsList.addSetting(new SettingBool("LiveExcludeOwn", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingBool DirectOnlineLog = (SettingBool) SettingsList.addSetting(new SettingBool("DirectOnlineLog", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool showSandbox = (SettingBool) SettingsList.addSetting(new SettingBool("showSandbox", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Platform, SettingUsage.ACB));
    public static final SettingBool showGestureHelp = (SettingBool) SettingsList.addSetting(new SettingBool("showGestureHelp", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform, SettingUsage.ACB));


    // Settings Compass
    public static final SettingInt HardwareCompassLevel = (SettingInt) SettingsList.addSetting(new SettingInt("HardwareCompassLevel", SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool HardwareCompass = (SettingBool) SettingsList.addSetting(new SettingBool("HardwareCompass", SettingCategory.Gps, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt gpsUpdateTime = (SettingInt) SettingsList.addSetting(new SettingInt("gpsUpdateTime", SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowMap = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowMap", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowWP_Name = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowWP_Name", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowWP_Icon = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowWP_Icon", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowAttributes = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowAttributes", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowGcCode = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowGcCode", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowCoords = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowCoords", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowWpDesc = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowWpDesc", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowSatInfos = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowSatInfos", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowSunMoon = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowSunMoon", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowTargetDirection = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowTargetDirection", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowSDT = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowSDT", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowLastFound = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowLastFound", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString OverrideUrl = (SettingString) SettingsList.addSetting(new SettingString("OverrideUrl", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));
    // Folder
    public static final SettingFolder TrackFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("TrackFolder", SettingCategory.Folder, EXPERT, CB.WorkPath + "/user/tracks", SettingStoreType.Global, SettingUsage.ACB, true));
    // Files
    public static final SettingFile DatabasePath = (SettingFile) SettingsList.addSetting(new SettingFile("DatabasePath", SettingCategory.Folder, NEVER, CB.WorkPath + "/cachebox.db3", SettingStoreType.Global, SettingUsage.ACB, "db3"));
    public static final SettingFile FieldNotesGarminPath = (SettingFile) SettingsList.addSetting(new SettingFile("FieldNotesGarminPath", SettingCategory.Folder, DEVELOPER, CB.WorkPath + "/user/geocache_visits.txt", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFile gpxExportFileName = (SettingFile) SettingsList.addSetting(new SettingFile("gpxExportFileName", SettingCategory.Folder, NEVER, CB.WorkPath + "/user/export.gpx", SettingStoreType.Global, SettingUsage.ACB, "gpx"));
    //
    public static final SettingBool MapShowRating = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowRating", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapShowDT = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowDT", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapShowTitles = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowTitles", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool TrackRecorderStartup = (SettingBool) SettingsList.addSetting(new SettingBool("TrackRecorderStartup", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapShowCompass = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowCompass", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassNorthOriented = (SettingBool) SettingsList.addSetting(new SettingBool("CompassNorthOriented", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MapNorthOriented = (SettingBool) SettingsList.addSetting(new SettingBool("MapNorthOriented", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ImportGpx = (SettingBool) SettingsList.addSetting(new SettingBool("ImportGpx", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CacheMapData = (SettingBool) SettingsList.addSetting(new SettingBool("CacheMapData", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CacheImageData = (SettingBool) SettingsList.addSetting(new SettingBool("CacheImageData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CacheSpoilerData = (SettingBool) SettingsList.addSetting(new SettingBool("CacheSpoilerData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SuppressPowerSaving = (SettingBool) SettingsList.addSetting(new SettingBool("SuppressPowerSaving", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool GCAdditionalImageDownload = (SettingBool) SettingsList.addSetting(new SettingBool("GCAdditionalImageDownload", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool StartWithAutoSelect = (SettingBool) SettingsList.addSetting(new SettingBool("StartWithAutoSelect", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool FieldnotesUploadAll = (SettingBool) SettingsList.addSetting(new SettingBool("FieldnotesUploadAll", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool MultiDBAsk = (SettingBool) SettingsList.addSetting(new SettingBool("MultiDBAsk", SettingCategory.Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SearchWithoutFounds = (SettingBool) SettingsList.addSetting(new SettingBool("SearchWithoutFounds", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SearchWithoutOwns = (SettingBool) SettingsList.addSetting(new SettingBool("SearchWithoutOwns", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SearchOnlyAvailable = (SettingBool) SettingsList.addSetting(new SettingBool("SearchOnlyAvailable", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool quickButtonShow = (SettingBool) SettingsList.addSetting(new SettingBool("quickButtonShow", SettingCategory.QuickList, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool DescriptionNoAttributes = (SettingBool) SettingsList.addSetting(new SettingBool("DescriptionNoAttributes", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool quickButtonLastShow = (SettingBool) SettingsList.addSetting(new SettingBool("quickButtonLastShow", SettingCategory.QuickList, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool newInstall = (SettingBool) SettingsList.addSetting(new SettingBool("newInstall", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ImperialUnits = (SettingBool) SettingsList.addSetting(new SettingBool("ImperialUnits", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowDirektLine = (SettingBool) SettingsList.addSetting(new SettingBool("ShowDirektLine", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ImportRatings = (SettingBool) SettingsList.addSetting(new SettingBool("ImportRatings", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ImportPQsFromGeocachingCom = (SettingBool) SettingsList.addSetting(new SettingBool("ImportPQsFromGeocachingCom", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool switchViewApproach = (SettingBool) SettingsList.addSetting(new SettingBool("switchViewApproach", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool hasCallPermission = (SettingBool) SettingsList.addSetting(new SettingBool("hasCallPermission", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool vibrateFeedback = (SettingBool) SettingsList.addSetting(new SettingBool("vibrateFeedback", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool hasPQ_PlugIn = (SettingBool) SettingsList.addSetting(new SettingBool("hasPQ_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool hasFTF_PlugIn = (SettingBool) SettingsList.addSetting(new SettingBool("hasFTF_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool dynamicZoom = (SettingBool) SettingsList.addSetting(new SettingBool("dynamicZoom", SettingCategory.CarMode, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool dynamicFilterAtSearch = (SettingBool) SettingsList.addSetting(new SettingBool("dynamicFilterAtSearch", SettingCategory.Misc, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool DeleteLogs = (SettingBool) SettingsList.addSetting(new SettingBool("DeleteLogs", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompactDB = (SettingBool) SettingsList.addSetting(new SettingBool("CompactDB", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool AskAgain = (SettingBool) SettingsList.addSetting(new SettingBool("AskAgain", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform, SettingUsage.ALL));
    public static final SettingBool RememberAsk_Get_API_Key = (SettingBool) SettingsList.addSetting(new SettingBool("RememberAsk_Get_API_Key", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool Ask_Switch_GPS_ON = (SettingBool) SettingsList.addSetting(new SettingBool("Ask_Switch_GPS_ON", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform, SettingUsage.ALL));
    public static final SettingBool TB_DirectLog = (SettingBool) SettingsList.addSetting(new SettingBool("TB_DirectLog", SettingCategory.Internal, NEVER, true, SettingStoreType.Platform, SettingUsage.ALL));
    public static final SettingBool MapHideMyFinds = (SettingBool) SettingsList.addSetting(new SettingBool("MapHideMyFinds", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    // int
    public static final SettingInt LogMaxMonthAge = (SettingInt) SettingsList.addSetting(new SettingInt("LogMaxMonthAge", SettingCategory.Internal, DEVELOPER, 6, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt LogMinCount = (SettingInt) SettingsList.addSetting(new SettingInt("LogMinCount", SettingCategory.Internal, DEVELOPER, 99999, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt installRev = (SettingInt) SettingsList.addSetting(new SettingInt("installRev", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt MapIniWidth = (SettingInt) SettingsList.addSetting(new SettingInt("MapIniWidth", SettingCategory.Map, NEVER, 480, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt MapIniHeight = (SettingInt) SettingsList.addSetting(new SettingInt("MapIniHeight", SettingCategory.Map, NEVER, 535, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt VibrateTime = (SettingInt) SettingsList.addSetting(new SettingInt("VibrateTime", SettingCategory.Misc, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FoundOffset = (SettingInt) SettingsList.addSetting(new SettingInt("FoundOffset", SettingCategory.Misc, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt MultiDBAutoStartTime = (SettingInt) SettingsList.addSetting(new SettingInt("MultiDBAutoStartTime", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt lastSearchRadius = (SettingInt) SettingsList.addSetting(new SettingInt("lastSearchRadius", SettingCategory.API, NEVER, 5, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt LastMapToggleBtnState = (SettingInt) SettingsList.addSetting(new SettingInt("LastMapToggleBtnState", SettingCategory.Map, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt dynamicZoomLevelMax = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMax", SettingCategory.CarMode, NORMAL, 17, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt dynamicZoomLevelMin = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMin", SettingCategory.CarMode, NORMAL, 15, SettingStoreType.Global, SettingUsage.ACB));
    // String
    public static final SettingString LastSelectedCache = (SettingString) SettingsList.addSetting(new SettingString("LastSelectedCache", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
    public static final SettingString CacheHistory = (SettingString) SettingsList.addSetting(new SettingString("CacheHistory", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
    public static final SettingString NavigationProvider = (SettingString) SettingsList
            .addSetting(new SettingString("NavigationProvider", SettingCategory.Internal, DEVELOPER, "http://openls.geog.uni-heidelberg.de/testing2015/route?", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString FoundTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("FoundTemplate", SettingCategory.Templates, NORMAL, FOUND, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString AttendedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("AttendedTemplate", SettingCategory.Templates, NORMAL, ATTENDED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString WebcamTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("WebCamTemplate", SettingCategory.Templates, NORMAL, WEBCAM, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString DNFTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DNFTemplate", SettingCategory.Templates, NORMAL, DNF, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString NeedsMaintenanceTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("NeedsMaintenanceTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString AddNoteTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("AddNoteTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString DiscoverdTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DiscoverdTemplate", SettingCategory.Templates, NORMAL, DISCOVERD, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString VisitedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("VisitedTemplate", SettingCategory.Templates, NORMAL, VISITED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString DroppedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DroppedTemplate", SettingCategory.Templates, NORMAL, DROPPED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString GrabbedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("GrabbedTemplate", SettingCategory.Templates, NORMAL, GRABED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString PickedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("PickedTemplate", SettingCategory.Templates, NORMAL, PICKED, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString SpoilersDescriptionTags = (SettingString) SettingsList.addSetting(new SettingString("SpoilersDescriptionTags", SettingCategory.Internal, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString quickButtonList = (SettingString) SettingsList.addSetting(new SettingString("quickButtonList", SettingCategory.QuickList, DEVELOPER, "5,0,1,3,2", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString GcJoker = (SettingString) SettingsList.addSetting(new SettingString("GcJoker", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL));
    public static final SettingStringArray Navis = (SettingStringArray) SettingsList.addSetting(new SettingStringArray("Navis", SettingCategory.Misc, NORMAL, "Google", SettingStoreType.Global, SettingUsage.ACB, navis));

    // ArrayInt
    public static final SettingIntArray ZoomCross = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("ZoomCross", SettingCategory.Map, NORMAL, 16, SettingStoreType.Global, SettingUsage.ACB, CrossLevel));
    public static final SettingIntArray SoundApproachDistance = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("SoundApproachDistance", SettingCategory.Misc, NORMAL, 50, SettingStoreType.Global, SettingUsage.ACB, approach));
    public static final SettingIntArray TrackDistance = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("TrackDistance", SettingCategory.Misc, NORMAL, 3, SettingStoreType.Global, SettingUsage.ACB, TrackDistanceArray));

    // double

    // longString
    //	public static final SettingLongString Filter = (SettingLongString) SettingsList.addSetting(new SettingLongString("Filter", SettingCategory.Misc, NEVER, FilterProperties.presets[0].toString(), SettingStoreType.Local, SettingUsage.ALL));
    public static final SettingLongString FilterNew = (SettingLongString) SettingsList.addSetting(new SettingLongString("FilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
    public static final SettingLongString UserFilter = (SettingLongString) SettingsList.addSetting(new SettingLongString("UserFilter", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingLongString UserFilterNew = (SettingLongString) SettingsList.addSetting(new SettingLongString("UserFilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingTime ScreenLock = (SettingTime) SettingsList.addSetting(new SettingTime("ScreenLock", SettingCategory.Misc, NEVER, 60000, SettingStoreType.Global, SettingUsage.ACB));

    // AudioSettings

    public static final SettingsAudio Approach = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("Approach", SettingCategory.Sounds, EXPERT, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio GPS_lose = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("GPS_lose", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_lose.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio GPS_fix = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("GPS_fix", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_Fix.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio AutoResortSound = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("AutoResortSound", SettingCategory.Sounds, EXPERT, new Audio("data/sound/AutoResort.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowFieldnotesCMwithFirstShow = (SettingBool) SettingsList.addSetting(new SettingBool("ShowFieldnotesCMwithFirstShow", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowFieldnotesAsDefaultView = (SettingBool) SettingsList.addSetting(new SettingBool("ShowFieldnotesAsDefaultView", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool LiveMapEnabeld = (SettingBool) SettingsList.addSetting(new SettingBool("LiveMapEnabeld", SettingCategory.LiveMap, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingBool AppRaterDontShowAgain = (SettingBool) SettingsList.addSetting(new SettingBool("AppRaterDontShowAgain", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Platform, SettingUsage.ACB));
    public static final SettingInt AppRaterlaunchCount = (SettingInt) SettingsList.addSetting(new SettingInt("AppRaterlaunchCount", SettingCategory.Internal, NEVER, 0, SettingStoreType.Platform, SettingUsage.ACB));
    public static final SettingString AppRaterFirstLunch = (SettingString) SettingsList.addSetting(new SettingString("AppRaterFirstLunch", SettingCategory.Internal, NEVER, "0", SettingStoreType.Platform, SettingUsage.ACB));

    public static final SettingBool nightMode = (SettingBool) SettingsList.addSetting(new SettingBool("nightMode", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingFolder SkinFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("SkinFolder", SettingCategory.Folder, DEVELOPER, "default", SettingStoreType.Global, SettingUsage.ACB, false));

    public static final SettingInt FONT_SIZE_COMPASS_DISTANCE = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_COMPASS_DISTANCE", SettingCategory.Skin, EXPERT, 25, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FONT_SIZE_BIG = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_BIG", SettingCategory.Skin, EXPERT, 16, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FONT_SIZE_NORMAL = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_NORMAL", SettingCategory.Skin, EXPERT, 14, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FONT_SIZE_NORMAL_BUBBLE = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_NORMAL_BUBBLE", SettingCategory.Skin, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FONT_SIZE_SMALL = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_SMALL", SettingCategory.Skin, EXPERT, 12, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt FONT_SIZE_SMALL_BUBBLE = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_SMALL_BUBBLE", SettingCategory.Skin, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingBool useMipMap = (SettingBool) SettingsList.addSetting(new SettingBool("useMipMap", SettingCategory.Skin, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool dontUseAmbient = (SettingBool) SettingsList.addSetting(new SettingBool("dontUseAmbient", SettingCategory.Skin, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingInt ambientTime = (SettingInt) SettingsList.addSetting(new SettingInt("ambientTime", SettingCategory.Skin, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingDouble MapViewFontFaktor = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapViewFontFaktor", SettingCategory.Map, NEVER, 1.0, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingInt LongClicktime = (SettingInt) SettingsList.addSetting(new SettingInt("LongClicktime", SettingCategory.Misc, EXPERT, 600, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingsAudio GlobalVolume = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("GlobalVolume", SettingCategory.Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingFloat MapViewDPIFaktor = (SettingFloat) SettingsList.addSetting(new SettingFloat("MapViewDPIFaktor", SettingCategory.Map, EXPERT, CB.getScalefactor(), SettingStoreType.Global, SettingUsage.ACB));

    // überprüfen
    public static final SettingFolder ImageCacheFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("ImageCacheFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/cache", SettingStoreType.Local, SettingUsage.ACB, true));

    public static final SettingBool GestureOn = (SettingBool) SettingsList.addSetting(new SettingBool("GestureOn", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingColor LiveMapBackgroundColor = (SettingColor) SettingsList.addSetting(new SettingColor("LiveMapBackgroundColor", SettingCategory.LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingColor SolvedMysteryColor = (SettingColor) SettingsList.addSetting(new SettingColor("SolvedMysteryColor", SettingCategory.Skin, EXPERT, new HSV_Color(0.2f, 1f, 0.2f, 1f), SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SettingsShowExpert = (SettingBool) SettingsList.addSetting(new SettingBool("SettingsShowExpert", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool SettingsShowAll = (SettingBool) SettingsList.addSetting(new SettingBool("SettingsShowAll", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFile Sel_LanguagePath = (SettingFile) SettingsList.addSetting(new SettingFile("Sel_LanguagePath", SettingCategory.Folder, NEVER, "lang/en-GB/strings.ini", SettingStoreType.Platform, SettingUsage.ALL, "lan"));
    public static final SettingFolder LanguagePath = (SettingFolder) SettingsList.addSetting(new SettingFolder("LanguagePath", SettingCategory.Folder, NEVER, "lang", SettingStoreType.Global, SettingUsage.ALL, true));


    public static final SettingFolder TileCacheFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("TileCacheFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/cache", SettingStoreType.Global, SettingUsage.ALL, true));
    public static final SettingFolder TileCacheFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("TileCacheFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true));
    public static final SettingFolder MapPackFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("MapPackFolder", SettingCategory.Map, EXPERT, CB.WorkPath + "/repository/maps", SettingStoreType.Global, SettingUsage.ALL, false));
    public static final SettingFolder MapPackFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("MapPackFolderLocal", SettingCategory.Map, NEVER, CB.WorkPath + "/repository/maps", SettingStoreType.Local, SettingUsage.ALL, false));

    public static final SettingStringList CurrentMapLayer = (SettingStringList) SettingsList.addSetting(new SettingStringList("CurrentMapLayer", SettingCategory.Map, DEVELOPER, new String[]{"Mapnik"}, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString CurrentMapOverlayLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapOverlayLayer", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingDouble MapInitLatitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapInitLatitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL));

    public static final SettingDouble MapInitLongitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapInitLongitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL));

    public static final SettingInt lastZoomLevel = (SettingInt) SettingsList.addSetting(new SettingInt("lastZoomLevel", SettingCategory.Map, DEVELOPER, 14, SettingStoreType.Global, SettingUsage.ALL));

    public static final SettingBool MoveMapCenterWithSpeed = (SettingBool) SettingsList.addSetting(new SettingBool("MoveMapCenterWithSpeed", SettingCategory.CarMode, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingInt MoveMapCenterMaxSpeed = (SettingInt) SettingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed", SettingCategory.CarMode, NORMAL, 60, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingBool ShowAccuracyCircle = (SettingBool) SettingsList.addSetting(new SettingBool("ShowAccuracyCircle", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowMapCenterCross = (SettingBool) SettingsList.addSetting(new SettingBool("ShowMapCenterCross", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingBool PositionMarkerTransparent = (SettingBool) SettingsList.addSetting(new SettingBool("PositionMarkerTransparent", SettingCategory.Map, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingIntArray OsmMinLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("OsmMinLevel", SettingCategory.Map, EXPERT, 7, SettingStoreType.Global, SettingUsage.ACB, Level));
    public static final SettingIntArray OsmMaxLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("OsmMaxLevel", SettingCategory.Map, EXPERT, 19, SettingStoreType.Global, SettingUsage.ACB, Level));
    public static final SettingIntArray CompassMapMinZoomLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("CompassMapMinZoomLevel", SettingCategory.Map, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB, Level));
    public static final SettingIntArray CompassMapMaxZommLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("CompassMapMaxZommLevel", SettingCategory.Map, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB, Level));

    public static final SettingFolder RenderThemesFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("RenderThemesFolder", SettingCategory.Map, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL, false));
    public static final SettingFile MapsforgeDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    public static final SettingFile MapsforgeNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    public static final SettingFile MapsforgeCarDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    public static final SettingFile MapsforgeCarNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));


    public static final SettingBool DEBUG_MapGrid = (SettingBool) SettingsList.addSetting(new SettingBool("DEBUG_MapGrid", SettingCategory.Debug, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB));


    //    public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", SettingCategory.LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", SettingCategory.LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);

}
