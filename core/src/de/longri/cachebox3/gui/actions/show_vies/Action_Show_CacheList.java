/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.show_vies;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.show_activities.Action_ShowFilterSettings;
import de.longri.cachebox3.gui.actions.show_activities.Action_Show_SelectDB_Dialog;
import de.longri.cachebox3.gui.activities.EditCache;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.CacheListView;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_CacheList extends Abstract_Action_ShowView {


    public Action_Show_CacheList() {
        super("cacheList", "  (" + String.valueOf(Database.Data.Query.size()) + ")", MenuID.AID_SHOW_CACHELIST);
    }


    @Override
    public void execute() {
        if (isActVisible()) return;

        CacheListView view = new CacheListView();
        CB.viewmanager.showView(view);

    }

    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.cacheListIcon;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("CacheListContextMenu");


        if (!(CB.viewmanager.getActView() instanceof CacheListView))
            return null;

        final CacheListView cacheListView = (CacheListView) CB.viewmanager.getActView();

        cm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                EditCache editCache = null;
                switch (item.getMenuItemId()) {
                    case MenuID.MI_RESORT:
                        cacheListView.resort();
                        return true;
                    case MenuID.MI_FilterSet:
                        new Action_ShowFilterSettings().execute();
                        return true;
                    case MenuID.MI_RESET_FILTER:
                        CB.viewmanager.toast("RESET FILTER NOT IMPLEMENTED NOW");
//                        FilterInstances.setLastFilter(new FilterProperties());
//                        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
                        return true;
                    case MenuID.MI_SEARCH_LIST:
                        CB.viewmanager.toast("Search NOT IMPLEMENTED NOW");
//                        if (SearchDialog.that == null) {
//                            new SearchDialog();
//                        }
//
//                        SearchDialog.that.showNotCloseAutomaticly();

                        return true;
                    case MenuID.MI_IMPORT:
                        CB.viewmanager.toast("Show Import NOT IMPLEMENTED NOW");
                        // TabMainView.actionShowImportMenu.CallExecute();
                        return true;
                    case MenuID.MI_SYNC:
                        CB.viewmanager.toast("Sync NOT IMPLEMENTED NOW");
//                        SyncActivity sync = new SyncActivity();
//                        sync.show();
                        return true;
                    case MenuID.MI_MANAGE_DB:
                        new Action_Show_SelectDB_Dialog(Action_Show_SelectDB_Dialog.ViewMode.ASK).execute();
                        return true;
                    case MenuID.MI_AUTO_RESORT:
                        CB.viewmanager.toast("Toggle Autoresort NOT IMPLEMENTED NOW");

//                        CB.setAutoResort(!(CB.getAutoResort()));
//                        if (CB.getAutoResort()) {
//                            synchronized (Database.Data.Query) {
//                                Database.Data.Query.Resort(CB.getSelectedCoord(), new CacheWithWP(CB.getSelectedCache(), CB.getSelectedWaypoint()));
//                            }
//                        }
                        return true;
                    case MenuID.MI_CHK_STATE_API:

                        CB.viewmanager.toast("Check state NOT IMPLEMENTED NOW");

//
//                        if (GroundspeakAPI.ApiLimit()) {
//                            CB.MsgDownloadLimit();
//                            return;
//                        }
//
//                        // First check API-Key with visual Feedback
//                        CB.chkAPiLogInWithWaitDialog(new IChkRedyHandler() {
//
//                            @Override
//                            public void checkReady(int MemberType) {
//                                TimerTask tt = new TimerTask() {
//
//                                    @Override
//                                    public void run() {
//                                        new CB_Action_chkState().execute();
//                                    }
//                                };
//                                Timer t = new Timer();
//                                t.schedule(tt, 100);
//                            }
//                        });

                        return true;

                    case MenuID.MI_NEW_CACHE:
                        if (editCache == null)
                            editCache = new EditCache("editCache");
                        editCache.create();
                        return true;

                    case MenuID.AID_SHOW_DELETE_DIALOG:
                        CB.viewmanager.toast("deleteIcon NOT IMPLEMENTED NOW");
                        //   TabMainView.actionDelCaches.execute();
                        return true;
                }
                return false;
            }

        });

        String DBName = Database.Data == null || !Database.Data.isStarted() ? Translation.Get("noDB") : Database.Data.getPath();
        try {
            int Pos = DBName.lastIndexOf("/");
            DBName = DBName.substring(Pos + 1);
            Pos = DBName.lastIndexOf(".");
            DBName = DBName.substring(0, Pos);
        } catch (Exception e) {
            DBName = "???";
        }

        MenuItem mi;
        cm.addItem(MenuID.MI_RESORT, "ResortList", CB.getSkin().getMenuIcon.sortIcon);
        cm.addItem(MenuID.MI_FilterSet, "filter", CB.getSkin().getMenuIcon.filterIcon);
        cm.addItem(MenuID.MI_RESET_FILTER, "MI_RESET_FILTER", CB.getSkin().getMenuIcon.resetFilterIcon);
        cm.addItem(MenuID.MI_SEARCH_LIST, "search", CB.getSkin().getMenuIcon.searchIcon);
        cm.addItem(MenuID.MI_IMPORT, "importExport", CB.getSkin().getMenuIcon.importIcon);
//        if (SyncActivity.RELEASED)
//            cm.addItem(MenuID.MI_SYNC, "sync", CB.getSprite(IconNames.importIcon.name()));
        mi = cm.addItem(MenuID.MI_MANAGE_DB, "manage", "  (" + DBName + ")", CB.getSkin().getMenuIcon.manageDB);
        mi = cm.addItem(MenuID.MI_AUTO_RESORT, "AutoResort");
        mi.setCheckable(true);
        //    mi.setChecked(CB.getAutoResort());
        cm.addItem(MenuID.MI_CHK_STATE_API, "chkState", CB.getSkin().getMenuIcon.GC_Live);
        cm.addItem(MenuID.MI_NEW_CACHE, "MI_NEW_CACHE", CB.getSkin().getMenuIcon.addCacheIcon);
        cm.addItem(MenuID.AID_SHOW_DELETE_DIALOG, "DeleteCaches", CB.getSkin().getMenuIcon.deleteIcon);

        return cm;
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof CacheListView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(CacheListView.class.getName());
    }
}
