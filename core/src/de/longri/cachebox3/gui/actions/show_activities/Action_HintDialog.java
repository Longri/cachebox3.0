/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.show_activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.dialogs.HintDialog;
import de.longri.cachebox3.gui.menu.MenuID;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_HintDialog extends AbstractAction {

    public Action_HintDialog() {
        super(IMPLEMENTED, "hint", MenuID.AID_SHOW_HINT);
    }

    @Override
    public void execute() {
        if (hasHint()) {
            new HintDialog().show();
        }
    }

    @Override
    public Drawable getIcon() {
        return hasHint() ? CB.getSkin().getMenuIcon.hintIconOn : CB.getSkin().getMenuIcon.hintIconOff;
    }

    public boolean hasHint() {
        // return true if any Cache selected and this Cache has a Hint
        if (EventHandler.getSelectedCache() == null)
            return false;
        return EventHandler.getSelectedCache().hasHint();
    }
}
