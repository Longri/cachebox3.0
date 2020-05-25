/*
 * Copyright (C) 2011-2020 team-cachebox.de
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


import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.menuBtn1.Action_ParkingDialog;
import de.longri.cachebox3.gui.menu.menuBtn1.contextmenus.Action_Switch_Autoresort;
import de.longri.cachebox3.gui.menu.menuBtn1.contextmenus.RememberGeoCache;
import de.longri.cachebox3.gui.menu.menuBtn2.Action_HintDialog;
import de.longri.cachebox3.gui.menu.menuBtn3.ShowTracks;
import de.longri.cachebox3.gui.menu.menuBtn4.Action_RecVideo;
import de.longri.cachebox3.gui.menu.menuBtn4.Action_TakePhoto;
import de.longri.cachebox3.gui.menu.menuBtn4.Action_Upload_Drafts;
import de.longri.cachebox3.gui.menu.menuBtn4.todo.Action_RecVoice;
import de.longri.cachebox3.gui.menu.menuBtn5.Action_Switch_Torch;
import de.longri.cachebox3.gui.menu.menuBtn5.todo.Action_Toggle_Day_Night;
import de.longri.cachebox3.gui.menu.quickBtns.Action_Add_WP;
import de.longri.cachebox3.gui.menu.quickBtns.Action_EditFilterSettings;
import de.longri.cachebox3.gui.menu.quickBtns.Action_QuickDraft;
import de.longri.cachebox3.gui.menu.quickBtns.Action_SearchDialog;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.translation.Translation;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 *
 * @author Longri
 */
public enum QuickAction {
    DescriptionView, // 0
    WaypointView, // 1
    LogView, // 2
    MapView, // 3
    CompassView, // 4
    CacheListView, // 5
    TrackListView, // 6
    TakePhoto, // 7
    TakeVideo, // 8
    VoiceRecord, // 9
    LiveSearch, // 10
    Filter, // 11
    AutoResort, // 12
    Solver, // 13 !!! todo
    Spoiler, // 14
    Hint, // 15
    Parking, // 16 todo
    Day_Night, // 17
    Drafts, // 18
    QuickDrafts, // 19
    TrackableListView, // 20
    addWP, // 21
    Solver2, // 22 todo
    NotesView, // 23
    uploadDraft, // 24
    torch, // 25
    createRoute, // todo
    rememberGeoCache,
    empty;


    public AbstractAction getAction() {
        switch (this) {
            case DescriptionView:
                return CB.viewmanager.getAction_Show_DescriptionView();
            case WaypointView:
                return CB.viewmanager.getAction_Show_WaypointView();
            case LogView:
                return CB.viewmanager.getAction_Show_LogView();
            case MapView:
                return CB.viewmanager.getAction_Show_MapView();
            case CompassView:
                return CB.viewmanager.getAction_Show_CompassView();
            case CacheListView:
                return CB.viewmanager.getAction_Show_CacheList();
            case TrackListView:
                return ShowTracks.getInstance();
            case TakePhoto:
                return new Action_TakePhoto();
            case TakeVideo:
                return new Action_RecVideo();
            case VoiceRecord:
                return new Action_RecVoice();
            case LiveSearch:
                return new Action_SearchDialog();
            case Filter:
                return new Action_EditFilterSettings();
            case AutoResort:
                return new Action_Switch_Autoresort();
            case Solver:
                return CB.viewmanager.getAction_Show_SolverView();
            case Spoiler:
                return CB.viewmanager.getAction_Show_SpoilerView();
            case Hint:
                return new Action_HintDialog();
            case Parking:
                return new Action_ParkingDialog();
            case Day_Night:
                return new Action_Toggle_Day_Night();
            case Drafts:
                return CB.viewmanager.getAction_Show_DraftsView();
            case QuickDrafts:
                return new Action_QuickDraft();
            case TrackableListView:
                return CB.viewmanager.getAction_Show_TrackableListView();
            case addWP:
                return new Action_Add_WP();
            case Solver2:
                return CB.viewmanager.getAction_Show_SolverView2(); // todo
            case NotesView:
                return CB.viewmanager.getAction_Show_NoteView();
            case uploadDraft:
                return new Action_Upload_Drafts();
            case torch:
                return new Action_Switch_Torch();
            case createRoute:
                return null; // todo
            case rememberGeoCache:
                return RememberGeoCache.getInstance();
        }
        return null; // empty
    }

    public CharSequence getName() {
        switch (this) {
            case DescriptionView:
                return Translation.get("Description");
            case WaypointView:
                return Translation.get("Waypoints");
            case LogView:
                return Translation.get("ShowLogs");
            case MapView:
                return Translation.get("Map");
            case CompassView:
                return Translation.get("Compass");
            case CacheListView:
                return Translation.get("cacheList");
            case TrackListView:
                return Translation.get("Tracks");
            case TakePhoto:
                return Translation.get("TakePhoto");
            case TakeVideo:
                return Translation.get("RecVideo");
            case VoiceRecord:
                return Translation.get("VoiceRec");
            case LiveSearch:
                return Translation.get("Search");
            case Filter:
                return Translation.get("filter");
            case AutoResort:
                return Translation.get("AutoResort");
            case Solver:
                return Translation.get("Solver");
            case Spoiler:
                return Translation.get("spoiler");
            case Hint:
                return Translation.get("hint");
            case Parking:
                return Translation.get("MyParking");
            case Day_Night:
                return Translation.get("DayNight");
            case Drafts:
                return Translation.get("Drafts");
            case QuickDrafts:
                return Translation.get("QuickDraft");
            case TrackableListView:
                return Translation.get("TBList");
            case addWP:
                return Translation.get("AddWaypoint");
            case Solver2:
                return Translation.get("Solver") + " 2";
            case NotesView:
                return Translation.get("Notes");
            case uploadDraft:
                return Translation.get("uploadDrafts");
            case torch:
                return Translation.get("torch");
            case createRoute:
                return Translation.get("generateRoute");
            case rememberGeoCache:
                return Translation.get("rememberGeoCacheTitle");
        }
        return "empty";
    }


}
