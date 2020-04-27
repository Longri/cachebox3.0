package de.longri.cachebox3.locator.track;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.locator.Coordinate;

import java.util.ArrayList;

public class TrackList {
    private static TrackList trackList;
    private boolean aTrackChanged;
    private int thisZoom;
    private final ArrayList<Track> tracks;
    private Track routingTrack; // for identifying the track! has been originally from openRouteService implementation. now from BRouter
    // for rendering
    private ArrayList<DrawTrack> tracksToDraw;
    private GlyphLayout glyphLayout;

    private TrackList() {
        tracks = new ArrayList<>();
        aTrackChanged = false;
        thisZoom = -1;
        tracksToDraw = new ArrayList<>();
    }

    public static TrackList getTrackList() {
        if (trackList == null) trackList = new TrackList();
        return trackList;
    }

    public int getNumberOfTracks() {
        return tracks.size();
    }

    public Track getTrack(int position) {
        return tracks.get(position);
    }

    public void addTrack(Track track) {
        // Dont use this for internal RoutingTrack!! Use setRoutingTrack(Track track)
        tracks.add(track);
        trackListChanged();
    }

    public void removeTrack(Track track) {
        if (track == routingTrack) {
            routingTrack = null;
        }
        tracks.remove(track);
        trackListChanged();
    }

    public void trackListChanged() {
        aTrackChanged = true;
    }

    // =================================================================================================================

    public boolean existsRoutingTrack() {
        return routingTrack != null;
    }

    public void setRoutingTrack(Track track) {
        if (routingTrack == null) {
            track.setColor(new Color(0.85f, 0.1f, 0.2f, 1f));
        } else {
            // erst alten routingTrack löschen
            tracks.remove(routingTrack);
            track.setColor(routingTrack.getColor());
        }
        tracks.add(0, track);
        routingTrack = track;
        trackListChanged();
    }

    public void removeRoutingTrack() {
        tracks.remove(routingTrack);
        routingTrack = null;
        trackListChanged();
    }

    public static class DrawTrack {
        private final Color mColor;
        protected ArrayList<Coordinate> trackPoints;
        double tracklength;
        Sprite arrow;
        Sprite point;
        float overlap;

        public DrawTrack(Color color, boolean isInternalRoutingTrack) {
            if (isInternalRoutingTrack) {
                /*
                arrow = new Sprite(Sprites.Arrows.get(5));
                point = new Sprite(Sprites.Arrows.get(10));
                 */
                arrow.scale(1.6f);
                point.scale(0.2f);
                overlap = 1.9f;
            } else {
                /*
                arrow = Sprites.Arrows.get(5);
                point = Sprites.Arrows.get(10);
                 */
                overlap = 0.9f;
            }
            mColor = color;
            trackPoints = new ArrayList<>();
            tracklength = 0;
        }

    }
}
