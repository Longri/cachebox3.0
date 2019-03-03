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
package de.longri.cachebox3.utils.json_parser;

import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 08.10.2017
 */
class DraftUploadResultParserTest {


    @Test
    void result() {
        try {
            String result = TestUtils.getResourceRequestString("testsResources/UploadFieldNoteResultOk.json", null);
            boolean resultOk = DraftUploadResultParser.result(result);

            assertThat("Result must be OK", resultOk);

        } catch (Exception e) {
            e.printStackTrace();
            assertThat("Exception on Test", false);
        }


    }

}