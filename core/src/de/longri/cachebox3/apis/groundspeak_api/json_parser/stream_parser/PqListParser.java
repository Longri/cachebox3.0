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
package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonStreamParser;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import de.longri.cachebox3.utils.ICancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Date;

/**
 * Created by Longri on 26.03.2018.
 */
public class PqListParser {

    private final Logger log = LoggerFactory.getLogger(PqListParser.class);

    private final ICancel iCancel;

    public PqListParser(ICancel icancel) {
        this.iCancel = icancel;
    }


    private boolean pocketQueryList = false;

    public ApiResultState parsePqList(InputStream stream, final Array<PocketQuery.PQ> pqList) {
        final ApiResultState[] resultState = {ApiResultState.IO};

        // Parse JSON Result
        final JsonStreamParser parser = new JsonStreamParser() {

            public String name;
            public String guid;
            public int cacheCount;
            public Date lastGenerated;
            public double sizeB;
            public boolean downloadAvailable = false;


            @Override
            public void startArray(String name) {
                if (name.equals("PocketQueryList")) {
                    pocketQueryList = true;
                }
            }

            @Override
            public void endArray(String name) {
                if (name.equals("PocketQueryList")) {
                    this.cancel();
                }
            }

            @Override
            public void pop() {
                if (pocketQueryList) {
                    // new PQ
                    if (name != null) {
                        PocketQuery.PQ newPQ = new PocketQuery.PQ();
                        newPQ.name = name;
                        newPQ.guid = guid;
                        newPQ.cacheCount = cacheCount;
                        newPQ.lastGenerated = lastGenerated;
                        newPQ.sizeMB = sizeB / 1048576.0;
                        newPQ.downloadAvailable = downloadAvailable;
                        pqList.add(newPQ);
                    }
                    name = null;
                }
            }


            @Override
            public void string(String name, String value) {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                super.string(name, value);

                if (value.equals("null"))
                    value = null;

                if (pocketQueryList) {
                    if (name.equals("GUID")) {
                        this.guid = value;
                    } else if (name.equals("Name")) {
                        this.name = value;
                    } else if (name.equals("DateLastGenerated")) {
                        this.lastGenerated = getDateFromLongString(value);
                    }
                } else if (name.equals("StatusMessage") && value.contains("expired")) {
                    // break reading and set ApiResultState
                    resultState[0] = ApiResultState.EXPIRED_API_KEY;
                    this.cancel();
                }
            }

            @Override
            public void number(String name, long value, String stringValue) {
                super.number(name, value, stringValue);
                if (pocketQueryList) {
                    if (name.equals("FileSizeInBytes")) {
                        this.sizeB = value;
                    } else if (name.equals("PQCount")) {
                        this.cacheCount = (int) value;
                    }
                }
            }

            @Override
            public void bool(String name, boolean value) {
                if (pocketQueryList) {
                    if (name.equals("IsDownloadAvailable")) {
                        this.downloadAvailable = value;
                    }
                }
            }
        };
        parser.parse(stream);

        return resultState[0];
    }


    private static final String DATE_START = "Date(";

    private synchronized Date getDateFromLongString(String value) {
        Date date = new Date();
        try {
            int date1 = value.indexOf(DATE_START);
            int date2 = value.lastIndexOf("-");
            String dateString = value.substring(date1 + DATE_START.length(), date2);
            if (dateString.startsWith("\"")) dateString = dateString.substring(1);
            date = new Date(Long.valueOf(dateString));
        } catch (Exception exc) {
            log.error("ParseDate from value:'{}'", value, exc);
        }
        return date;
    }
}
