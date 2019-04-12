package gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlStreamParser;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.utils.CharSequenceUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 03.04.2018.
 */
class XmlStreamParserTest {

    static {
        TestUtils.initialGdx();
    }


    private final char[] GPX_NAME = "Cache Listing Generated from Geocaching.com".toCharArray();
    private final char[] GROUNDSPEAK_COUNTRY = "Germany".toCharArray();
    private final char[] ATTRIBUTE1 = "Bicycles".toCharArray();
    private final char[] ATTRIBUTE2 = "Available at all times".toCharArray();
    private final char[] ATTRIBUTE3 = "Public restrooms nearby".toCharArray();
    private final char[] ATTRIBUTE4 = "Parking available".toCharArray();
    private final char[] ATTRIBUTE5 = "Fuel Nearby".toCharArray();
    private final char[] ATTRIBUTE6 = "Hunting".toCharArray();
    private final char[] ATTRIBUTE7 = "Short hike (less than 1km)".toCharArray();
    private final char[] ATTRIBUTE8 = "Climbing gear".toCharArray();
    private final char[] ATTRIBUTE9 = "Ticks".toCharArray();
    private final char[] ATTRIBUTE10 = "Dogs".toCharArray();
    private long id;
    private boolean available;
    private boolean archived;
    private double lat;
    private double lon;
    private String longDescription;


    @Test
    void parse() {

        final Logger log = LoggerFactory.getLogger(XmlStreamParserTest.class);

        final FileHandle testFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC2T9RW.gpx", true);
        final XmlStreamParser parser = new XmlStreamParser();
        parser.registerDataHandler("/gpx/name", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                int index = CharSequenceUtil.indexOf(data, offset, length, GPX_NAME, 0, GPX_NAME.length, 0);
                assertThat("Value should be \"Cache Listing Generated from Geocaching.com\"", index == 0);
            }
        });

        parser.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:country", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                int index = CharSequenceUtil.indexOf(data, offset, length, GROUNDSPEAK_COUNTRY, 0, GROUNDSPEAK_COUNTRY.length, 0);
                assertThat("Value should be \"Germany\"", index == 0);
            }
        });

        final AtomicInteger attributeCount = new AtomicInteger(0);
        parser.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                char[] expected = null;
                switch (attributeCount.incrementAndGet()) {
                    case 1:
                        expected = ATTRIBUTE1;
                        break;
                    case 2:
                        expected = ATTRIBUTE2;
                        break;
                    case 3:
                        expected = ATTRIBUTE3;
                        break;
                    case 4:
                        expected = ATTRIBUTE4;
                        break;
                    case 5:
                        expected = ATTRIBUTE5;
                        break;
                    case 6:
                        expected = ATTRIBUTE6;
                        break;
                    case 7:
                        expected = ATTRIBUTE7;
                        break;
                    case 8:
                        expected = ATTRIBUTE8;
                        break;
                    case 9:
                        expected = ATTRIBUTE9;
                        break;
                    case 10:
                        expected = ATTRIBUTE10;
                        break;
                }
                int index = CharSequenceUtil.indexOf(data, offset, length, expected, 0, expected.length, 0);
                assertThat("Value should be " + new String(expected), index == 0);
            }
        });


        final char[] MIN_LAT = "minlat".toCharArray();
        final char[] MAX_LAT = "maxlat".toCharArray();
        parser.registerValueHandler("/gpx/bounds", new XmlStreamParser.ValueHandler() {
            @Override
            public void handleValue(char[] valueName, char[] data, int offset, int length) {
                if (CharSequenceUtil.equals(MIN_LAT, valueName)) {
                    lat = CharSequenceUtil.parseDouble(data, offset, length);

                } else if (CharSequenceUtil.equals(MAX_LAT, valueName)) {
                    lon = CharSequenceUtil.parseDouble(data, offset, length);

                }

            }
        }, MIN_LAT, MAX_LAT);


        final char[] ID = "id".toCharArray();
        final char[] AVAILABLE = "available".toCharArray();
        final char[] ARCHIVED = "archived".toCharArray();
        parser.registerValueHandler("/gpx/wpt/groundspeak:cache", new XmlStreamParser.ValueHandler() {
            @Override
            public void handleValue(char[] valueName, char[] data, int offset, int length) {
                if (CharSequenceUtil.equals(ID, valueName)) {
                    id = CharSequenceUtil.parseLong(data, offset, length);

                } else if (CharSequenceUtil.equals(AVAILABLE, valueName)) {
                    available = CharSequenceUtil.parseBoolean(data, offset, length);

                } else if (CharSequenceUtil.equals(ARCHIVED, valueName)) {
                    archived = CharSequenceUtil.parseBoolean(data, offset, length);

                }
            }
        }, ID, AVAILABLE, ARCHIVED);

        parser.registerDataHandler("/gpx/wpt/groundspeak:cache/groundspeak:long_description", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                longDescription = new String(data, offset, length);
            }
        });


        parser.parse(testFile);

        assertThat("Value should be 49.349817", lat == 49.349817);
        assertThat("Value should be 49.351283", lon == 49.351283);

        assertThat("Value should be 2190117", id == 2190117L);
        assertThat("Value should be true", available == true);
        assertThat("Value should be false", archived == false);


        longDescription = longDescription.replaceAll("\r\n", "\n");
        assertEquals(expectedLongDescription, longDescription, "Description Wrong");

    }

    private final String expectedLongDescription = "&lt;br /&gt;\n" +
            "&lt;center&gt;&lt;img src=\n" +
            "\"http://img.geocaching.com/cache/9b0334c7-c419-41c8-b883-8bb0adf20ac3.jpg\" /&gt;&lt;br /&gt;\n" +
            "\n" +
            "&lt;br /&gt;\n" +
            "&lt;font face=\"tahoma\" size=\"3\" color=\"#330033\"&gt;&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "Der Hampir, so sagt man, optisch ein liebes zartes Wesen&lt;br /&gt;\n" +
            "im dunklen Hardtwald treibt er seine Spesen.&lt;br /&gt;\n" +
            "So süß, so flauschig sogleich&lt;br /&gt;\n" +
            "auch sein Fell so samtig und weich!&lt;br /&gt;\n" +
            "Deshalb lass dich blos nicht blenden,&lt;br /&gt;\n" +
            "sonst könnte es sehr böse für dich enden!&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "Aaaaaber wenn du ihn entdeckst,&lt;br /&gt;\n" +
            "so achte dich vor ihm, die Gefahr besteht dass du vergisst&lt;br /&gt;\n" +
            "und vor lauter Kummer und Sorgen ihm tief in die Augen\n" +
            "erblickst!!&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "Es ist dann zu spät!&lt;br /&gt;\n" +
            "Dann hat dich der Hampir bereits erspäht!!&lt;br /&gt;\n" +
            "Der Hampir, so sagt man erschallt sein Gelächter&lt;br /&gt;\n" +
            "wenn es Beute vor sich hat, so schaurig so grell,&lt;br /&gt;\n" +
            "rette dich wenn du kannst schneller als schnell!&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "Und wage dich nicht in den Wald&lt;br /&gt;\n" +
            "in der Nacht beim Vollmond ist es dort bitterkalt!&lt;br /&gt;\n" +
            "Nebelschwaden dort, aber die schaurige Gestalten&lt;br /&gt;\n" +
            "verstecken sich im dunkeln mit dem Gedanken,&lt;br /&gt;\n" +
            "ihre Beute noch schneller zu jagen als der Hampir!&lt;br /&gt;\n" +
            "Dennoch willst du in den Wald?! Überlege es dir!!&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "Du meinst, ach was... Hampire... die gibt es doch nicht?!&lt;br /&gt;\n" +
            "Die Hasen die warnen: HIER wartet er auf dich!!!&lt;br /&gt;\n" +
            "&lt;br /&gt;&lt;/font&gt;&lt;/center&gt;\n" +
            "&lt;font face=\"tahoma\" size=\"3\" color=\"#330033\"&gt;&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "Fotos dürft Ihr gerne machen &lt;img src=\n" +
            "'http://www.geocaching.com/images/icons/icon_smile_big.gif' border=\n" +
            "\"0\" align=\"middle\" /&gt;&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "&lt;br /&gt;\n" +
            "ein besonderer Dank an Monas Cacherteam, für die handwerkliche\n" +
            "Meisterleistung!!&lt;br /&gt;\n" +
            "Es ist genau so geworden, wie es sich die Hasen vorgestellt\n" +
            "haben!!&lt;br /&gt;\n" +
            "&lt;br /&gt;&lt;/font&gt;&lt;br /&gt;\n" +
            "&lt;a href=\"http://www.andyhoppe.com/\" title=\n" +
            "\"Counter/Zähler\"&gt;&lt;img src=\"http://c.andyhoppe.com/1302990447\"\n" +
            "style=\"border:none\" alt=\"Counter/Zähler\" /&gt;&lt;/a&gt;&lt;p&gt;Additional Hidden Waypoints&lt;/p&gt;PK2T9RW - GC2T9RW Parking&lt;br /&gt;N 49° 21.077 E 008° 37.840&lt;br /&gt;Raststätte Hardtwald West.\n" +
            "Und für Ortskundige: einfach Richtung ADAC Übungsgelände. Dann müsst Ihr nicht auf die Autobahn.&lt;br /&gt;";
}