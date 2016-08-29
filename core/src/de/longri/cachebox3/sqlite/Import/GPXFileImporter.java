/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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
package de.longri.cachebox3.sqlite.Import;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;
import de.longri.cachebox3.GlobalCore;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.WaypointDAO;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GPXFileImporter {
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(GPXFileImporter.class);
    private final static SimpleDateFormat DATE_PATTERN_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
    private final static SimpleDateFormat DATE_PATTERN_3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final static SimpleDateFormat DATE_PATTERN_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    static {
        DATE_PATTERN_1.setTimeZone(TimeZone.getTimeZone("EST"));
        DATE_PATTERN_2.setTimeZone(TimeZone.getTimeZone("EST"));
        DATE_PATTERN_3.setTimeZone(TimeZone.getTimeZone("EST"));
    }

    private final File mGpxFile;
    private final String mDisplayFilename;
    private IImportHandler mImportHandler;
    private final ImporterProgress mip;
    private Integer currentwpt = 0;
    private Integer countwpt = 0;
    private Integer errors = 0;

    private Cache cache;
    private final Waypoint waypoint = new Waypoint(true);
    private final LogEntry log = new LogEntry();
    private Category category = new Category();
    private GpxFilename gpxFilename = null;

    private String gpxName = "";
    private String gpxAuthor = "";

    public GPXFileImporter(File gpxFileName) {
        super();
        mGpxFile = gpxFileName;
        mip = null;
        mDisplayFilename = gpxFileName.getName();
    }

    GPXFileImporter(File file, ImporterProgress ip) {
        super();
        mGpxFile = file;
        mip = ip;
        mDisplayFilename = file.getName();
    }

    /**
     * @param importHandler
     * @param countwpt
     * @throws Exception
     */
    public void doImport(IImportHandler importHandler, Integer countwpt) throws Exception {
        // http://www.thebuzzmedia.com/software/simple-java-xml-parser-sjxp/

        currentwpt = 0;
        this.countwpt = countwpt;

        mImportHandler = importHandler;

        category = mImportHandler.getCategory(mGpxFile.getAbsolutePath());
        if (category == null)
            return;

        gpxFilename = mImportHandler.NewGpxFilename(category, mGpxFile.getAbsolutePath());
        if (gpxFilename == null)
            return;

        Map<String, String> values = new HashMap<String, String>();

        System.setProperty("sjxp.namespaces", "false");

        List<IRule<Map<String, String>>> ruleList = new ArrayList<IRule<Map<String, String>>>();

        ruleList = createGPXRules(ruleList);
        ruleList = createWPTRules(ruleList);
        ruleList = createGroundspeakRules(ruleList);
        ruleList = createGSAKRules(ruleList);
        ruleList = createGSAKRulesWithOutExtensions(ruleList);
        ruleList = createTerraRules(ruleList);
        ruleList = createCacheboxRules(ruleList);

        @SuppressWarnings("unchecked")
        XMLParser<Map<String, String>> parserCache = new XMLParser<Map<String, String>>(ruleList.toArray(new IRule[0]));

        InputStream inputStream = new FileInputStream(mGpxFile);

        parserCache.parse(inputStream, values);

        mImportHandler = null;
    }

    private List<IRule<Map<String, String>>> createGPXRules(List<IRule<Map<String, String>>> ruleList) throws Exception {

        // Basic GPX Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                gpxName = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/author") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                gpxAuthor = text;
            }
        });

        return ruleList;
    }

    public static int CacheCount = 0;
    public static int LogCount = 0;

    private List<IRule<Map<String, String>>> createCacheboxRules(List<IRule<Map<String, String>>> ruleList) throws Exception {
        // Cachebox Extension

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/Parent") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_Parrent", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/note") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cachebox-extension_note", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/solver") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cachebox-extension_solver", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/clue") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cachebox-extension_clue", text);
            }
        });

        return ruleList;
    }

    private List<IRule<Map<String, String>>> createWPTRules(List<IRule<Map<String, String>>> ruleList) throws Exception {

        // Basic wpt Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    values.clear();
                } else {
                    if (values.get("wpt_type").startsWith("Geocache")) {
                        try {
                            createCache(values);
                            CacheCount++;
                        } catch (Exception e) {
                            errors++;
                            logger.error("CreateCache", e);
                        }
                    } else if (values.get("wpt_type").startsWith("Waypoint|")) {
                        try {
                            createWaypoint(values);
                        } catch (Exception e) {
                            errors++;
                            logger.error("CreateWaypoint", e);
                        }
                    }
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(IRule.Type.ATTRIBUTE, "/gpx/wpt", "lat", "lon") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("wpt_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(IRule.Type.CHARACTER, "/gpx/wpt/type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                if (text.startsWith("TerraCache|Classic Cache")) {
                    values.put("wpt_type", "Geocache|Traditional Cache"); // nötig um GPX von TerraCaching.com einzulesen
                } else {
                    values.put("wpt_type", text);
                }
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(IRule.Type.CHARACTER, "/gpx/wpt/name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(IRule.Type.CHARACTER, "/gpx/wpt/desc") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_desc", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cmt") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_cmt", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/time") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_time", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_url", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/link") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_link", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/sym") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                if (text.startsWith("Default")) {
                    values.put("wpt_sym", "Geocache"); // nötig um GPX von Navicache.com einzulesen
                } else {
                    values.put("wpt_sym", text);
                }
            }
        });

        return ruleList;
    }

    private List<IRule<Map<String, String>>> createGroundspeakRules(List<IRule<Map<String, String>>> ruleList) throws Exception {

        // groundspeak:cache Rules for GPX from GC.com

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache", "id", "available", "archived") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:placed_by") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_placed_by", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:container") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_container", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:difficulty") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_difficulty", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:terrain") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_terrain", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:country") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_country", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:state") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_state", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_attributes_count"))
                        values.put("cache_attributes_count", String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
                    else
                        values.put("cache_attributes_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:short_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_short_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:short_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_short_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:long_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:long_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_long_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:encoded_hints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

        return ruleList;
    }

    private List<IRule<Map<String, String>>> createGSAKRules(List<IRule<Map<String, String>>> ruleList) throws Exception {

        // GSAK Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/gsak:wptExtension/gsak:Parent") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_Parrent", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/gsak:wptExtension/gsak:LatBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates", "True");
                values.put("cache_gsak_corrected_coordinates_before_lat", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/gsak:wptExtension/gsak:LonBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates_before_lon", text);
            }
        });

        // Cache Rules for GPX from GSAK

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache", "id", "available", "archived") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:placed_by") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_placed_by", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:container") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_container", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:difficulty") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_difficulty", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:terrain") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_terrain", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:country") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_country", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:state") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_state", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_attributes_count"))
                        values.put("cache_attributes_count", String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
                    else
                        values.put("cache_attributes_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_short_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_short_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_long_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:encoded_hints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

        return ruleList;
    }

    private List<IRule<Map<String, String>>> createGSAKRulesWithOutExtensions(List<IRule<Map<String, String>>> ruleList) throws Exception {

        // GSAK Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/gsak:wptExtension/gsak:Parent") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_Parrent", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/gsak:wptExtension/gsak:LatBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates", "True");
                values.put("cache_gsak_corrected_coordinates_before_lat", text);
            }

        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/gsak:wptExtension/gsak:LonBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates_before_lon", text);
            }

        });

        // Cache Rules for GPX from GSAK

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache", "id", "available", "archived") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:placed_by") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_placed_by", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:container") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_container", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:difficulty") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_difficulty", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:terrain") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_terrain", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:country") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_country", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:state") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_state", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_attributes_count"))
                        values.put("cache_attributes_count", String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
                    else
                        values.put("cache_attributes_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_short_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_short_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_long_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:encoded_hints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

        return ruleList;
    }

    private List<IRule<Map<String, String>>> createTerraRules(List<IRule<Map<String, String>>> ruleList) throws Exception {

        // Terra Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        // Cache Rules for GPX from Terracaching.com

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:hint") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules
        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:user") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                if (text.startsWith("find")) // Umcodieren auf groundspeak style
                {
                    text = "Found it"; // Evtl. ist noch eine Umcodierung für "nicht gefunden" nötig
                }

                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:entry") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

        return ruleList;
    }

    /**
     * @param values
     * @throws Exception
     */
    private void createCache(Map<String, String> values) throws Exception {
        // create new Cache Object for each imported cache to avoid that informations of one cache are copied into anohter cache.
        cache = new Cache(true);
        // if (cache.detail == null) cache.detail = new CacheDetail();

        if (gpxAuthor.toLowerCase().contains("gctour")) {
            cache.setTourName(gpxName);
        }

        if (values.containsKey("wpt_attribute_lat") && values.containsKey("wpt_attribute_lon")) {
            cache.Pos = new CoordinateGPS(new Double(values.get("wpt_attribute_lat")).doubleValue(), new Double(values.get("wpt_attribute_lon")).doubleValue());
        }

        if (values.containsKey("wpt_name")) {
            cache.setGcCode(values.get("wpt_name"));
            cache.Id = Cache.GenerateCacheId(cache.getGcCode());
        }

        if (values.containsKey("wpt_time")) {
            cache.setDateHidden(parseDate(values.get("wpt_time")));
        }

        if (values.containsKey("cache_attribute_id")) {
            cache.setGcId(values.get("cache_attribute_id"));
        }

        if (values.containsKey("wpt_url")) {
            cache.setUrl(values.get("wpt_url"));
        }

        // Ein evtl. in der Datenbank vorhandenen "Favorit" nicht überschreiben
        // Boolean fav = LoadBooleanValueFromDB("select favorit from Caches where GcCode = \"" + cache.GcCode + "\"");

        // Read from IndexDBList
        boolean fav = CacheInfoList.CacheIsFavoriteInDB(cache.getGcCode());

        cache.setFavorite(fav);

        if (values.containsKey("wpt_sym")) {
            // Ein evtl. in der Datenbank vorhandenen "Found" nicht überschreiben
            // Boolean Found = LoadBooleanValueFromDB("select found from Caches where GcCode = \"" + cache.GcCode + "\"");

            // Read from IndexDBList
            boolean Found = CacheInfoList.CacheIsFoundInDB(cache.getGcCode());

            if (!Found) {
                cache.setFound(values.get("wpt_sym").equalsIgnoreCase("Geocache Found"));
            } else {
                cache.setFound(true);
            }
        }

        if (values.containsKey("cache_attribute_available")) {
            if (values.get("cache_attribute_available").equalsIgnoreCase("True")) {
                cache.setAvailable(true);
            } else {
                cache.setAvailable(false);
            }
        } else {
            cache.setAvailable(true);
        }

        if (values.containsKey("cache_attribute_archived")) {
            if (values.get("cache_attribute_archived").equalsIgnoreCase("True")) {
                cache.setArchived(true);
            } else {
                cache.setArchived(false);
            }
        } else {
            cache.setArchived(false);
        }

        if (values.containsKey("cache_name")) {
            cache.setName(values.get("cache_name"));
        } else if (values.containsKey("wpt_desc")) // kein Name gefunden? Dann versuche den WP-Namen
        {
            cache.setName(values.get("wpt_desc"));
        }

        if (values.containsKey("cache_placed_by")) {
            cache.setPlacedBy(values.get("cache_placed_by"));
        }

        if (values.containsKey("cache_owner")) {
            cache.setOwner(values.get("cache_owner"));
        }

        if (values.containsKey("cache_type")) {
            cache.Type = CacheTypes.parseString(values.get("cache_type"));
            if (cache.getGcCode().indexOf("MZ") == 0)
                cache.Type = CacheTypes.Munzee;
        } else {
            cache.Type = CacheTypes.Undefined;
        }

        if (values.containsKey("cache_container")) {
            cache.Size = CacheSizes.parseString(values.get("cache_container"));
        } else {
            cache.Size = CacheSizes.other;
        }

        if (values.containsKey("cache_difficulty")) {
            try {
                cache.setDifficulty(Float.parseFloat(values.get("cache_difficulty")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cache.getDifficulty() < 0)
                cache.setDifficulty(0f);
        }

        if (values.containsKey("cache_terrain")) {
            try {
                cache.setTerrain(Float.parseFloat(values.get("cache_terrain")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cache.getTerrain() < 0)
                cache.setTerrain(0f);
        }

        if (values.containsKey("cache_country")) {
            cache.setCountry(values.get("cache_country"));
        }

        if (values.containsKey("cache_state")) {
            cache.setState(values.get("cache_state"));
        }

        if (values.containsKey("cache_attributes_count")) {
            try {
                int count = Integer.parseInt(values.get("cache_attributes_count"));

                for (int i = 1; i <= count; i++) {
                    int attrGcComId = -1;
                    int attrGcComVal = -1;

                    attrGcComId = Integer.parseInt(values.get("cache_attribute_" + String.valueOf(i) + "_id"));
                    try {
                        attrGcComVal = Integer.parseInt(values.get("cache_attribute_" + String.valueOf(i) + "_inc"));
                    } catch (Exception ex) {
                        // if there is no given value "inc" in attribute definition this should be = 1 (gccapp gpx files)
                        attrGcComVal = 1;
                    }

                    if (attrGcComId > 0 && attrGcComVal != -1) {
                        if (attrGcComVal > 0) {
                            cache.addAttributePositive(Attributes.getAttributeEnumByGcComId(attrGcComId));
                        } else {
                            cache.addAttributeNegative(Attributes.getAttributeEnumByGcComId(attrGcComId));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (values.containsKey("cache_short_description")) {
            cache.setShortDescription(values.get("cache_short_description").trim());

            if (values.containsKey("cache_short_description_html") && values.get("cache_short_description_html").equalsIgnoreCase("False")) {
                cache.setShortDescription(cache.getShortDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
            }
        }

        if (values.containsKey("cache_long_description")) {
            cache.setLongDescription(values.get("cache_long_description").trim());

            if (values.containsKey("cache_long_description_html") && values.get("cache_long_description_html").equalsIgnoreCase("False")) {
                cache.setLongDescription(cache.getLongDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
            }
        }

        if (values.containsKey("cache_encoded_hints")) {
            cache.setHint(values.get("cache_encoded_hints"));
        }

        if (values.containsKey("cache_logs_count")) {
            try {
                int count = Integer.parseInt(values.get("cache_logs_count"));

                for (int i = 1; i <= count; i++) {
                    log.CacheId = cache.Id;
                    String attrValue = values.get("cache_log_" + String.valueOf(i) + "_id");
                    if (attrValue != null) {
                        try {
                            log.Id = Long.parseLong(attrValue);
                            // GSAK Special improvisiert
                            if (log.Id < 0)
                                log.Id = log.CacheId + log.Id;
                        } catch (Exception ex) {
                            // Cache ID konnte nicht als Zahl interpretiert werden -> in eine eindeutige Zahl wandeln
                            log.Id = Cache.GenerateCacheId(attrValue);
                        }
                    }

                    if (values.containsKey("cache_log_" + String.valueOf(i) + "_date")) {
                        log.Timestamp = parseDate(values.get("cache_log_" + String.valueOf(i) + "_date"));
                    }

                    if (values.containsKey("cache_log_" + String.valueOf(i) + "_finder")) {
                        log.Finder = values.get("cache_log_" + String.valueOf(i) + "_finder");
                    }

                    if (values.containsKey("cache_log_" + String.valueOf(i) + "_text")) {
                        log.Comment = values.get("cache_log_" + String.valueOf(i) + "_text");
                    }

                    if (values.containsKey("cache_log_" + String.valueOf(i) + "_type")) {
                        log.Type = LogTypes.parseString(values.get("cache_log_" + String.valueOf(i) + "_type"));
                    }

                    if (log != null) {
                        LogCount++;
                        mImportHandler.handleLog(log);
                    }

                    log.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (values.containsKey("cache_gsak_corrected_coordinates")) { // Handle GSAK Corrected Coordinates
            if (values.get("cache_gsak_corrected_coordinates").equalsIgnoreCase("True")) {
                double lat = Double.parseDouble(values.get("cache_gsak_corrected_coordinates_before_lat"));
                double lon = Double.parseDouble(values.get("cache_gsak_corrected_coordinates_before_lon"));

                Coordinate coorectedCoord = cache.Pos;

                // set Original Coords
                cache.Pos = new Coordinate(lat, lon);

                // create final WP with Corrected Coords
                String newGcCode = Database.CreateFreeGcCode(cache.getGcCode());

                // Check if "Final GSAK Corrected" exist
                WaypointDAO WPDao = new WaypointDAO();
                CB_List<Waypoint> wplist = WPDao.getWaypointsFromCacheID(cache.Id, false);

                for (int i = 0; i < wplist.size(); i++) {
                    Waypoint wp = wplist.get(i);
                    if (wp.Type == CacheTypes.Final) {
                        if (wp.getTitle().equalsIgnoreCase("Final GSAK Corrected")) {
                            newGcCode = wp.getGcCode();
                            break;
                        }
                    }
                }

                // "Final GSAK Corrected" is used for recognition of finals from GSAK on gpx - Import
                Waypoint FinalWp = new Waypoint(newGcCode, CacheTypes.Final, "", coorectedCoord.getLatitude(), coorectedCoord.getLongitude(), cache.Id, "", "Final GSAK Corrected");

                cache.waypoints.add(FinalWp);

                // the coordinates of the Cache are not changed. we have a Final with valid coordinates
                // cache.setCorrectedCoordinates(true);
            }
        }

        cache.setGPXFilename_ID(gpxFilename.Id);

        currentwpt++;

        StringBuilder info = new StringBuilder();

        info.append(mDisplayFilename);
        info.append(GlobalCore.br);
        info.append("Cache: ");
        info.append(currentwpt);
        info.append("/");
        info.append(countwpt);

        if (errors > 0) {
            info.append(GlobalCore.br);
            info.append("Errors: ");
            info.append(errors);
        }

        if (mip != null)
            mip.ProgressInkrement("ImportGPX", info.toString(), false);

        // Write Note and Solver
        if (values.containsKey("cachebox-extension_solver")) {
            Database.SetSolver(cache.Id, values.get("cachebox-extension_solver"));
        }
        if (values.containsKey("cachebox-extension_note")) {
            Database.SetNote(cache.Id, values.get("cachebox-extension_note"));
        }

        // Merge mit cache info
        if (CacheInfoList.ExistCache(cache.getGcCode())) {
            CacheInfoList.mergeCacheInfo(cache);
        } else {
            // Neue CacheInfo erstellen und zur Liste Hinzufügen
            CacheInfoList.putNewInfo(cache);
        }

        // write to Database
        mImportHandler.handleCache(cache);

    }

    private void createWaypoint(Map<String, String> values) throws Exception {
        if (values.containsKey("wpt_attribute_lat") && values.containsKey("wpt_attribute_lon")) {
            waypoint.Pos = new CoordinateGPS(new Double(values.get("wpt_attribute_lat")).doubleValue(), new Double(values.get("wpt_attribute_lon")).doubleValue());
        } else {
            waypoint.Pos = new CoordinateGPS(0, 0);
        }

        if (values.containsKey("wpt_name")) {
            waypoint.setGcCode(values.get("wpt_name"));
            waypoint.setTitle(waypoint.getGcCode());
            // TODO Hack to get parent Cache

            if (values.containsKey("cache_gsak_Parrent")) {
                String parent = values.get("cache_gsak_Parrent");
                waypoint.CacheId = Cache.GenerateCacheId(parent);
            } else {
                waypoint.CacheId = Cache.GenerateCacheId("GC" + waypoint.getGcCode().substring(2, waypoint.getGcCode().length()));
            }

        }

        if (values.containsKey("wpt_desc")) {
            waypoint.setTitle(values.get("wpt_desc"));
        }

        if (values.containsKey("wpt_type")) {
            String typeString = values.get("wpt_type");
            if (typeString.contains("Waypoint|Flag")) {
                typeString = values.get("wpt_desc");
            }

            waypoint.parseTypeString(typeString);
        }

        if (values.containsKey("wpt_cmt")) {
            waypoint.setDescription(values.get("wpt_cmt"));
        }

        if (values.containsKey("cachebox-extension_clue")) {
            waypoint.setClue(values.get("cachebox-extension_clue"));
        }

        currentwpt++;
        if (mip != null)
            mip.ProgressInkrement("ImportGPX", mDisplayFilename + "\nWaypoint: " + currentwpt + "/" + countwpt + "\n" + waypoint.getGcCode() + " - " + waypoint.getDescription(), false);

        mImportHandler.handleWaypoint(waypoint);

        waypoint.clear();

    }

    private static Date parseDate(String text) throws Exception {
        Date date = parseDateWithFormat(DATE_PATTERN_1, text);
        if (date != null) {
            return date;
        } else {
            date = parseDateWithFormat(DATE_PATTERN_2, text);
            if (date != null) {
                return date;
            } else {
                date = parseDateWithFormat(DATE_PATTERN_3, text);
                if (date != null) {
                    return date;
                } else {
                    throw new RuntimeException("Illegal date format");
                }
            }
        }
    }

    private static Date parseDateWithFormat(SimpleDateFormat df, String text) throws Exception {
        // TODO write an own parser, original works but to match.

        Date date = null;
        try {
            date = df.parse(text);
        } catch (ParseException e) {
        }
        return date;
    }
}
