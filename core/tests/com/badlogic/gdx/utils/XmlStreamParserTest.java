package com.badlogic.gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.utils.CharSequenceUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;

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


    @Test
    void parse() {

        final Logger log = LoggerFactory.getLogger(XmlStreamParserTest.class);

        final FileHandle testFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC2T9RW.gpx");
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
                    double lat = CharSequenceUtil.parseDouble(data, offset, length);
                    assertThat("Value should be 49.349817", lat == 49.349817);
                }

            }
        }, MIN_LAT, MAX_LAT);


        parser.parse(testFile);

    }
}