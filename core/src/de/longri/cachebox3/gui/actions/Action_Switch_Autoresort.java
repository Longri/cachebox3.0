/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
public class Action_Switch_Autoresort extends AbstractAction {

    public Action_Switch_Autoresort() {
        super(NOT_IMPLEMENTED, "AutoResort", MenuID.AID_AUTO_RESORT);
    }

    @Override
    public void execute() {

        CB.viewmanager.toast("Switch Autoresort not implemented");

//        GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
//        if (GlobalCore.getAutoResort()) {
//            synchronized (Database.Data.cacheList) {
//                if (GlobalCore.isSetSelectedCache()) {
//                    CacheWithWP ret = Database.Data.cacheList.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
//                    GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
//                    GlobalCore.setNearestCache(ret.getCache());
//                    ret.dispose();
//                }
//            }
//        }
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.autoSortOffIcon;
    }

}
