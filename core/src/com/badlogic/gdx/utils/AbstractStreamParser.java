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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 03.04.2018.
 */
public abstract class AbstractStreamParser {

    private final Logger log = LoggerFactory.getLogger(AbstractStreamParser.class);


    protected final boolean DEBUG = false;
    private static final int DEFAULT_BUFFER_LENGTH = 512;//1024;
    protected final AtomicBoolean CANCELD = new AtomicBoolean(false);
    protected float percent = 0;
    private int actBufferLength = DEFAULT_BUFFER_LENGTH;
    private Reader reader;
    private char[] buf = new char[actBufferLength];
    private char[] tmp = new char[actBufferLength];

    public void parse(final InputStream input) {
        parse(input, 1);
    }

    public void parse(final InputStream input, long length) {
        try {
            this.reader = new InputStreamReader(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        if (DEBUG) log.debug("Start parsing");

        try {

            int readed = 0;
            int offset = 0;
            while (!CANCELD.get()) {

                if (offset < actBufferLength && actBufferLength > DEFAULT_BUFFER_LENGTH && offset < DEFAULT_BUFFER_LENGTH) {
                    actBufferLength = actBufferLength >> 1;
                    if (DEBUG)
                        log.debug("can decrease buffer to {}", actBufferLength);
                    buf = new char[actBufferLength];
                    System.arraycopy(tmp, 0, buf, 0, offset);
                    tmp = new char[actBufferLength];
                }

                if (offset == actBufferLength) {
                    //must increase buffer size!
                    actBufferLength = actBufferLength << 1;
                    if (DEBUG)
                        log.debug("increase buffer to {}", actBufferLength);
                    buf = new char[actBufferLength];
                    System.arraycopy(tmp, 0, buf, 0, offset);
                    tmp = new char[actBufferLength];
                }

                int canReadLength = actBufferLength - offset;
                int readedLength = 0;
                boolean fillBuffer = canReadLength > 0;
                while (fillBuffer) {
                    readedLength = reader.read(buf, offset, canReadLength);
                    if (readedLength < canReadLength && readedLength > -1) {
                        offset += readedLength;
                        canReadLength = actBufferLength - offset;
                        if (canReadLength <= 0) fillBuffer = false;
                    } else fillBuffer = false;
                }

                readed += readedLength;

                percent = (float) readed / length * 100.0f;
                if (DEBUG) log.debug("Read Buffer: available {}/{} = {}%", readed, length, percent);
                if (DEBUG) log.debug(new String(buf));

                int lastOffset = parse(buf);
                if (readedLength == -1) break;
                offset = actBufferLength - lastOffset;

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
    }

    /**
     * Parse the data and return offset of last processed item
     *
     * @param data
     * @return
     */
    abstract int parse(char[] data);
}
