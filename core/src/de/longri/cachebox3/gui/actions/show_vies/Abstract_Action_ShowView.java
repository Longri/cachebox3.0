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
import de.longri.cachebox3.gui.views.AbstractView;

/**
 * Created by Longri on 16.08.2016.
 */
public abstract class Abstract_Action_ShowView extends AbstractAction {
    public Abstract_Action_ShowView(String name, int id) {
        super(name, id);
    }

    public Abstract_Action_ShowView(String name, String nameExtention, int id) {
        super(name, nameExtention, id);
    }

    public abstract boolean hasContextMenu();

    public abstract Menu getContextMenu();

    public abstract boolean isActVisible();

    public abstract boolean viewTypeEquals(AbstractView actView);
}
