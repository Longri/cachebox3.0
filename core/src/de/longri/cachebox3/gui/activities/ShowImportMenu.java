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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gpx.GpxWptCounter;
import de.longri.cachebox3.gpx.GroundspeakGpxStreamImporter;
import de.longri.cachebox3.gpx.ImportHandler;
import de.longri.cachebox3.gui.dialogs.CancelProgressDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static de.longri.cachebox3.CB.*;

/**
 * Created by Longri on 12.04.2017.
 */
public class ShowImportMenu extends Menu {

    private final Logger log = LoggerFactory.getLogger(ShowImportMenu.class);

    public ShowImportMenu() {
        super("ImportMenuTitle");

        addMenuItem("chkState", CB.getSkin().menuIcon.gc_logo, () -> new UpdateStatusAndOthers().show());
        addMenuItem("API_PocketQuery", CB.getSkin().menuIcon.import_PQ, () -> new ImportPQActivity(Database.Data).show());
        addMenuItem("GPX_IMPORT", CB.getSkin().menuIcon.gpxFile, this::importGpxFile);
        addMenuItem("moreImport", CB.getSkin().menuIcon.importIcon, this::selectableImport); // todo ISSUE (#123 add More Import)
        addMenuItem("importCachesOverPosition", CB.getSkin().menuIcon.target, this::importOverPosition);
        // todo ISSUE (#125 add Import over name, owner code) menu.addItem(MenuID.MI_IMPORT_GS_API_SEARCH, "API_IMPORT_NAME_OWNER_CODE");
        addMenuItem("GCVoteRatings", null, () -> {
            SelectableImport.getInstance("moreImport", CB.getSkin().menuIcon.importIcon).importGCVote();
        }); // todo create icon: CB.getSkin().getMenuIcon.importGCVote
        addDivider(0);

        //todo ISSUE (#121 add GPX export)  addItem(MenuID.MI_EXPORT_RUN, "export");
        //if (!StringH.isEmpty(Config.CBS_IP.getValue()))
        //    addItem(MenuID.MI_IMPORT_CBS, "CB-Server");

    }

    private void selectableImport() {
        SelectableImport.getInstance("moreImport", CB.getSkin().menuIcon.importIcon).show();
    }

    private void importOverPosition() {
        postAsync(new NamedRunnable("ShowImportMenu") {
            @Override
            public void run() {
                if (GroundspeakAPI.getInstance().isAccessTokenInvalid()) {
                    if (GroundspeakAPI.getInstance().APIError == 401) {
                        MessageBox.show(Translation.get("apiKeyNeeded"), Translation.get("ImportMenuTitle"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                    } else {
                        MessageBox.show(Translation.get("getApiKey") + "\n" + GroundspeakAPI.getInstance().LastAPIError, Translation.get("ImportMenuTitle"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                    }
                } else {
                    postOnGlThread(new NamedRunnable("ShowImportMenu") {
                        @Override
                        public void run() {
                            new ImportGCPosition().show();
                        }
                    });
                }
            }
        });
    }

    private void importGpxFile() {
        FileChooser folderChooser = new FileChooser(Translation.get("select_file"),
                FileChooser.Mode.OPEN, FileChooser.SelectionMode.FILES, "gpx", "GPX");
        folderChooser.setSelectionReturnListener(new FileChooser.SelectionReturnListner() {
            @Override
            public void selected(final FileHandle fileHandle) {
                if (fileHandle == null) return;
                postAsync(new NamedRunnable("Import Gpx file") {
                    @Override
                    public void run() {
                        importGpxFile(fileHandle);
                    }
                });
            }
        });
        folderChooser.setDirectory(Gdx.files.absolute(WorkPath));
        folderChooser.show();
    }

    private void importGpxFile(final FileHandle fileHandle) {
        postOnGlThread(new NamedRunnable("Show cancel progress dialog for import gpx") {
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

                                EventHandler.fire(new CacheListChangedEvent());
                                String msg;
                                if (!iCancel.cancel()) {
                                    long importTime = System.currentTimeMillis() - importStart;
                                    msg = "Import " + String.valueOf(cacheCount.get()) + "Caches \n"
                                            + String.valueOf(waypointsCount.get()) + " Waypoints \n"
                                            + String.valueOf(logCount.get()) + " Logs \n"
                                            + "in " + String.valueOf(importTime / 1000) + " sec!";
                                    viewmanager.toast(msg, ViewManager.ToastLength.EXTRA_LONG);
                                } else {
                                    msg = "Import canceled";
                                    viewmanager.toast(msg);
                                }
                                log.debug(msg);

                                postOnNextGlThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        postAsync(new NamedRunnable("Reload cacheList after import") {
                                            @Override
                                            public void run() {
                                                Database.Data.cacheList.setUnfilteredSize(Database.Data.getCacheCountOnThisDB());
                                                log.debug("Call loadFilteredCacheList()");
                                                loadFilteredCacheList(null);
                                                postOnNextGlThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        EventHandler.fire(new CacheListChangedEvent());
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
