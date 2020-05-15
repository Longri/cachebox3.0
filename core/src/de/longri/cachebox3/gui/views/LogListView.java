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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
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

import java.util.ArrayList;
import java.util.Collections;

import static de.longri.cachebox3.apis.GroundspeakAPI.OK;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 24.07.16.
 */
public class LogListView extends AbstractView implements SelectedCacheChangedListener {
    private static Logger log = LoggerFactory.getLogger(LogListView.class);

    private final ListView logListView = new ListView(VERTICAL);
    Array<LogEntry> logEntries;
    private final ListViewAdapter listViewAdapter = new ListViewAdapter() {

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
    private String currentGcCode;
    private boolean logsOfFriendsAreShown;
    private ArrayList<String> friendList;

    public LogListView(BitStore reader) {
        super(reader);
    }

    public LogListView() {
        super("LogListView");
        addActor(logListView);
        EventHandler.add(this);
        logsOfFriendsAreShown = false;
        createFriendList();
    }

    private void createFriendList() {
        String friends = Config.friends.getValue().replace(", ", "|").replace(",", "|");
        friendList = new ArrayList<>();
        Collections.addAll(friendList, friends.split("\\|"));
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
        CB.postOnNextGlThread(new NamedRunnable("LogListView") {
            @Override
            public void run() {
                AbstractCache selectedCache = EventHandler.getSelectedCache();
                String selectedGcCode = selectedCache == null ? "" : selectedCache.getGeoCacheCode().toString();
                if (!selectedGcCode.equals(currentGcCode)) {
                    if (selectedCache != null) {
                        currentGcCode = selectedGcCode;
                        if (logsOfFriendsAreShown) {
                            if (logEntries == null) logEntries = new Array<>();
                            else logEntries.clear();
                            for (LogEntry logEntry : Database.Data.getLogs(selectedCache)) {
                                if (!friendList.contains(logEntry.finder)) {
                                    continue;
                                }
                                logEntries.add(logEntry);
                            }
                            logListView.setEmptyString(Translation.get("NoFriendLogs"));
                        } else {
                            logEntries = Database.Data.getLogs(selectedCache);
                            logListView.setEmptyString(Translation.get("EmptyLogList"));
                        }
                        logEntries.sort((o1, o2) -> o1.logDate.compareTo(o2.logDate) * -1);
                    } else {
                        currentGcCode = "";
                    }
                }
                // if (logEntries.size == 0) logListView.setAdapter(null);
                logListView.setAdapter(listViewAdapter);
            }
        });
    }

    @Override
    public void sizeChanged() {
        logListView.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    @Override
    public void dispose() {
        EventHandler.remove(this);
    }


    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        currentGcCode = null;
        setListViewAdapter();
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu contextMenu = new Menu("LogListViewMenuTitle");
        MenuItem mi;
        boolean isSelected = (EventHandler.getSelectedCache() != null);
        if (isSelected) {
            boolean selectedCacheIsNoGC = !EventHandler.getSelectedCache().getGeoCacheCode().toString().startsWith("GC");
            // menu only for GC caches
            if (selectedCacheIsNoGC) return contextMenu;
        } else {
            // no logs without a selected cache
            return contextMenu;
        }
        // now a GC Cache is selected
        contextMenu.addMenuItem("ReloadLogs", CB.getSkin().menuIcon.downloadLogs, () -> reloadLogs(true));
        if (Config.friends.getValue().length() > 0) {
            contextMenu.addMenuItem("LoadLogsOfFriends", CB.getSkin().menuIcon.downloadFriendsLogs, () -> reloadLogs(false));
            mi = contextMenu.addMenuItem("FilterLogsOfFriends", CB.getSkin().menuIcon.friendsLogs, () -> {
                logsOfFriendsAreShown = !logsOfFriendsAreShown;
                selectedCacheChanged(null); // reload adapter
            });
            mi.setCheckable(true);
            mi.setChecked(logsOfFriendsAreShown);
        }
        contextMenu.addMenuItem("ImportFriends", CB.getSkin().menuIcon.me5ImportFriends, this::getFriends);
        contextMenu.addMenuItem("LoadLogImages", CB.getSkin().menuIcon.downloadLogImages, () -> {
            // todo implement
        });

        return contextMenu;
    }

    private void getFriends() {
        String friends = GroundspeakAPI.getInstance().fetchFriends();
        if (GroundspeakAPI.getInstance().APIError == OK) {
            Config.friends.setValue(friends);
            Config.AcceptChanges();
            MessageBox.show(Translation.get("ok") + ":\n" + friends, Translation.get("Friends"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
        } else {
            MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get("Friends"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
        }
    }

    // perhaps ask for number of logs to fetch
    private void reloadLogs(boolean loadAllLogs) {
        // todo animation while waiting
        Array<LogEntry> logList = GroundspeakAPI.getInstance().fetchGeoCacheLogs(EventHandler.getSelectedCache(), loadAllLogs, null);
        if (GroundspeakAPI.getInstance().APIError != OK) {
            MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get("errorAPI"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
        } else {
            // if not all: try to load more friend logs and then filter
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
        logsOfFriendsAreShown = !loadAllLogs;
        selectedCacheChanged(null); // reload adapter
    }

}
