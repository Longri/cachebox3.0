package de.longri.cachebox3.locator.track;

import com.badlogic.gdx.graphics.Color;
import de.longri.cachebox3.gui.activities.ProjectionCoordinate;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.locator.Coordinate;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackCreator {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackCreator.class);
    private static TrackCreator trackCreator;
    private Track createdTrack;

    private TrackCreator() {

    }

    public static TrackCreator getInstance() {
        if (trackCreator == null) trackCreator = new TrackCreator();
        return trackCreator;
    }

    public void createTrack() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US);
        String sDate = datFormat.format(new Date());
        createdTrack = new Track("created_" + sDate + ".gpx");
        createdTrack.setColor(Color.GOLDENROD);
        createdTrack.setVisible(true);
        createdTrack.setTrackLength(0);
        createdTrack.setElevationDifference(0);
        createdTrack.addPoint(MapView.getLastCenterPos());
        createdTrack.createTrackLayer();
        TrackList.getTrackList().addTrack(createdTrack);
    }

    public void addPoint() {
        Coordinate mapCenterPos = MapView.getLastCenterPos();
        if (createdTrack == null) createTrack();
        createdTrack.addPoint(mapCenterPos);
        createdTrack.addPointToTrackLayer(mapCenterPos);
    }

    public void addProjection() {
        if (createdTrack == null) createTrack();
        ProjectionCoordinate inputProjectedCoordinate = new ProjectionCoordinate(createdTrack.get(createdTrack.size - 1)) {
            @Override
            public void callBack(Coordinate newCoord) {
                createdTrack.addPoint(newCoord);
                createdTrack.addPointToTrackLayer(newCoord);
            }
        };
        inputProjectedCoordinate.show();
    }
}
