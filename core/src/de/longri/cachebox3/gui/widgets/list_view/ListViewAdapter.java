/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets.list_view;

/**
 * Created by Longri on 03.02.18.
 */
public interface ListViewAdapter {

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
     * @param view the ListViewItem for update
     */
    void update(ListViewItem view);

}
