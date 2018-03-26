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
import com.badlogic.gdx.utils.JsonStreamParser;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;

import java.io.InputStream;

/**
 * Created by Longri on 26.03.2018.
 */
public class PqListParser {

    public ApiResultState parsePqList(InputStream stream, Array<PocketQuery.PQ> pqList) {
        ApiResultState resultState = ApiResultState.IO;

        // Parse JSON Result
        final JsonStreamParser parser = new JsonStreamParser() {
            public void number(String name, long value, String stringValue) {
                super.number(name, value, stringValue);

            }
        };
        parser.parse(stream);

        return resultState;
    }

}
