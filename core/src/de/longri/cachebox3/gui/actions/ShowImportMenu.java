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
package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gpx.GpxWptCounter;
import de.longri.cachebox3.gpx.GroundspeakGpxStreamImporter;
import de.longri.cachebox3.gpx.ImportHandler;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.activities.ImportGcPos;
import de.longri.cachebox3.gui.activities.ImportPQActivity;
import de.longri.cachebox3.gui.dialogs.CancelProgressDialog;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 12.04.2017.
 */
public class ShowImportMenu extends Menu {

    private final Logger log = LoggerFactory.getLogger(ShowImportMenu.class);

    public ShowImportMenu() {
        super("ImportMenu");
        this.setOnItemClickListener(clickListener);

        //ISSUE (#121 add GPX export)  addItem(MenuID.MI_EXPORT_RUN, "export");

        addItem(MenuID.MI_IMPORT_GS, "API_IMPORT", CB.getSkin().getMenuIcon.GC_Live).setMoreMenu(getGcImportMenu());
        addItem(MenuID.MI_IMPORT_GPX, "GPX_IMPORT", CB.getSkin().getMenuIcon.gpxFile);


//        if (!StringH.isEmpty(Config.CBS_IP.getValue()))
//            addItem(MenuID.MI_IMPORT_CBS, "CB-Server");

        //ISSUE (#122 add GC_Vote import)   addItem(MenuID.MI_IMPORT_GCV, "GC_Vote");
        //ISSUE (#123 add More Import)   addItem(MenuID.MI_IMPORT, "moreImport");

    }

    private final OnItemClickListener clickListener = new OnItemClickListener() {
        @Override
        public boolean onItemClick(MenuItem item) {
            switch (item.getMenuItemId()) {
                case MenuID.MI_IMPORT_GS:
                    //do nothing, will show more menu
                    break;
                case MenuID.MI_IMPORT_GPX:
                    importGpxFile();
                    break;
            }
            return true;
        }
    };


    private Menu getGcImportMenu() {
        Menu menu = new Menu("GcImportMenu");
        menu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {

                switch (item.getMenuItemId()) {
                    case MenuID.MI_IMPORT_GS_PQ:
                        ImportPQActivity imp = new ImportPQActivity();
                        imp.show();
                        return true;
                    case MenuID.MI_IMPORT_GS_API_POSITION:
                        CB.postAsync(new NamedRunnable("ShowImportMenu") {
                            @Override
                            public void run() {
                                if (!CB.checkApiKeyNeeded()) {
                                    CB.postOnGlThread(new NamedRunnable("ShowImportMenu") {
                                        @Override
                                        public void run() {
                                            new ImportGcPos().show();
                                        }
                                    });
                                }
                            }
                        });
                        return true;
                    case MenuID.MI_IMPORT_GS_API_SEARCH:
//                        SearchOverNameOwnerGcCode.ShowInstanz();
                        return true;
                }

                return true;
            }
        });
        menu.addItem(MenuID.MI_IMPORT_GS_PQ, "API_PocketQuery", CB.getSkin().getMenuIcon.import_PQ);
        menu.addItem(MenuID.MI_IMPORT_GS_API_POSITION, "API_IMPORT_OVER_POSITION", CB.getSkin().getMenuIcon.target);
        //ISSUE (#125 add Import over name, owner code) menu.addItem(MenuID.MI_IMPORT_GS_API_SEARCH, "API_IMPORT_NAME_OWNER_CODE");

        return menu;
    }

    private void importGpxFile() {
        FileChooser folderChooser = new FileChooser(Translation.get("select_file"),
                FileChooser.Mode.OPEN, FileChooser.SelectionMode.FILES, "gpx", "GPX");
        folderChooser.setSelectionReturnListener(new FileChooser.SelectionReturnListner() {
            @Override
            public void selected(final FileHandle fileHandle) {
                if (fileHandle == null) return;
                CB.postAsync(new NamedRunnable("Import Gpx file") {
                    @Override
                    public void run() {
                        importGpxFile(fileHandle);
                    }
                });
            }
        });
        folderChooser.setDirectory(Gdx.files.absolute(CB.WorkPath));
        folderChooser.show();
    }

    private void importGpxFile(final FileHandle fileHandle) {
        CB.postOnGlThread(new NamedRunnable("Show cancel progress dialog for import gpx") {
            @Override
            public void run() {
                new CancelProgressDialog("Import Gpx", Translation.get("GPX_IMPORT").toString(),
                        new ProgressCancelRunnable() {

                            ICancel iCancel = new ICancel() {
                                @Override
                                public boolean cancel() {
                                    return isCanceled();
                                }
                            };

                            @Override
                            public void canceled() {
                                log.debug("cancel clicked");
                            }

                            @Override
                            public void run() {
                                long importStart = System.currentTimeMillis();
                                GpxWptCounter counter = new GpxWptCounter();
                                counter.doImport(fileHandle);

                                int count = counter.getWptCount();
                                log.debug("Begin import of {} Waypoints", count);

                                final AtomicInteger importProgress = new AtomicInteger(0);
                                final AtomicInteger logCount = new AtomicInteger(0);
                                final AtomicInteger cacheCount = new AtomicInteger(0);
                                final AtomicInteger waypointsCount = new AtomicInteger(0);

                                GroundspeakGpxStreamImporter importer = new GroundspeakGpxStreamImporter(Database.Data, new ImportHandler() {
                                    @Override
                                    public void incrementCaches(String mysteryGcCode) {
                                        setProgress(importProgress.incrementAndGet(), "");
                                        cacheCount.incrementAndGet();
                                    }

                                    @Override
                                    public void incrementWaypoints() {
                                        setProgress(importProgress.incrementAndGet(), "");
                                        waypointsCount.incrementAndGet();
                                    }

                                    @Override
                                    public void incrementLogs() {
                                        logCount.incrementAndGet();
                                    }
                                });
                                importer.doImport(fileHandle);

                                CacheListChangedEventList.Call();
                                String msg;
                                if (!iCancel.cancel()) {
                                    long importTime = System.currentTimeMillis() - importStart;
                                    msg = "Import " + String.valueOf(cacheCount.get()) + "Caches \n"
                                            + String.valueOf(waypointsCount.get()) + " Waypoints \n"
                                            + String.valueOf(logCount.get()) + " Logs \n"
                                            + "in " + String.valueOf(importTime / 1000) + " sec!";
                                    CB.viewmanager.toast(msg, ViewManager.ToastLength.EXTRA_LONG);
                                } else {
                                    msg = "Import canceled";
                                    CB.viewmanager.toast(msg);
                                }
                                log.debug(msg);

                                CB.postOnNextGlThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CB.postAsync(new NamedRunnable("Reload Query after import") {
                                            @Override
                                            public void run() {
                                                Database.Data.Query.setUnfilteredSize(Database.Data.getCacheCountOnThisDB());
                                                log.debug("Call loadFilteredCacheList()");
                                                CB.loadFilteredCacheList(null);
                                                CB.postOnNextGlThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        CacheListChangedEventList.Call();
                                                    }
                                                });
                                            }
                                        });

                                    }
                                });

                            }
                        }
                ).show();
            }
        });
    }
}
