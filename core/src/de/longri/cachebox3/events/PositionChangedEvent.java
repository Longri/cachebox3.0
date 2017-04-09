package de.longri.cachebox3.events;

import de.longri.cachebox3.locator.CoordinateGPS;

/**
 * Created by Longri on 23.03.2017.
 */
public class PositionChangedEvent extends AbstractEvent<CoordinateGPS> {
    public final CoordinateGPS pos;

    public PositionChangedEvent(CoordinateGPS pos) {
        super(CoordinateGPS.class);
        this.pos = pos;
    }

    public PositionChangedEvent(CoordinateGPS pos, short id) {
        super(CoordinateGPS.class, id);
        this.pos = pos;
    }

    @Override
    Class getListenerClass() {
        return PositionChangedListener.class;
    }
}
