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
import de.longri.cachebox3.gui.views.TrackListView;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.core.GeoPoint;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TrackRecorder implements PositionChangedListener {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackRecorder.class);
    private static TrackRecorder trackRecorder;
    private Track recordingTrack;
    private boolean isStarted;
    private boolean isPaused;
    private FileHandle gpxFile;
    private String annotationDataFriendlyName;
    private String annotationDataMediaPath;
    private Coordinate annotationDataMediaCoordinate;
    private String annotationDataTimestamp;
    private Coordinate lastRecordedPosition;
    private double lastElevation;
    private int insertPosition;
    private boolean duringTrackPointWriting;
    private boolean duringAnnotateMediaWriting;
    private boolean stillHaveToAnnotateMedia;

    private TrackRecorder() {
        isStarted = false;
        isPaused = false;
        gpxFile = null;
        lastRecordedPosition = null;
        lastElevation = 0;
        duringTrackPointWriting = false;
        duringAnnotateMediaWriting = false;
        stillHaveToAnnotateMedia = false;
    }

    public static TrackRecorder getInstance() {
        if (trackRecorder == null) trackRecorder = new TrackRecorder();
        return trackRecorder;
    }

    public void startRecording() {

        EventHandler.add(this);

        recordingTrack = new Track(Translation.get("actualTrack"));
        recordingTrack.setColor(Color.BLUE);
        recordingTrack.setVisible(true);
        recordingTrack.setTrackLength(0);
        recordingTrack.setElevationDifference(0);

        if (gpxFile == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US);
            String sDate = dateFormat.format(new Date());
            gpxFile = Gdx.files.absolute(Settings.TrackFolder.getValue() + "/Track_" + sDate + ".gpx");
            gpxFile.parent().mkdirs();
            String gpxHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n";
            gpxFile.writeString(gpxHeader + "<time>" + getDateTimeString() + "</time>\n<trk><trkseg>" + "\n</trkseg>\n</trk>\n</gpx>\n", true);
            insertPosition = 24; // 24 characters from the end (before \n</trackseg>...)
            // set real bounds or basecamp (mapsource) will not import this track
            // "<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n";
        }

        isPaused = false;
        isStarted = true;
        duringTrackPointWriting = false;
        duringAnnotateMediaWriting = false;
        // updateRecorderButtonAccessibility();
    }

    public void pauseRecording() {
        isPaused = true;
    }

    public void continueRecording() {
        isPaused = false;
    }

    public void stopRecording() {
        EventHandler.remove(this);
        if (recordingTrack != null) {
            recordingTrack.setName(Translation.get("recordetTrack"));
        }
        isPaused = false;
        isStarted = false;
        gpxFile = null;
    }

    public void annotateMedia(final String friendlyName, final String mediaPath, final Coordinate location, final String timestamp) {
        if (!isStarted) return;

        duringAnnotateMediaWriting = true;

        if (duringTrackPointWriting) {
            // remember annotation data
            annotationDataFriendlyName = friendlyName;
            annotationDataMediaPath = mediaPath;
            annotationDataMediaCoordinate = location;
            annotationDataTimestamp = timestamp;
            stillHaveToAnnotateMedia = true; // do not ignore
            return;
        }

        // write wpt (way point) to gpx track file
        String xml = "<wpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude()
                + "\">\n   <ele>" + location.getElevation() + "</ele>\n   <time>" + timestamp + "</time>\n"
                + "   <name>" + friendlyName + "</name>\n   <link href=\"" + mediaPath + "\" />\n</wpt>\n";

        RandomAccessFile rand;
        try {
            rand = new RandomAccessFile(gpxFile.file(), "rw");
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

            insertPosition += b.length;

        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (IOException e) {
            log.error("IOException", e);
        }
        duringAnnotateMediaWriting = false;
        // also write trkpt (track point) to gpx track file
        positionChanged(new PositionChangedEvent(location, true));
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

    @Override
    public void positionChanged(PositionChangedEvent event) {
        if (gpxFile == null || isPaused || !event.gpsProvided)
            return;

        if (duringAnnotateMediaWriting || duringTrackPointWriting) {
            return; // ignore this position change
        }

        Coordinate newCoord = EventHandler.getMyPosition(); // the event possibly has no elevation

        if (lastRecordedPosition == null) {
            // wait for 2 Coordinates
            lastRecordedPosition = newCoord;
            lastElevation = newCoord.getElevation();
        } else {
            Coordinate newPoint;
            double elevationDifference = 0;

            // wurden seit dem letzten aufgenommenen Wegpunkt mehr als x Meter zurückgelegt? Wenn nicht, dann nicht aufzeichnen.
            float[] dist = new float[1];

            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, lastRecordedPosition.getLatitude(), lastRecordedPosition.getLongitude(), event.pos.getLatitude(), event.pos.getLongitude(), dist);
            float distanceToLastRecordedPosition = dist[0];

            if (distanceToLastRecordedPosition > Config.TrackDistance.getValue()) {
                duringTrackPointWriting = true;
                StringBuilder sb = new StringBuilder();
                sb.append("<trkpt lat=\"").append(event.pos.getLatitude()).append("\" lon=\"").append(event.pos.getLongitude()).append("\">\n")
                        .append("   <ele>").append(newCoord.getElevation()).append("</ele>\n")
                        .append("   <time>").append(getDateTimeString()).append("</time>\n")
                        .append("   <course>").append(newCoord.getHeading()).append("</course>\n")
                        .append("   <speed>").append(newCoord.getSpeed()).append("</speed>\n")
                        .append("</trkpt>\n");

                RandomAccessFile rand;
                // update file
                try {
                    rand = new RandomAccessFile(gpxFile.file(), "rw");

                    // suche letzte "</trk>"

                    int i = (int) rand.length();
                    byte[] bEnde = new byte[insertPosition];
                    rand.seek(i - insertPosition); // Seek to start point of file

                    for (int ct = 0; ct < insertPosition; ct++) {
                        bEnde[ct] = rand.readByte(); // read byte from the file
                    }

                    // insert point
                    byte[] b = sb.toString().getBytes();
                    rand.setLength(i + b.length);
                    rand.seek(i - insertPosition);
                    rand.write(b);
                    rand.write(bEnde);
                    rand.close();
                } catch (FileNotFoundException e) {
                    log.error("FileNotFoundException", e);
                } catch (IOException e) {
                    log.error("Trackrecorder IOException", e);
                }
                // update Track (recordingTrack)
                newPoint = new Coordinate(event.pos.getLatitude(), event.pos.getLongitude(), newCoord.getElevation(), newCoord.getHeading(), new Date());
                recordingTrack.add(newPoint);
                lastRecordedPosition = newCoord;
                recordingTrack.setTrackLength(recordingTrack.getTrackLength() + distanceToLastRecordedPosition);
                elevationDifference = Math.abs(lastElevation - newCoord.getElevation());
                if (elevationDifference >= 25) {
                    recordingTrack.setElevationDifference(recordingTrack.getElevationDifference() + elevationDifference);
                    lastElevation = newCoord.getElevation();
                }
                // update TrackList View
                if (CB.viewmanager.getCurrentView() instanceof TrackListView) {
                    TrackListView trackListView = (TrackListView) CB.viewmanager.getCurrentView();
                    if (trackListView.currentRecordingTrackItem != null)
                        trackListView.currentRecordingTrackItem.notifyTrackChanged();
                }
                // update map View (even if "hidden")
                recordingTrack.addPointToTrackLayer(new GeoPoint(event.pos.getLatitude(), event.pos.getLongitude()));
                // ready
                duringTrackPointWriting = false;
                // may be annotate
                if (stillHaveToAnnotateMedia) {
                    stillHaveToAnnotateMedia = false;
                    annotateMedia(annotationDataFriendlyName, annotationDataMediaPath, annotationDataMediaCoordinate, annotationDataTimestamp);
                }
            }
        }
    }

    public Track getRecordingTrack() {
        return recordingTrack;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isPaused() {
        return isPaused;
    }
}
