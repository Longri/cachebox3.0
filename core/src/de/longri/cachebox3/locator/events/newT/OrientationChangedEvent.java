package de.longri.cachebox3.locator.events.newT;

/**
 * Created by Longri on 23.03.2017.
 */
public class OrientationChangedEvent extends AbstractEvent<Float> {
    public final float orientation;

    public OrientationChangedEvent(float orientation) {
        super(Float.class);
        this.orientation = orientation;
    }

    public OrientationChangedEvent(float orientation, short id) {
        super(Float.class, id);
        this.orientation = orientation;
    }

    @Override
    Class getListenerClass() {
        return OrientationChangedListener.class;
    }
}
