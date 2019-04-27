/*
 * Copyright (C) 2016 -2017 team-cachebox.de
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

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_SearchDialog extends AbstractAction {

    public Action_SearchDialog() {
        super(NOT_IMPLEMENTED, "search", MenuID.AID_SEARCH);
    }

    @Override
    public void execute() {
        CB.viewmanager.toast("Show Search not implemented");


//        if (TabMainView.cacheListView == null || !TabMainView.cacheListView.isVisible()) {
//            TabMainView.actionShowCacheList.Execute();
//        }
//
//        if (SearchDialog.that == null) {
//            new SearchDialog();
//        }
//
//        SearchDialog.that.showNotCloseAutomaticly();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.searchIcon;
    }
}
