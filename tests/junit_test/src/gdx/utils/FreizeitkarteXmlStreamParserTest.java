/*
 * Copyright (C) 2019 team-cachebox.de
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
package gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.gui.menu.menuBtn3.Action_MapDownload;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Language;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 2019-07-04.
 */
public class FreizeitkarteXmlStreamParserTest {

    static {
        TestUtils.initialGdx();
    }

    @Test
    void parse_de() {

        Language altLang = Config.localization.getEnumValue();
        Config.localization.setEnumValue(Language.de);

        final FileHandle testFile = TestUtils.getResourceFileHandle("testsResources/freizeit-karte.xml", true);
        Action_MapDownload action_mapDownload = new Action_MapDownload();
        Array<Action_MapDownload.MapRepositoryInfo> list = action_mapDownload.getMapInfoList(testFile.read());

        assertThat("list must not NULL", list != null);
        assertThat("list count must be 85", list.size == 85);

        Action_MapDownload.MapRepositoryInfo info = list.get(0);

        assertEquals("Freizeitkarte_BADEN-WUERTTEMBERG", info.name, "Name must be 'Freizeitkarte_BADEN-WUERTTEMBERG'");
        assertEquals("Freizeitkarte Baden-Württemberg", info.description, "Description must be 'Freizeitkarte Baden-Württemberg'");
        assertEquals("http://download.freizeitkarte-osm.de/android/1906/freizeitkarte_baden-wuerttemberg.map.zip", info.url, "Url must be 'http://download.freizeitkarte-osm.de/android/1906/freizeitkarte_baden-wuerttemberg.map.zip'");
        assertEquals(274632406, info.size, "Size must be '274632406'");
        // assertEquals("abff36c09edabdfe74c77a419b385b9b", info.md5, "Md5 must be 'abff36c09edabdfe74c77a419b385b9b'");

        Config.localization.setEnumValue(altLang);

    }

    @Test
    void parse_en() {

        Language altLang = Config.localization.getEnumValue();
        Config.localization.setEnumValue(Language.en_GB);

        final FileHandle testFile = TestUtils.getResourceFileHandle("testsResources/freizeit-karte.xml", true);
        Action_MapDownload action_mapDownload = new Action_MapDownload();
        Array<Action_MapDownload.MapRepositoryInfo> list = action_mapDownload.getMapInfoList(testFile.read());

        assertThat("list must not NULL", list != null);
        assertThat("list count must be 85", list.size == 85);

        Action_MapDownload.MapRepositoryInfo info = list.get(0);

        assertEquals("Freizeitkarte_BADEN-WUERTTEMBERG", info.name, "Name must be 'Freizeitkarte_BADEN-WUERTTEMBERG'");
        assertEquals("Freizeitkarte Baden-Wuerttemberg", info.description, "Description must be 'Freizeitkarte Baden-Wuerttemberg'");
        assertEquals("http://download.freizeitkarte-osm.de/android/1906/freizeitkarte_baden-wuerttemberg.map.zip", info.url, "Url must be 'http://download.freizeitkarte-osm.de/android/1906/freizeitkarte_baden-wuerttemberg.map.zip'");
        assertEquals(274632406, info.size, "Size must be '274632406'");
        // assertEquals("abff36c09edabdfe74c77a419b385b9b", info.MD5, "Md5 must be 'abff36c09edabdfe74c77a419b385b9b'");

        Config.localization.setEnumValue(altLang);

    }

}
