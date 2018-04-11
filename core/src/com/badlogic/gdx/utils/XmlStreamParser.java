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


    public static abstract class EndTagHandler {
        protected abstract void handleEndTag();
    }

    public static abstract class DataHandler {
        protected abstract void handleData(char[] data, int offset, int length);
    }

    public static abstract class ValueHandler {
        Array<char[]> valueList = new Array<>();

        protected abstract void handleValue(char[] valueName, char[] data, int offset, int length);
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

    protected final IntMap<ObjectMap<char[], EndTagHandler>> endTagHandlerMap = new IntMap<>();
    protected final IntMap<ObjectMap<char[], DataHandler>> dataHandlerMap = new IntMap<>();
    protected final IntMap<ObjectMap<char[], ValueHandler>> valueHandlerMap = new IntMap<>();
    private final char[] QUOTE = new char[]{'"'};

    private int lastStartPeek;
    private DataHandler lastHandler;

    public void registerEndTagHandler(String locationPath, EndTagHandler handler) {
        // remove '/'
        locationPath = locationPath.replaceAll("/", "");

        ObjectMap<char[], EndTagHandler> handlerMap = endTagHandlerMap.get(locationPath.length());
        if (handlerMap == null) {
            handlerMap = new ObjectMap<>();
            handlerMap.put(locationPath.toCharArray(), handler);
            endTagHandlerMap.put(locationPath.length(), handlerMap);
        } else {
            handlerMap.put(locationPath.toCharArray(), handler);
        }
    }

    public void registerDataHandler(String locationPath, DataHandler dataHandler) {
        // remove '/'
        locationPath = locationPath.replaceAll("/", "");
        ObjectMap<char[], DataHandler> handlerMap = dataHandlerMap.get(locationPath.length());
        if (handlerMap == null) {
            handlerMap = new ObjectMap<>();
            handlerMap.put(locationPath.toCharArray(), dataHandler);
            dataHandlerMap.put(locationPath.length(), handlerMap);
        } else {
            handlerMap.put(locationPath.toCharArray(), dataHandler);
        }
    }

    public void registerValueHandler(String locationPath, ValueHandler valueHandler, char[]... valueNames) {
        // remove '/'
        locationPath = locationPath.replaceAll("/", "");
        char[] path = locationPath.toCharArray();
        ObjectMap<char[], ValueHandler> handlerMap = valueHandlerMap.get(locationPath.length());
        if (handlerMap == null) {
            handlerMap = new ObjectMap<>();
            handlerMap.put(path, valueHandler);
            valueHandler.valueList.addAll(valueNames);
            valueHandlerMap.put(locationPath.length(), handlerMap);
        } else {
            ValueHandler contains = handlerMap.get(path);
            if (contains != null) {
                contains.valueList.addAll(valueNames);
            } else {
                valueHandler.valueList.addAll(valueNames);
                handlerMap.put(path, valueHandler);
            }
        }
    }

    public void parse(FileHandle xmlFileHandle) throws GdxRuntimeException {
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
                    //search for endTagHandler
                    ObjectMap<char[], EndTagHandler> handlerMap = endTagHandlerMap.get(activeNameTags.size);
                    if (handlerMap != null) {
                        for (ObjectMap.Entry<char[], EndTagHandler> entry : handlerMap.entries()) {
                            if (activeNameTags.equals(entry.key)) {
                                entry.value.handleEndTag();
                            }
                        }
                    }

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
                        ObjectMap<char[], ValueHandler> handlerMap = valueHandlerMap.get(activeNameTags.size);
                        if (handlerMap != null) {
                            for (ObjectMap.Entry<char[], ValueHandler> entry : handlerMap.entries()) {
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
                        }

                        //search for endTagHandler
                        ObjectMap<char[], EndTagHandler> endHandlerMap = endTagHandlerMap.get(activeNameTags.size);
                        if (endHandlerMap != null) {
                            for (ObjectMap.Entry<char[], EndTagHandler> entry : endHandlerMap.entries()) {
                                if (activeNameTags.equals(entry.key)) {
                                    entry.value.handleEndTag();
                                }
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
                        ObjectMap<char[], DataHandler> handlerMap = dataHandlerMap.get(activeNameTags.size);
                        if (handlerMap != null) {
                            for (ObjectMap.Entry<char[], DataHandler> entry : handlerMap.entries()) {
                                if (activeNameTags.equals(entry.key)) {
                                    lastStartPeek = endPeek + 1;
                                    lastHandler = entry.value;
                                }
                            }
                        }

                        //check if ValueHandler registered
                        int valuesStart = peek;
                        int valuesLength = endPeek - peek;
                        ObjectMap<char[], ValueHandler> vHandlerMap = valueHandlerMap.get(activeNameTags.size);
                        if (vHandlerMap != null) {
                            for (ObjectMap.Entry<char[], ValueHandler> entry : vHandlerMap.entries()) {
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
