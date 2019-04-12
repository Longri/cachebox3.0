

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.settings.types.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.SQLiteGdxException;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import java.util.Calendar;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by longri on 30.06.17.
 */
public class SettingBaseTest {

    static {
        TestUtils.initialGdx();
    }

    private static final SettingBool testBool = (SettingBool) Config.settingsList.addSetting(new SettingBool("testBool"
            , SettingCategory.RememberAsk, SettingMode.Normal, false, SettingStoreType.Global,
            SettingUsage.ACB, true));


    private static final SettingsBlob testBlob = (SettingsBlob) Config.settingsList.addSetting(new SettingsBlob("testBlob"
            , SettingCategory.RememberAsk, SettingMode.Normal, SettingStoreType.Global,
            SettingUsage.ACB, false, new byte[]{}));


    @Test
    public void desiredTimeTest() throws SQLiteGdxException, PlatformAssertionError {


        byte[] testByteArray = new byte[]{12, 127, -127, 7, 32};


        //Create new test config.db
        FileHandle configFileHandle = Gdx.files.local("testConfig.db3");
        if (configFileHandle.exists()) configFileHandle.delete();

        // configs are stored on config db and DATA, create a DATA DB
        FileHandle dataFileHandle = Gdx.files.local("testData.db3");
        if (dataFileHandle.exists()) dataFileHandle.delete();

        Database.Data = new Database(Database.DatabaseType.CacheBox3);
        Database.Data.startUp(dataFileHandle);

        Database.Settings = new Database(Database.DatabaseType.Settings);
        Database.Settings.startUp(configFileHandle);

        assertThat("Database file must exist", configFileHandle.exists());
        assertThat("", !testBool.getValue());
        assertThat("", testBlob.getValue().length == 0);

        assertThat("Setting must desired", testBool.isExpired());

        testBool.setValue(true);
        testBlob.setValue(testByteArray);
        assertThat("", testBool.getValue());
        assertThat("", testBlob.getValue().length == testByteArray.length);
        assertThat("Setting must not desired", testBool.isExpired());

        long now = Calendar.getInstance().getTimeInMillis();
        long desired = now + 1000;// future 1 sec

        testBool.setExpiredTime(desired);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat("Setting must not desired", !testBool.isExpired());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat("Setting must desired", testBool.isExpired());


        now = Calendar.getInstance().getTimeInMillis();
        desired = now + 1000;// future 10 sec

        testBool.setExpiredTime(desired);

        // close config DB and reload
        Config.AcceptChanges();
        Config.AcceptChanges();
        Config.AcceptChanges();
        //wait 5sec
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Database.Settings.close();

        Database.Settings = new Database(Database.DatabaseType.Settings);
        Database.Settings.startUp(configFileHandle);

        Config.readFromDB(true);
        assertThat("", testBool.getValue());
        assertThat("Setting must not desired", !testBool.isExpired());


        //change a setting and check if stored after reload
        Config.showGestureHelp.setValue(false);


        // close config DB and reload
        Config.AcceptChanges();

        //wait 5sec
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Database.Settings.close();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Database.Settings = new Database(Database.DatabaseType.Settings);
        Database.Settings.startUp(configFileHandle);

        Config.readFromDB(true);
        assertThat("", testBool.getValue());
        assertThat("Setting must desired", testBool.isExpired());
        assertThat("", testBlob.getValue().length == testByteArray.length);
        assertThat("", testBlob.getValue()[0] == testByteArray[0]);
        assertThat("", testBlob.getValue()[1] == testByteArray[1]);
        assertThat("", testBlob.getValue()[2] == testByteArray[2]);
        assertThat("", testBlob.getValue()[3] == testByteArray[3]);

        //check changed setting
        assertThat("Setting must changed to false", !Config.showGestureHelp.getValue());


        //clean up test file's
        configFileHandle.delete();
        dataFileHandle.delete();

    }

}
