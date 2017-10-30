/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.views.listview;

/**
 * Created by Longri on 30.08.2016.
 */
public interface Adapter {

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    int getCount();

    /**
     * get a View that displays the data at the specified position in the data set.
     *
     * @param index The position of the item within the adapter's data set of the item whose view we want.
     * @return A View corresponding to the data at the specified position.
     */
    ListViewItem getView(int index);

    /**
     * @param view
     */
    void update(ListViewItem view);

    /**
     * Gibt die Größe zur Berechnung der Position eines Items zurück.</br> </br>Für V_ListView => die Höhe </br>Für H_ListView => die Breite
     *
     * @param index
     * @return
     */
    float getItemSize(int index);

}
