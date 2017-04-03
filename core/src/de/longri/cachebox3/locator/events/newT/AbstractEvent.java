package de.longri.cachebox3.locator.events.newT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.03.2017.
 */
public abstract class AbstractEvent<T> {

    private final Logger log;

    private final Class clazz;
    public final short ID;

    public AbstractEvent(Class<T> clazz) {
        this(clazz, EventHandler.getId());
    }

    public AbstractEvent(Class<T> clazz, short eventID) {
        this.clazz = clazz;
        this.ID = eventID;
        log = LoggerFactory.getLogger(clazz);
    }

    abstract Class getListenerClass();

}
