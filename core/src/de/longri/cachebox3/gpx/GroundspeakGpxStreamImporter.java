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
    private final char[] GEO_CACHE = "Geocache".toCharArray();
    private final char[] CACHES_FOUND = "Geocache Found".toCharArray();
    private final char[] HREF = "href".toCharArray();


    public GroundspeakGpxStreamImporter(Database database, ImportHandler importHandler) {
        super(database, importHandler);
        this.resetValues();
    }


    @Override
    protected void registerGenerallyHandler() {
        this.registerDataHandler("/gpx/wpt/cmt", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                shortDescription = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

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

        this.registerEndTagHandler("/gpx/wpt", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                createNewWPT();
            }
        });
        this.registerDataHandler("/gpx/wpt/sym", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                if (CharSequenceUtil.contains(data, offset, length, GEO_CACHE, 0, GEO_CACHE.length)) {
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
    }

    @Override
    protected void registerGroundspeakHandler() {
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
                hint = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:long_description", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                longDescription = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:short_description", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                shortDescription = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

        this.registerEndTagHandler("/gpx/wpt/groundspeak:cache/groundspeak:travelbugs/groundspeak:travelbug", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                tbCount++;
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
                logComment = CharSequenceUtil.getHtmlString(data, offset, length);
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

    @Override
    protected void registerCacheboxHandler() {
        registerGroundspeakHandler();
        this.registerDataHandler("/gpx/wpt/cachebox-extension/Parent", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                gsakParent = new String(data, offset, length);
            }
        });
        this.registerDataHandler("/gpx/wpt/cachebox-extension/clue", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                wpClue = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/cachebox-extension/note", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                note = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/cachebox-extension/solver", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                solver = new String(data, offset, length);
            }
        });

    }

    @Override
    protected void registerOpenCachingHandler() {
        registerGroundspeakHandler();
        this.registerDataHandler("/gpx/wpt/gsak:wptExtension/gsak:Parent", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                gsakParent = new String(data, offset, length);
            }
        });
    }

    @Override
    protected void registerGsakHandler() {

        this.registerDataHandler("/gpx/wpt/gsak:wptExtension/gsak:Parent", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                gsakParent = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/gsak:wptExtension/gsak:LatBeforeCorrect", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                hasCorrectedCoord = true;
                correctedLatitude = latitude;
                latitude = CharSequenceUtil.parseDouble(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/gsak:wptExtension/gsak:LonBeforeCorrect", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                correctedLongitude = longitude;
                longitude = CharSequenceUtil.parseDouble(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/gsak:wptExtension/gsak:FavPoints", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                favPoints = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/gsak:wptExtension/gsak:GcNote", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                note = new String(data, offset, length);
            }
        });

        registerGroundspeakHandler();
    }

    @Override
    protected void registerGsakHandler_1_1() {

        this.registerDataHandler("/gpx/wpt/extensions/gsak:wptExtension/gsak:Parent", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                gsakParent = new String(data, offset, length);
            }
        });


        this.registerValueHandler("gpx/wpt/link",
                new ValueHandler() {
                    @Override
                    protected void handleValue(char[] valueName, char[] data, int offset, int length) {
                        if (CharSequenceUtil.equals(HREF, valueName)) {
                            url = new String(data, offset, length);
                        }
                    }
                }, HREF);

        this.registerDataHandler("/gpx/wpt/extensions/gsak:wptExtension/gsak:LatBeforeCorrect", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                hasCorrectedCoord = true;
                correctedLatitude = latitude;
                latitude = CharSequenceUtil.parseDouble(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/gsak:wptExtension/gsak:LonBeforeCorrect", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                correctedLongitude = longitude;
                longitude = CharSequenceUtil.parseDouble(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/gsak:wptExtension/gsak:FavPoints", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                favPoints = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/gsak:wptExtension/gsak:GcNote", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                note = new String(data, offset, length);
            }
        });

        this.registerValueHandler("gpx/wpt/extensions/groundspeak:cache",
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
        this.registerValueHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute",
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


        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:name", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                title = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:placed_by", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                placed_by = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:owner", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                owner = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:container", new DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                container = CacheSizes.parseString(new String(data, offset, length));
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/url", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                url = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:difficulty", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                double dif = CharSequenceUtil.parseDouble(data, offset, length);
                difficulty = (float) (dif);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:terrain", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                double ter = CharSequenceUtil.parseDouble(data, offset, length);
                terrain = (float) (ter);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:country", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                country = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:state", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                state = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:encoded_hints", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                hint = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                longDescription = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                shortDescription = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/cmt", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                shortDescription = CharSequenceUtil.getHtmlString(data, offset, length).replace("\r\n", "\n");
            }
        });

        this.registerEndTagHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:travelbugs/groundspeak:travelbug", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                tbCount++;
            }

        });

        this.registerValueHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log",
                new ValueHandler() {
                    @Override
                    protected void handleValue(char[] valueName, char[] data, int offset, int length) {
                        if (CharSequenceUtil.equals(ID, valueName)) {
                            logId = CharSequenceUtil.parseLong(data, offset, length);
                        }
                    }
                }, ID);

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logDate = parseDate(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logFinder = new String(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logComment = CharSequenceUtil.getHtmlString(data, offset, length);
            }
        });

        this.registerDataHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type", new DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                logType = LogTypes.parseString(new String(data, offset, length));
            }
        });

        this.registerEndTagHandler("/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                createNewLogEntry();
            }

        });
    }


}
