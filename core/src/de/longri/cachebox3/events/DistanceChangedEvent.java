package de.longri.cachebox3.events;

/**
 * Created by Longri on 23.03.2017.
 */
public class DistanceChangedEvent extends AbstractEvent<Float> {
    public final float distance;

    public DistanceChangedEvent(float distance, short id) {
        super(Float.class, id);
        this.distance = distance;
    }

    @Override
    Class getListenerClass() {
        return DistanceChangedListener.class;
    }
}
