/*
 * Copyright (C) 2017-2018 team-cachebox.de
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
package de.longri.cachebox3.gui.skin.styles;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 31.05.2017.
 */
public class LogTypesStyle extends AbstractIconStyle {
    public Drawable
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
            visited; // 25
    public Drawable ownFavorite;

    @Override
    public int getPrefWidth() {
        return CB.getScaledInt(26);
    }

    @Override
    public int getPrefHeight() {
        return CB.getScaledInt(26);
    }
}
