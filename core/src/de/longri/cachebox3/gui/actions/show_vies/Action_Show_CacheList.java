/*
 * Copyright (C) 2016 team-cachebox.de
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

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.show_activities.Action_ShowFilterSettings;
import de.longri.cachebox3.gui.activities.EditCache;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.CacheListView;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.IconNames;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_CacheList extends Abstract_Action_ShowView {

    public Action_Show_CacheList() {
        //TODO add db size to name super("cacheList", "  (" + String.valueOf(Database.Data.Query.size()) + ")", MenuID.AID_SHOW_CACHELIST);
        super("cacheList", "  (" + String.valueOf("xxx") + ")", MenuID.AID_SHOW_CACHELIST);
    }


    @Override
    protected void Execute() {
        if (isActVisible()) return;

        CacheListView view = new CacheListView();
        CB.viewmanager.showView(view);

    }

    public Sprite getIcon() {
        return CB.getSprite(IconNames.cacheListIcon.name());
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

        cm.addOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(MenuItem item) {
                EditCache editCache = null;
                switch (item.getMenuItemId()) {
                    case MenuID.MI_RESORT:
                        cacheListView.resort();
                        return;
                    case MenuID.MI_FilterSet:
                        new Action_ShowFilterSettings().Execute();
                        return;
                    case MenuID.MI_RESET_FILTER:
                        CB.viewmanager.toast("RESET FILTER NOT IMPLEMENTED NOW");
//                        FilterInstances.setLastFilter(new FilterProperties());
//                        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
                        return;
                    case MenuID.MI_SEARCH_LIST:
                        CB.viewmanager.toast("Search NOT IMPLEMENTED NOW");
//                        if (SearchDialog.that == null) {
//                            new SearchDialog();
//                        }
//
//                        SearchDialog.that.showNotCloseAutomaticly();

                        return;
                    case MenuID.MI_IMPORT:
                        CB.viewmanager.toast("Show Import NOT IMPLEMENTED NOW");
                        // TabMainView.actionShowImportMenu.CallExecute();
                        return;
                    case MenuID.MI_SYNC:
                        CB.viewmanager.toast("Sync NOT IMPLEMENTED NOW");
//                        SyncActivity sync = new SyncActivity();
//                        sync.show();
                        return;
                    case MenuID.MI_MANAGE_DB:
                        CB.viewmanager.toast("Manage DB NOT IMPLEMENTED NOW");
                        //  TabMainView.actionShowSelectDbDialog.Execute();
                        return;
                    case MenuID.MI_AUTO_RESORT:
                        CB.viewmanager.toast("Toggle Autoresort NOT IMPLEMENTED NOW");

//                        GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
//                        if (GlobalCore.getAutoResort()) {
//                            synchronized (Database.Data.Query) {
//                                Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
//                            }
//                        }
                        return;
                    case MenuID.MI_CHK_STATE_API:

                        CB.viewmanager.toast("Check state NOT IMPLEMENTED NOW");

//
//                        if (GroundspeakAPI.ApiLimit()) {
//                            GlobalCore.MsgDownloadLimit();
//                            return;
//                        }
//
//                        // First check API-Key with visual Feedback
//                        GlobalCore.chkAPiLogInWithWaitDialog(new IChkRedyHandler() {
//
//                            @Override
//                            public void checkReady(int MemberType) {
//                                TimerTask tt = new TimerTask() {
//
//                                    @Override
//                                    public void run() {
//                                        new CB_Action_chkState().Execute();
//                                    }
//                                };
//                                Timer t = new Timer();
//                                t.schedule(tt, 100);
//                            }
//                        });

                        return;

                    case MenuID.MI_NEW_CACHE:
                        if (editCache == null)
                            editCache = new EditCache("editCache");
                        editCache.create();
                        return;

                    case MenuID.AID_SHOW_DELETE_DIALOG:
                        CB.viewmanager.toast("deleteIcon NOT IMPLEMENTED NOW");
                        //   TabMainView.actionDelCaches.Execute();
                        return;
                }
            }

        });

        String DBName = Database.Data.getPath();
        try {
            int Pos = DBName.lastIndexOf("/");
            DBName = DBName.substring(Pos + 1);
            Pos = DBName.lastIndexOf(".");
            DBName = DBName.substring(0, Pos);
        } catch (Exception e) {
            DBName = "???";
        }

        MenuItem mi;
        cm.addItem(MenuID.MI_RESORT, "ResortList", CB.getSprite(IconNames.sortIcon.name()));
        cm.addItem(MenuID.MI_FilterSet, "filter", CB.getSprite(IconNames.filter.name()));
        cm.addItem(MenuID.MI_RESET_FILTER, "MI_RESET_FILTER", CB.getSprite(IconNames.filter.name()));
        cm.addItem(MenuID.MI_SEARCH_LIST, "search", CB.getSprite(IconNames.lupe.name()));
        cm.addItem(MenuID.MI_IMPORT, "importExport", CB.getSprite(IconNames.importIcon.name()));
//        if (SyncActivity.RELEASED)
//            cm.addItem(MenuID.MI_SYNC, "sync", CB.getSprite(IconNames.importIcon.name()));
        mi = cm.addItem(MenuID.MI_MANAGE_DB, "manage", "  (" + DBName + ")", CB.getSprite(IconNames.manageDb.name()));
        mi = cm.addItem(MenuID.MI_AUTO_RESORT, "AutoResort");
        mi.setCheckable(true);
        //    mi.setChecked(GlobalCore.getAutoResort());
        cm.addItem(MenuID.MI_CHK_STATE_API, "chkState", CB.getSprite(IconNames.gc_liveIcon.name()));
        cm.addItem(MenuID.MI_NEW_CACHE, "MI_NEW_CACHE", CB.getSprite(IconNames.addCacheIcon.name()));
        cm.addItem(MenuID.AID_SHOW_DELETE_DIALOG, "DeleteCaches", CB.getSprite(IconNames.deleteIcon.name()));

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
