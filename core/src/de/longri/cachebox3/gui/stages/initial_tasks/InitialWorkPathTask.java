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
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void runnable(WorkCallback callback) {

        CB.WorkPath = PlatformConnector.getWorkPath();

        log.info("WorkPath set to :" + CB.WorkPath);

        boolean nomedia = CanvasAdapter.platform == Platform.ANDROID;

        // initial Database on user path
        ini_Dir(CB.WorkPath + "/user", false);

        callback.taskNameChange("open/create settings DB");
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
        callback.taskNameChange("load settings");
        Config.ReadFromDB();


        log.debug("ini_Dirs");
        ini_Dir(Config.PocketQueryFolder.getValue(), false);
        ini_Dir(Config.TileCacheFolder.getValue(), nomedia);
        ini_Dir(Config.TrackFolder.getValue(), false);
        ini_Dir(Config.UserImageFolder.getValue(), nomedia);
        ini_Dir(CB.WorkPath + "/repository", nomedia);
        ini_Dir(CB.WorkPath + "/repositories", nomedia);
        FileHandle dataFileHandle = ini_Dir(CB.WorkPath + "/data", nomedia);
        ini_Dir(CB.WorkPath + "/user/temp", nomedia);
        ini_Dir(Config.DescriptionImageFolder.getValue(), nomedia);
        ini_Dir(Config.MapPackFolder.getValue(), nomedia);
        ini_Dir(Config.SpoilerFolder.getValue(), nomedia);


        //copy attributes*.png to data folder
        //this png files are used on description view, as Html image

        callback.taskNameChange("generate attributes images");
        FileHandle attFileHandle = dataFileHandle.child("Attributes");
        attFileHandle.mkdirs();

        FileHandle[] assetAttributes = getAttributesFiles();
        for (FileHandle fh : assetAttributes) {
            String name = fh.name();
            FileHandle target = attFileHandle.child(name);
            if (!target.exists()) fh.copyTo(target);
        }


//DEBUG SKIN set for debug
//        Config.daySkinName.setValue("testDay");
        callback.taskNameChange("load skin");
        Config.daySkinName.loadDefault();

    }

    private FileHandle[] getAttributesFiles() {
        // Cannot list a classpath directory
        // so we hardcoded the files
        FileHandle[] files = new FileHandle[]{ //
                Gdx.files.internal("attributes/att_1_0.png"),
                Gdx.files.internal("attributes/att_1_1.png"),
                Gdx.files.internal("attributes/att_2_1.png"),
                Gdx.files.internal("attributes/att_3_1.png"),
                Gdx.files.internal("attributes/att_4_1.png"),
                Gdx.files.internal("attributes/att_5_1.png"),
                Gdx.files.internal("attributes/att_6_0.png"),
                Gdx.files.internal("attributes/att_6_1.png"),
                Gdx.files.internal("attributes/att_7_0.png"),
                Gdx.files.internal("attributes/att_7_1.png"),
                Gdx.files.internal("attributes/att_8_0.png"),
                Gdx.files.internal("attributes/att_8_1.png"),
                Gdx.files.internal("attributes/att_9_0.png"),
                Gdx.files.internal("attributes/att_9_1.png"),
                Gdx.files.internal("attributes/att_10_0.png"),
                Gdx.files.internal("attributes/att_10_1.png"),
                Gdx.files.internal("attributes/att_11_1.png"),
                Gdx.files.internal("attributes/att_12_1.png"),
                Gdx.files.internal("attributes/att_13_0.png"),
                Gdx.files.internal("attributes/att_13_1.png"),
                Gdx.files.internal("attributes/att_14_0.png"),
                Gdx.files.internal("attributes/att_14_1.png"),
                Gdx.files.internal("attributes/att_15_0.png"),
                Gdx.files.internal("attributes/att_15_1.png"),
                Gdx.files.internal("attributes/att_16_0.png"),
                Gdx.files.internal("attributes/att_16_1.png"),
                Gdx.files.internal("attributes/att_17_0.png"),
                Gdx.files.internal("attributes/att_17_1.png"),
                Gdx.files.internal("attributes/att_18_1.png"),
                Gdx.files.internal("attributes/att_19_1.png"),
                Gdx.files.internal("attributes/att_20_1.png"),
                Gdx.files.internal("attributes/att_21_1.png"),
                Gdx.files.internal("attributes/att_22_1.png"),
                Gdx.files.internal("attributes/att_23_1.png"),
                Gdx.files.internal("attributes/att_24_0.png"),
                Gdx.files.internal("attributes/att_24_1.png"),
                Gdx.files.internal("attributes/att_25_0.png"),
                Gdx.files.internal("attributes/att_25_1.png"),
                Gdx.files.internal("attributes/att_26_1.png"),
                Gdx.files.internal("attributes/att_27_0.png"),
                Gdx.files.internal("attributes/att_27_1.png"),
                Gdx.files.internal("attributes/att_28_0.png"),
                Gdx.files.internal("attributes/att_28_1.png"),
                Gdx.files.internal("attributes/att_29_0.png"),
                Gdx.files.internal("attributes/att_29_1.png"),
                Gdx.files.internal("attributes/att_30_0.png"),
                Gdx.files.internal("attributes/att_30_1.png"),
                Gdx.files.internal("attributes/att_31_0.png"),
                Gdx.files.internal("attributes/att_31_1.png"),
                Gdx.files.internal("attributes/att_32_0.png"),
                Gdx.files.internal("attributes/att_32_1.png"),
                Gdx.files.internal("attributes/att_33_0.png"),
                Gdx.files.internal("attributes/att_33_1.png"),
                Gdx.files.internal("attributes/att_34_0.png"),
                Gdx.files.internal("attributes/att_34_1.png"),
                Gdx.files.internal("attributes/att_35_0.png"),
                Gdx.files.internal("attributes/att_35_1.png"),
                Gdx.files.internal("attributes/att_36_0.png"),
                Gdx.files.internal("attributes/att_36_1.png"),
                Gdx.files.internal("attributes/att_37_0.png"),
                Gdx.files.internal("attributes/att_37_1.png"),
                Gdx.files.internal("attributes/att_38_0.png"),
                Gdx.files.internal("attributes/att_38_1.png"),
                Gdx.files.internal("attributes/att_39_1.png"),
                Gdx.files.internal("attributes/att_40_0.png"),
                Gdx.files.internal("attributes/att_40_1.png"),
                Gdx.files.internal("attributes/att_41_0.png"),
                Gdx.files.internal("attributes/att_41_1.png"),
                Gdx.files.internal("attributes/att_42_1.png"),
                Gdx.files.internal("attributes/att_43_1.png"),
                Gdx.files.internal("attributes/att_44_1.png"),
                Gdx.files.internal("attributes/att_45_0.png"),
                Gdx.files.internal("attributes/att_45_1.png"),
                Gdx.files.internal("attributes/att_46_0.png"),
                Gdx.files.internal("attributes/att_46_1.png"),
                Gdx.files.internal("attributes/att_47_0.png"),
                Gdx.files.internal("attributes/att_47_1.png"),
                Gdx.files.internal("attributes/att_48_1.png"),
                Gdx.files.internal("attributes/att_49_1.png"),
                Gdx.files.internal("attributes/att_50_1.png"),
                Gdx.files.internal("attributes/att_51_1.png"),
                Gdx.files.internal("attributes/att_52_0.png"),
                Gdx.files.internal("attributes/att_52_1.png"),
                Gdx.files.internal("attributes/att_53_0.png"),
                Gdx.files.internal("attributes/att_53_1.png"),
                Gdx.files.internal("attributes/att_54_0.png"),
                Gdx.files.internal("attributes/att_54_1.png"),
                Gdx.files.internal("attributes/att_55_0.png"),
                Gdx.files.internal("attributes/att_55_1.png"),
                Gdx.files.internal("attributes/att_56_0.png"),
                Gdx.files.internal("attributes/att_56_1.png"),
                Gdx.files.internal("attributes/att_57_0.png"),
                Gdx.files.internal("attributes/att_57_1.png"),
                Gdx.files.internal("attributes/att_58_0.png"),
                Gdx.files.internal("attributes/att_58_1.png"),
                Gdx.files.internal("attributes/att_59_0.png"),
                Gdx.files.internal("attributes/att_59_1.png"),
                Gdx.files.internal("attributes/att_60_1.png"),
                Gdx.files.internal("attributes/att_61_0.png"),
                Gdx.files.internal("attributes/att_61_1.png"),
                Gdx.files.internal("attributes/att_62_0.png"),
                Gdx.files.internal("attributes/att_62_1.png"),
                Gdx.files.internal("attributes/att_63_0.png"),
                Gdx.files.internal("attributes/att_63_1.png"),
                Gdx.files.internal("attributes/att_64_0.png"),
                Gdx.files.internal("attributes/att_64_1.png"),
                Gdx.files.internal("attributes/att_65_0.png"),
                Gdx.files.internal("attributes/att_65_1.png"),
                Gdx.files.internal("attributes/att_66_0.png"),
                Gdx.files.internal("attributes/att_66_1.png"),
        };
        return files;
    }

    private FileHandle ini_Dir(String folder, boolean withNoMedia) {
        FileHandle ff = Gdx.files.absolute(folder);
        if (!ff.exists()) {
            ff.mkdirs();
        }


        if (!withNoMedia) return ff;
        // prevent mediascanner to parse all the images in this folder
        File nomedia = new File(ff.file(), ".nomedia");
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return ff;
    }


}
