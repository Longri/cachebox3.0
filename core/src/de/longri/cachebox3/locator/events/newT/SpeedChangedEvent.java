package de.longri.cachebox3.locator.events.newT;

/**
 * Created by Longri on 23.03.2017.
 */
public class SpeedChangedEvent extends AbstractEvent<Float> {
    public final float speed;

    public SpeedChangedEvent(float speed) {
        super(Float.class);
        this.speed = speed;
    }

    public SpeedChangedEvent(float speed, short id) {
        super(Float.class, id);
        this.speed = speed;
    }

    @Override
    Class getListenerClass() {
        return SpeedChangedListener.class;
    }
}
