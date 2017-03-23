package de.longri.cachebox3.locator.events.newT;

import de.longri.cachebox3.locator.Coordinate;

/**
 * Created by Longri on 23.03.2017.
 */
public class DistanceChangedEvent extends AbstractEvent<Float> {
    public final float distance;

    public DistanceChangedEvent(float distance) {
        super(Float.class);
        this.distance = distance;
    }

    @Override
    Class getListenerClass() {
        return DistanceChangedListener.class;
    }
}
