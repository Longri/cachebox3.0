/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.GlobalCore;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.activities.SelectDB_Activity;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheListDAO;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Categories;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.cachebox3.utils.IconNames;
import org.slf4j.LoggerFactory;

public class Action_Show_SelectDB_Dialog extends AbstractAction {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Action_Show_SelectDB_Dialog.class);

    public Action_Show_SelectDB_Dialog() {
        super("manageDB", MenuID.AID_SHOW_SELECT_DB_DIALOG);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return CB.getSprite(IconNames.manageDb.name());
    }

    @Override
    public void Execute() {

        if (GlobalCore.isSetSelectedCache()) {
            // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
            Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
            Config.AcceptChanges();
            log.debug("LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
        }

        SelectDB_Activity selectDBDialog = new SelectDB_Activity(new SelectDB_Activity.IReturnListener() {
            @Override
            public void back() {
                returnFromSelectDB();
            }
        });
        selectDBDialog.show();

    }

    // WaitDialog wd;

    private void returnFromSelectDB() {
        //   wd = WaitDialog.ShowWait("Load DB ...");

        log.debug("\r\nSwitch DB");
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                loadSelectedDB();

                GlobalCore.setAutoResort(Config.StartWithAutoSelect.getValue());
                CacheListChangedEventList.Call();

                //  TabMainView.that.filterSetChanged();

                //  wd.dismis();
            }
        });

        thread.start();

    }

    public void loadSelectedDB() {
        if (Database.Data != null) {
            if (Database.Data.Query != null) Database.Data.Query.clear();
           if(Database.Data.isStarted()) Database.Data.Close();
        }

        FileHandle fileHandle = Gdx.files.absolute(Config.DatabasePath.getValue());

        try {
            Database.Data.StartUp(fileHandle);
        } catch (SQLiteGdxException e) {
            log.error("can't open DB", e);
            return;
        }

        Config.ReadFromDB();

        CB.Categories = new Categories();

        FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));

        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
        Database.Data.GPXFilenameUpdateCacheCount();

        synchronized (Database.Data.Query) {
            CacheListDAO cacheListDAO = new CacheListDAO();
            cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
        }

        // set selectedCache from lastselected Cache
        GlobalCore.setSelectedCache(null);
        String sGc = Config.LastSelectedCache.getValue();
        if (sGc != null && !sGc.equals("")) {
            for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
                Cache c = Database.Data.Query.get(i);

                if (c.getGcCode().equalsIgnoreCase(sGc)) {
                    try {
                        log.debug("returnFromSelectDB:Set selectedCache to " + c.getGcCode() + " from lastSaved.");
                        c.loadDetail();
                        GlobalCore.setSelectedCache(c);
                    } catch (Exception e) {
                        log.error("set last selected Cache", e);
                    }
                    break;
                }
            }
        }
        // Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
        if ((GlobalCore.getSelectedCache() == null) && (Database.Data.Query.size() > 0)) {
            log.debug("Set selectedCache to " + Database.Data.Query.get(0).getGcCode() + " from firstInDB");
            GlobalCore.setSelectedCache(Database.Data.Query.get(0));
        }
    }
}
