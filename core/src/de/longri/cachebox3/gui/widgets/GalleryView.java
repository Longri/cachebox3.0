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
package de.longri.cachebox3.gui.widgets;

import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.gui.widgets.list_view.DefaultListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.GalleryListView;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryView extends Catch_Table {

    private final GalleryListView overview;
    private final GalleryListView gallery;
    private final DefaultListViewAdapter overViewAdapter = new DefaultListViewAdapter();
    private final DefaultListViewAdapter galeryAdapter = new DefaultListViewAdapter();


    public GalleryView() {
        overview = new GalleryListView();
        gallery = new GalleryListView();
    }

    public void clearGalery() {
        overViewAdapter.clear();
        galeryAdapter.clear();
    }
}
