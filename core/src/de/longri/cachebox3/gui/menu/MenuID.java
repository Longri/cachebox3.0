/* 
 * Copyright (C) 2011-2015 team-cachebox.de
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
package de.longri.cachebox3.gui.menu;

/**
 * Diese Klasse enth�lt alle Menu Item ID�s.</br></br> Da ein Men� auch aus mehreren Men�s zusammengesetzt werden kann, m�ssen die Items
 * eine Eindeutige ID haben.</br>
 *
 * @author Longri
 */
public class MenuID {
    // WaypointView
    public static final int MI_EDIT = 0;
    public static final int MI_ADD = 1;
    public static final int MI_DELETE = 2;
    public static final int MI_PROJECTION = 3;
    public static final int MI_FROM_GPS = 4;

    // SolverView2
    public static final int MI_CHANGE_LINE = 5;
    public static final int MI_DELETE_LINE = 6;
    public static final int MI_INSERT_LINE = 7;
    public static final int MI_SET_AS_WAYPOINT = 8;
    public static final int MI_SET_AS_MAPCENTER = 9;

    // FieldNotesView
    public static final int MI_FOUND = 10;
    public static final int MI_NOT_FOUND = 11;
    public static final int MI_MAINTANCE = 12;
    public static final int MI_NOTE = 13;
    // public static final int MI_MANAGE = 14;
    public static final int MI_UPLOAD_FIELDNOTE = 15;
    public static final int MI_DELETE_ALL_FIELDNOTES = 16;
    public static final int MI_DELETE_FIELDNOTE = 17;
    public static final int MI_EDIT_FIELDNOTE = 18;
    public static final int MI_SELECT_CACHE = 19;

    // CB_AllContextMenuHandler
    public static final int MI_ABOUT = 20;
    public static final int MI_DAY_NIGHT = 21;
    public static final int MI_SETTINGS = 22;
    public static final int MI_SCREENLOCK = 23;
    public static final int MI_QUIT = 24;
    public static final int MI_DESCRIPTION = 25;
    public static final int MI_WAYPOINTS = 26;
    public static final int MI_SHOW_LOGS = 27;
    public static final int MI_HINT = 28;
    public static final int MI_SPOILER = 29;
    public static final int MI_FIELDNOTES = 30;
    public static final int MI_NOTES = 31;
    public static final int MI_SOLVER = 32;
    public static final int MI_JOKER = 33;
    public static final int MI_Layer = 34;
    public static final int MI_ALIGN_TO_COMPSS = 35;

    public static final int MI_SEARCH = 37;
    public static final int MI_TREC_REC = 38;
    public static final int MI_HIDE_FINDS = 39;
    public static final int MI_SHOW_RATINGS = 40;
    public static final int MI_SHOW_DT = 41;
    public static final int MI_SHOW_TITLE = 42;
    public static final int MI_SHOW_DIRECT_LINE = 43;
    public static final int MI_MAPVIEW_VIEW = 44;
    public static final int MI_RELOAD_CACHE_INFO = 45;

    // CB_Action_ShowTrackListView
    public static final int MI_GENERATE = 46;
    public static final int MI_RENAME = 47;
    public static final int MI_LOAD = 48;
    public static final int MI_SAVE = 49;
    public static final int MI_DELETE_TRACK = 50;
    public static final int MI_P2P = 51;
    public static final int MI_PROJECT = 52;
    public static final int MI_CIRCLE = 53;
    public static final int MI_OPENROUTE = 54;

    // CB_Action_ShowDescriptionView
    public static final int MI_FAVORIT = 55;
    public static final int MI_RELOAD_CACHE = 56;

    // CB_Action_ShowCacheList
    public static final int MI_MANAGE_DB = 57;
    public static final int MI_AUTO_RESORT = 58;
    public static final int MI_RESORT = 59;
    public static final int MI_FilterSet = 60;
    public static final int MI_SEARCH_LIST = 61;
    public static final int MI_IMPORT = 62;
    public static final int MI_CHK_STATE_API = 63;

    // CB_Action_RecTrack
    public static final int MI_START = 64;
    public static final int MI_PAUSE = 65;
    public static final int MI_STOP = 66;

    // CB_Action_QuickFieldNote
    public static final int MI_QUICK_FOUND = 67;
    public static final int MI_QUICK_NOT_FOUND = 68;

    // SettingsActivity
    public static final int MI_SHOW_EXPERT = 69;
    public static final int MI_SHOW_ALL = 70;
    public static final int MI_SHOW_Normal = 82;

    //
    public static final int MI_START_WITHOUT_SELECTION = 71;
    public static final int MI_AUTO_START_DISABLED = 72;
    public static final int MI_5 = 73;
    public static final int MI_10 = 74;
    public static final int MI_25 = 75;
    public static final int MI_60 = 76;

    //
    public static final int MI_LAYER = 77;
    public static final int MI_SETTINGS_MAP = 78;
    public static final int MI_ROTATE = 79;
    public static final int MI_CENTER_WP = 80;

    public static final int MI_SHOW_ALL_WAYPOINTS = 81;

    // AbstractAction ID�s

    public static final int AID_TEST_VIEW = -1;
    public static final int AID_TEST2 = -2;

    public static final int AID_SHOW_MAP = 100;
    public static final int AID_SHOW_HINT = 101;
    public static final int AID_SHOW_CACHELIST = 102;
    public static final int AID_SHOW_CACHELIST_CONTEXT_MENU = 103;
    public static final int AID_SHOW_COMPASS = 103;
    public static final int AID_SHOW_CREDITS = 104;
    public static final int AID_SHOW_DESCRIPTION = 105;
    public static final int AID_SHOW_FIELDNOTES = 106;
    public static final int AID_SHOW_JOKERS = 107;
    public static final int AID_SHOW_LOGS = 108;
    public static final int AID_SHOW_NOTES = 109;
    public static final int AID_SHOW_SOLVER = 110;
    public static final int AID_SHOW_SPOILER = 111;
    public static final int AID_SHOW_TRACKABLELIST = 112;
    public static final int AID_SHOW_TRACKLIST = 113;
    public static final int AID_SHOW_WAYPOINTS = 114;
    public static final int AID_SHOW_SETTINGS = 115;
    public static final int AID_TRACKLIST_CREATE = 116;
    public static final int AID_TRACKLIST_LOAD = 117;
    public static final int AID_TRACKLIST_DELETE = 118;
    public static final int AID_SHOW_FILTER_SETTINGS = 119;
    public static final int AID_NAVIGATE_TO = 120;
    public static final int AID_TRACK_REC = 121;
    public static final int AID_VOICE_REC = 122;
    public static final int AID_TAKE_PHOTO = 123;
    public static final int AID_VIDEO_REC = 124;
    public static final int AID_DELETE_CACHES = 125;
    public static final int AID_PARKING = 126;
    public static final int AID_DAY_NIGHT = 127;
    public static final int AID_LOCK = 128;
    public static final int AID_QUIT = 129;
    public static final int AID_SHOW_ABOUT = 130;
    public static final int AID_SHOW_SOLVER2 = 131;
    public static final int AID_SEARCH = 132;
    public static final int AID_AUTO_RESORT = 133;
    public static final int AID_SHOW_SELECT_DB_DIALOG = 134;
    public static final int AID_SHOW_TRACK_MENU = 135;
    public static final int AID_SHOW_QUIT = 136;
    public static final int AID_QUICK_FIELDNOTE = 137;
    public static final int AID_CHK_STATE = 138;
    public static final int AID_GENERATE_ROUTE = 139;
    public static final int AID_SHOW_PARKING_DIALOG = 140;
    public static final int AID_SHOW_DELETE_DIALOG = 141;

    // Compass
    public static final int MI_COMPASS_SHOW_MAP = 142;
    public static final int MI_COMPASS_SHOW_NAME = 143;
    public static final int MI_COMPASS_SHOW_ICON = 144;
    public static final int MI_COMPASS_SHOW_ATTRIBUTES = 145;
    public static final int MI_COMPASS_SHOW_GC_CODE = 146;
    public static final int MI_COMPASS_SHOW_COORDS = 147;
    public static final int MI_COMPASS_SHOW_WP_DESC = 148;
    public static final int MI_COMPASS_SHOW_SAT_INFO = 149;
    public static final int MI_COMPASS_SHOW_SUN_MOON = 150;
    public static final int MI_COMPASS_SHOW_TARGET_DIRECTION = 151;
    public static final int MI_COMPASS_SHOW_S_D_T = 152;
    public static final int MI_COMPASS_SHOW_LAST_FOUND = 153;

    public static final int MI_COMPASS_SHOW = 155;
    public static final int MI_RELOAD_SPOILER = 156;
    public static final int MI_SHOW_ACCURACY_CIRCLE = 157;
    public static final int MI_MAP_SHOW_COMPASS = 158;

    public static final int AID_SHOW_FILTER_DIALOG = 159;

    public static final int MI_EDIT_CACHE = 160;
    public static final int MI_NEW_CACHE = 161;
    public static final int MI_MAPVIEW_OVERLAY_VIEW = 162;
    public static final int MI_DELETE_CACHE = 163;
    public static final int MI_RESET_FILTER = 164;

    public static final int MI_REFRESH_TB_LIST = 165;
    public static final int MI_TB_DISCOVERED = 166;
    public static final int MI_TB_DROPPED = 167;
    public static final int MI_TB_PICKED = 168;
    public static final int MI_TB_VISIT = 169;
    public static final int MI_TB_GRABBED = 170;
    public static final int MI_TB_NOTE = 171;
    public static final int MI_SELECT_PATH = 172;
    public static final int MI_CLEAR_PATH = 173;
    public static final int MI_SHOW_CENTERCROSS = 174;
    public static final int MI_START_PICTUREAPP = 175;

    public static final int AID_ADD_WP = 176;
    public static final int AID_UPLOAD_FIELD_NOTE = 177;

    public static final int MI_ENABLED = 178;
    public static final int MI_TEMPORARILY_DISABLED = 179;
    public static final int MI_OWNER_MAINTENANCE = 180;
    public static final int MI_ATTENDED = 181;
    public static final int MI_WEBCAM_FOTO_TAKEN = 182;
    public static final int MI_REVIEWER_NOTE = 183;
    public static final int MI_WILL_ATTENDED = 184;
    public static final int MI_WP_SHOW = 185;
    public static final int MI_SYNC = 186;

    // Rpc
    public static final int MI_RpcGetExportList = 187;

    public static final int AID_SHOW_IMPORT_MENU = 188;
    public static final int MI_IMPORT_GS = 189;
    public static final int MI_IMPORT_CBS = 190;
    public static final int MI_IMPORT_GPX = 191;
    public static final int MI_IMPORT_GCV = 192;
    public static final int MI_IMPORT_GS_PQ = 193;
    public static final int MI_IMPORT_GS_API_POSITION = 194;
    public static final int MI_IMPORT_GS_API_SEARCH = 195;

    // Export
    public static final int MI_EXPORT_RUN = 196;

    public static final int MI_MAP_DOWNOAD = 199;
    public static final int AID_LOADLOGS = 200;
    public static final int MI_LOAD_FRIENDS_LOGS = 201;
    public static final int MI_FILTERLOGS = 202;
    public static final int MI_RELOADLOGS = 203;

    public static final int AID_TORCH = 204;
    public static final int MI_ADD_MISSING_VARIABLES = 205;

    public static final int AID_SWITCH_DESCRIPTION = 206;
    public static final int AID_SHOW_DescExt = 207;

    public static final int AID_HELP = 209;
    public static final int MI_RENDERTHEMES = 210;
}
