package de.longri.cachebox3.locator.track;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

public class TrackList extends ArrayList<Track> {
    private static TrackList trackList;
    private Track routingTrack; // for identifying the track! has been originally from openRouteService implementation. now from BRouter

    private TrackList() {
    }

    public static TrackList getTrackList() {
        if (trackList == null) trackList = new TrackList();
        return trackList;
    }

    public int getNumberOfTracks() {
        return size();
    }

    public Track getTrack(int position) {
        return get(position);
    }

    public void addTrack(Track track) {
        // Dont use this for internal RoutingTrack!! Use setRoutingTrack(Track track)
        add(track);
        trackListChanged();
    }

    public void removeTrack(Track track) {
        if (track == routingTrack) {
            routingTrack = null;
        }
        remove(track);
        trackListChanged();
    }

    public void trackListChanged() {
        // todo
        // todo handle visible changed
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
            remove(routingTrack);
            track.setColor(routingTrack.getColor());
        }
        add(0, track);
        routingTrack = track;
        trackListChanged();
    }

    public void removeRoutingTrack() {
        remove(routingTrack);
        routingTrack = null;
        trackListChanged();
    }
}
