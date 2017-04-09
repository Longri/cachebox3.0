package de.longri.cachebox3.events;

import de.longri.cachebox3.types.Waypoint;

/**
 * Created by Longri on 23.03.2017.
 */
public class SelectedWayPointChangedEvent extends AbstractEvent<Waypoint> {
    public final Waypoint wayPoint;

    public SelectedWayPointChangedEvent(Waypoint wayPoint) {
        super(Waypoint.class);
        this.wayPoint = wayPoint;
    }

    public SelectedWayPointChangedEvent(Waypoint wayPoint, short id) {
        super(Waypoint.class, id);
        this.wayPoint = wayPoint;
    }

    @Override
    Class getListenerClass() {
        return SelectedWayPointChangedListener.class;
    }
}
