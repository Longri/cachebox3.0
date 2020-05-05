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
package de.longri.cachebox3.gui.actions.extendsAbstractAction;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.sqlite.Database;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Switch_Autoresort extends AbstractAction {

    public Action_Switch_Autoresort() {
        super(ENABLED, "AutoResort");
    }

    @Override
    public void execute() {
        if (Database.Data.cacheList == null) return;
        CB.setAutoResort(!(CB.getAutoResort()));
        if (CB.getAutoResort()) {
            synchronized (Database.Data.cacheList) {
                if (EventHandler.isSetSelectedCache()) {
                    Database.Data.cacheList.resort(null, null);
                }
            }
            EventHandler.add(Database.Data.cacheList);
        }
        else {
            EventHandler.remove(Database.Data.cacheList);
        }
    }

    @Override
    public Drawable getIcon() {
        return CB.getAutoResort() ? CB.getSkin().menuIcon.autoSortOnIcon : CB.getSkin().menuIcon.autoSortOffIcon;
    }

}
