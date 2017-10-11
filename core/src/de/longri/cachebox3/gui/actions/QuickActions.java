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
import de.longri.cachebox3.gui.actions.show_activities.Action_ShowFilterSettings;
import de.longri.cachebox3.gui.actions.show_vies.*;
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

    empty,;


    final static Logger log = LoggerFactory.getLogger(QuickActions.class);

    /**
     * Gibt eine ArrayList von Actions zurück aus einem übergebenen String Array
     *
     * @param configList
     * @param button
     * @return ArrayList <Actions>
     */
    public static MoveableList<QuickButtonItem> getListFromConfig(String[] configList, float height, Drawable button) {
        MoveableList<QuickButtonItem> retList = new MoveableList<QuickButtonItem>();
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
                    if (QuickActions.getActionEnumById(EnumId) != null) {
                        QuickButtonItem tmp = new QuickButtonItem(index++, button, QuickActions.getActionEnumById(EnumId), QuickActions.getName(EnumId), type);
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

            String ActionsString = "";
            int counter = 0;
            for (int i = 0, n = retList.size; i < n; i++) {
                QuickButtonItem tmp = retList.get(i);
                ActionsString += String.valueOf(tmp.getAction().ordinal());
                if (counter < retList.size - 1) {
                    ActionsString += ",";
                }
                counter++;
            }
            Config.quickButtonList.setValue(ActionsString);
            Config.AcceptChanges();
        }
        return retList;
    }

    /**
     * Gibt die ID des übergebenen Enums zurück
     *
     * @param attrib
     * @return long
     */
    public static int GetIndex(QuickActions attrib) {
        return attrib.ordinal();
    }

    public static AbstractAction getActionEnumById(int id) {
        switch (id) {
            case 0:
                return CB.viewmanager.getAction_Show_DescriptionView();
            case 1:
                return CB.viewmanager.getAction_Show_WaypointView();
            case 2:
                return CB.viewmanager.getAction_Show_LogView();
            case 3:
                return CB.viewmanager.getAction_Show_MapView();
            case 4:
                return CB.viewmanager.getAction_Show_CompassView();
            case 5:
                return CB.viewmanager.getAction_Show_CacheList();
            case 6:
                return CB.viewmanager.getAction_Show_TrackListView();
            case 7:
                return new Action_TakePhoto();
            case 8:
                return new Action_RecVideo();
            case 9:
                return new Action_RecVoice();
            case 10:
                return new Action_Show_Search();
            case 11:
                return new Action_ShowFilterSettings();
            case 12:
                return new Action_Switch_Autoresort();
            case 13:
                return CB.viewmanager.getAction_Show_SolverView();
            case 14:
                return CB.viewmanager.getAction_Show_SpoilerView();
            case 15:
                return new Action_Show_Hint();
            case 16:
                return new Action_Show_ParkingDialog();
            case 17:
                return new Action_Toggle_Day_Night();
            case 18:
                return CB.viewmanager.getAction_Show_DraftsView();
            case 19:
                return new Action_QuickDraft();
            case 20:
                return CB.viewmanager.getAction_Show_TrackableListView();
            case 21:
                return new Action_Add_WP();
            case 22:
                return CB.viewmanager.getAction_Show_SolverView2();
            case 23:
                return CB.viewmanager.getAction_Show_NoteView();
            case 24:
                return new Action_Upload_Fieldnotes();
            case 25:
                return new Action_Switch_Torch();

        }
        return null;
    }

    public static String getName(int id) {
        switch (id) {
            case 0:
                return Translation.Get("Description");
            case 1:
                return Translation.Get("Waypoints");
            case 2:
                return Translation.Get("ShowLogs");
            case 3:
                return Translation.Get("Map");
            case 4:
                return Translation.Get("Compass");
            case 5:
                return Translation.Get("cacheList");
            case 6:
                return Translation.Get("Tracks");
            case 7:
                return Translation.Get("TakePhoto");
            case 8:
                return Translation.Get("RecVideo");
            case 9:
                return Translation.Get("VoiceRec");
            case 10:
                return Translation.Get("Search");
            case 11:
                return Translation.Get("filter");
            case 12:
                return Translation.Get("AutoResort");
            case 13:
                return Translation.Get("Solver");
            case 14:
                return Translation.Get("spoiler");
            case 15:
                return Translation.Get("hint");
            case 16:
                return Translation.Get("MyParking");
            case 17:
                return Translation.Get("DayNight");
            case 18:
                return Translation.Get("Fieldnotes");
            case 19:
                return Translation.Get("QuickDraft");
            case 20:
                return Translation.Get("TBList");
            case 21:
                return Translation.Get("AddWaypoint");
            case 22:
                return Translation.Get("Solver") + " 2";
            case 23:
                return Translation.Get("Notes");
            case 24:
                return Translation.Get("uploadDrafts");
            case 25:
                return Translation.Get("torch");

        }
        return "empty";
    }


}
