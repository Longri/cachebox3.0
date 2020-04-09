/*
 * Copyright (C) 2011-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.actions;


import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.extendsAbstractAction.*;
import de.longri.cachebox3.gui.actions.extendsAbstractAction.todo.Action_RecVoice;
import de.longri.cachebox3.gui.actions.extendsAbstractAction.todo.Action_Switch_Autoresort;
import de.longri.cachebox3.gui.actions.extendsAbstractAction.todo.Action_Toggle_Day_Night;
import de.longri.cachebox3.gui.widgets.QuickButtonItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.MoveableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 *
 * @author Longri
 */
public enum QuickActions {
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
    Solver, // 13
    Spoiler, // 14
    Hint, // 15
    Parking, // 16
    Day_Night, // 17
    Drafts, // 18
    QuickDrafts, // 19
    TrackableListView, // 20
    addWP, // 21
    Solver2, // 22
    Notesview, // 23
    uploadDraft, // 24
    torch, // 25

    // ScreenLock, // 21

    empty,
    ;


    final static Logger log = LoggerFactory.getLogger(QuickActions.class);

    /**
     * Gibt eine ArrayList von Actions zurück aus einem übergebenen String Array
     *
     * @param configList ?
     * @param button     ?
     * @return ArrayList <Actions>
     */
    public static MoveableList<QuickButtonItem> getListFromConfig(String[] configList, Drawable button) {
        MoveableList<QuickButtonItem> retList = new MoveableList<>();
        if (configList == null || configList.length == 0) {
            return retList;
        }

        boolean invalidEnumId = false;
        try {
            int index = 0;

            for (String s : configList) {
                s = s.replace(",", "");
                int EnumId = Integer.parseInt(s);
                if (EnumId > -1) {

                    QuickActions type = QuickActions.values()[EnumId];
                    if (type != null) {
                        QuickButtonItem tmp = new QuickButtonItem(index++, button, type.getAction(), type.getName(), type);
                        retList.add(tmp);
                    } else
                        invalidEnumId = true;
                }
            }
        } catch (Exception e) {// wenn ein Fehler auftritt, gib die bis dorthin gelesenen Items zurück
            log.error("getListFromConfig", e);
        }
        if (invalidEnumId) {
            //	    write valid id's back

            StringBuilder actionsString = new StringBuilder();
            int counter = 0;
            for (int i = 0, n = retList.size; i < n; i++) {
                QuickButtonItem tmp = retList.get(i);
                actionsString.append(tmp.getAction().ordinal());
                if (counter < retList.size - 1) {
                    actionsString.append(",");
                }
                counter++;
            }
            Config.quickButtonList.setValue(actionsString.toString());
            Config.AcceptChanges();
        }
        return retList;
    }

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
                return CB.viewmanager.getAction_Show_TrackListView();
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
                return CB.viewmanager.getAction_Show_SolverView2();
            case Notesview:
                return CB.viewmanager.getAction_Show_NoteView();
            case uploadDraft:
                return new Action_Upload_Drafts();
            case torch:
                return new Action_Switch_Torch();
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
            case Notesview:
                return Translation.get("Notes");
            case uploadDraft:
                return Translation.get("uploadDrafts");
            case torch:
                return Translation.get("torch");
        }
        return "empty";
    }


}
