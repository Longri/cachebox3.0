/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.menu;

/**
 * OptionMenu extends Menu without closing the Menu with click on Item.
 * Closed called only with click on BackButton on Top/Left
 * Created by Longri on 21.05.17.
 */
public class OptionMenu extends Menu {
    public OptionMenu(String name) {
        super(name);
        hideWithItemClick = false;
    }

    public OptionMenu(String name, MenuStyle style) {
        super(name, style);
        hideWithItemClick = false;
    }

    public OptionMenu(String name, String styleName) {
        super(name, styleName);
        hideWithItemClick = false;
    }
}
