package de.longri.cachebox3.locator.events.newT;

import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.types.Waypoint;

/**
 * Created by Longri on 23.03.2017.
 */
public class SelectedCoordChangedEvent extends AbstractEvent<Coordinate> {
    public final Coordinate coordinate;

    public SelectedCoordChangedEvent(Coordinate coordinate, short id) {
        super(Coordinate.class, id);
        this.coordinate = coordinate;
    }

    @Override
    Class getListenerClass() {
        return SelectedCoordChangedListener.class;
    }
}
