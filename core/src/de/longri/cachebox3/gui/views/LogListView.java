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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.07.16.
 */
public class LogListView extends AbstractView {
    private static Logger log = LoggerFactory.getLogger(LogListView.class);

    private final ListView listView = new ListView();

    private Array<ListViewItem> items;
    private String actGcCode;

    public LogListView() {
        super("LogListView");
        this.addActor(listView);
    }

    @Override
    public void onShow() {
        if (actGcCode == null || !actGcCode.equals(EventHandler.getSelectedCache().getGcCode())) {
            Array<LogEntry> logEntries = Database.getLogs(EventHandler.getSelectedCache());
            actGcCode = EventHandler.getSelectedCache().getGcCode();
            if (items != null)
                items.clear();
            else
                items = new Array<>();
            for (int i = 0, n = logEntries.size; i < n; i++) {
                items.add(new LogListViewItem(i, logEntries.get(i)));
            }
        }
        listView.setAdapter(listViewAdapter);
    }

    @Override
    public void sizeChanged() {
        listView.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    private Adapter listViewAdapter = new Adapter() {

        @Override
        public int getCount() {
            return items.size;
        }

        @Override
        public ListViewItem getView(int index) {
            return items.get(index);
        }

        @Override
        public void update(ListViewItem view) {
            // nothing to do
        }

        @Override
        public float getItemSize(int index) {
            return items.get(index).getHeight();
        }
    };

    @Override
    public void dispose() {

    }

    public Menu getContextMenu() {
        return null;
    }
}
