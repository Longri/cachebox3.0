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
import org.slf4j.LoggerFactory;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 18.04.2017.
 */
class JsonStreamParserTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI();
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);
    final static org.slf4j.Logger log = LoggerFactory.getLogger(JsonStreamParserTest.class);

    final static String testString = "{\n" +
            "  \"Status\": {\n" +
            "    \"ImageURL\": \"http:\\/\\/www.geocaching.com\\/images\\/wpttypes\\/2.gif\",\n" +
            "    \"StatusCode\": 0,\n" +
            "    \"StatusMessage\": \"OK\",\n" +
            "    \"ExceptionDetails\": \"\",\n" +
            "    \"Warnings\": [],\n" +
            "    \"IsContainer\": false,\n" +
            "    \"IsGrandfathered\": null\n" +
            "  },\n" +
            "  \"Geocaches\": [\n" +
            "    {\n" +
            "      \"AccountID\": 137464\n" +
            "    },\n" +
            "    {\n" +
            "      \"AccountID\": 137464\n" +
            "    }\n" +
            "  ],\n" +
            "  \"PQCount\": 0\n" +
            "}";

    final static char[] testArray = testString.toCharArray();

    @Test
    void parseData() {

        final StringBuilder sb = new StringBuilder();

        JsonStreamParser jtp = new JsonStreamParser() {
            public void startArray(String name) {
                sb.append("startArray: ");
                sb.append(name);
                sb.append("\n");
            }

            public void endArray(String name) {
                sb.append("endArray: ");
                sb.append(name);
                sb.append("\n");
            }

            public void startObject(String name) {
                sb.append("startObject: ");
                sb.append(name);
                sb.append("\n");
            }

            public void pop() {
                sb.append("pop()");
                sb.append("\n");
            }

            public void string(String name, String value) {
                sb.append("StringValue: ");
                sb.append(name);
                sb.append(" = ");
                sb.append(value);
                sb.append("\n");
            }

            public void number(String name, double value, String stringValue) {
                sb.append("DoubleValue: ");
                sb.append(name);
                sb.append(" = ");
                sb.append(value);
                sb.append("\n");
            }

            public void number(String name, long value, String stringValue) {
                sb.append("longValue: ");
                sb.append(name);
                sb.append(" = ");
                sb.append(value);
                sb.append("\n");
            }

            public void bool(String name, boolean value) {
                sb.append("BoolValue: ");
                sb.append(name);
                sb.append(" = ");
                sb.append(value);
                sb.append("\n");
            }
        };

        jtp.parse(testArray);

        String expected = "startObject: null\n" +
                "startObject: Status\n" +
                "StringValue: ImageURL = http://www.geocaching.com/images/wpttypes/2.gif\n" +
                "longValue: StatusCode = 0\n" +
                "StringValue: StatusMessage = OK\n" +
                "StringValue: ExceptionDetails = \n" +
                "startArray: Warnings\n" +
                "pop()\n" +
                "endArray: Warnings\n" +
                "BoolValue: IsContainer = false\n" +
                "StringValue: IsGrandfathered = null\n" +
                "pop()\n" +
                "startArray: Geocaches\n" +
                "startObject: null\n" +
                "longValue: AccountID = 137464\n" +
                "pop()\n" +
                "startObject: null\n" +
                "longValue: AccountID = 137464\n" +
                "pop()\n" +
                "pop()\n" +
                "endArray: Geocaches\n" +
                "longValue: PQCount = 0\n" +
                "pop()\n";

        assertEquals(expected, sb.toString());

    }


    @Test
    void getName() {
        JsonStreamParser jtp = new JsonStreamParser();
        String result = jtp.getName(testArray, 4);
        assertEquals("Status", result);

        result = jtp.getName(testArray, 20);
        assertEquals("ImageURL", result);

        result = jtp.getName(testArray, 92);
        assertEquals("StatusCode", result);

        result = jtp.getName(testArray, 113);
        assertEquals("StatusMessage", result);

        result = jtp.getName(testArray, 140);
        assertEquals("ExceptionDetails", result);

        result = jtp.getName(testArray, 168);
        assertEquals("Warnings", result);
    }


    @Test
    void searchNameBefore() {
        JsonStreamParser jtp = new JsonStreamParser();
        int result = jtp.searchNameBefore(testArray, 0);
        assertEquals(-1, result);

        result = jtp.searchNameBefore(testArray, 14);
        assertEquals(4, result);

        result = jtp.searchNameBefore(testArray, 86);
        assertEquals(20, result);

        result = jtp.searchNameBefore(testArray, 107);
        assertEquals(92, result);

        result = jtp.searchNameBefore(testArray, 134);
        assertEquals(113, result);

        result = jtp.searchNameBefore(testArray, 162);
        assertEquals(140, result);

        result = jtp.searchNameBefore(testArray, 180);
        assertEquals(168, result);
    }

    @Test
    void searchPeekTest() {

        JsonStreamParser jtp = new JsonStreamParser();
        int result = jtp.searchPeek(testArray, 0);
        assertEquals(0, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(14, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(86, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(107, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(134, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(162, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(180, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(181, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(182, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(208, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(240, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(241, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(258, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(264, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(296, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(297, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(303, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(335, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(339, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(340, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(357, result);

        result = jtp.searchPeek(testArray, result + 1);
        assertEquals(-1, result);

    }


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


        String Exclude = "[Geocaches, GeocacheLogs]";

        Array<String> exEmpty = new Array<>();

        Array<String> exLogs = new Array<>();
        exLogs.add("[Geocaches, GeocacheLogs]");

        Array<String> exAttribute = new Array<>();
        exAttribute.add("[Geocaches, Attributes]");

        Array<String> multiExclude = new Array<>();
        multiExclude.add("[Geocaches, Attributes]");
        multiExclude.add("[Geocaches, GeocacheLogs]");

        Array[] excludeList = new Array[]{exEmpty, exLogs, exAttribute, multiExclude};

        for (Array exclude : excludeList) {
            for (String path : testFiles) {
                if (path == null || path.isEmpty()) continue;

                log.debug(" ---Parse file " + path + "------- EXCLUD:" + exclude);
                StringBuilder sb2 = new StringBuilder();
                StringBuilder sb = new StringBuilder();
                parse(exclude, path, sb, sb2);

                //use this for debugging if sb content to large for display on terminal
//                if (!sb.toString().contentEquals(sb2)) {
//                    assertTrue(false, "Exclude{"+exclude+"} Path:{" + path + "} is wrong!");
//                }
                assertEquals(sb.toString(), sb2.toString());
                log.debug(" --------------------------- ");
            }
        }


    }

    private void parse(final Array<String> exclude, String file, final StringBuilder sb, final StringBuilder sb2) throws FileNotFoundException {
        long start = System.currentTimeMillis();

        InputStream stream = TestUtils.getResourceRequestStream(file);
        long dummyLength = 1;

        final Array<String> ex = new Array<>();
        final AtomicBoolean isExclude = new AtomicBoolean(false);
        new GdxJsonReader() {

            boolean isExclude() {

                for (String exc : exclude) {
                    if (ex.toString().equals(exc)) {
                        return true;
                    }
                }
                return false;
            }


            public void startArray(String name) {
                super.startArray(name);
                ex.add(name);
                if (isExclude()) {
                    isExclude.set(true);
                } else {
                    if (!isExclude.get()) {
                        sb.appendLine("startArray " + name);
                    }
                }
            }


            public void endArray(String name) {
                if (!isExclude.get()) {
                    sb.appendLine("endArray " + name);
                }
                if (isExclude()) {
                    isExclude.set(false);
                }
                ex.pop();
            }

            public void startObject(String name) {
                super.startObject(name);
                if (!isExclude.get()) {
                    sb.appendLine("startObject " + name);
                }
            }

            public void pop() {
                super.pop();
                if (!isExclude.get()) {
                    sb.appendLine("pop ");
                }


                if (ex.size > 0 && this.root != null && this.root.name != null && this.root.name.equals(ex.peek())) {
                    endArray(this.root.name);
                }


            }

            public void string(String name, String value) {
                super.string(name, value);
                if (!isExclude.get()) {
                    sb.appendLine("string " + name + "  " + value);
                }
            }

            public void number(String name, double value, String stringValue) {
                super.number(name, value, stringValue);
                if (!isExclude.get()) {
                    sb.appendLine("number(Double) " + name + "  " + value);
                }
            }

            public void number(String name, long value, String stringValue) {
                super.number(name, value, stringValue);
                if (!isExclude.get()) {
                    sb.appendLine("number(Long) " + name + "  " + value);
                }
            }

            public void bool(String name, boolean value) {
                super.bool(name, value);
                if (!isExclude.get()) {
                    sb.appendLine("bool " + name + "  " + value);
                }
            }
        }.parse(stream, dummyLength);

        log.debug("Parse time JsonParser: {}", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        stream = TestUtils.getResourceRequestStream(file);

        JsonStreamParser jsonStreamParser = new JsonStreamParser() {
            public void startArray(String name) {
                sb2.appendLine("startArray " + name);
            }

            public void endArray(String name) {
                sb2.appendLine("endArray " + name);
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
        };
        jsonStreamParser.setExclude(exclude);

        jsonStreamParser.parse(stream, dummyLength);

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