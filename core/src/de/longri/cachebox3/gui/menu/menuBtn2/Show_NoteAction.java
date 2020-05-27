/*
 * Copyright (C) 2016 - 2020 team-cachebox.de
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
package de.longri.cachebox3.gui.menu.menuBtn2;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractShowAction;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.NotesView;

/**
 * Created by Longri on 14.09.2016.
 */
public class Show_NoteAction extends AbstractShowAction {
    public Show_NoteAction() {
        super(NotesView.class, ENABLED, "Notes", MenuID.AID_SHOW_NOTES);
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getCurrentView() instanceof NotesView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(NotesView.class.getName());
    }

    @Override
    public void execute() {
        if (isActVisible()) return;
        NotesView view = new NotesView();
        CB.viewmanager.showView(view);
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.me2Note;
    }
}