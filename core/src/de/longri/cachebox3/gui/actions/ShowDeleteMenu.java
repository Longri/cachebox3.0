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
package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.activities.BlockUiProgress_Activity;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheList3DAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 16.04.2018.
 */
public class ShowDeleteMenu extends Menu {
    private final Logger log = LoggerFactory.getLogger(ShowDeleteMenu.class);

    public ShowDeleteMenu() {
        super("DeleteMenu");
        this.setOnItemClickListener(clickListener);
        addItem(MenuID.MI_DELETE_FILTER, "DelActFilter", CB.getSkin().getMenuIcon.deleteFilter);
        addItem(MenuID.MI_DELETE_ARCHIEVED, "DelArchived", CB.getSkin().getMenuIcon.deleteArchieved);
        addItem(MenuID.MI_DELETE_FOUNDS, "DelFound", CB.getSkin().getMenuIcon.deleteFounds);

    }

    private final OnItemClickListener clickListener = new OnItemClickListener() {
        @Override
        public boolean onItemClick(MenuItem item) {

            FilterProperties selectedFilter = null;
            CompoundCharSequence msg = null;
            switch (item.getMenuItemId()) {
                case MenuID.MI_DELETE_FILTER:
                    log.debug("Delete Caches (Filter selection)");
                    selectedFilter = CB.viewmanager.getActFilter();
                    msg = Translation.get("DelActFilter");
                    break;
                case MenuID.MI_DELETE_ARCHIEVED:
                    log.debug("Delete Caches (Archived)");
                    selectedFilter = FilterInstances.ARCHIEVED;
                    msg=Translation.get("DelArchived");
                    break;
                case MenuID.MI_DELETE_FOUNDS:
                    log.debug("Delete Caches (Founds)");
                    selectedFilter = FilterInstances.MYFOUNDS;
                    msg=Translation.get("DelFound");
                    break;
            }

            final FilterProperties filter = selectedFilter;
            MessageBox.show(msg, null, MessageBoxButtons.YesNo, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
                @Override
                public boolean onClick(int which, Object data) {
                    if (which == ButtonDialog.BUTTON_POSITIVE)
                        deleteCaches(filter);
                    return true;
                }
            });


            return true;
        }
    };

    private void deleteCaches(FilterProperties filter) {

        BlockUiProgress_Activity blockUi = new BlockUiProgress_Activity(Translation.get("DB_Convert"));
        blockUi.show();


        //check if Filter set to delete whole Database
        int wholeCount = Database.Data.getCacheCountOnThisDB();

        LongArray deleteCacheIdList = new LongArray();
        Array<String> deleteCacheGcCodeList = new Array<>();
        CacheList3DAO dao = new CacheList3DAO();
        dao.readCacheListIDs(Database.Data, deleteCacheIdList, deleteCacheGcCodeList,
                filter.getSqlWhere(Config.GcLogin.getValue()));
        int filteredCacheCount = deleteCacheIdList.size;
        if (wholeCount == filteredCacheCount) {
            log.debug("Filter is set to delete whole Database");
            Database.Data.beginTransaction();
            Database.Data.execSQL("DELETE FROM Attributes;");
            Database.Data.execSQL("DELETE FROM CacheCoreInfo;");
            Database.Data.execSQL("DELETE FROM CacheInfo;");
            Database.Data.execSQL("DELETE FROM CacheText;");
            Database.Data.execSQL("DELETE FROM Category;");
            Database.Data.execSQL("DELETE FROM GPXFilenames;");
            Database.Data.execSQL("DELETE FROM Images;");
            Database.Data.execSQL("DELETE FROM Logs;");
            Database.Data.execSQL("DELETE FROM PocketQueries;");
            Database.Data.execSQL("DELETE FROM Replication;");
            Database.Data.execSQL("DELETE FROM Waypoints;");
            Database.Data.execSQL("DELETE FROM WaypointsText;");
            Database.Data.endTransaction();

            Database.Data.Query.clear();
            CacheListChangedEventList.Call();
        } else {
            log.debug("delete {} Caches", filteredCacheCount);

            Array<AbstractCache> queryDeleteList = new Array<>();

            GdxSqlitePreparedStatement attributesStatement = Database.Data.myDB.prepare("DELETE FROM Attributes WHERE id = ?");
            GdxSqlitePreparedStatement cacheCoreInfoStatement = Database.Data.myDB.prepare("DELETE FROM CacheCoreInfo WHERE id = ?");
            GdxSqlitePreparedStatement cacheInfoStatement = Database.Data.myDB.prepare("DELETE FROM CacheInfo WHERE id = ?");
            GdxSqlitePreparedStatement cacheTextStatement = Database.Data.myDB.prepare("DELETE FROM CacheText WHERE id = ?");
            GdxSqlitePreparedStatement imagesStatement = Database.Data.myDB.prepare("DELETE FROM Images WHERE CacheId = ?");
            GdxSqlitePreparedStatement logsStatement = Database.Data.myDB.prepare("DELETE FROM Logs WHERE CacheId = ?");
            GdxSqlitePreparedStatement waypointsStatement = Database.Data.myDB.prepare("DELETE FROM Waypoints WHERE CacheId = ?");
            GdxSqlitePreparedStatement waypointsTextStatement = Database.Data.myDB.prepare("DELETE FROM WaypointsText WHERE GcCode = ?");

            Database.Data.beginTransaction();
            for (int i = deleteCacheIdList.size - 1; i >= 0; i--) {
                long cacheId = deleteCacheIdList.get(i);
                queryDeleteList.add(Database.Data.Query.getCacheById(cacheId));
                attributesStatement.bind(cacheId).commit().reset();
                cacheCoreInfoStatement.bind(cacheId).commit().reset();
                cacheInfoStatement.bind(cacheId).commit().reset();
                cacheTextStatement.bind(cacheId).commit().reset();
                imagesStatement.bind(cacheId).commit().reset();
                logsStatement.bind(cacheId).commit().reset();
                waypointsStatement.bind(cacheId).commit().reset();
                waypointsTextStatement.bind(deleteCacheGcCodeList.get(i)).commit().reset();
            }
            Database.Data.endTransaction();
            attributesStatement.close();
            cacheCoreInfoStatement.close();
            cacheInfoStatement.close();
            cacheTextStatement.close();
            imagesStatement.close();
            logsStatement.close();
            waypointsStatement.close();
            waypointsTextStatement.close();

            while (queryDeleteList.size > 0) {
                AbstractCache delCache = queryDeleteList.pop();
                Database.Data.Query.removeValue(delCache, true);
            }

        }
        Database.Data.execSQL("VACUUM;");
        deleteImages(deleteCacheGcCodeList);

        blockUi.finish();
    }

    private void deleteImages(Array<String> deleteCacheGcCodeList) {
        //TODO
    }

}
