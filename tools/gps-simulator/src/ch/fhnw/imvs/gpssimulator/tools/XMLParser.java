/*
 * Copyright (c) 2007 by the University of Applied Sciences Northwestern Switzerland (FHNW)
 * 
 * This program can be redistributed or modified under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 * This program is distributed without any warranty or implied warranty
 * of merchantability or fitness for a particular purpose.
 *
 * See the GNU General Public License for more details.
 */

package ch.fhnw.imvs.gpssimulator.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;

public class XMLParser extends DefaultHandler {

    private static SimpleDateFormat datePattern1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
    private static SimpleDateFormat datePattern2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static SimpleDateFormat datePattern3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private final List<XMLData> xmlData;
    private int time = 0;
    private double lastLatitude;
    private double lastLongitude;
    private double lastAltitude;
    private Date beginnTime;
    private int lastTime;
    private String LastValue;
    private double lastCourse;
    private double lastSpeed;

    private XMLParser() {
        time = 0;
        xmlData = new ArrayList<XMLData>();
    }

    public static List<XMLData> parse(File file) throws IOException {
        try {
            XMLParser h = new XMLParser();

            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(h);
            xr.setErrorHandler(h);
            FileReader r = new FileReader(file.getAbsoluteFile());
            xr.parse(new InputSource(r));

            return h.xmlData;
        } catch (SAXException e) {
            return null;
        }
    }

    // private double convert(double deg){
    // int sig = deg>0?1:-1;
    // deg = Math.abs(deg);
    // int degInt = (int) deg;
    // double decFrac = deg - degInt;
    // return sig * (100*degInt + 60*decFrac);
    // }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (qName.equals("waypoint")) {
            int timeDiff = Integer.parseInt(atts.getValue("time"));
            double latitude = Double.parseDouble(atts.getValue("latitude"));
            double longitude = Double.parseDouble(atts.getValue("longitude"));
            double altitude = Double.parseDouble(atts.getValue("altitude"));
            double dCource = Double.parseDouble(atts.getValue("course"));
            time = time + timeDiff;
            xmlData.add(new XMLData(time, latitude, longitude, altitude, (int) dCource,0));

        }

        if (qName.equals("trkpt")) {
            lastLatitude = Double.parseDouble(atts.getValue("lat"));
            lastLongitude = Double.parseDouble(atts.getValue("lon"));
        }


    }



    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        LastValue = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String name, String qName) {

        if (qName.equals("ele")) {
            lastAltitude = Double.parseDouble(LastValue);
        }

        if (qName.equals("time")) {
            if (beginnTime == null) {
                try {
                    beginnTime = parseDate(LastValue);
                    return;
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            try {
                // lastTime = (int) (Math.abs(parseDate(LastValue).getTime() - beginnTime.getTime()) / 1000);
                lastTime = (int) (Math.abs(parseDate(LastValue).getTime() - beginnTime.getTime()));
            } catch (Exception e) {

                e.printStackTrace();
            }
        }


        if (qName.equals("course")) {
            double dCource = Double.parseDouble(LastValue);
            lastCourse =  dCource;
        }

        if (qName.equals("speed")) {
            lastSpeed = Double.parseDouble(LastValue);

        }

        if (qName.equals("trkpt")) {
            xmlData.add(new XMLData(lastTime, lastLatitude, lastLongitude, lastAltitude, lastCourse, lastSpeed));
        }
    }

    private static Date parseDate(String text) throws Exception {
        Date date = parseDateWithFormat(datePattern1, text);
        if (date != null) {
            return date;
        } else {
            date = parseDateWithFormat(datePattern2, text);
            if (date != null) {
                return date;
            } else {
                date = parseDateWithFormat(datePattern3, text);
                if (date != null) {
                    return date;
                } else {
                    throw new XMLParseException(null, "Illegal date format");
                }
            }
        }
    }

    private static Date parseDateWithFormat(SimpleDateFormat df, String text) throws Exception {
        // TODO hier m�sste mal �ber die Zeitzone nachgedacht werden -
        // irgendwas ist an den Daten, die von GC.com kommen, komisch
        Date date = null;
        try {
            date = df.parse(text);
        } catch (ParseException e) {
        }
        return date;
    }

}
