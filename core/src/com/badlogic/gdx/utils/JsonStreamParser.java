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

import java.io.InputStream;

/**
 * Created by Longri on 18.04.2017.
 */
public class JsonStreamParser implements JsonParser {
    @Override
    public JsonValue parse(InputStream input) {
        return null;
    }

    @Override
    public void startArray(String name) {

    }

    @Override
    public void endArray(String name) {

    }

    @Override
    public void startObject(String name) {

    }

    @Override
    public void pop() {

    }

    @Override
    public void string(String name, String value) {

    }

    @Override
    public void number(String name, double value, String stringValue) {

    }

    @Override
    public void number(String name, long value, String stringValue) {

    }

    @Override
    public void bool(String name, boolean value) {

    }
}
