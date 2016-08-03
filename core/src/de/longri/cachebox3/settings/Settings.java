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

    public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
    public static final SettingModus NORMAL = SettingModus.Normal;
    public static final SettingModus EXPERT = SettingModus.Expert;
    public static final SettingModus NEVER = SettingModus.Never;

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


    public static final SettingString GcLogin = new SettingString("GcLogin", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL);
    public static final SettingEncryptedString GcAPI = new SettingEncryptedString("GcAPI", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Platform, SettingUsage.ALL);
    public static final SettingEncryptedString GcAPIStaging = new SettingEncryptedString("GcAPIStaging", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Platform, SettingUsage.ALL);

    // Folder Settings
    public static final SettingFolder DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/images", SettingStoreType.Global, SettingUsage.ALL, true);
    public static final SettingFolder DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
    public static final SettingFolder SpoilerFolder = new SettingFolder("SpoilerFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/spoilers", SettingStoreType.Global, SettingUsage.ALL, true);
    public static final SettingFolder SpoilerFolderLocal = new SettingFolder("SpoilerFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
    public static final SettingFolder PocketQueryFolder = new SettingFolder("PocketQueryFolder", SettingCategory.Folder, DEVELOPER, CB.WorkPath + "/PocketQuery", SettingStoreType.Global, SettingUsage.ALL, true);
    public static final SettingFolder UserImageFolder = new SettingFolder("UserImageFolder", SettingCategory.Folder, NORMAL, CB.WorkPath + "/User/Media", SettingStoreType.Global, SettingUsage.ALL, true);
    public static final SettingBool StagingAPI = new SettingBool("StagingAPI", SettingCategory.Folder, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ALL);

    public static final SettingInt conection_timeout = new SettingInt("conection_timeout", SettingCategory.Internal, DEVELOPER, 10000, SettingStoreType.Global, SettingUsage.ALL);
    public static final SettingInt socket_timeout = new SettingInt("socket_timeout", SettingCategory.Internal, DEVELOPER, 60000, SettingStoreType.Global, SettingUsage.ALL);
    public static final SettingEncryptedString GcVotePassword = new SettingEncryptedString("GcVotePassword", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL);
    public static final SettingDouble ParkingLatitude = new SettingDouble("ParkingLatitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingDouble ParkingLongitude = new SettingDouble("ParkingLongitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool FieldNotesLoadAll = new SettingBool("FieldNotesLoadAll", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt FieldNotesLoadLength = new SettingInt("FieldNotesLoadLength", SettingCategory.Fieldnotes, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingString Friends = (SettingString) SettingsList.addSetting(new SettingString("Friends", SettingCategory.Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool ShowAllWaypoints = new SettingBool("ShowAllWaypoints", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool DisableLiveMap = new SettingBool("DisableLiveMap", SettingCategory.LiveMap, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt LiveMaxCount = new SettingInt("LiveMaxCount", SettingCategory.LiveMap, EXPERT, 350, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool LiveExcludeFounds = new SettingBool("LiveExcludeFounds", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool LiveExcludeOwn = new SettingBool("LiveExcludeOwn", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingBool DirectOnlineLog = new SettingBool("DirectOnlineLog", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool showSandbox = new SettingBool("showSandbox", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Platform, SettingUsage.ACB);


    // Settings Compass
    public static final SettingInt HardwareCompassLevel = (SettingInt) SettingsList.addSetting(new SettingInt("HardwareCompassLevel", SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool HardwareCompass = new SettingBool("HardwareCompass", SettingCategory.Gps, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt gpsUpdateTime = (SettingInt) SettingsList.addSetting(new SettingInt("gpsUpdateTime", SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingBool CompassShowMap = new SettingBool("CompassShowMap", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowWP_Name = new SettingBool("CompassShowWP_Name", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowWP_Icon = new SettingBool("CompassShowWP_Icon", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowAttributes = new SettingBool("CompassShowAttributes", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowGcCode = new SettingBool("CompassShowGcCode", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowCoords = new SettingBool("CompassShowCoords", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowWpDesc = new SettingBool("CompassShowWpDesc", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowSatInfos = new SettingBool("CompassShowSatInfos", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowSunMoon = new SettingBool("CompassShowSunMoon", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowTargetDirection = new SettingBool("CompassShowTargetDirection", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowSDT = new SettingBool("CompassShowSDT", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassShowLastFound = new SettingBool("CompassShowLastFound", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingString OverrideUrl = (SettingString) SettingsList.addSetting(new SettingString("OverrideUrl", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));
    // Folder
    public static final SettingFolder TrackFolder = new SettingFolder("TrackFolder", SettingCategory.Folder, EXPERT, CB.WorkPath + "/User/Tracks", SettingStoreType.Global, SettingUsage.ACB, true);
    // Files
    public static final SettingFile DatabasePath = (SettingFile) SettingsList.addSetting(new SettingFile("DatabasePath", SettingCategory.Folder, NEVER, CB.WorkPath + "/cachebox.db3", SettingStoreType.Global, SettingUsage.ACB, "db3"));
    public static final SettingFile FieldNotesGarminPath = (SettingFile) SettingsList
            .addSetting(new SettingFile("FieldNotesGarminPath", SettingCategory.Folder, DEVELOPER, CB.WorkPath + "/User/geocache_visits.txt", SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingFile gpxExportFileName = new SettingFile("gpxExportFileName", SettingCategory.Folder, NEVER, CB.WorkPath + "/User/export.gpx", SettingStoreType.Global, SettingUsage.ACB, "gpx");
    //
    public static final SettingBool MapShowRating = new SettingBool("MapShowRating", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool MapShowDT = new SettingBool("MapShowDT", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool MapShowTitles = new SettingBool("MapShowTitles", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool TrackRecorderStartup = new SettingBool("TrackRecorderStartup", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool MapShowCompass = new SettingBool("MapShowCompass", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompassNorthOriented = new SettingBool("CompassNorthOriented", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool MapNorthOriented = new SettingBool("MapNorthOriented", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ImportGpx = new SettingBool("ImportGpx", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CacheMapData = new SettingBool("CacheMapData", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CacheImageData = new SettingBool("CacheImageData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CacheSpoilerData = new SettingBool("CacheSpoilerData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool SuppressPowerSaving = new SettingBool("SuppressPowerSaving", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool GCAdditionalImageDownload = new SettingBool("GCAdditionalImageDownload", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool StartWithAutoSelect = new SettingBool("StartWithAutoSelect", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool FieldnotesUploadAll = new SettingBool("FieldnotesUploadAll", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool MultiDBAsk = new SettingBool("MultiDBAsk", SettingCategory.Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool SearchWithoutFounds = new SettingBool("SearchWithoutFounds", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool SearchWithoutOwns = new SettingBool("SearchWithoutOwns", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool SearchOnlyAvailable = new SettingBool("SearchOnlyAvailable", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool quickButtonShow = new SettingBool("quickButtonShow", SettingCategory.QuickList, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool DescriptionNoAttributes = new SettingBool("DescriptionNoAttributes", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool quickButtonLastShow = new SettingBool("quickButtonLastShow", SettingCategory.QuickList, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool newInstall = new SettingBool("newInstall", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ImperialUnits = new SettingBool("ImperialUnits", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ShowDirektLine = new SettingBool("ShowDirektLine", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ImportRatings = new SettingBool("ImportRatings", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool switchViewApproach = new SettingBool("switchViewApproach", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool hasCallPermission = new SettingBool("hasCallPermission", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool vibrateFeedback = new SettingBool("vibrateFeedback", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool hasPQ_PlugIn = new SettingBool("hasPQ_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool hasFTF_PlugIn = new SettingBool("hasFTF_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool dynamicZoom = new SettingBool("dynamicZoom", SettingCategory.CarMode, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool dynamicFilterAtSearch = new SettingBool("dynamicFilterAtSearch", SettingCategory.Misc, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool DeleteLogs = new SettingBool("DeleteLogs", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool CompactDB = new SettingBool("CompactDB", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool AskAgain = new SettingBool("AskAgain", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform, SettingUsage.ALL);
    public static final SettingBool RememberAsk_Get_API_Key = new SettingBool("RememberAsk_Get_API_Key", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool Ask_Switch_GPS_ON = new SettingBool("Ask_Switch_GPS_ON", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform, SettingUsage.ALL);
    public static final SettingBool TB_DirectLog = new SettingBool("TB_DirectLog", SettingCategory.Internal, NEVER, true, SettingStoreType.Platform, SettingUsage.ALL);
    public static final SettingBool MapHideMyFinds = new SettingBool("MapHideMyFinds", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
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
    public static final SettingIntArray ZoomCross = new SettingIntArray("ZoomCross", SettingCategory.Map, NORMAL, 16, SettingStoreType.Global, SettingUsage.ACB, CrossLevel);
    public static final SettingIntArray SoundApproachDistance = new SettingIntArray("SoundApproachDistance", SettingCategory.Misc, NORMAL, 50, SettingStoreType.Global, SettingUsage.ACB, approach);
    public static final SettingIntArray TrackDistance = new SettingIntArray("TrackDistance", SettingCategory.Misc, NORMAL, 3, SettingStoreType.Global, SettingUsage.ACB, TrackDistanceArray);

    // double

    // longString
    //	public static final SettingLongString Filter = new SettingLongString("Filter", SettingCategory.Misc, NEVER, FilterProperties.presets[0].toString(), SettingStoreType.Local, SettingUsage.ALL);
    public static final SettingLongString FilterNew = new SettingLongString("FilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL);
    public static final SettingLongString UserFilter = new SettingLongString("UserFilter", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingLongString UserFilterNew = new SettingLongString("UserFilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingTime ScreenLock = (SettingTime) SettingsList.addSetting(new SettingTime("ScreenLock", SettingCategory.Misc, NEVER, 60000, SettingStoreType.Global, SettingUsage.ACB));

    // AudioSettings

    public static final SettingsAudio Approach = new SettingsAudio("Approach", SettingCategory.Sounds, EXPERT, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingsAudio GPS_lose = new SettingsAudio("GPS_lose", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_lose.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingsAudio GPS_fix = new SettingsAudio("GPS_fix", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_Fix.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingsAudio AutoResortSound = new SettingsAudio("AutoResortSound", SettingCategory.Sounds, EXPERT, new Audio("data/sound/AutoResort.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ShowFieldnotesCMwithFirstShow = new SettingBool("ShowFieldnotesCMwithFirstShow", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ShowFieldnotesAsDefaultView = new SettingBool("ShowFieldnotesAsDefaultView", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool LiveMapEnabeld = new SettingBool("LiveMapEnabeld", SettingCategory.LiveMap, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingBool AppRaterDontShowAgain = new SettingBool("AppRaterDontShowAgain", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Platform, SettingUsage.ACB);
    public static final SettingInt AppRaterlaunchCount = (SettingInt) SettingsList.addSetting(new SettingInt("AppRaterlaunchCount", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString AppRaterFirstLunch = new SettingString("AppRaterFirstLunch", SettingCategory.Internal, NEVER, "0", SettingStoreType.Platform, SettingUsage.ACB);

    public static final SettingBool nightMode = new SettingBool("nightMode", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingFolder SkinFolder = new SettingFolder("SkinFolder", SettingCategory.Folder, DEVELOPER, "default", SettingStoreType.Global, SettingUsage.ACB, false);

    public static final SettingInt FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", SettingCategory.Skin, EXPERT, 25, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", SettingCategory.Skin, EXPERT, 16, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", SettingCategory.Skin, EXPERT, 14, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", SettingCategory.Skin, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", SettingCategory.Skin, EXPERT, 12, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", SettingCategory.Skin, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingBool useMipMap = new SettingBool("useMipMap", SettingCategory.Skin, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool dontUseAmbient = new SettingBool("dontUseAmbient", SettingCategory.Skin, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingInt ambientTime = new SettingInt("ambientTime", SettingCategory.Skin, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingDouble MapViewFontFaktor = new SettingDouble("MapViewFontFaktor", SettingCategory.Map, NEVER, 1.0, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingInt LongClicktime = new SettingInt("LongClicktime", SettingCategory.Misc, EXPERT, 600, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingsAudio GlobalVolume = new SettingsAudio("GlobalVolume", SettingCategory.Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingFloat MapViewDPIFaktor = new SettingFloat("MapViewDPIFaktor", SettingCategory.Map, EXPERT, CB.getScalefactor(), SettingStoreType.Global, SettingUsage.ACB);

    // überprüfen
    public static final SettingFolder ImageCacheFolder = new SettingFolder("ImageCacheFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/cache", SettingStoreType.Local, SettingUsage.ACB, true);

    public static final SettingBool GestureOn = new SettingBool("GestureOn", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingColor LiveMapBackgroundColor = new SettingColor("LiveMapBackgroundColor", SettingCategory.LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingColor SolvedMysteryColor = new SettingColor("SolvedMysteryColor", SettingCategory.Skin, EXPERT, new HSV_Color(0.2f, 1f, 0.2f, 1f), SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool SettingsShowExpert = new SettingBool("SettingsShowExpert", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool SettingsShowAll = new SettingBool("SettingsShowAll", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingFile Sel_LanguagePath = (SettingFile) SettingsList.addSetting(new SettingFile("Sel_LanguagePath", SettingCategory.Folder, NEVER, "data/lang/en-GB/strings.ini", SettingStoreType.Platform, SettingUsage.ALL, "lan"));
    public static final SettingFolder LanguagePath = new SettingFolder("LanguagePath", SettingCategory.Folder, NEVER, "data/lang", SettingStoreType.Global, SettingUsage.ALL, true);


    public static final SettingFolder TileCacheFolder = new SettingFolder("TileCacheFolder", SettingCategory.Folder, NEVER, CB.WorkPath + "/repository/cache", SettingStoreType.Global, SettingUsage.ALL, true);
    public static final SettingFolder TileCacheFolderLocal = new SettingFolder("TileCacheFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
    public static final SettingFolder MapPackFolder = new SettingFolder("MapPackFolder", SettingCategory.Map, EXPERT, CB.WorkPath + "/repository/maps", SettingStoreType.Global, SettingUsage.ALL, false);
    public static final SettingFolder MapPackFolderLocal = new SettingFolder("MapPackFolderLocal", SettingCategory.Map, NEVER, CB.WorkPath + "/repository/maps", SettingStoreType.Local, SettingUsage.ALL, false);

    public static final SettingStringList CurrentMapLayer = (SettingStringList) SettingsList.addSetting(new SettingStringList("CurrentMapLayer", SettingCategory.Map, DEVELOPER, new String[]{"Mapnik"}, SettingStoreType.Global, SettingUsage.ACB));
    public static final SettingString CurrentMapOverlayLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapOverlayLayer", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingDouble MapInitLatitude = new SettingDouble("MapInitLatitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

    public static final SettingDouble MapInitLongitude = new SettingDouble("MapInitLongitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

    public static final SettingInt lastZoomLevel = (SettingInt) SettingsList.addSetting(new SettingInt("lastZoomLevel", SettingCategory.Map, DEVELOPER, 14, SettingStoreType.Global, SettingUsage.ALL));

    public static final SettingBool MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", SettingCategory.CarMode, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingInt MoveMapCenterMaxSpeed = (SettingInt) SettingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed", SettingCategory.CarMode, NORMAL, 60, SettingStoreType.Global, SettingUsage.ACB));

    public static final SettingBool ShowAccuracyCircle = new SettingBool("ShowAccuracyCircle", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    public static final SettingBool ShowMapCenterCross = new SettingBool("ShowMapCenterCross", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingBool PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", SettingCategory.Map, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);

    public static final SettingIntArray OsmMinLevel = new SettingIntArray("OsmMinLevel", SettingCategory.Map, EXPERT, 7, SettingStoreType.Global, SettingUsage.ACB, Level);
    public static final SettingIntArray OsmMaxLevel = new SettingIntArray("OsmMaxLevel", SettingCategory.Map, EXPERT, 19, SettingStoreType.Global, SettingUsage.ACB, Level);
    public static final SettingIntArray CompassMapMinZoomLevel = new SettingIntArray("CompassMapMinZoomLevel", SettingCategory.Map, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB, Level);
    public static final SettingIntArray CompassMapMaxZommLevel = new SettingIntArray("CompassMapMaxZommLevel", SettingCategory.Map, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB, Level);

    public static final SettingFolder RenderThemesFolder = new SettingFolder("RenderThemesFolder", SettingCategory.Map, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL, false);
    public static final SettingFile MapsforgeDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    public static final SettingFile MapsforgeNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    public static final SettingFile MapsforgeCarDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    public static final SettingFile MapsforgeCarNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));


    public static final SettingBool DEBUG_MapGrid = new SettingBool("DEBUG_MapGrid", SettingCategory.Debug, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);


    //    public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", SettingCategory.LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", SettingCategory.LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14,
//            SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
//    public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);

}
