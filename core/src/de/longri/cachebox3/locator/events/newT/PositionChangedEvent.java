package de.longri.cachebox3.locator.events.newT;

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

    @Override
    Class getListenerClass() {
        return PositionChangedListener.class;
    }
}
