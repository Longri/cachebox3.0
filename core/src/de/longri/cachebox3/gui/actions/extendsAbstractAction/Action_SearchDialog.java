/*
 * Copyright (C) 2016 -2020 team-cachebox.de
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
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.actions.MenuID;
import de.longri.cachebox3.gui.activities.SearchDialog;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_SearchDialog extends AbstractAction {
    private static final String title = "Search";

    public Action_SearchDialog() {
        super(title, MenuID.AID_SEARCH);
    }

    @Override
    public void execute() {
        SearchDialog.getInstance(title, CB.getSkin().menuIcon.searchIcon).show();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.searchIcon;
    }
}
