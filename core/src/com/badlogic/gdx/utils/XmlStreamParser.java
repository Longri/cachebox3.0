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
package com.badlogic.gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.utils.CharSequenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 03.04.2018.
 */
public class XmlStreamParser extends AbstractStreamParser {

    private final Logger log = LoggerFactory.getLogger(XmlStreamParser.class);

    public interface DataHandler {
        void handleData(char[] data, int offset, int length);
    }

    public static abstract class ValueHandler {
        Array<char[]> valueList = new Array<>();

        abstract void handleValue(char[] valueName, char[] data, int offset, int length);
    }

    private final CharArray activeNameTags = new CharArray() {

        @Override
        public boolean equals(Object object) {
            if (object instanceof char[]) {
                char[] data = (char[]) object;
                if (data.length != this.size) return false;
                char[] tmp = this.items;
                for (int i = 0; i < data.length; i++)
                    if (tmp[i] != data[i]) return false;
                return true;
            }
            return super.equals(object);
        }
    };
    private final ObjectMap<char[], DataHandler> dataHandlerMap = new ObjectMap<>();
    private final ObjectMap<char[], ValueHandler> valueHandlerMap = new ObjectMap<>();
    private final char[] QUOTE = new char[]{'"'};
    private int lastStartPeek;
    private DataHandler lastHandler;


    public void registerDataHandler(String locationPath, DataHandler dataHandler) {
        // remove '/'
        locationPath = locationPath.replaceAll("/", "");
        dataHandlerMap.put(locationPath.toCharArray(), dataHandler);
    }

    public void registerValueHandler(String locationPath, ValueHandler valueHandler, char[]... valueNames) {
        // remove '/'
        locationPath = locationPath.replaceAll("/", "");

        char[] path = locationPath.toCharArray();

        ValueHandler contains = valueHandlerMap.get(path);
        if (contains != null) {
            contains.valueList.addAll(valueNames);
        } else {
            valueHandler.valueList.addAll(valueNames);
            valueHandlerMap.put(path, valueHandler);
        }
    }

    public void parse(FileHandle xmlFileHandle) {
        this.parse(xmlFileHandle.read());
    }

    @Override
    protected int parse(char[] data) {
        int offset = 0;
        lastStartPeek = 0;

        while (!CANCELD.get() && offset < data.length) {
            int peek = searchPeek(data, offset);
            if (peek == -1) return offset;

            if (data[peek] == '<') {
                int endPeek = searchPeek(data, peek + 1);
                if (endPeek < 0)
                    return offset;

                // check if <?
                if (data[peek + 1] == '?') {
                    // search for end
                    if (data[endPeek - 1] == '?') {
                        // xml version from peek to endPeek
                        if (DEBUG) log.debug("XML version found");
                    }
                    offset = endPeek + 1;
                    continue;
                }

                // check if </
                if (data[peek + 1] == '/') {
                    // search for end
                    int space = searchSpace(data, peek);
                    int nameLength = space - peek - 2;
                    activeNameTags.size -= nameLength;

                    if (lastHandler != null) {
                        lastHandler.handleData(data, lastStartPeek, space - lastStartPeek - nameLength - 2);
                        lastHandler = null;
                    }

                    if (DEBUG)
                        log.debug("Name END Tag found: " + new String(data, peek + 2, nameLength) + "   Names: {}", activeNameTags.toString(""));
                    offset = space + 1;
                    continue;
                }

                if (data[endPeek] == '>') {
                    // get name ( search next space )
                    int space = searchSpace(data, peek);

                    if (data[endPeek - 1] == '/') {
                        // add to activeNameTags
                        activeNameTags.addAll(data, peek + 1, space - peek - 1);

                        //check if DataHandler registered
                        int valuesStart = space;
                        int valuesLength = endPeek - space;
                        for (ObjectMap.Entry<char[], ValueHandler> entry : valueHandlerMap.entries()) {
                            if (activeNameTags.equals(entry.key)) {


                                // handled valueNames
                                ValueHandler handler = entry.value;
                                for (char[] valueName : handler.valueList) {
                                    // check value exist
                                    int valueStart = CharSequenceUtil.indexOf(data, valuesStart, valuesLength, valueName, 0, valueName.length, 0);
                                    if (valueStart >= 0) {
                                        valueStart += valuesStart; //add offset (valuesStart)
                                        valueStart += valueName.length + 2; // add name and ="

                                        //search value end ( next ")
                                        int valueEnd = CharSequenceUtil.indexOf(data, valueStart, valuesLength - (valueStart - valuesStart), QUOTE, 0, QUOTE.length, 0);

                                        handler.handleValue(valueName, data, valueStart, valueEnd);

                                    } else {
                                        log.warn("Value " + new String(valueName) + " not found");
                                    }

                                }

                                if (DEBUG) log.debug("ValueHandler found");
                            }
                        }

                        // remove from activeNameTags
                        activeNameTags.size -= space - peek - 1;

                        if (DEBUG) {
                            String name = new String(data, peek + 1, space - peek - 1);
                            log.debug("Name End Tag found: " + name + "   Names: {}", activeNameTags.toString(""));
                        }
                        offset = endPeek + 1;
                        continue;
                    } else {
                        activeNameTags.addAll(data, peek + 1, space - peek - 1);
                        //check if DataHandler registered
                        for (ObjectMap.Entry<char[], DataHandler> entry : dataHandlerMap.entries()) {
                            if (activeNameTags.equals(entry.key)) {
                                lastStartPeek = endPeek + 1;
                                lastHandler = entry.value;
                            }
                        }

                        //check if ValueHandler registered
                        int valuesStart = peek;
                        int valuesLength = endPeek - peek;
                        for (ObjectMap.Entry<char[], ValueHandler> entry : valueHandlerMap.entries()) {
                            if (activeNameTags.equals(entry.key)) {
                                // handled valueNames
                                ValueHandler handler = entry.value;
                                for (char[] valueName : handler.valueList) {
                                    // check value exist
                                    int valueStart = CharSequenceUtil.indexOf(data, valuesStart, valuesLength, valueName, 0, valueName.length, 0);
                                    if (valueStart >= 0) {
                                        valueStart += valuesStart; //add offset (valuesStart)
                                        valueStart += valueName.length + 2; // add name and ="

                                        //search value end ( next ")
                                        int valueEnd = CharSequenceUtil.indexOf(data, valueStart, valuesLength - (valueStart - valuesStart), QUOTE, 0, QUOTE.length, 0);

                                        handler.handleValue(valueName, data, valueStart, valueEnd);

                                    } else {
                                        log.warn("Value " + new String(valueName) + " not found");
                                    }

                                }
                            }
                        }

                        if (DEBUG) {
                            String name = new String(data, peek + 1, space - peek - 1);
                            log.debug("Name Tag found: " + name + "   Names: {}", activeNameTags.toString(""));
                        }

                        offset = endPeek + 1;
                        continue;
                    }
                }
            }
            offset = peek + 1;
        }
        return offset;
    }

    private int searchPeek(char[] data, int offset) {
        for (int i = offset, n = data.length; i < n; i++) {
            switch (data[i]) {
                case '<':
                case '>':
                    return i;
            }
        }
        return -1;
    }

    private int searchSpace(char[] data, int offset) {
        for (int i = offset, n = data.length; i < n; i++) {
            switch (data[i]) {
                case ' ':
                case '>':
                    return i;
            }
        }
        return -1;
    }

}
