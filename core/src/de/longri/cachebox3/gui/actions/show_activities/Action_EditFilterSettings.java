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
package de.longri.cachebox3.gui.actions.show_activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.activities.EditFilterSettings;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.cachebox3.utils.NamedRunnable;

public class Action_EditFilterSettings extends AbstractAction {

    public Action_EditFilterSettings() {
        super(IMPLEMENTED, "filter", MenuID.AID_SHOW_FILTER_DIALOG);
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.filterIcon;
    }

    @Override
    public void execute() {
        EditFilterSettings edFi = new EditFilterSettings(CB.viewmanager.getActFilter()) {
            public void callBack(final FilterProperties properties) {
                CB.postAsync(new NamedRunnable("Action_EditFilterSettings") {
                    @Override
                    public void run() {
                        CB.viewmanager.setNewFilter(properties);
                    }
                });
            }
        };
        edFi.show();
    }
}
