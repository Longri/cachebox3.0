/*
 * Copyright (C) 2019 team-cachebox.de
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
 * Created by Longri on 2019-04-20.
 */
public class DividerItem extends MenuItem {
    private final float minHeight;

    public DividerItem(int Index, Menu parentMenu, Menu.MenuStyle style) {
        super(Index, parentMenu);
        overrideBackground(style.divider);
        minHeight = style.divider == null ? 0 : style.divider.getMinHeight();
    }

    @Override
    public void pack() {
        this.setPrefHeight(minHeight);
        this.setHeight(minHeight);
    }

}
