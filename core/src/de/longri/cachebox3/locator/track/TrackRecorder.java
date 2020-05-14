/*
 * Copyright (C) 2014-2020 team-cachebox.de
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
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.location.PositionChangedEvent;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.locator.Coordinate;
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
import java.util.Locale;
import java.util.TimeZone;

public class TrackRecorder implements PositionChangedListener {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackRecorder.class);
    private static TrackRecorder trackRecorder;
    private final String gpxHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n";
    public boolean pauseRecording = false;
    public boolean recording = false;
    public double saveAltitude = 0;
    public Coordinate lastRecordedPosition = null;
    String mFriendlyName = "";
    String mMediaPath = "";
    Coordinate mMediaCoord = null;
    String mTimestamp = "";
    private FileHandle gpxfile = null;
    private boolean writeAnnotateMedia = false;

    private int insertPos = 24;

    private boolean mustWriteMedia = false;
    private boolean mustRecPos = false;
    private boolean writePos = false;

    private TrackRecorder() {
    }

    public static TrackRecorder getInstance() {
        if (trackRecorder == null) trackRecorder = new TrackRecorder();
        return trackRecorder;
    }

    public void startRecording() {

        EventHandler.add(this);

        CB.currentRoute = new Track(Translation.get("actualTrack"));
        CB.currentRoute.setColor(Color.BLUE);
        CB.currentRoute.setVisible(true);
        CB.currentRoute.setActualTrack(true);
        CB.currentRouteCount = 0;
        CB.currentRoute.setTrackLength(0);
        CB.currentRoute.setAltitudeDifference(0);

        String directory = Settings.TrackFolder.getValue();

        if (gpxfile == null) {
            gpxfile = Gdx.files.absolute(directory + "/" + generateTrackFileName());
            gpxfile.parent().mkdirs();
            gpxfile.writeString(gpxHeader + "<time>" + getDateTimeString() + "</time>\n<trk><trkseg>\n</trkseg>\n</trk>\n</gpx>\n", true);
            // set real bounds or basecamp (mapsource) will not import this track
            // "<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n";

        }

        pauseRecording = false;
        recording = true;

        // updateRecorderButtonAccessibility();
    }

    private String getDateTimeString() {
        Date timestamp = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp);
        datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sDate += "T" + datFormat.format(timestamp) + "Z";
        return sDate;
    }

    public void annotateMedia(final String friendlyName, final String mediaPath, final Coordinate location, final String timestamp) {
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

        String xml = "<wpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude()
                + "\">\n   <ele>" + location.getElevation() + "</ele>\n   <time>" + timestamp + "</time>\n"
                + "   <name>" + friendlyName + "</name>\n   <link href=\"" + mediaPath + "\" />\n</wpt>\n";

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
        positionChanged(new PositionChangedEvent(location, true));
    }

    public void pauseRecording() {
        pauseRecording = !pauseRecording;
    }

    public void stopRecording() {
        EventHandler.remove(this);
        if (CB.currentRoute != null) {
            CB.currentRoute.setActualTrack(false);
            CB.currentRoute.setName(Translation.get("recordetTrack"));
        }
        pauseRecording = false;
        recording = false;
        gpxfile = null;
    }

    private String generateTrackFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US);
        String sDate = dateFormat.format(new Date());

        return "Track_" + sDate + ".gpx";
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
        if (gpxfile == null || pauseRecording || !event.gpsProvided)
            return;

        if (writeAnnotateMedia) {
            mustRecPos = true;
        }

        Coordinate newCoord = EventHandler.getMyPosition();

        if (lastRecordedPosition == null) {
            // wait for 2 coords
            lastRecordedPosition = newCoord;
            saveAltitude = newCoord.getElevation();
        } else {
            writePos = true;
            Coordinate newPoint;
            double altitudeDifference = 0;

            // wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
            float[] dist = new float[1];

            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, lastRecordedPosition.getLatitude(), lastRecordedPosition.getLongitude(), event.pos.getLatitude(), event.pos.getLongitude(), dist);
            float cachedDistance = dist[0];

            if (cachedDistance > Config.TrackDistance.getValue()) {
                StringBuilder sb = new StringBuilder();
                sb.append("<trkpt lat=\"").append(event.pos.getLatitude()).append("\" lon=\"").append(event.pos.getLongitude()).append("\">\n")
                        .append("   <ele>").append(newCoord.getElevation()).append("</ele>\n")
                        .append("   <time>").append(getDateTimeString()).append("</time>\n")
                        .append("   <course>").append(newCoord.getHeading()).append("</course>\n")
                        .append("   <speed>").append(newCoord.getSpeed()).append("</speed>\n")
                        .append("</trkpt>\n");

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
                    log.error("Trackrecorder IOException", e);
                }

                newPoint = new Coordinate(event.pos.getLatitude(), event.pos.getLongitude(), newCoord.getElevation(),
                        newCoord.getHeading(), new Date());

                CB.currentRoute.getTrackPoints().add(newPoint);

                // notify TrackListView
                //TODO notify TrackListView
//				if (TrackListView.that != null)
//					TrackListView.that.notifyActTrackChanged();
//
//				RouteOverlay.RoutesChanged();
                lastRecordedPosition = newCoord;
                CB.currentRoute.setTrackLength(CB.currentRoute.getTrackLength() + cachedDistance);

                altitudeDifference = Math.abs(saveAltitude - newCoord.getElevation());
                if (altitudeDifference >= 25) {
                    CB.currentRoute.setAltitudeDifference(CB.currentRoute.getAltitudeDifference() + altitudeDifference);
                    saveAltitude = newCoord.getElevation();
                }
                writePos = false;

                if (mustWriteMedia) {
                    mustWriteMedia = false;
                    annotateMedia(mFriendlyName, mMediaPath, mMediaCoord, mTimestamp);
                }
            }
        }
    }
}
