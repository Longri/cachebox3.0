package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.activities.InputString;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.track.Track;
import de.longri.cachebox3.locator.track.TrackList;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.HSV_Color;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.serializable.BitStore;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static de.longri.cachebox3.gui.dialogs.ButtonDialog.BUTTON_POSITIVE;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 24.07.16.
 */
public class TrackListView extends AbstractView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackListView.class);

    private static ListView tracksView;
    private ListViewAdapter tracksViewAdapter;
    private TrackListViewItem currentRouteItem;

    public TrackListView(BitStore reader) {
        super(reader);
    }

    public TrackListView() {
        super("TrackListView");
        tracksView = new ListView(VERTICAL);
        tracksViewAdapter = new ListViewAdapter() {
            @Override
            public int getCount() {
                int size = TrackList.getTrackList().getNumberOfTracks();
                if (CB.currentRoute != null)
                    size++;
                return size;
            }

            @Override
            public ListViewItem getView(int viewPosition) {
                log.info("get track item number " + viewPosition + " (" + (CB.currentRoute != null ? "with " : "without ") + "tracking." + ")");
                int tracksIndex = viewPosition;
                if (CB.currentRoute != null) {
                    if (viewPosition == 0) {
                        currentRouteItem = new TrackListViewItem(viewPosition, CB.currentRoute);
                        return currentRouteItem;
                    }
                    tracksIndex--; // viewPosition - 1, if tracking is activated
                }
                return new TrackListViewItem(viewPosition, TrackList.getTrackList().getTrack(tracksIndex));
            }

            @Override
            public void update(ListViewItem view) {

            }
        };

        tracksView.setAdapter(tracksViewAdapter);
        addChild(tracksView);
    }

    @Override
    public void onShow() {
        super.onShow();
        //TODO Create ListViewItems and add to ListView
    }

    @Override
    public void onHide() {
        super.onHide();
    }

    protected void create() {
        super.create();
    }

    @Override
    public void dispose() {
        //TODO clear and destroy ListView and all ListViewItems
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
        FileChooser fc = new FileChooser(Translation.get("LoadTrack"), FileChooser.Mode.OPEN, FileChooser.SelectionMode.FILES, "gpx", "GPX");
        // button text Translation.get("load")
        fc.setDirectory(Config.TrackFolder.getValue(), false);
        fc.setSelectionReturnListener(fileHandle -> {
            if (fileHandle != null)
                readFromGpxFile(fileHandle);
        });
        fc.show();
    }

    public void readFromGpxFile(FileHandle fileHandle) {
        // !!! it is possible that a gpx file contains more than 1 <trk> segments
        // they are all added to the tracks (TrackList)
        ArrayList<Track> tracksFromGpxFile = new ArrayList<>();
        float[] dist = new float[4];
        double distance = 0;
        double altitudeDifference = 0;
        double deltaAltitude;
        Coordinate fromPosition = new Coordinate(0, 0);
        BufferedReader reader;
        HSV_Color trackColor = null;

        try {
            InputStreamReader isr = new InputStreamReader(fileHandle.read(), StandardCharsets.UTF_8);
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
                                track.setFileName(fileHandle.path());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(fileHandle.name()); // FileIO.getFileName(file)
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
                                track.setFileName(fileHandle.path());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(fileHandle.name()); // FileIO.getFileName(file)
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
                            tracksFromGpxFile.add(track);
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
                            tracksFromGpxFile.add(track);
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
        for (Track track : tracksFromGpxFile) {
            if (trackColor != null) track.setColor(trackColor);
            TrackList.getTrackList().addTrack(track);
        }
        tracksView.dataSetChanged();

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

    private static class TrackListViewItem extends ListViewItem {
        final org.slf4j.Logger log = LoggerFactory.getLogger(TrackListViewItem.class);
        /*
        private Sprite chkOff;
        private Sprite chkOn;
        private static CB_RectF colorIcon;
        private static CB_RectF checkBoxIcon;
        private static CB_RectF scaledCheckBoxIcon;
         */
        private Track track;
        private CB_Label trackName;
        private CB_Label trackLength;

        public TrackListViewItem(int index, Track aTrack) {
            super(index);
            track = aTrack;
            trackName = new CB_Label(aTrack.getName());
            add(trackName);
            addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    // tracksView.setSelection(index);
                    setSelected(true);
                    Vector2 clickedPosition = new Vector2(x, y);
                /*
                if (colorIcon.contains(clickedPosition)) {
                    colorIconClicked();
                } else if (checkBoxIcon.contains(clickedPosition)) {
                    checkBoxIconClicked();
                } else
                 {
                 */
                    {
                        Menu cm = new Menu("TrackRecordMenuTitle");
                        cm.addMenuItem("ShowOnMap", null, () -> positionLatLon()); // CB.getSkin().getMenuIcon.
                        cm.addMenuItem("rename", null, () -> setTrackName());
                        cm.addMenuItem("save", null, () -> saveAsFile()); //CB.getSkin().getMenuIcon.
                        cm.addMenuItem("unload", null, () -> unloadTrack());

                        // (rename, save,) delete darf nicht mit dem aktuellen Track gemacht werden....
                        if (!track.isActualTrack()) {
                            if (track.getFileName().length() > 0) {
                                if (!track.isActualTrack()) {
                                    FileHandle trackAbstractFile = new FileHandle(track.getFileName());
                                    if (trackAbstractFile.exists()) {
                                        cm.addMenuItem("delete", CB.getSkin().getMenuIcon.deleteIcon,
                                                () -> {
                                                    ButtonDialog bd = new ButtonDialog("", Translation.get("DeleteTrack"),
                                                            Translation.get("DeleteTrack"),
                                                            MessageBoxButton.YesNo,
                                                            MessageBoxIcon.Question,
                                                            (which, data) -> {
                                                                if (which == BUTTON_POSITIVE) {
                                                                    try {
                                                                        trackAbstractFile.delete();
                                                                        TrackList.getTrackList().removeTrack(track);
                                                                        // TrackListView.getInstance().notifyDataSetChanged();
                                                                    } catch (Exception ex) {
                                                                        new ButtonDialog("", ex.toString(), Translation.get("Error"), MessageBoxButton.OK, MessageBoxIcon.Error, null).show();
                                                                    }
                                                                }
                                                                return true;
                                                            });
                                                    bd.show();
                                                });
                                    }
                                }
                            }
                        }
                        cm.show();
                    }
                }
            });
        }

        private void positionLatLon() {
        /*
        if (track.getTrackPoints().size > 0) {
            Coordinate trackpoint = track.getTrackPoints().get(0);
            double latitude = trackpoint.getLatitude();
            double longitude = trackpoint.getLongitude();
            ShowMap.getInstance().execute();
            ShowMap.getInstance().normalMapView.setBtnMapStateToFree(); // btn
            // ShowMap.getInstance().normalMapView.setMapState(MapViewBase.MapState.FREE);
            ShowMap.getInstance().normalMapView.setCenter(new Coordinate(latitude, longitude));
        }
         */
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            /*
            left = getPadLeft();
            drawColorRec(batch);
            if (trackName == null || trackLength == null) {
                createLabel();
            }
            drawRightChkBox(batch);
             */
        }

        private void createLabel() {
            if (trackName == null) {
                trackName = new CB_Label(track.getName());
                trackName.setText(track.getName());
                add(trackName);
            }

            // draw Length
            if (trackLength == null) {
                trackLength = new CB_Label();
                trackLength.setText(Translation.get("length") + ": " + UnitFormatter.distanceString((float) track.getTrackLength(), true) + " / " + UnitFormatter.distanceString((float) track.getAltitudeDifference(), true));
                add(trackLength);
            }

            invalidate();

        }

        private void drawColorRec(Batch batch) {
        /*
        if (track == null)
            return;
        if (colorIcon == null) {
            colorIcon = new CB_RectF(0, 0, getHeight(), getHeight());
            colorIcon = colorIcon.scaleCenter(0.95f);
        }

        if (colorReck == null) {
            colorReck = Sprites.getSprite("text-field-back");
            colorReck.setBounds(colorIcon.getX(), colorIcon.getY(), colorIcon.getWidth(), colorIcon.getHeight());
            colorReck.setColor(track.getColor());
        }

        colorReck.draw(batch);

        left += colorIcon.getWidth() + UiSizes.getInstance().getMargin();
         */
        }

        private void drawRightChkBox(Batch batch) {
        /*
        if (checkBoxIcon == null || scaledCheckBoxIcon == null) {
            checkBoxIcon = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);
            scaledCheckBoxIcon = checkBoxIcon.scaleCenter(0.8f);
        }

        if (chkOff == null) {
            chkOff = Sprites.getSprite("check-off");
            chkOff.setBounds(scaledCheckBoxIcon.getX(), scaledCheckBoxIcon.getY(), scaledCheckBoxIcon.getWidth(), scaledCheckBoxIcon.getHeight());
        }

        if (chkOn == null) {
            chkOn = Sprites.getSprite("check-on");
            chkOn.setBounds(scaledCheckBoxIcon.getX(), scaledCheckBoxIcon.getY(), scaledCheckBoxIcon.getWidth(), scaledCheckBoxIcon.getHeight());
        }

        if (track.isVisible()) {
            chkOn.draw(batch);
        } else {
            chkOff.draw(batch);
        }
         */

        }

        private void checkBoxIconClicked() {
            track.setVisible(!track.isVisible());
            TrackList.getTrackList().trackListChanged();
            invalidate();
        }

        private void colorIconClicked() {
        /*
        GL.that.RunOnGL(() -> {
            ColorPicker clrPick = new ColorPicker(track.getColor(), color -> {
                if (color == null) return;
                track.setColor(color);
                colorReck = null;
            });
            clrPick.show();
        });
        invalidate();

         */
        }

        public void notifyTrackChanged() {
            if (trackLength != null)
                trackLength.setText(Translation.get("length") + ": " + UnitFormatter.distanceString((float) track.getTrackLength(), true) + " / " + UnitFormatter.distanceString((float) track.getAltitudeDifference(), true));
        }

        public Track getTrack() {
            return track;
        }

        private void setTrackName() {
            InputString is = new InputString(Translation.get("RenameTrack").toString(), null) {
                public void callBack(String trackname) {
                    track.setName(trackname);
                    tracksView.dataSetChanged();
                }
            };
            is.setText(track.getName());
            is.show();
        }

        private void saveAsFile() {
        /*
        if (track.getName().length() > 0) {
            new FileOrFolderPicker(Config.TrackFolder.getValue(),
                    Translation.get("SaveTrack"),
                    Translation.get("save"),
                    abstractFile -> {
                        if (abstractFile != null) {
                            String trackName = track.getName().replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
                            String extension = track.getName().toLowerCase().endsWith(".gpx") ? "" : ".gpx";
                            AbstractFile f = FileFactory.createFile(abstractFile, trackName + extension);
                            saveRoute(f, track);
                            if (f.exists()) {
                                track.setFileName(f.getAbsolutePath());
                                log.info(f.getAbsolutePath() + " saved.");
                            } else {
                                log.error("Error saving " + abstractFile + "/" + track.getName() + extension);
                            }
                        }
                    }).show();
        } else {
            // existing gpx-file
            new FileOrFolderPicker(Config.TrackFolder.getValue(),
                    "*.gpx",
                    Translation.get("SaveTrack"),
                    Translation.get("save"),
                    abstractFile -> {
                        if (abstractFile != null) {
                            saveRoute(abstractFile, track);
                            log.debug("TrackListViewItem: Load Track :" + abstractFile);
                        }
                    }).show();
        }
         */
        }

        private void saveRoute(FileHandle gpxAbstractFile, Track track) {
            Writer writer = null;
            writer = gpxAbstractFile.writer(false);
            try {
                writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.append(
                        "<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");

                Date now = new Date();
                SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String sDate = datFormat.format(now);
                datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                sDate += "T" + datFormat.format(now) + "Z";
                writer.append("<time>").append(sDate).append("</time>\n");

                writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");

                writer.append("<trk>\n");
                writer.append("<name>").append(track.getName()).append("</name>\n");
                writer.append("<extensions>\n<gpxx:TrackExtension>\n");
                writer.append("<gpxx:ColorRGB>").append(track.getColor().toString()).append("</gpxx:ColorRGB>\n");
                writer.append("</gpxx:TrackExtension>\n</extensions>\n");
                writer.append("<trkseg>\n");
                writer.flush();
            } catch (IOException e) {
                log.error("SaveTrack", e);
            }

            try {
                for (int i = 0; i < track.getTrackPoints().size; i++) {
                    writer.append("<trkpt lat=\"").append(String.valueOf(track.getTrackPoints().get(i).getLatitude())).append("\" lon=\"").append(String.valueOf(track.getTrackPoints().get(i).getLongitude())).append("\">\n");

                    writer.append("   <ele>").append(String.valueOf(track.getTrackPoints().get(i).getElevation())).append("</ele>\n");
                    SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    String sDate = datFormat.format(track.getTrackPoints().get(i).getDate());
                    datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    sDate += "T" + datFormat.format(track.getTrackPoints().get(i).getDate()) + "Z";
                    writer.append("   <time>").append(sDate).append("</time>\n");
                    writer.append("</trkpt>\n");
                }
                writer.append("</trkseg>\n");
                writer.append("</trk>\n");
                writer.append("</gpx>\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                log.error("SaveTrack", e);
            }
        }

        private void unloadTrack() {
            if (track.isActualTrack()) {
                new ButtonDialog("", Translation.get("IsActualTrack"), null, MessageBoxButton.OK, MessageBoxIcon.Warning, null).show();
            } else {
                TrackList.getTrackList().removeTrack(track); // index passt nicht mehr
                // TrackListView.getInstance().notifyDataSetChanged();
                dispose();
            }
        }

    }

}
