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
package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.JsonStreamParser;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by longri on 02.07.17.
 */
public class ApiLimitParser {

    final String MAXCALLSBYIPIN1MINUTE = "MaxCallsbyIPIn1Minute";

    public int parseCallsPerMinute(InputStream stream) {

        final AtomicInteger resultCalls = new AtomicInteger(-1);

        // Parse JSON Result
        final JsonStreamParser parser = new JsonStreamParser() {
            public void number(String name, long value, String stringValue) {
                super.number(name, value, stringValue);
                if (MAXCALLSBYIPIN1MINUTE.equals(name)) {
                    resultCalls.set((int) value);
                    cancel();

                }
            }
        };
        parser.parse(stream);
        return resultCalls.get();
    }
}
