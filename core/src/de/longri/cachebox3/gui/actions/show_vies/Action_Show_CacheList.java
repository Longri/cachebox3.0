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

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.CacheListView;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_CacheList extends Abstract_Action_ShowView {

    public Action_Show_CacheList() {
        //TODO add db size to name super("cacheList", "  (" + String.valueOf(Database.Data.Query.size()) + ")", MenuID.AID_SHOW_CACHELIST);
        super("cacheList", "  (" + String.valueOf("xxx") + ")", MenuID.AID_SHOW_CACHELIST);
    }


    @Override
    protected void Execute() {
        if (isActVisible()) return;

        CacheListView view = new CacheListView();
        CB.viewmanager.showView(view);

    }

    @Override
    public boolean hasContextMenu() {
        return false;
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof CacheListView;
    }
}
