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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;

/**
 * Created by Longri on 15.05.2017.
 */
public class SelectBox<T extends SelectBoxItem> extends IconButton {

    private Array<T> entries;

    public SelectBox() {
        super("");
    }

    public void set(Array<T> list) {
        this.entries = list;
    }

    public void select(int index) {
        SelectBoxItem item = entries.get(index);
        this.setText(item.getName());
        this.setIcon(item.getDrawable());
        this.layout();
    }

    public void select(T item) {
        this.setText(item.getName());
        this.setIcon(item.getDrawable());
        this.layout();
    }
}
