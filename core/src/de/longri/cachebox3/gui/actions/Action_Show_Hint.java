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
package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.events.EventHandler;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Show_Hint extends AbstractAction {

    public Action_Show_Hint() {
        super("hint", MenuID.AID_SHOW_HINT);
    }

    @Override
    public void execute() {
        CB.viewmanager.toast("Show Hint dialog not implemented");

//        if (getEnabled())
//            HintDialog.show();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.hintIcon;
    }

    @Override
    public boolean getEnabled() {
        // return true if any Cache selected and this Cache has a Hint
        if (EventHandler.getSelectedCache() == null)
            return false;
        String hintText = EventHandler.getSelectedCache().getHint();
        if ((hintText == null) || (hintText.length() == 0))
            return false;
        return true;
    }
}
