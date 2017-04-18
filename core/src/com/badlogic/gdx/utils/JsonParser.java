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
public interface JsonParser {

    public JsonValue parse(InputStream input);

    void startArray(String name);

    void endArray(String name);

    void startObject(String name);

    void pop();

    void string(String name, String value);

    void number(String name, double value, String stringValue);

    void number(String name, long value, String stringValue);

    void bool(String name, boolean value);

}
