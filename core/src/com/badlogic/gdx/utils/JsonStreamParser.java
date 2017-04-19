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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

/**
 * Created by Longri on 18.04.2017.
 */
public class JsonStreamParser implements JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonStreamParser.class);
    private static final int BUFFER_LENGTH = 1024;
    Reader reader;
    char[] buf = new char[BUFFER_LENGTH];
    char[] tmp = new char[BUFFER_LENGTH];
    float percent = 0;
    int lastNameStart = -1;

    private final boolean DEBUG = false;


    @Override
    public JsonValue parse(final InputStream input) {
        this.reader = new InputStreamReader(input);

        if (DEBUG) log.debug("Start parsing");

        try {
            float available = input.available();
            int readed = 0;
            int offset = 0;
            while (true) {

                int length = reader.read(buf, offset, BUFFER_LENGTH - offset);
                if (length == -1) break;
                if (length == 0) {

                } else
                    readed += length;

                percent = (float) readed / available * 100.0f;
                if (DEBUG) log.debug("Read Buffer: available {}/{} = {}%", readed, available, percent);
                if (DEBUG) log.debug(new String(buf));

                int lastOffset = parse(buf);
                offset = BUFFER_LENGTH - lastOffset;

                if (offset == 0) {
                    // clear buffer
                    Arrays.fill(buf, '\0');
                } else {
                    // move unhandled char's
                    Arrays.fill(tmp, '\0');
                    System.arraycopy(buf, lastOffset, tmp, 0, offset);
                    Arrays.fill(buf, '\0');
                    System.arraycopy(tmp, 0, buf, 0, offset);
                }

                if (DEBUG) log.debug("Last Offset: {}", lastOffset);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamUtils.closeQuietly(reader);
        }
        return null;
    }

    /**
     * Parse the data and return offset of last processed item
     *
     * @param data
     * @return
     */
    private int parse(char[] data) {
        String actName = null;
        lastNameStart = -1;
        int offset = 0;
        while (offset < data.length) {
            int peek = searchPeek(data, offset);
//            if (!isEndPeek(data, peek)) {
            int nameStart = searchNameBefore(data, peek);
            if (nameStart == -1)
                actName = null;
            else {
                actName = getName(data, nameStart);
                String valueString = getValue(data, nameStart + actName.length(), peek);
                if (valueString != null) {
                    try {
                        handleValue(actName, valueString);
                    } catch (Exception e) {
                        log.error("Error with parse value near {} value;'{}'", actName, valueString);
                    }
                    offset = peek + 1;
//                        continue;
                }
            }
//            }

            if (peek >= 0) {
                switch (data[peek]) {
                    case '{':
                        startObject(actName);
                        break;
                    case '[':
                        startArray(actName);
                        break;
                    case '}':
                        pop();
                        break;
                    case ']':
                        endArray(actName);
                        pop();
                        break;
                    case ',':
                }
                offset = peek + 1;
            } else {
                break;
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

    private void handleValue(String actName, String valueString) {
        // is String?
        if (valueString.startsWith("\"")) {
            string(actName, valueString.substring(1, valueString.length() - 1));
        } else if (valueString.contains(".")) {
            // parse double
            number(actName, Double.valueOf(valueString), valueString);
        } else if (valueString.toLowerCase().equals("true") || valueString.toLowerCase().equals("false")) {
            bool(actName, valueString.toLowerCase().equals("true"));
        } else {
            //parse long
            long value = Long.valueOf(valueString);
            number(actName, value, valueString);
        }
    }

    private String getValue(char[] data, int start, int end) {
        int found = -1;
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
        return null;
    }

    private String getName(char[] data, int nameStart) {
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
            if (DEBUG) log.debug("Found Name: {}", name);
            return name;
        }
        return null;
    }

    private int searchNameBefore(char[] data, int peek) {
        int found = -1;
        boolean first = true;
        boolean end = true;
        for (int i = peek; i >= 0; i--) {
            switch (data[i]) {
                case ':':
                    end = false;
                    break;
                case '"':
                    if (end) break;
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

    private int searchPeek(char[] data, int offset) {

        for (int i = offset, n = data.length; i < n; i++) {
            switch (data[i]) {
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
