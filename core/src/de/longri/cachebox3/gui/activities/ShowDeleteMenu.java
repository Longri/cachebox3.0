/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.activities;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.activities.DeleteCaches;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.cachebox3.utils.NamedRunnable;

/**
 * Created by Longri on 16.04.2018.
 */
public class ShowDeleteMenu extends Menu {

    public ShowDeleteMenu() {
        super("DeleteMenuTitle");
        addMenuItem("DelActFilter", CB.getSkin().getMenuIcon.deleteFilter, () -> askAndExecute(CB.viewmanager.getActFilter(), "DelActFilter"));
        addMenuItem("DelArchived", CB.getSkin().getMenuIcon.deleteArchieved, () -> askAndExecute(FilterInstances.ARCHIEVED, "DelArchived"));
        addMenuItem("DelFound", CB.getSkin().getMenuIcon.deleteFounds, () -> askAndExecute(FilterInstances.MYFOUNDS, "DelFound"));
    }

    private void askAndExecute(final FilterProperties filter, String msg) {
        MessageBox.show(Translation.get(msg), null, MessageBoxButton.YesNo, MessageBoxIcon.Question, (which, data) -> {
            if (which == ButtonDialog.BUTTON_POSITIVE)
                CB.postAsync(new NamedRunnable("Delete Caches") {
                    @Override
                    public void run() {
                        new DeleteCaches().deleteCaches(filter.getSqlWhere(Config.GcLogin.getValue()));
                    }
                });
            return true;
        });
    }

}
