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
import de.longri.cachebox3.gui.actions.show_activities.Action_Show_SelectDB_Dialog;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 28.08.16.
 */
public class LoadDbTask extends AbstractInitTask {
    final static Logger log = LoggerFactory.getLogger(LoadDbTask.class);

    public LoadDbTask(String name) {
        super(name);
    }

    @Override
    public void runnable() {

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // initial DB
                Database.Data = new Database(Database.DatabaseType.CacheBox);

                Action_Show_SelectDB_Dialog selectDbDialog = new Action_Show_SelectDB_Dialog(Action_Show_SelectDB_Dialog.ViewMode.FORCE_SHOW);

                // search number of DB3 files
                FileList fileList = null;
                try {
                    fileList = new FileList(CB.WorkPath, "DB3");
                } catch (Exception ex) {
                    log.error("search number of DB3 files", ex);
                }
                if ((fileList.size > 1) && Config.MultiDBAsk.getValue()) {
                    selectDbDialog.execute();
                    //TODO wait for return;
                } else {
                    if (fileList.size == 0) {
                        Config.DatabaseName.setValue("cachebox.db3");
                    } else {
                        Config.DatabaseName.setValue(Utils.GetFileName(fileList.get(0).getName()));
                    }
                    EventHandler.fire(new IncrementProgressEvent(10,"load db"));
                    selectDbDialog.loadSelectedDB();
                }
            }
        });

    }

    @Override
    public int getProgressMax() {
        return 10;
    }
}
