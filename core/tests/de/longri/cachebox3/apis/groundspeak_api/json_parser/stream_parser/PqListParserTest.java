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
package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.GetPocketQueryList;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 26.03.2018.
 */
class PqListParserTest {

    @Test
    void parsePqList() throws FileNotFoundException {
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/GetPqListResult.json");
        PqListParser parser = new PqListParser(null);

        Array<PocketQuery> pqList = new Array<>();

        ApiResultState result = parser.parsePqList(stream, pqList);

        assertThat("Result must be ApiResultState.IO", result == ApiResultState.IO);
        assertThat("PocketQuery List count must be 6", pqList.size == 6);

        for (int i = 0; i < pqList.size; i++) {
            PocketQuery pq = pqList.get(i);
            switch (i) {
                case 0:
                    assertThat("wrong value", pq.guid.equals("a1244aab-f585-4648-8dea-f9b979b533be"));
                    assertThat("wrong value", pq.name.equals("Birkenwerder"));
                    assertThat("wrong value", pq.lastGenerated.equals(new Date(1521969736000L)));
                    assertThat("wrong value", pq.downloadAvailable == false);
                    assertThat("wrong value", pq.sizeMB == 1899174/ 1048576.0);
                    assertThat("wrong value", pq.cacheCount == 1000);
                    break;
                case 1:
                    assertThat("wrong value", pq.guid.equals("85c3888c-d097-4055-b02e-0f182d71fa9e"));
                    assertThat("wrong value", pq.name.equals("H&#246;now"));
                    assertThat("wrong value", pq.lastGenerated.equals(new Date(1521971482000L)));
                    assertThat("wrong value", pq.downloadAvailable == true);
                    assertThat("wrong value", pq.sizeMB == 1753293/ 1048576.0);
                    assertThat("wrong value", pq.cacheCount == 1000);
                    break;
                case 5:
                    assertThat("wrong value", pq.guid.equals("e6071d91-9425-4828-94f2-543c7fafc899"));
                    assertThat("wrong value", pq.name.equals("Wandlitz"));
                    assertThat("wrong value", pq.lastGenerated.equals(new Date(1522049839000L)));
                    assertThat("wrong value", pq.downloadAvailable == true);
                    assertThat("wrong value", pq.sizeMB == 1847508/ 1048576.0);
                    assertThat("wrong value", pq.cacheCount == 1000);
            }
        }
    }
}