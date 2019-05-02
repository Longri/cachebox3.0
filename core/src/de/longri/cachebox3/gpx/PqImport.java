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
package de.longri.cachebox3.gpx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.gui.activities.PqListItem;
import de.longri.cachebox3.gui.widgets.AligmentLabel;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItemInterface;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnZip;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 11.05.18.
 */
public class PqImport {


    public PqImport(Database database) {
        this.database = database;
    }

    public interface IReadyHandler {
        void ready(int importedCaches, int importedWaypoints, int importedLogs);
    }

    private final static Logger log = LoggerFactory.getLogger(PqImport.class);

    private final Database database;

    public void importNow(final Array<ListViewItemInterface> selectedItems, final ICancel iCancel,
                          final AligmentLabel downloadLabel, final CB_ProgressBar downloadProgress,
                          final AligmentLabel extractLabel, final CB_ProgressBar extractProgress,
                          final AligmentLabel importLabel, final CB_ProgressBar importProgress,
                          final AligmentLabel correctedLabel, final IReadyHandler readyHandler) {

        // get import information
        final AtomicInteger downloadPqCount = new AtomicInteger(0),
                downloadedPqs = new AtomicInteger(0), extractedPQs = new AtomicInteger(0),
                downloadBytes = new AtomicInteger(0), readyDownloadBytes = new AtomicInteger(0),
                importCache = new AtomicInteger(0), readyImportedCaches = new AtomicInteger(0),
                corrected = new AtomicInteger(0), mysteries = new AtomicInteger(0);


        for (ListViewItemInterface item : selectedItems) {
            PqListItem pqListItem = (PqListItem) item;
            downloadPqCount.incrementAndGet();
            downloadBytes.addAndGet((int) (0 * 1048576.0)); // API 1.0 has no size: pqListItem.getSize()
            importCache.addAndGet(pqListItem.getCount());
        }

        //set initial progress values
        if (downloadLabel != null)
            downloadLabel.setText(Translation.get("downloaded", "0", Integer.toString(downloadPqCount.get())));
        if (downloadProgress != null) downloadProgress.setRange(0, downloadBytes.get());

        if (extractLabel != null)
            extractLabel.setText(Translation.get("extracted", "0", Integer.toString(downloadPqCount.get())));
        if (extractProgress != null) extractProgress.setRange(0, downloadPqCount.get());

        if (importLabel != null)
            importLabel.setText(Translation.get("imported", "0", Integer.toString(importCache.get())));
        if (importProgress != null) importProgress.setRange(0, importCache.get());

        if (correctedLabel != null) correctedLabel.setText(Translation.get("correctedCoords", "0", "0"));


        final int TRANSACTION_ID = 290272;
        database.beginTransactionExclusive(TRANSACTION_ID);

        //start download async
        final Array<FileHandle> downloadedFiles = new Array<>();
        final Array<FileHandle> extractedfolder = new Array<>();
        final AtomicBoolean downloadReady = new AtomicBoolean(false);
        final AtomicBoolean extractReady = new AtomicBoolean(false);
        final AtomicBoolean importReady = new AtomicBoolean(false);
        final AtomicBoolean correctReady = new AtomicBoolean(false);
        CB.postAsync(new NamedRunnable("Download PQs") {
            @Override
            public void run() {
                for (ListViewItemInterface item : selectedItems) {
                    GroundspeakAPI.PQ pqItem = ((PqListItem) item).getPocketQuery();
                    String pqFolder = Config.PocketQueryFolder.getValue();
                    GroundspeakAPI.fetchPocketQuery(pqItem, pqFolder);
                    if (GroundspeakAPI.APIError == GroundspeakAPI.OK) {
                        downloadedFiles.add(new FileHandle(pqFolder + "/" + pqItem.GUID + ".zip"));
                    }
                }
                log.debug("Download ready");
                downloadReady.set(true);
            }
        });

        CB.postAsync(new NamedRunnable("Extract Zips") {
            @Override
            public void run() {
                final UnZip unZip = new UnZip();
                while (!downloadReady.get() || downloadedFiles.size > 0) {

                    if (downloadedFiles.size > 0) {
                        FileHandle zipFile = downloadedFiles.pop();
                        FileHandle targetFolder = null;
                        try {
                            targetFolder = unZip.extractFolder(zipFile);
                        } catch (IOException e) {
                            log.error("Extract zipFile", e);
                        }
                        if (targetFolder != null) {
                            extractedfolder.add(targetFolder);

                            //delete source zip file
                            zipFile.delete();

                            CB.postOnGlThread(new NamedRunnable("Update progress label") {
                                @Override
                                public void run() {
                                    // update progress Label
                                    int progressValue = extractedPQs.incrementAndGet();
                                    if (extractLabel != null) extractLabel.setText(Translation.get("extracted",
                                            Integer.toString(progressValue),
                                            Integer.toString(downloadPqCount.get())));
                                    if (extractProgress != null) extractProgress.setValue(progressValue);
                                    CB.requestRendering();
                                }
                            });
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                log.debug("Extract ready");
                extractReady.set(true);
            }
        });

        final Array<String> mysterieCodes = new Array<>();
        final AtomicInteger readyImportedLogs = new AtomicInteger();
        final AtomicInteger readyImportedWaypoints = new AtomicInteger();

        CB.postAsync(new NamedRunnable("Import extracted gpx files") {

            final ImportHandler importHandler = new ImportHandler() {
                @Override
                public void incrementCaches(String mysteryGcCode) {
                    CB.postOnGlThread(new NamedRunnable("Update progress label") {
                        @Override
                        public void run() {
                            // update progress Label
                            int progressValue = readyImportedCaches.incrementAndGet();
                            if (importCache.get() < 100 || progressValue % 10 == 0) {
                                if (importLabel != null) importLabel.setText(Translation.get("imported",
                                        Integer.toString(progressValue),
                                        Integer.toString(importCache.get())));
                                if (importProgress != null) importProgress.setValue(progressValue);
                                CB.requestRendering();
                            }
                        }
                    });
                }

                @Override
                public void incrementWaypoints() {
                    readyImportedWaypoints.incrementAndGet();
                }

                @Override
                public void incrementLogs() {
                    readyImportedLogs.incrementAndGet();
                }
            };
            final GroundspeakGpxStreamImporter importer = new GroundspeakGpxStreamImporter(database, importHandler);


            @Override
            public void run() {
                while (!extractReady.get() || extractedfolder.size > 0) {
                    if (extractedfolder.size > 0) {
                        FileHandle gpxFolder = extractedfolder.pop();

                        if (gpxFolder.exists() && gpxFolder.isDirectory()) {
                            for (FileHandle gpxFile : gpxFolder.list(".gpx")) {
                                importer.doImport(gpxFile);
                            }
                            gpxFolder.deleteDirectory();
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                log.debug("import ready");
                importReady.set(true);
            }
        });

        CB.postAsync(new NamedRunnable("Check corrected Coordinates") {
            @Override
            public void run() {
                while (!importReady.get() || mysterieCodes.size > 0) {
                    //TODO use https://api.groundspeak.com/LiveV6/geocaching.svc/help/operations/GetUserWaypoints
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                correctReady.set(true);
            }
        });

        CB.postAsync(new NamedRunnable("Wait for finish") {
            @Override
            public void run() {
                while (!correctReady.get()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("PocketQuery ready imported");
                database.endTransactionExclusive(TRANSACTION_ID);
                if (readyHandler != null) {
                    readyHandler.ready(readyImportedCaches.get(), readyImportedWaypoints.get(), readyImportedLogs.get());
                }


                //write last import

                // get all exist PQ entries
                GdxSqliteCursor cursor = database.rawQuery("SELECT * FROM PocketQueries");
                ObjectMap<String, Integer> existMap = new ObjectMap<>();
                if (cursor != null) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        existMap.put(cursor.getString(1), cursor.getInt(0));
                        cursor.next();
                    }
                }

                int idx = existMap.size + 1;
                String dateStringNow = Database.cbDbFormat.format(new Date());
                for (ListViewItemInterface item : selectedItems) {
                    String pqName = ((PqListItem) item).getPocketQuery().name;

                    if (pqName != null && existMap.containsKey(pqName)) {
                        database.execSQL("UPDATE  PocketQueries SET(CreationTimeOfPQ) VALUES('" + dateStringNow + "') WHERE id=" + existMap.get(pqName));
                        log.debug("Update PQ import '{}' to {}", pqName, dateStringNow);
                    } else {
                        database.execSQL("INSERT INTO PocketQueries (Id, PQName, CreationTimeOfPQ) VALUES('" + idx++ + "','" + pqName + "','" + dateStringNow + "')");
                        log.debug("Insert PQ import '{}' to {}", pqName, dateStringNow);
                    }
                }
            }
        });
    }
}
