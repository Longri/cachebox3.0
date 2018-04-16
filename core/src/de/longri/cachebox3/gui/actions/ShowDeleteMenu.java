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
package de.longri.cachebox3.gui.actions;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 16.04.2018.
 */
public class ShowDeleteMenu extends Menu {
    private final Logger log = LoggerFactory.getLogger(ShowDeleteMenu.class);

    public ShowDeleteMenu() {
        super("DeleteMenu");
        this.setOnItemClickListener(clickListener);
        addItem(MenuID.MI_DELETE_FILTER, "DelActFilter", CB.getSkin().getMenuIcon.deleteFilter);
        addItem(MenuID.MI_DELETE_ARCHIEVED, "DelArchived", CB.getSkin().getMenuIcon.deleteArchieved);
        addItem(MenuID.MI_DELETE_FOUNDS, "DelFound", CB.getSkin().getMenuIcon.deleteFounds);

    }

    private final OnItemClickListener clickListener = new OnItemClickListener() {
        @Override
        public boolean onItemClick(MenuItem item) {
            switch (item.getMenuItemId()) {
                case MenuID.MI_DELETE_FILTER:
                    log.debug("Delete Caches (Filter selection)");
                    break;
                case MenuID.MI_DELETE_ARCHIEVED:
                    log.debug("Delete Caches (Archived)");
                    break;
                case MenuID.MI_DELETE_FOUNDS:
                    log.debug("Delete Caches (Founds)");
                    break;
            }
            return true;
        }
    };

}
