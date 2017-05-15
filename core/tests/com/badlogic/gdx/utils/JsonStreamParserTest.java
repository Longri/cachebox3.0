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
package com.badlogic.gdx.utils;

import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.*;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 18.04.2017.
 */
class JsonStreamParserTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);
    final static org.slf4j.Logger log = LoggerFactory.getLogger(JsonStreamParserTest.class);

    @Test
    void parse() throws FileNotFoundException {

        String[] testFiles = new String[]{
                "testsResources/Error-with-parse-value-near-OwnerActionable.txt",
                "testsResources/LongValueString.json",
                "testsResources/ArrayTest.json",
                "testsResources/GetYourUserProfile_request.json",
                "testsResources/GetYourUserProfile_request.txt",
                "testsResources/JsonArrayTest.json",
                "testsResources/JsonArrayTestExtended.json",
                "testsResources/SearchGc_request.txt",
                "testsResources/SearchGc_request.json",
                "testsResources/SearchGc_result.json",
                "testsResources/SearchGc_result.txt",
                "testsResources/SearchGcCoordinate_request.txt",
                "testsResources/SearchGcOwner_request.txt",
                "testsResources/SearchGcOwner_result.json",
                "testsResources/SearchGcOwner_result.txt",
                "testsResources/5378.txt",
                "testsResources/5379.txt",
                "testsResources/88065379.txt",
                "testsResources/88065380.txt",
                "testsResources/88075378.txt",
                "testsResources/88075379.txt",
                "testsResources/88075380.txt",
                "testsResources/88085378.txt",
                "testsResources/88085379.txt",
                "testsResources/88085380.txt",
        };

        for (String path : testFiles) {
            if (path == null || path.isEmpty()) continue;

            log.debug(" ---Parse file " + path);
            StringBuilder sb2 = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            parse(path, sb, sb2);
            assertEquals(sb.toString(), sb2.toString());
            log.debug(" --------------------------- ");
        }

    }

    private void parse(String file, final StringBuilder sb, final StringBuilder sb2) throws FileNotFoundException {
        long start = System.currentTimeMillis();

        InputStream stream = TestUtils.getResourceRequestStream(file);
        long dummyLength = 1;
        new GdxJsonReader() {
            public void startArray(String name) {
                super.startArray(name);
                sb.appendLine("startArray " + name);
            }

            public void endArray(String name) {
                super.endArray(name);
                sb.appendLine("endArray " + name);
            }

            public void startObject(String name) {
                super.startObject(name);
                sb.appendLine("startObject " + name);
            }

            public void pop() {
                super.pop();
                sb.appendLine("pop ");
            }

            public void string(String name, String value) {
                super.string(name, value);
                sb.appendLine("string " + name + "  " + value);
            }

            public void number(String name, double value, String stringValue) {
                super.number(name, value, stringValue);
                sb.appendLine("number(Double) " + name + "  " + value);
            }

            public void number(String name, long value, String stringValue) {
                super.number(name, value, stringValue);
                sb.appendLine("number(Long) " + name + "  " + value);
            }

            public void bool(String name, boolean value) {
                super.bool(name, value);
                sb.appendLine("bool " + name + "  " + value);
            }
        }.parse(stream, dummyLength);

        log.debug("Parse time JsonParser: {}", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        stream = TestUtils.getResourceRequestStream(file);

        new JsonStreamParser() {
            public void startArray(String name) {
                sb2.appendLine("startArray " + name);
            }

            public void endArray(String name) {
               // will not call on original JasonParser sb2.appendLine("endArray " + name);
            }

            public void startObject(String name) {
                sb2.appendLine("startObject " + name);
            }

            public void pop() {
                sb2.appendLine("pop ");
            }

            public void string(String name, String value) {
                sb2.appendLine("string " + name + "  " + value);
            }

            public void number(String name, double value, String stringValue) {
                sb2.appendLine("number(Double) " + name + "  " + value);
            }

            public void number(String name, long value, String stringValue) {
                sb2.appendLine("number(Long) " + name + "  " + value);
            }

            public void bool(String name, boolean value) {
                sb2.appendLine("bool " + name + "  " + value);
            }
        }.parse(stream, dummyLength);

        log.debug("Parse time JsonStreamParser: {}", System.currentTimeMillis() - start);
    }


    @Test
    void handleValue() throws FileNotFoundException {
        String valueString = "\"Mit Team Kreuz haben wir hier eine kleine Berlin-Heimwegrunde gemacht. Wir konnten uns schnell und ohne Muggelaufmerksamkeit loggen. Das Rätsel löste eine Cacherfreundin.\\u000d\\u000a\\u000d\\u000aDanke und Happy Hunting sagen die\\u000d\\u000a\\u000d\\u000a............\\\\|\\/............\\u000d\\u000a..........@@............\\u000d\\u000a...o00.(_°_).00o...\\u000d\\u000a..CiAZuCCHiNi's..\"";
        final String[] result = new String[]{""};

        JsonStreamParser parser = new JsonStreamParser() {
            @Override
            public void string(String name, String value) {
                result[0] = value;
            }

        };

        parser.handleValue(null, valueString);

        String expected = "Mit Team Kreuz haben wir hier eine kleine Berlin-Heimwegrunde gemacht. Wir konnten uns schnell und ohne Muggelaufmerksamkeit loggen. Das Rätsel löste eine Cacherfreundin.\r\n" +
                "\r\n" +
                "Danke und Happy Hunting sagen die\r\n" +
                "\r\n" +
                "............\\|/............\r\n" +
                "..........@@............\r\n" +
                "...o00.(_°_).00o...\r\n" +
                "..CiAZuCCHiNi's..";

        assertEquals(expected, result[0]);

    }

}