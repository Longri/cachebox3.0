package de.longri.cachebox3.locator.events.newT;

/**
 * Created by Longri on 23.03.2017.
 */
public abstract class AbstractEvent<T> {

    //TODO add abstract Logging

    private final Class clazz;

    public AbstractEvent(Class<T> clazz) {
        this.clazz = clazz;
    }

    abstract Class getListenerClass();

}
