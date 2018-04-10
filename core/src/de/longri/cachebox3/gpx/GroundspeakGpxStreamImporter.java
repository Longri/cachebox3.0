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
package de.longri.cachebox3.gpx;

import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.CharSequenceUtil;

/**
 * Created by Longri on 04.04.2018.
 */
public class GroundspeakGpxStreamImporter extends AbstractGpxStreamImporter {


    private final char[] LAT = "lat".toCharArray();
    private final char[] LON = "lon".toCharArray();
    private final char[] ID = "id".toCharArray();
    private final char[] INC = "inc".toCharArray();
    private final char[] AVAILABLE = "available".toCharArray();
    private final char[] ARCHIEVED = "archived".toCharArray();
    private final char[] CACHES_FOUND = "Geocache Found".toCharArray();


    public GroundspeakGpxStreamImporter(Database database, ImportHandler importHandler) {
        super(database, importHandler);

        this.resetValues();

        //register Handler
        this.registerValueHandler("/gpx/wpt",
                new ValueHandler() {
                    @Override
                    protected void handleValue(char[] valueName, char[] data, int offset, int length) {
                        if (CharSequenceUtil.equals(LAT, valueName)) {
                            latitude = CharSequenceUtil.parseDouble(data, offset, length);
                        } else if (CharSequenceUtil.equals(LON, valueName)) {
                            longitude = CharSequenceUtil.parseDouble(data, offset, length);
                        }
                    }
                }, LAT, LON);

        this.registerValueHandler("gpx/wpt/groundspeak:cache",
                new ValueHandler() {
                    @Override
                    protected void handleValue(char[] valueName, char[] data, int offset, int length) {
                        if (CharSequenceUtil.equals(AVAILABLE, valueName)) {
                            available = CharSequenceUtil.parseBoolean(data, offset, length);
                        } else if (CharSequenceUtil.equals(ARCHIEVED, valueName)) {
                            archived = CharSequenceUtil.parseBoolean(data, offset, length);
                        }
                    }
                }, AVAILABLE, ARCHIEVED);

        this.registerValueHandler("/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute",
                new ValueHandler() {
                    int attId;

                    @Override
                    protected void handleValue(char[] valueName, char[] data, int offset, int length) {
                        if (CharSequenceUtil.equals(ID, valueName)) {
                            attId = CharSequenceUtil.parseInteger(data, offset, length);
                        } else if (CharSequenceUtil.equals(INC, valueName)) {
                            int inc = CharSequenceUtil.parseInteger(data, offset, length);
                            Attributes att = Attributes.getAttributeEnumByGcComId(attId);
                            if (att != null && att != Attributes.Default) {
                                if (inc > 0) {
                                    positiveAttributes.add(att);
                                } else {
                                    negativeAttributes.add(att);
                                }
                            }
                        }
                    }
                }, ID, INC);

        this.registerDataHandler("/gpx/wpt/type", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                type = CacheTypes.parseString(new String(data, offset, length));
            }
        });

        this.registerDataHandler("/gpx/wpt/desc", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                wpTitle = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/name", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                gcCode = new String(data, offset, length);
                id = AbstractCache.GenerateCacheId(gcCode);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:name", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                title = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:placed_by", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                placed_by = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:owner", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                owner = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:container", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                container = CacheSizes.parseString(new String(data, offset, length));
            }
        });

        this.registerDataHandler("/gpx/wpt/url", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                url = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:difficulty", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                double dif = CharSequenceUtil.parseDouble(data, offset, length);
                difficulty = (float) (dif);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:terrain", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                double ter = CharSequenceUtil.parseDouble(data, offset, length);
                terrain = (float) (ter);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:country", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                country = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:state", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                state = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:encoded_hints", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                hint = new String(data, offset, length).replace("\r\n", "\n");
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:long_description", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                longDescription = new String(data, offset, length).replace("\r\n", "\n");
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:short_description", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                shortDescription = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/sym", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {

                if (gcCode.startsWith("GC")) {
                    //for Cache
                    found = CharSequenceUtil.equals(data, offset, length, CACHES_FOUND, 0, CACHES_FOUND.length);
                } else {
                    //for Waypoint
                    type = CacheTypes.parseString(new String(data, offset, length));
                }
            }
        });

        this.registerDataHandler("/gpx/wpt/time", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                wpDate = parseDate(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/cmt", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                shortDescription = new String(data, offset, length).replace("\r\n", "\n");
            }
        });

        this.registerEndTagHandler("/gpx/wpt/groundspeak:cache/groundspeak:travelbugs/groundspeak:travelbug", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                tbCount++;
            }

        });

        this.registerEndTagHandler("/gpx/wpt", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                createNewWPT();
            }
        });

        this.registerValueHandler("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log",
                new ValueHandler() {
                    @Override
                    protected void handleValue(char[] valueName, char[] data, int offset, int length) {
                        if (CharSequenceUtil.equals(ID, valueName)) {
                            logId = CharSequenceUtil.parseLong(data, offset, length);
                        }
                    }
                }, ID);

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logDate = parseDate(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logFinder = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logComment = new String(data, offset, length).replace("\r\n", "\n");
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logType = LogTypes.parseString(new String(data, offset, length));
            }
        });

        this.registerEndTagHandler("/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                createNewLogEntry();
            }

        });
    }

}
