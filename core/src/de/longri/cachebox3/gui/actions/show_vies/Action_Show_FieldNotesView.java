/*
 * Copyright (C) 2016 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.show_vies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.FieldNotesView;
import de.longri.cachebox3.gui.views.SpoilerView;
import de.longri.cachebox3.utils.IconNames;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Show_FieldNotesView extends Abstract_Action_ShowView {
    public Action_Show_FieldNotesView() {
        super("Fieldnotes", MenuID.AID_SHOW_FIELDNOTES);
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        if (CB.viewmanager.getActView() instanceof FieldNotesView) {
            FieldNotesView fieldNotesView = (FieldNotesView) CB.viewmanager.getActView();
            return fieldNotesView.getContextMenu();
        }
        return null;
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof FieldNotesView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(FieldNotesView.class.getName());
    }

    @Override
    public void execute() {
        if (isActVisible()) return;
        FieldNotesView view = new FieldNotesView();
        CB.viewmanager.showView(view);
    }

    @Override
    public Sprite getIcon() {
        return CB.getSprite(IconNames.fieldnoteListIcon.name());
    }
}
