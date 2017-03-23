package de.longri.cachebox3.locator.events.newT;

import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.types.Cache;

/**
 * Created by Longri on 23.03.2017.
 */
public class SelectedCacheChangedEvent extends AbstractEvent<Cache> {
    public final Cache cache;

    public SelectedCacheChangedEvent(Cache cache) {
        super(Cache.class);
        this.cache = cache;
    }

    @Override
    Class getListenerClass() {
        return SelectedCacheChangedListener.class;
    }
}
