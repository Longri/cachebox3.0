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
package de.longri.cachebox3.gui.actions.show_views;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.CacheListView;
import de.longri.cachebox3.sqlite.Database;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_CacheList extends Abstract_Action_ShowView {


    public Action_Show_CacheList() {
        super(CacheListView.class, IMPLEMENTED, "cacheList", null, MenuID.AID_SHOW_CACHELIST);
    }

    private final StringBuilder sb = new StringBuilder();

    public String getNameExtention() {

        int unFiltered = Database.Data.cacheList.getUnFilteredSize();

        sb.length = 0; // reset StringBuilder
        sb.append("  (");
        sb.append(Integer.toString(Database.Data.cacheList.size));
        if (unFiltered != Database.Data.cacheList.size) {
            sb.append("/");
            sb.append(Integer.toString(unFiltered));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void execute() {
        if (isActVisible()) return;

        CacheListView view = new CacheListView();
        CB.viewmanager.showView(view);

    }

    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.cacheListIcon;
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof CacheListView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(CacheListView.class.getName());
    }
}
