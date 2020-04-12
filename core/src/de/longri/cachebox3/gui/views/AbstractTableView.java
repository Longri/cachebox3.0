/*
 * Copyright (C) 2020 team-cachebox.de
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
package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;

/**
 * Created by Longri on 11.05.2017.
 */
public abstract class AbstractTableView extends AbstractView {

    protected final Catch_Table contentTable = new Catch_Table();

    public AbstractTableView(de.longri.serializable.BitStore reader) {
        super(reader);
    }

    public AbstractTableView(String name) {
        super(name);
        contentTable.defaults().pad(CB.scaledSizes.MARGIN);
        this.addChild(contentTable);
    }

    protected void create() {
        super.create();
    }

    protected void boundsChanged(float x, float y, float width, float height) {
        contentTable.setBounds(0, 0, this.getWidth(), this.getHeight());
    }
}
