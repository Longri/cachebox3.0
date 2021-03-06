/*
 * Copyright (C) 2016-2020 team-cachebox.de
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
package de.longri.cachebox3.gui.menu.menuBtn4;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.gui.views.DraftsView;
import de.longri.cachebox3.gui.widgets.menu.Menu;

/**
 * Created by Longri on 14.09.2016.
 * this is only used for quick button
 */
public class Action_Upload_Drafts extends AbstractAction {

    public Action_Upload_Drafts() {
        super("uploadDrafts", MenuID.AID_UPLOAD_FIELD_NOTE);
    }

    @Override
    public void execute() {
        final Menu cm = new Menu("DraftsContextMenuTitle");
        cm.addMenuItem("uploadDrafts", CB.getSkin().menuIcon.uploadDraft, DraftsView.getInstance()::uploadDraft);
        cm.addMenuItem("directLog", CB.getSkin().menuIcon.me2Logbook, DraftsView.getInstance()::uploadLog);
        cm.show();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.uploadDraft;
    }
}
