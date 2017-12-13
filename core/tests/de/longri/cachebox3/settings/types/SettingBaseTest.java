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
package de.longri.cachebox3.settings.types;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by longri on 30.06.17.
 */
class SettingBaseTest {

    static {
        TestUtils.initialGdx();
        new SettingsList();
    }

    public static final SettingBool testBool = (SettingBool) SettingsList.addSetting(new SettingBool("testBool"
            , SettingCategory.RememberAsk, SettingMode.Normal, false, SettingStoreType.Global,
            SettingUsage.ACB,true));


    @Test
    void desiredTimeTest() throws SQLiteGdxException {

        //Create new test config.db
        FileHandle configFileHandle = Gdx.files.local("testConfig.db3");
        if (configFileHandle.exists()) configFileHandle.delete();


        Database.Settings = new Database(Database.DatabaseType.Settings);
        Database.Settings.startUp(configFileHandle);

        assertThat("Database file must exist", configFileHandle.exists());
        assertThat("", !testBool.getValue());

        assertThat("Setting must desired", testBool.isExpired());

        testBool.setValue(true);
        assertThat("", testBool.getValue());
        assertThat("Setting must not desired", testBool.isExpired());

        long now = Calendar.getInstance().getTimeInMillis();
        long desired = now + 1000 ;// future 1 sec

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
         desired = now + 1000 ;// future 10 sec

        testBool.setExpiredTime(desired);

        // close config DB and reload
        Config.AcceptChanges();
        Database.Settings.close();

        Database.Settings = new Database(Database.DatabaseType.Settings);
        Database.Settings.startUp(configFileHandle);

        Config.readFromDB(false);
        assertThat("", testBool.getValue());
        assertThat("Setting must not desired", !testBool.isExpired());

        // close config DB and reload
        Config.AcceptChanges();
        Database.Settings.close();
        //wait 10sec
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Database.Settings = new Database(Database.DatabaseType.Settings);
        Database.Settings.startUp(configFileHandle);

        Config.readFromDB(false);
        assertThat("", testBool.getValue());
        assertThat("Setting must desired", testBool.isExpired());

        //clean up test file's
        configFileHandle.delete();

    }

}