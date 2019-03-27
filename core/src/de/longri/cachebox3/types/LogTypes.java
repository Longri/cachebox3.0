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
package de.longri.cachebox3.types;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;
import de.longri.cachebox3.gui.skin.styles.LogTypesStyle;
import de.longri.cachebox3.settings.Config;

public enum LogTypes implements SelectBoxItem {
    found, // 0
    didnt_find, // 1
    note, // 2
    published, // 3
    enabled, // 4
    needs_maintenance, // 5
    temporarily_disabled, // 6
    owner_maintenance, // 7
    will_attend, // 8
    attended, // 9
    webcam_photo_taken, // 10
    archived, // 11
    reviewer_note, // 12
    needs_archived, // 13
    unarchive, // 14
    retract, // 15
    update_coord, // 16
    retrieve, // 17
    dropped_off, // 18
    mark_missing, // 19
    grab_it, // 20
    discovered, // 21
    move_to_collection, // 22
    move_to_inventory, // 23
    announcement, // 24
    visited, // 25
    ownFavorite,
    unknown;

    /**
     * Returns True if the log type a TB Log
     *
     * @return
     */
    public boolean isTbLog() {
        int t = this.ordinal();
        if (t == 17)
            return true; // retrieve
        if (t == 18)
            return true; // dropped_off
        if (t == 19)
            return true; // mark_missing
        if (t == 20)
            return true; // grab_it
        if (t == 21)
            return true; // discovered
        if (t == 22)
            return true; // move_to_collection
        if (t == 23)
            return true; // move_to_inventory
        if (t == 25)
            return true; // visited

        return false;
    }

    /**
     * Returns True if the log type possible to direct online Log
     *
     * @return
     */
    public boolean isDirectLogType() {
        if (Config.DirectOnlineLog.getValue())
            return true;
        int t = this.ordinal();
        if (t == 4)
            return true; // enabled
        if (t == 5)
            return true; // needs_maintenance
        if (t == 6)
            return true; // temporarily_disabled
        if (t == 7)
            return true; // owner_maintenance
        if (t == 8)
            return true; // will_attend

        if (t == 17)
            return true; // retrieve
        if (t == 18)
            return true; // dropped_off
        if (t == 19)
            return true; // mark_missing
        if (t == 20)
            return true; // grab_it
        if (t == 21)
            return true; // discovered
        if (t == 22)
            return true; // move_to_collection
        if (t == 23)
            return true; // move_to_inventory
        if (t == 25)
            return true; // visited

        return false;
    }

    public static LogTypes parseString(String text) {
        if (text.equalsIgnoreCase("found it")) {
            return found;
        }
        if (text.equalsIgnoreCase("didn't find it")) {
            return didnt_find;
        }
        if (text.equalsIgnoreCase("not found")) {
            return didnt_find;
        }
        if (text.equalsIgnoreCase("write note")) {
            return note;
        }
        if (text.equalsIgnoreCase("publish listing")) {
            return published;
        }
        if (text.equalsIgnoreCase("enable listing")) {
            return enabled;
        }
        if (text.equalsIgnoreCase("needs maintenance")) {
            return needs_maintenance;
        }
        if (text.equalsIgnoreCase("temporarily disable listing")) {
            return temporarily_disabled;
        }
        if (text.equalsIgnoreCase("owner maintenance")) {
            return owner_maintenance;
        }
        if (text.equalsIgnoreCase("update coordinates")) {
            return owner_maintenance;
        }
        if (text.equalsIgnoreCase("will attend")) {
            return will_attend;
        }
        if (text.equalsIgnoreCase("attended")) {
            return attended;
        }
        if (text.equalsIgnoreCase("webcam photo taken")) {
            return webcam_photo_taken;
        }
        if (text.equalsIgnoreCase("archive")) {
            return archived;
        }
        if (text.equalsIgnoreCase("unarchive")) {
            return archived;
        }
        if (text.equalsIgnoreCase("post reviewer note")) {
            return reviewer_note;
        }
        if (text.equalsIgnoreCase("needs archived")) {
            return needs_archived;
        }
        if (text.equalsIgnoreCase("other")) {
            return note;
        }
        if (text.equalsIgnoreCase("note")) {
            return note;
        }
        if (text.equalsIgnoreCase("geocoins")) {
            return note;
        }
        if (text.equalsIgnoreCase("cache disabled!")) {
            return temporarily_disabled;
        }
        if (text.equalsIgnoreCase("retract listing")) {
            return archived;
        }
        return note;
    }

    public int getIconID() {
        switch (this.ordinal()) {
            case 0:
                return 0; // Found
            case 1:
                return 1; // DNF
            case 2:
                return 2; // Note
            case 3:
                return 3; // Publish
            case 4:
                return 4; // Enable
            case 5:
                return 5; // needs maintains
            case 6:
                return 6; // Disable
            case 7:
                return 7; // Owner Maintains
            case 8:
                return 8; // Will attend
            case 9:
                return 9; // Attended
            case 10:
                return 10; // Photo
            case 11:
                return 11; // Archive
            case 12:
                return 12; // Reviewer Note
            case 13:
                return 13; // needs maintains
            case 14:
                return 11; // Unarchive
            case 15:
                return 14; // Retract
            case 16:
                return 16; // Update Coords
            case 17:
                return 17; // Retrive
            case 18:
                return 18; // Dropped
            case 19:
                return 2; // Mark missing
            case 20:
                return 19; // Grab It
            case 21:
                return 20; // Discover
            case 22:
                return 2; // Move to Collection
            case 23:
                return 2; // Move to Inventory
            case 24:
                return 15; // Announcement
            case 25:
                return 21; // Visited

        }

        return -1; // Note
    }

    public static LogTypes GC2CB_LogType(int value) {
        switch (value) {
            case 1:
                return unarchive;
            case 2:
                return found;
            case 3:
                return didnt_find;
            case 4:
                return note;
            case 5:
                return archived;
            case 7:
                return needs_archived;
            case 9:
                return will_attend;
            case 10:
                return attended;
            case 11:
                return webcam_photo_taken;
            case 12:
                return unarchive;
            case 13:
                return retrieve;
            case 14:
                return dropped_off;
            case 16:
                return mark_missing;
            case 18:
                return reviewer_note;
            case 19:
                return grab_it;
            case 22:
                return temporarily_disabled;
            case 23:
                return enabled;
            case 24:
                return published;
            case 25:
                return retract;
            case 45:
                return needs_maintenance;
            case 46:
                return owner_maintenance;
            case 47:
                return update_coord;
            case 48:
                return discovered;
            case 68:
                return reviewer_note;
            case 69:
                return move_to_collection;
            case 70:
                return move_to_inventory;
            case 75:
                return visited;
        }

        return note;
    }

    public int getGcLogTypeId() {
        return CB_LogType2GC(this);
    }

    /**
     * GS LogTypeId's:</br>4 - Post Note </br>13 - Retrieve It from a Cache </br>14 - Place in a cache </br>16 - Mark as missing </br>19 -
     * Grab </br>48 - Discover </br>69 - Move to collection </br>70 - Move to inventory </br>75 - Visit
     *
     * @param value
     * @return
     */
    public static int CB_LogType2GC(LogTypes value) {
        switch (value) {
            case unarchive:
                return 1;
            case found:
                return 2;
            case didnt_find:
                return 3;
            case note:
                return 4;
            case archived:
                return 5;
            case needs_archived:
                return 7;
            case will_attend:
                return 9;
            case attended:
                return 10;
            case webcam_photo_taken:
                return 11;
            // GC hat unarchive doppelt, wir nutzen nur [1]
            // case unarchive:
            // return 12;
            case retrieve:
                return 13;
            case dropped_off:
                return 14;
            case mark_missing:
                return 16;
            case reviewer_note:
                return 18;
            case grab_it:
                return 19;
            case temporarily_disabled:
                return 22;
            case enabled:
                return 23;
            case published:
                return 24;
            case retract:
                return 25;
            case needs_maintenance:
                return 45;
            case owner_maintenance:
                return 46;
            case update_coord:
                return 47;
            case discovered:
                return 48;
            // GC hat reviewer_note doppelt, wir nutzen nur [18]
            // case reviewer_note:
            // return 68;
            case move_to_collection:
                return 69;
            case move_to_inventory:
                return 70;
            case visited:
                return 75;

            default:
                break;

        }

        return 4;
    }

    @Override
    public String toString() {

        switch (this) {
            case unarchive:
                return "unarchive";
            case found:
                return "Found it";
            case didnt_find:
                return "Didn't find it";
            case note:
                return "note";
            case archived:
                return "archived";
            case needs_archived:
                return "needs_archived";
            case will_attend:
                return "will_attend";
            case attended:
                return "attended";
            case webcam_photo_taken:
                return "webcam_photo_taken";
            // GC hat unarchive doppelt, wir nutzen nur [1]
            // case unarchive:
            // return 12;
            case retrieve:
                return "retrieve";
            case dropped_off:
                return "dropped_off";
            case mark_missing:
                return "mark_missing";
            case reviewer_note:
                return "reviewer_note";
            case grab_it:
                return "grab_it";
            case temporarily_disabled:
                return "temporarily_disabled";
            case enabled:
                return "enabled";
            case published:
                return "published";
            case retract:
                return "retract";
            case needs_maintenance:
                return "needs_maintenance";
            case owner_maintenance:
                return "owner_maintenance";
            case update_coord:
                return "update_coord";
            case discovered:
                return "discovered";
            // GC hat reviewer_note doppelt, wir nutzen nur [18]
            // case reviewer_note:
            // return 68;
            case move_to_collection:
                return "move_to_collection";
            case move_to_inventory:
                return "move_to_inventory";
            case visited:
                return "visited";

            default:
                return "";

        }
    }

    public String getName() {
        return this.name();
    }


    static LogTypesStyle logListTypeStyle;

    @Override
    public Drawable getDrawable() {
        // for select Box interface, use 'cacheList' style
        if (logListTypeStyle == null) logListTypeStyle = VisUI.getSkin().get("logViewLogStyles", LogTypesStyle.class);
        return getDrawable(logListTypeStyle);
    }

    public Drawable getDrawable(LogTypesStyle style) {
        if (style == null) return null;
        switch (this) {
            case found:
                return style.found;
            case didnt_find:
                return style.didnt_find;
            case note:
                return style.note;
            case published:
                return style.published;
            case enabled:
                return style.enabled;
            case needs_maintenance:
                return style.needs_maintenance;
            case temporarily_disabled:
                return style.temporarily_disabled;
            case owner_maintenance:
                return style.owner_maintenance;
            case will_attend:
                return style.will_attend;
            case attended:
                return style.attended;
            case webcam_photo_taken:
                return style.webcam_photo_taken;
            case archived:
                return style.archived;
            case reviewer_note:
                return style.reviewer_note;
            case needs_archived:
                return style.needs_archived;
            case unarchive:
                return style.unarchive;
            case retract:
                return style.retract;
            case update_coord:
                return style.update_coord;
            case retrieve:
                return style.retrieve;
            case dropped_off:
                return style.dropped_off;
            case mark_missing:
                return style.mark_missing;
            case grab_it:
                return style.grab_it;
            case discovered:
                return style.discovered;
            case move_to_collection:
                return style.move_to_collection;
            case move_to_inventory:
                return style.move_to_inventory;
            case announcement:
                return style.announcement;
            case visited:
                return style.visited;
            case ownFavorite:
                return style.ownFavorite;
        }
        return null;
    }
}
