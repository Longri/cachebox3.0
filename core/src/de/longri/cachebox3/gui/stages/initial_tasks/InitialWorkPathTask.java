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
package de.longri.cachebox3.gui.stages.initial_tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;

import java.io.File;
import java.io.IOException;

/**
 * Created by Longri on 02.08.16.
 */
public class InitialWorkPathTask extends AbstractInitTask {
    final static Logger log = LoggerFactory.getLogger(InitialWorkPathTask.class);

    public InitialWorkPathTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void runable() {

        CB.WorkPath = PlatformConnector.getWorkPath();

        log.info("WorkPath set to :" + CB.WorkPath);

        boolean nomedia = CB.platform == CB.Platform.ANDROID;

        // initial Database on user path
        ini_Dir(CB.WorkPath + "/user", false);

        try {
            FileHandle configFileHandle = Gdx.files.absolute(CB.WorkPath + "/user/config.db3");
            Database.Settings = new Database(Database.DatabaseType.Settings);
            Database.Settings.StartUp(configFileHandle);
        } catch (SQLiteGdxException e) {
            log.error("can't open config.db3", e);
        }


        try {
            FileHandle fieldNotesFileHandle = Gdx.files.absolute(CB.WorkPath + "/user/fieldNotes.db3");
            Database.FieldNotes = new Database(Database.DatabaseType.FieldNotes);
            Database.FieldNotes.StartUp(fieldNotesFileHandle);
        } catch (SQLiteGdxException e) {
            log.error("can't open fieldNotes.db3", e);
        }


        //load settings
        Config.ReadFromDB();


        log.debug("ini_Dirs");
        ini_Dir(Config.PocketQueryFolder.getValue(), false);
        ini_Dir(Config.TileCacheFolder.getValue(), nomedia);
        ini_Dir(Config.TrackFolder.getValue(), false);
        ini_Dir(Config.UserImageFolder.getValue(), nomedia);
        ini_Dir(CB.WorkPath + "/repository", nomedia);
        ini_Dir(CB.WorkPath + "/repositories", nomedia);
        ini_Dir(CB.WorkPath + "/skins", nomedia);
        ini_Dir(CB.WorkPath + "/data", nomedia);
        ini_Dir(CB.WorkPath + "/user/temp", nomedia);
        ini_Dir(Config.DescriptionImageFolder.getValue(), nomedia);
        ini_Dir(Config.MapPackFolder.getValue(), nomedia);
        ini_Dir(Config.SpoilerFolder.getValue(), nomedia);


    }

    private void ini_Dir(String folder, boolean withNoMedia) {
        FileHandle ff = Gdx.files.absolute(folder);
        if (!ff.exists()) {
            ff.mkdirs();
        }


        if (!withNoMedia) return;
        // prevent mediascanner to parse all the images in this folder
        File nomedia = new File(ff.file(), ".nomedia");
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }


}
