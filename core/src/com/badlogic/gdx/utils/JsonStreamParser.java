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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 18.04.2017.
 */
public class JsonStreamParser extends AbstractStreamParser {

    private static final Logger log = LoggerFactory.getLogger(JsonStreamParser.class);
    private final static String NULL = "null";

    private Array<String> arrayNameStack = new Array<>();
    private int lastPeek;
    private int lastNameStart = -1;
    private Array<String> exclude;
    private final AtomicBoolean isExclude = new AtomicBoolean(false);
    private final AtomicInteger isExcludeCount = new AtomicInteger(0);
    private AtomicBoolean noHandleForNextValue = new AtomicBoolean(false);

    public void cancel() {
        CANCELD.set(true);
    }

    private boolean ifExclude() {
        if (this.exclude == null) return false;
        String stack = arrayNameStack.toString();
        for (String exc : exclude) {
            if (exc != null && stack.equals(exc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse the data and return offset of last processed item
     *
     * @param data
     * @return
     */
    @Override
    int parse(char[] data) {

        if (data[data.length - 1] == '\\') {
            return 0;
        }

        String actName = null;
        lastNameStart = -1;
        lastPeek = -1;
        int offset = 0;
        boolean incommingExclude = isExclude.get();
        noHandleForNextValue.set(false);
        while (!CANCELD.get() && offset < data.length) {
            int peek = searchPeek(data, offset);
            if (peek == -1) return offset;

            if (isExclude.get()) {
                offset = peek + 1;
                lastPeek = peek;

                if (data[peek] == '[') {
                    isExcludeCount.incrementAndGet();
                } else if (data[peek] == ']') {
                    if (isExcludeCount.decrementAndGet() < 0) {
                        isExclude.set(false);
                        return peek;

                    }
                }

            } else {
                int nameStart = searchNameBefore(data, peek);
                if (nameStart == -1)
                    actName = null;
                else {
                    actName = getName(data, nameStart);
                }

                boolean noValue = false;
                switch (data[peek]) {
                    case '{':
                    case '[':
                        noValue = true;
                }
                if (!noValue) {
                    if (!(actName == null && lastPeek == -1)) {
                        String valueString = getValue(data, nameStart + (actName != null ? actName.length() : lastPeek + 2), peek);
                        if (valueString != null) {
                            if (!noHandleForNextValue.get() && !isExclude.get()) {
                                try {
                                    handleValue(actName, valueString);
                                } catch (Exception e) {
                                    log.error("Error with parse value near {} value;'{}'", actName, valueString);
                                }
                            } else {
                                noHandleForNextValue.set(false);
                            }
                            offset = peek + 1;
                        }
                    }
                }

                if (peek >= 0) {
                    switch (data[peek]) {
                        case '{':
                            startObject(actName);
                            break;
                        case '[':
                            arrayNameStack.add(actName);
                            if (ifExclude()) {
                                isExclude.set(true);
                                isExcludeCount.set(0);
                                break;
                            }
                            startArray(actName);

                            break;
                        case '}':
                            pop();
                            break;
                        case ']':
                            if (ifExclude()) {
                                arrayNameStack.pop();
                            } else {
                                pop();
                                endArray(arrayNameStack.pop());
                            }
                            break;
                        case ',':
                    }
                    offset = peek + 1;
                    lastPeek = peek;
                } else {
                    break;
                }
            }
        }
        return offset;
    }

    private boolean isEndPeek(char[] data, int offset) {
        if (offset < 0 || offset > data.length) return false;
        switch (data[offset]) {
            case '}':
            case ']':
                return true;
        }
        return false;
    }

    void handleValue(String actName, String valueString) {
        valueString = unescape(valueString.trim());

        if (NULL.equals(valueString)) {
            string(actName, NULL);
        } else if (valueString.startsWith("\"")) {
            string(actName, unescape(valueString.substring(1, valueString.length() - 1)));
        } else if (valueString.contains(".")) {
            // parse double
            number(actName, Double.valueOf(valueString), valueString);
        } else if (valueString.toLowerCase(Locale.ENGLISH).equals("true") || valueString.toLowerCase(Locale.ENGLISH).equals("false")) {
            bool(actName, valueString.toLowerCase(Locale.ENGLISH).equals("true"));
        } else {
            //parse long
            long value = Long.valueOf(valueString);
            number(actName, value, valueString);
        }
    }

    /**
     * Unescape function from com.badlogic.gdx.utils.JsonReader!
     *
     * @param value
     * @return
     * @author Nathan Sweet
     */
    private String unescape(String value) {
        int length = value.length();
        StringBuilder buffer = new StringBuilder(length + 16);
        for (int i = 0; i < length; ) {
            char c = value.charAt(i++);
            if (c != '\\') {
                buffer.append(c);
                continue;
            }
            if (i == length) break;
            c = value.charAt(i++);
            if (c == 'u') {
                buffer.append(Character.toChars(Integer.parseInt(value.substring(i, i + 4), 16)));
                i += 4;
                continue;
            }
            switch (c) {
                case '"':
                case '\\':
                case '/':
                    break;
                case 'b':
                    c = '\b';
                    break;
                case 'f':
                    c = '\f';
                    break;
                case 'n':
                    c = '\n';
                    break;
                case 'r':
                    c = '\r';
                    break;
                case 't':
                    c = '\t';
                    break;
                default:
                    buffer.append('\\');
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    private String getValue(char[] data, int start, int end) {
        int found = -1;
        if (start < 0) return null;
        for (int i = start; i < end; i++) {
            switch (data[i]) {
                case ':':
                    found = i + 1;
                    i = end;//break for
                    break;
            }
        }
        if (found > start && found != end) {
            String value = null;
            try {
                value = new String(data, found, end - found);
            } catch (Exception e) {
                log.error("found: {} end: {}", found, end, e);
            }
            if (DEBUG) log.debug("Found Value: {}", value);
            return value;
        }

        //try to trimmed value
        if (end - start > 0) {
            String value = new String(data, start, end - start).trim();
            if (value.startsWith("\"") && value.endsWith("\"")) return value;
        }
        return null;
    }

     String getName(char[] data, int nameStart) {
        int found = -1;
        for (int i = nameStart + 1, n = data.length; i < n; i++) {
            switch (data[i]) {
                case '"':
                    found = i;
                    i = data.length; //break for
                    break;
            }
        }
        if (found > nameStart) {
            String name = new String(data, nameStart + 1, found - nameStart - 1);
            if (DEBUG) log.debug("Found name: {}", name);
            return name;
        }
        return null;
    }

     int searchNameBefore(char[] data, int peek) {
        int found = -1;
        boolean first = true;
        boolean end = true;
        boolean isString = false;
        for (int i = peek; i >= 0; i--) {
            if (isString) {
                switch (data[i]) {
                    case '"':
                        // check if escaped
                        if (i >= 1 && data[i - 1] == '\\')
                            continue;
                        isString = false;
                        break;
                }
                continue;
            }

            switch (data[i]) {
                case ':':
                    end = false;
                    break;
                case '"':
                    if (end) {
                        // check if escaped
                        if (i >= 1 && data[i - 1] == '\\')
                            continue;
                        isString = true;
                        break;
                    }
                    if (first) {
                        first = false;
                    } else {
                        found = i;
                        i = 0; //break for
                    }
                    break;
            }
        }
        if (lastNameStart == found) return -1;
        lastNameStart = found;
        return found;
    }

    int searchPeek(char[] data, int offset) {

        boolean isString = false;

        for (int i = offset, n = data.length; i < n; i++) {
            if (isString) {
                switch (data[i]) {
                    case '"':
                        // check if escaped
                        if (i >= 1 && data[i - 1] == '\\')
                            continue;
                        isString = false;
                        break;
                }
                continue;
            }

            switch (data[i]) {
                case '"':
                    // check if escaped
                    if (i >= 1 && data[i - 1] == '\\')
                        continue;
                    isString = true;
                    break;
                case '{':
                case '}':
                case '[':
                case ']':
                case ',':
                    return i;
            }
        }
        return -1;
    }

    public void startArray(String name) {
    }

    public void endArray(String name) {
    }

    public void startObject(String name) {
    }

    public void pop() {
    }

    public void string(String name, String value) {
    }

    public void number(String name, double value, String stringValue) {
    }

    public void number(String name, long value, String stringValue) {
    }

    public void bool(String name, boolean value) {
    }

    public int getProgress() {
        return (int) percent;
    }

    public void setExclude(Array<String> exclude) {
        this.exclude = exclude;
    }
}
