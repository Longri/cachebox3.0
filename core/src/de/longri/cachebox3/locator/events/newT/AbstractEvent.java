package de.longri.cachebox3.locator.events.newT;

/**
 * Created by Longri on 23.03.2017.
 */
public abstract class AbstractEvent<T> {

    //TODO add abstract Logging

    private final Class clazz;
    public final short ID;

    public AbstractEvent(Class<T> clazz) {
       this(clazz,EventHandler.getId());
    }

    public AbstractEvent(Class<T> clazz, short eventID) {
        this.clazz = clazz;
        this.ID = eventID;
    }

    abstract Class getListenerClass();

}
