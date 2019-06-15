/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.stages.initial_tasks;

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.gui.actions.show_activities.Action_SelectDB_Dialog;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.FileList;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 28.08.16.
 */
public class LoadDbTask extends AbstractInitTask {
    final static Logger log = LoggerFactory.getLogger(LoadDbTask.class);
    final BitStore instanceStateReader;

    public LoadDbTask(String name, BitStore instanceStateReader) {
        super(name);
        this.instanceStateReader = instanceStateReader;
    }

    @Override
    public void runnable() {

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // initial DB
                Database.Data = new Database(Database.DatabaseType.CacheBox3);

                final Action_SelectDB_Dialog selectDbDialog = new Action_SelectDB_Dialog(Action_SelectDB_Dialog.ViewMode.FORCE_SHOW);

                // search number of DB3 files
                final FileList fileList = new FileList(CB.WorkPath, "DB3");

                //restore saved instance state
                String dbName = null;
                if (instanceStateReader != null) {
                    try {
                        // get Name of last DB
                        dbName = instanceStateReader.readString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                final String finalDbName = dbName;

                if (finalDbName == null && (fileList.size > 1) && Config.MultiDBAsk.getValue()) {
                    CB.postAsync(new NamedRunnable("LoadDbTask") {
                        @Override
                        public void run() {
                            //wait for initial viewmanager

                            while (CB.viewmanager == null) {
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    selectDbDialog.execute();
                                }
                            });
                        }
                    });
                } else {
                    CB.postAsync(new NamedRunnable("LoadDbTask") {
                        @Override
                        public void run() {
                            //wait for initial viewmanager
                            while (CB.viewmanager == null) {
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    if (finalDbName != null) {
                                        Config.DatabaseName.setValue(finalDbName);
                                    } else if (fileList.size == 0) {
                                        Config.DatabaseName.setValue("cachebox.db3");
                                    } else {
                                        Config.DatabaseName.setValue(Utils.getFileName(fileList.get(0).getName()));
                                    }
                                    EventHandler.fire(new IncrementProgressEvent(10, "load db"));
                                    selectDbDialog.loadSelectedDB();
                                }
                            });
                        }
                    });
                }
            }
        });

    }

    @Override
    public int getProgressMax() {
        return 10;
    }
}
