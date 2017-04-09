package de.longri.cachebox3.events;

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

    public SelectedCacheChangedEvent(Cache cache, short id) {
        super(Cache.class, id);
        this.cache = cache;
    }

    @Override
    Class getListenerClass() {
        return SelectedCacheChangedListener.class;
    }
}
