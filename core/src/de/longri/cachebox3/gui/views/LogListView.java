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
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.LogEntry;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;

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

    // todo possibly store in settings?
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
                if (actGcCode == null || !actGcCode.equals(EventHandler.getSelectedCache().getGcCode())) {
                    logEntries = Database.Data.getLogs(EventHandler.getSelectedCache());
                    actGcCode = EventHandler.getSelectedCache() == null ? "" : EventHandler.getSelectedCache().getGcCode().toString();

                    logEntries.sort(new Comparator<LogEntry>() {
                        @Override
                        public int compare(LogEntry o1, LogEntry o2) {
                            return o1.Timestamp.compareTo(o2.Timestamp) * -1;
                        }
                    });
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
        Menu cm = new Menu("LogViewContextMenu");

        cm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case MenuID.MI_LOAD_FRIENDS_LOGS:
                        reloadLogs(false);
                        return true;
                    case MenuID.MI_RELOADLOGS:
                        reloadLogs(true);
                        return true;
                    case MenuID.MI_FILTERLOGS:
                        logsOfFriendsAreShown = !logsOfFriendsAreShown;
                        //todo this filter logs;
                        break;
                }
                return false;
            }
        });

        MenuItem mi;

        boolean isSelected = (EventHandler.getSelectedCache() != null);
        boolean selectedCacheIsNoGC = false;

        if (isSelected)
            selectedCacheIsNoGC = !EventHandler.getSelectedCache().getGcCode().toString().startsWith("GC");
        mi = cm.addItem(MenuID.MI_RELOADLOGS, "ReloadLogs", CB.getSkin().getMenuIcon.logViewIcon);
        // todo change icon logViewIcon
        if (!isSelected)
            mi.setEnabled(false);
        if (selectedCacheIsNoGC)
            mi.setEnabled(false);
        if (Config.Friends.getValue().length() > 0) {
            cm.addItem(MenuID.MI_LOAD_FRIENDS_LOGS, "LoadLogsOfFriends", CB.getSkin().getMenuIcon.logViewIcon);
            // todo change icon logViewIcon Sprites.getSprite(IconName.importIcon.name()));
            mi = cm.addItem(MenuID.MI_FILTERLOGS, "FilterLogsOfFriends", CB.getSkin().getMenuIcon.logViewIcon);
            // todo change icon logViewIcon Sprites.getSprite(IconName.filter.name()));
            mi.setCheckable(true);
            mi.setChecked(logsOfFriendsAreShown);
        }

        return cm;
    }

    private void reloadLogs(boolean loadAllLogs) {
        // todo animation while waiting
        ArrayList<LogEntry> logList = GroundspeakAPI.fetchGeoCacheLogs(EventHandler.getSelectedCache(), loadAllLogs, null);
        if (APIError != 0) {
            MessageBox.show(LastAPIError, Translation.get("errorAPI"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
        } else {
            if (logList.size() > 0) {

                Database.Data.beginTransaction();

                LogDAO dao = new LogDAO();
                if (loadAllLogs)
                    dao.deleteLogs(EventHandler.getSelectedCache().getId());
                for (LogEntry writeTmp: logList)
                {
                    // ChangedCount++;
                    dao.WriteToDatabase(writeTmp);
                }

                Database.Data.endTransaction();

            }
        }
    }

}
