package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.track.Track;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.HSV_Color;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.serializable.BitStore;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Longri on 24.07.16.
 */
public class TrackListView extends AbstractView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackListView.class);

    public TrackListView(BitStore reader) {
        super(reader);
    }

    public TrackListView() {
        super("TrackListView");
    }

    @Override
    public void dispose() {
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("TrackListViewContextMenuTitle");
        cm.addMenuItem("load", null, this::selectTrackFileReadAndAddToTracks);
        // cm.addMenuItem("generate", null, () -> TrackCreation.getInstance().execute());
        return cm;
    }

    private void selectTrackFileReadAndAddToTracks() {
        FileChooser fc = new FileChooser(Translation.get("LoadTrack"), FileChooser.Mode.SAVE, FileChooser.SelectionMode.ALL, "gpx", "GPX");
        // button text Translation.get("load")
        fc.setDirectory(Config.TrackFolder.getValue(), false);
        fc.setSelectionReturnListener(fileHandle -> {
            if (fileHandle != null)
                readFromGpxFile(fileHandle);
        });
        fc.show();
    }

    public void readFromGpxFile(FileHandle abstractFile) {
        // !!! it is possible that a gpx file contains more than 1 <trk> segments
        // they are all added to the tracks (Tracklist)
        ArrayList<Track> tracks = new ArrayList<>();
        float[] dist = new float[4];
        double distance = 0;
        double altitudeDifference = 0;
        double deltaAltitude;
        Coordinate fromPosition = new Coordinate(0, 0);
        BufferedReader reader;
        HSV_Color trackColor = null;

        try {
            InputStreamReader isr = new InputStreamReader(abstractFile.read(), StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            Track track = new Track("");

            String line;
            String tmpLine;
            String gpxName = null;
            boolean isSeg = false;
            boolean isTrk = false;
            boolean isRte = false;
            boolean isTrkptOrRtept = false;
            boolean readName = false;
            int anzSegments = 0;

            Coordinate lastAcceptedCoordinate = null;
            double lastAcceptedDirection = -1;
            Date lastAcceptedTime = null;

            StringBuilder sb = new StringBuilder();
            String rline;
            while ((rline = reader.readLine()) != null) {
                for (int i = 0; i < rline.length(); i++) {
                    char nextChar = rline.charAt(i);
                    sb.append(nextChar);

                    if (nextChar == '>') {
                        line = sb.toString().trim().toLowerCase();
                        tmpLine = sb.toString();
                        sb = new StringBuilder();

                        if (!isTrk) // Begin of the Track detected?
                        {
                            if (line.contains("<trk>")) {
                                isTrk = true;
                                continue;
                            }
                        }

                        if (!isSeg) // Begin of the Track Segment detected?
                        {
                            if (line.contains("<trkseg>")) {
                                isSeg = true;
                                track = new Track("");
                                track.setFileName(abstractFile.path());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(abstractFile.name()); // FileIO.getFileName(file)
                                else {
                                    if (anzSegments <= 1)
                                        track.setName(gpxName);
                                    else
                                        track.setName(gpxName + anzSegments);
                                }
                                continue;
                            }
                        }

                        if (!isRte) // Begin of the Route detected?
                        {
                            if (line.contains("<rte>")) {
                                isRte = true;
                                track = new Track("");
                                track.setFileName(abstractFile.path());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(abstractFile.name()); // FileIO.getFileName(file)
                                else {
                                    if (anzSegments <= 1)
                                        track.setName(gpxName);
                                    else
                                        track.setName(gpxName + anzSegments);
                                }
                                continue;
                            }
                        }

                        if ((line.contains("<name>")) & !isTrkptOrRtept) // found <name>?
                        {
                            readName = true;
                            continue;
                        }

                        if (readName & !isTrkptOrRtept) {
                            int cdata_start;
                            int name_start = 0;
                            int name_end;

                            name_end = line.indexOf("</name>");

                            // Name contains cdata?
                            cdata_start = line.indexOf("[cdata[");
                            if (cdata_start > -1) {
                                name_start = cdata_start + 7;
                                name_end = line.indexOf("]");
                            }

                            if (name_end > name_start) {
                                // tmpLine, damit Groß-/Kleinschreibung beachtet wird
                                if (isSeg | isRte)
                                    track.setName(tmpLine.substring(name_start, name_end));
                                else
                                    gpxName = tmpLine.substring(name_start, name_end);
                            }

                            readName = false;
                            continue;
                        }

                        if (line.contains("</trkseg>")) // End of the Track Segment detected?
                        {
                            if (track.getTrackPoints().size < 2)
                                track.setName("no Route segment found");
                            track.setVisible(true);
                            track.setTrackLength(distance);
                            track.setAltitudeDifference(altitudeDifference);
                            tracks.add(track);
                            isSeg = false;
                            break;
                        }

                        if (line.contains("</rte>")) // End of the Route detected?
                        {
                            if (track.getTrackPoints().size < 2)
                                track.setName("no Route segment found");
                            track.setVisible(true);
                            track.setTrackLength(distance);
                            track.setAltitudeDifference(altitudeDifference);
                            tracks.add(track);
                            isRte = false;
                            break;
                        }

                        if ((line.contains("<trkpt")) | (line.contains("<rtept"))) {
                            isTrkptOrRtept = true;
                            // Trackpoint lesen
                            int lonIdx = line.indexOf("lon=\"") + 5;
                            int latIdx = line.indexOf("lat=\"") + 5;

                            int lonEndIdx = line.indexOf("\"", lonIdx);
                            int latEndIdx = line.indexOf("\"", latIdx);

                            String latStr = line.substring(latIdx, latEndIdx);
                            String lonStr = line.substring(lonIdx, lonEndIdx);

                            double lat = Double.parseDouble(latStr);
                            double lon = Double.parseDouble(lonStr);

                            lastAcceptedCoordinate = new Coordinate(lat, lon);
                        }

                        if (line.contains("</time>")) {
                            // Time lesen
                            int timIdx = line.indexOf("<time>") + 6;
                            if (timIdx == 5)
                                timIdx = 0;
                            int timEndIdx = line.indexOf("</time>", timIdx);

                            String timStr = line.substring(timIdx, timEndIdx);

                            lastAcceptedTime = parseDate(timStr);
                        }

                        if (line.contains("</course>")) {
                            // Course lesen
                            int couIdx = line.indexOf("<course>") + 8;
                            if (couIdx == 7)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</course>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);

                            lastAcceptedDirection = Double.parseDouble(couStr);

                        }

                        if ((line.contains("</ele>")) & isTrkptOrRtept) {
                            // Elevation lesen
                            int couIdx = line.indexOf("<ele>") + 5;
                            if (couIdx == 4)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</ele>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);

                            lastAcceptedCoordinate.setElevation(Double.parseDouble(couStr));

                        }

                        if (line.contains("</gpxx:colorrgb>")) {
                            // Color lesen
                            int couIdx = line.indexOf("<gpxx:colorrgb>") + 15;
                            if (couIdx == 14)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</gpxx:colorrgb>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);
                            trackColor = new HSV_Color(couStr);
                            track.setColor(trackColor);
                        }

                        if ((line.contains("</trkpt>")) | (line.contains("</rtept>")) | ((line.contains("/>")) & isTrkptOrRtept)) {
                            // trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
                            isTrkptOrRtept = false;
                            if (lastAcceptedCoordinate != null) {
                                track.getTrackPoints().add(new Coordinate(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getElevation(), lastAcceptedDirection, lastAcceptedTime));

                                // Calculate the length of a Track
                                if (!fromPosition.isValid()) {
                                    fromPosition = new Coordinate(lastAcceptedCoordinate);
                                    fromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                    // fromPosition.setValid(true);
                                } else {
                                    MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, fromPosition.getLatitude(), fromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getLongitude(), dist);
                                    distance = distance + dist[0];
                                    deltaAltitude = Math.abs(fromPosition.getElevation() - lastAcceptedCoordinate.getElevation());
                                    fromPosition = new Coordinate(lastAcceptedCoordinate);

                                    if (deltaAltitude >= 25.0) // nur aufaddieren wenn Höhenunterschied größer 10 Meter
                                    {
                                        fromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                        altitudeDifference = altitudeDifference + deltaAltitude;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException ex) {
            log.error("readFromGpxFile", ex);
        }
        for (Track track : tracks) {
            if (trackColor != null) track.setColor(trackColor);
            //RouteOverlay.getInstance().addTrack(track);
        }
        //notifyDataSetChanged();

    }

    private Date parseDate(String dateString) {
        try {
            final int year = Integer.parseInt(dateString.substring(0, 4));
            final int month = Integer.parseInt(dateString.substring(5, 7));
            final int day = Integer.parseInt(dateString.substring(8, 10));

            final int hour = Integer.parseInt(dateString.substring(11, 13));
            final int minute = Integer.parseInt(dateString.substring(14, 16));
            final int second = Integer.parseInt(dateString.substring(17, 19));

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1); // Beware MONTH was counted for 0 to 11, so we have to subtract 1
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);

            return calendar.getTime();
        } catch (Exception ex) {
            log.error("Exception caught trying to parse date : ", ex);
        }
        return null;
    }

}
