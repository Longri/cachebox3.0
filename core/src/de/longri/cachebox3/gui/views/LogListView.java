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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.activities.ReloadCacheActivity;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Created by Longri on 24.07.16.
 */
public class LogListView extends AbstractView implements SelectedCacheChangedListener {
    private static Logger log = LoggerFactory.getLogger(LogListView.class);

    private final ListView listView = new ListView();

    private Array<ListViewItem> items;
    private String actGcCode;

    public LogListView() {
        super("LogListView");
        listView.setEmptyString(Translation.Get("EmptyLogList"));
        this.addActor(listView);
        EventHandler.add(this);
    }

    @Override
    public void onShow() {
        setListViewAdapter();
    }

    @Override
    public void onHide() {
        super.onHide();
    }

    private void setListViewAdapter() {
        CB.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (actGcCode == null || !actGcCode.equals(EventHandler.getSelectedCache().getGcCode())) {
                    Array<LogEntry> logEntries = Database.getLogs(EventHandler.getSelectedCache());
                    actGcCode = EventHandler.getSelectedCache() == null ? "" : EventHandler.getSelectedCache().getGcCode().toString();
                    if (items != null)
                        items.clear();
                    else
                        items = new Array<>();

                    logEntries.sort(new Comparator<LogEntry>() {
                        @Override
                        public int compare(LogEntry o1, LogEntry o2) {
                            return o1.Timestamp.compareTo(o2.Timestamp) * -1;
                        }
                    });

                    for (int i = 0, n = logEntries.size; i < n; i++) {
                        items.add(new LogListViewItem(i, logEntries.get(i)));
                    }
                }
                listView.setAdapter(listViewAdapter);
            }
        });
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
        EventHandler.remove(this);
    }


    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        actGcCode = null;
        setListViewAdapter();
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("LogViewContextMenu");

        cm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case MenuID.MI_RELOAD_CACHE:
                        new ReloadCacheActivity().show();
                        return true;
                }
                return false;
            }
        });

        MenuItem mi;

        boolean isSelected = (EventHandler.getSelectedCache() != null);
        boolean selectedCacheIsNoGC = false;

        if (isSelected)
            selectedCacheIsNoGC = !EventHandler.getSelectedCache().getGcCode().toString().startsWith("GC");
        mi = cm.addItem(MenuID.MI_RELOAD_CACHE, "ReloadCacheAPI", CB.getSkin().getMenuIcon.reloadCacheIcon);
        if (!isSelected)
            mi.setEnabled(false);
        if (selectedCacheIsNoGC)
            mi.setEnabled(false);
        return cm;
    }
}
