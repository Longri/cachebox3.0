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
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.LogEntry;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.apis.GroundspeakAPI.APIError;
import static de.longri.cachebox3.apis.GroundspeakAPI.LastAPIError;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 24.07.16.
 */
public class LogListView extends AbstractView implements SelectedCacheChangedListener {
    private static Logger log = LoggerFactory.getLogger(LogListView.class);

    private final ListView listView = new ListView(VERTICAL);
    Array<LogEntry> logEntries;

    private String actGcCode;
    // todo possibly store in settings?
    private boolean logsOfFriendsAreShown;
    private ListViewAdapter listViewAdapter = new ListViewAdapter() {

        @Override
        public int getCount() {
            return logEntries == null ? 0 : logEntries.size;
        }

        @Override
        public ListViewItem getView(int index) {
            return new LogListViewItem(index, logEntries.get(index));
        }

        @Override
        public void update(ListViewItem view) {
            // nothing to do
        }

    };

    public LogListView(BitStore reader) {
        super(reader);
    }

    public LogListView() {
        super("LogListView");
        listView.setEmptyString(Translation.get("EmptyLogList"));
        this.addActor(listView);
        EventHandler.add(this);
        logsOfFriendsAreShown = false;
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
        CB.postOnNextGlThread((new NamedRunnable("LogListView") {
            @Override
            public void run() {
                AbstractCache selectedCache = EventHandler.getSelectedCache();
                String selectedGcCode = selectedCache == null ? "" : selectedCache.getGcCode().toString();
                if (actGcCode == null || !actGcCode.equals(selectedGcCode)) {
                    if (selectedCache != null) {
                        actGcCode = selectedGcCode;
                        logEntries = Database.Data.getLogs(selectedCache);
                        logEntries.sort((o1, o2) -> o1.Timestamp.compareTo(o2.Timestamp) * -1);
                    } else {
                        // todo or set actGcCode to null ?
                        actGcCode = ""; // = selectedGcCode
                    }
                }
                listView.setAdapter(listViewAdapter);
            }
        }));
    }

    @Override
    public void sizeChanged() {
        listView.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

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
        Menu cm = new Menu("LogListViewMenuTitle");
        MenuItem mi;
        boolean isSelected = (EventHandler.getSelectedCache() != null);
        if (isSelected) {
            boolean selectedCacheIsNoGC = !EventHandler.getSelectedCache().getGcCode().toString().startsWith("GC");
            // menu only for GC caches
            if (selectedCacheIsNoGC) return cm;
        } else {
            // no menuitem without selected cache
            return cm;
        }
        // now a GC Cache is selected
        cm.addMenuItem("ReloadLogs", CB.getSkin().getMenuIcon.downloadLogs, () -> reloadLogs(true));
        if (Config.Friends.getValue().length() > 0) {
            cm.addMenuItem("LoadLogsOfFriends", CB.getSkin().getMenuIcon.downloadFriendsLogs, () -> reloadLogs(false));
            mi = cm.addMenuItem("FilterLogsOfFriends", CB.getSkin().getMenuIcon.friendsLogs, () -> {
                logsOfFriendsAreShown = !logsOfFriendsAreShown;
                // todo implement filter friends logs;
            });
            mi.setCheckable(true);
            mi.setChecked(logsOfFriendsAreShown);
        }
        cm.addMenuItem("LoadLogImages", CB.getSkin().getMenuIcon.downloadLogImages, () -> {
            // todo implement
        });

        return cm;
    }

    // todo perhaps ask for number of logs to fetch
    private void reloadLogs(boolean loadAllLogs) {
        // todo animation while waiting
        Array<LogEntry> logList = GroundspeakAPI.fetchGeoCacheLogs(EventHandler.getSelectedCache(), loadAllLogs, null);
        if (APIError != 0) {
            MessageBox.show(LastAPIError, Translation.get("errorAPI"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
        } else {
            if (logList.size > 0) {

                Database.Data.beginTransaction();

                LogDAO dao = new LogDAO();
                if (loadAllLogs)
                    dao.deleteLogs(EventHandler.getSelectedCache().getId());
                for (LogEntry writeTmp : logList) {
                    // ChangedCount++;
                    dao.WriteToDatabase(writeTmp);
                }

                Database.Data.endTransaction();

            }
        }
    }

}
