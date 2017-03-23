package de.longri.cachebox3.locator.events.newT;

import de.longri.cachebox3.locator.Coordinate;

/**
 * Created by Longri on 23.03.2017.
 */
public class PositionChangedEvent extends AbstractEvent<Coordinate> {
    public final Coordinate pos;

    public PositionChangedEvent(Coordinate pos) {
        super(Coordinate.class);
        this.pos = pos;
    }

    @Override
    Class getListenerClass() {
        return PositionChangedListener.class;
    }
}
