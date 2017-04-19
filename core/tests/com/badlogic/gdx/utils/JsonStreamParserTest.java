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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationLogger;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.utils.BuildInfo;
import de.longri.cachebox3.utils.converter.Base64;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.DummyLogApplication;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 18.04.2017.
 */
class JsonStreamParserTest {

    static {
        BuildInfo.setTestBuildInfo("JUnitTest");
        Gdx.net = new LwjglNet();
        Gdx.files = new LwjglFiles();
        Gdx.app = new DummyLogApplication();
        Gdx.app.setApplicationLogger(new LwjglApplicationLogger());
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void parse() throws FileNotFoundException {

        InputStream stream = TestUtils.getResourceRequestStream("testsResources/GetYourUserProfile_request.txt");
        final StringBuilder sb = new StringBuilder();
        new JsonReader() {
            public void startArray(String name) {
                sb.appendLine("startArray " + name);
            }

            public void endArray(String name) {
                sb.appendLine("endArray " + name);
            }

            public void startObject(String name) {
                sb.appendLine("startObject " + name);
            }

            public void pop() {
                sb.appendLine("pop ");
            }

            public void string(String name, String value) {
                sb.appendLine("string " + name + "  " + value);
            }

            public void number(String name, double value, String stringValue) {
                sb.appendLine("number(Double) " + name + "  " + value);
            }

            public void number(String name, long value, String stringValue) {
                sb.appendLine("number(Long) " + name + "  " + value);
            }

            public void bool(String name, boolean value) {
                sb.appendLine("bool " + name + "  " + value);
            }
        }.parse(stream);

        stream = TestUtils.getResourceRequestStream("testsResources/GetYourUserProfile_request.txt");
        final StringBuilder sb2 = new StringBuilder();
        new JsonStreamParser() {
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
        }.parse(stream);


        assertEquals(sb.toString(), sb2.toString());

    }

}