/* 
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.show_activities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.activities.SelectDB_Activity;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.views.CacheListView;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheListDAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Action_Show_SelectDB_Dialog extends AbstractAction {
    final static Logger log = LoggerFactory.getLogger(Action_Show_SelectDB_Dialog.class);

    public enum ViewMode {
        FORCE_SHOW, ASK
    }

    private final ViewMode viewMode;

    public Action_Show_SelectDB_Dialog(ViewMode viewMode) {
        super(IMPLEMENTED, "manageDB", MenuID.AID_SHOW_SELECT_DB_DIALOG);
        this.viewMode = viewMode;
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.manageDB;
    }

    @Override
    public void execute() {

        if (EventHandler.getSelectedCache() != null) {
            // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
            Config.LastSelectedCache.setValue(EventHandler.getSelectedCache().getGcCode());
            Config.AcceptChanges();
            log.debug("LastSelectedCache = " + EventHandler.getSelectedCache().getGcCode());
        }


        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                final SelectDB_Activity selectDBDialog = new SelectDB_Activity(new SelectDB_Activity.IReturnListener() {
                    @Override
                    public void back() {
                        returnFromSelectDB();
                    }
                }, Action_Show_SelectDB_Dialog.this.viewMode == ViewMode.FORCE_SHOW);
                selectDBDialog.show();
            }
        });
    }

    private final ViewManager.ToastLength WAIT_TOAST_LENGTH = ViewManager.ToastLength.WAIT;

    private void returnFromSelectDB() {
        log.debug("\r\nSwitch DB");
        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                loadSelectedDB();
            }
        });
    }

    public void loadSelectedDB() {
        if(CB.viewmanager!=null){
            CB.postAsync(new Runnable() {
                @Override
                public void run() {
                    CB.viewmanager.toast(Translation.Get("LoadDB"), WAIT_TOAST_LENGTH);
                    CB.requestRendering();
                }
            });
        }

        if (Database.Data != null) {
            if (Database.Data.Query != null) Database.Data.Query.clear();
            if (Database.Data.isStarted()) Database.Data.close();
        }

        FileHandle fileHandle = Gdx.files.absolute(CB.WorkPath + "/" + Config.DatabaseName.getValue());

        try {
            Database.Data.startUp(fileHandle);
        } catch (SQLiteGdxException e) {
            log.error("can't open DB", e);
            return;
        }
        Config.ReadFromDB();

        CB.Categories = new Categories();

        FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));

        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
        Database.Data.gpxFilenameUpdateCacheCount();


        log.debug("Read CacheList");
        CacheList tmpCacheList = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(tmpCacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
        log.debug("Readed " + tmpCacheList.size + "Caches into CacheList");
        Database.Data.Query = tmpCacheList;

        // set selectedCache from last selected Cache
        String sGc = Config.LastSelectedCache.getValue();
        AbstractCache lastSelectedAbstractCache = null;
        if (sGc != null && !sGc.equals("")) {
            for (int i = 0, n = Database.Data.Query.size; i < n; i++) {
                AbstractCache c = Database.Data.Query.get(i);

                if (c.getGcCode().equalsIgnoreCase(sGc)) {
                    try {
                        log.debug("returnFromSelectDB:Set selectedCache to " + c.getGcCode() + " from lastSaved.");
                        c.loadDetail();
                        EventHandler.fire(new SelectedCacheChangedEvent(c));
                        lastSelectedAbstractCache = c;
                    } catch (Exception e) {
                        log.error("set last selected Cache", e);
                    }
                    break;
                }
            }
        }
        // Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
        if ((lastSelectedAbstractCache == null) && (Database.Data.Query.size > 0)) {
            log.debug("Set selectedCache to " + Database.Data.Query.get(0).getGcCode() + " from firstInDB");
            EventHandler.fire(new SelectedCacheChangedEvent(Database.Data.Query.get(0)));
        }

        CB.setAutoResort(Config.StartWithAutoSelect.getValue());
        CacheListChangedEventList.Call();

        if (CB.viewmanager != null && CB.viewmanager.getActView() instanceof CacheListView) {
            CacheListView cacheListView = (CacheListView) CB.viewmanager.getActView();
            cacheListView.setWaitToastLength(WAIT_TOAST_LENGTH);
        } else {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    WAIT_TOAST_LENGTH.close();
                }
            });
        }

        //Fire progress changed event for progress changed on Splash
        EventHandler.fire(new IncrementProgressEvent(10, "load db"));

    }
}
