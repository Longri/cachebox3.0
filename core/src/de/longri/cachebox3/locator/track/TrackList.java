package de.longri.cachebox3.locator.track;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class TrackList extends Array<Track> {
    private static TrackList trackList;
    private Track routingTrack; // for identifying the track! has been originally from openRouteService implementation. now from BRouter

    private TrackList() {
    }

    public static TrackList getTrackList() {
        if (trackList == null) trackList = new TrackList();
        return trackList;
    }

    public void addTrack(Track track) {
        // Dont use this for internal RoutingTrack!! Use setRoutingTrack(Track track)
        add(track);
        track.viewTrack();
        trackListChanged();
    }

    public void removeTrack(Track track) {
        track.hideTrack();
        if (track == routingTrack) {
            routingTrack = null;
        }
        removeValue(track,true);
        trackListChanged();
    }

    public void trackListChanged() {
        // handle visibility changes in TrackListView / trackListChanged() in Tracklist
        // is not necessary, cause map is always recreated on show
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
            removeValue(routingTrack, true);
            track.setColor(routingTrack.getColor());
        }
        insert(0, track);
        routingTrack = track;
        trackListChanged();
    }

    public void removeRoutingTrack() {
        removeValue(routingTrack,true);
        routingTrack = null;
        trackListChanged();
    }
}
