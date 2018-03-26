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
package de.longri.cachebox3.gui.activities;

import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;

/**
 * Created by Longri on 26.03.2018.
 */
public class PqListItem extends ListViewItem {
    private final PocketQuery.PQ pq;

    public PqListItem(int index, PocketQuery.PQ pq) {
        super(index);
        this.pq = pq;

        VisLabel label = new VisLabel(pq.name);
        this.add(label).expandX().fillX();
    }
}
