/*
 * Copyright (C) 2014-2020 team-cachebox.de
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
package de.longri.cachebox3.gui.menu.menuBtn1.contextmenus;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.activities.SelectDB_Activity;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Action_SelectDB_Dialog extends AbstractAction {
    final static Logger log = LoggerFactory.getLogger(Action_SelectDB_Dialog.class);
    private final ViewMode viewMode;

    public Action_SelectDB_Dialog(ViewMode viewMode) {
        super("manageDB", MenuID.AID_SHOW_SELECT_DB_DIALOG);
        this.viewMode = viewMode;
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.manageDB;
    }

    @Override
    public void execute() {
        Gdx.app.postRunnable(() -> {
            final SelectDB_Activity selectDBDialog = new SelectDB_Activity(this::returnFromSelectDB, Action_SelectDB_Dialog.this.viewMode == ViewMode.FORCE_SHOW);
            selectDBDialog.show();
        });
    }

    private void returnFromSelectDB() {
        log.debug("\r\nSwitch DB");
        CB.postAsync(new NamedRunnable("Return from SelectDB") {
            @Override
            public void run() {
                loadSelectedDB();
            }
        });
    }

    public void loadSelectedDB() {
        if (Database.Data != null) {
            if (Database.Data.cacheList != null) Database.Data.cacheList.clear();
            if (Database.Data.isStarted()) Database.Data.close();
        }

        FileHandle fileHandle = Gdx.files.absolute(CB.WorkPath + "/" + Config.DatabaseName.getValue());
        if (!fileHandle.exists() || fileHandle.isDirectory()) {
            try {
                log.debug("can't open Database! File not exist : \n{}", fileHandle.file().getCanonicalPath());
            } catch (IOException e) {
                log.warn("can't open Database! File not exist : \n{}", fileHandle.file().getAbsolutePath());
            }

            // try to create

        }


        try {
            Database.Data.startUp(fileHandle);
        } catch (SQLiteGdxException e) {
            log.error("can't open DB", e);
            return;
        }

        CB.postAsync(new NamedRunnable("Action_SelectDB_Dialog") {
            @Override
            public void run() {
                Database.Data.cacheList.setUnfilteredSize(Database.Data.getCacheCountOnThisDB());
                log.debug("Call loadFilteredCacheList()");
                CB.loadFilteredCacheList(null);
            }
        });

        //restore MapState
        Config.readFromDB(true);
    }

    public enum ViewMode {
        FORCE_SHOW, ASK
    }

}
