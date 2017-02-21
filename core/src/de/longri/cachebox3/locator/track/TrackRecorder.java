/* 
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.locator.track;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.locator.Location;
import de.longri.cachebox3.locator.Locator;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.MathUtils;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TrackRecorder {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackRecorder.class);
    // StreamWriter outStream = null;
    private static FileHandle gpxfile = null;
    public static boolean pauseRecording = false;
    public static boolean recording = false;
    public static double SaveAltitude = 0;

    // / Letzte aufgezeichnete Position des Empfängers
    public static Location LastRecordedPosition = Location.NULL_LOCATION;

    public static void StartRecording() {

        CB.actRoute = new Track(Translation.Get("actualTrack"), Color.BLUE);
        CB.actRoute.ShowRoute = true;
        CB.actRoute.IsActualTrack = true;
        CB.actRouteCount = 0;
        CB.actRoute.TrackLength = 0;
        CB.actRoute.AltitudeDifference = 0;

        String directory = Settings.TrackFolder.getValue();

        if (gpxfile == null) {
            gpxfile = Gdx.files.absolute(directory + "/" + generateTrackFileName());
            gpxfile.mkdirs();

            writeAppend("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeAppend(
                    "<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
            writeAppend("<time>" + GetDateTimeString() + "</time>\n");
            // set real bounds or basecamp (mapsource) will not import this track
            // writeAppend("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");
            writeAppend("<trk><trkseg>\n");

            writeAppend("</trkseg>\n");
            writeAppend("</trk>\n");

            writeAppend("</gpx>\n");

        }

        pauseRecording = false;
        recording = true;

        // updateRecorderButtonAccessibility();
    }

    private static void writeAppend(String txt) {
        if (gpxfile == null) return;
        gpxfile.writeString(txt, true);
    }

    private static String GetDateTimeString() {
        Date timestamp = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp);
        datFormat = new SimpleDateFormat("HH:mm:ss");
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sDate += "T" + datFormat.format(timestamp) + "Z";
        return sDate;
    }

    private static boolean writeAnnotateMedia = false;

    private static int insertPos = 24;

    private static boolean mustWriteMedia = false;
    static String mFriendlyName = "";
    static String mMediaPath = "";
    static Location mMediaCoord = null;
    static String mTimestamp = "";

    public static void AnnotateMedia(final String friendlyName, final String mediaPath, final Location location, final String timestamp) {
        writeAnnotateMedia = true;

        if (writePos) {
            mFriendlyName = friendlyName;
            mMediaPath = mediaPath;
            mMediaCoord = location;
            mTimestamp = timestamp;
            mustWriteMedia = true;
        }

        if (gpxfile == null)
            return;

        String xml = "<wpt lat=\"" + String.valueOf(location.getLatitude()) + "\" lon=\"" + String.valueOf(location.getLongitude()) + "\">\n" + "   <ele>" + String.valueOf(location.getAltitude()) + "</ele>\n" + "   <time>" + timestamp + "</time>\n"
                + "   <name>" + friendlyName + "</name>\n" + "   <link href=\"" + mediaPath + "\" />\n" + "</wpt>\n";

        RandomAccessFile rand;
        try {
            rand = new RandomAccessFile(gpxfile.file(), "rw");
            int i = (int) rand.length();

            byte[] bEnde = new byte[8];

            rand.seek(i - 8); // Seek to start point of file

            for (int ct = 0; ct < 8; ct++) {
                bEnde[ct] = rand.readByte(); // read byte from the file
            }

            // insert point

            byte[] b = xml.getBytes();

            rand.setLength(i + b.length);
            rand.seek(i - 8);
            rand.write(b);
            rand.write(bEnde);
            rand.close();

            insertPos += b.length;

        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (IOException e) {
            log.error("IOException", e);
        }
        writeAnnotateMedia = false;
        if (mustRecPos) {
            mustRecPos = false;
        }
        recordPosition();
    }

    private static boolean mustRecPos = false;
    private static boolean writePos = false;

    private final static Location.ProviderType GPS = Location.ProviderType.GPS;
    private final static Locator.CompassType _GPS = Locator.CompassType.GPS;

    public static void recordPosition() {

        if (gpxfile == null || pauseRecording || !Locator.isGPSprovided())
            return;

        if (writeAnnotateMedia) {
            mustRecPos = true;
        }

        if (LastRecordedPosition.getProviderType() == Location.ProviderType.NULL) // Warte bis 2 gültige Koordinaten vorliegen
        {
            LastRecordedPosition = Locator.getLocation(GPS).cpy();
            SaveAltitude = LastRecordedPosition.getAltitude();
        } else {
            writePos = true;
            TrackPoint NewPoint;
            double AltDiff = 0;

            // wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter
            // zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
            float[] dist = new float[1];

            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, LastRecordedPosition.getLatitude(), LastRecordedPosition.getLongitude(), Locator.getLatitude(GPS), Locator.getLongitude(GPS), dist);
            float cachedDistance = dist[0];

            if (cachedDistance > Config.TrackDistance.getValue()) {
                StringBuilder sb = new StringBuilder();

                sb.append("<trkpt lat=\"" + String.valueOf(Locator.getLatitude(GPS)) + "\" lon=\"" + String.valueOf(Locator.getLongitude(GPS)) + "\">\n");
                sb.append("   <ele>" + String.valueOf(Locator.getAlt()) + "</ele>\n");
                sb.append("   <time>" + GetDateTimeString() + "</time>\n");
                sb.append("   <course>" + String.valueOf(Locator.getHeading(_GPS)) + "</course>\n");
                sb.append("   <speed>" + String.valueOf(Locator.SpeedOverGround()) + "</speed>\n");
                sb.append("</trkpt>\n");

                RandomAccessFile rand;
                try {
                    rand = new RandomAccessFile(gpxfile.file(), "rw");

                    // suche letzte "</trk>"

                    int i = (int) rand.length();
                    byte[] bEnde = new byte[insertPos];
                    rand.seek(i - insertPos); // Seek to start point of file

                    for (int ct = 0; ct < insertPos; ct++) {
                        bEnde[ct] = rand.readByte(); // read byte from the file
                    }

                    // insert point
                    byte[] b = sb.toString().getBytes();
                    rand.setLength(i + b.length);
                    rand.seek(i - insertPos);
                    rand.write(b);
                    rand.write(bEnde);
                    rand.close();
                } catch (FileNotFoundException e) {
                    log.error("FileNotFoundException", e);
                } catch (IOException e) {
                    log.error("Trackrecorder", "IOException", e);
                }

                NewPoint = new TrackPoint(Locator.getLongitude(GPS), Locator.getLatitude(GPS), Locator.getAlt(), Locator.getHeading(_GPS), new Date());

                CB.actRoute.Points.add(NewPoint);

                // notify TrackListView
                //TODO notify TrackListView
//				if (TrackListView.that != null)
//					TrackListView.that.notifyActTrackChanged();
//
//				RouteOverlay.RoutesChanged();
                LastRecordedPosition = Locator.getLocation(GPS).cpy();
                CB.actRoute.TrackLength += cachedDistance;

                AltDiff = Math.abs(SaveAltitude - Locator.getAlt());
                if (AltDiff >= 25) {
                    CB.actRoute.AltitudeDifference += AltDiff;
                    SaveAltitude = Locator.getAlt();
                }
                writePos = false;

                if (mustWriteMedia) {
                    mustWriteMedia = false;
                    AnnotateMedia(mFriendlyName, mMediaPath, mMediaCoord, mTimestamp);
                }
            }
        }
    }

    public static void PauseRecording() {
        pauseRecording = !pauseRecording;
    }

    public static void StopRecording() {
        if (CB.actRoute != null) {
            CB.actRoute.IsActualTrack = false;
            CB.actRoute.Name = Translation.Get("recordetTrack");
        }
        pauseRecording = false;
        recording = false;
        gpxfile = null;
    }

    private static String generateTrackFileName() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String sDate = datFormat.format(new Date());

        return "Track_" + sDate + ".gpx";
    }

}
