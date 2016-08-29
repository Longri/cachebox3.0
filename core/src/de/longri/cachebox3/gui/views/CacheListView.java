package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.GlobalCore;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.CacheWithWP;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView {

    public CacheListView() {
        super("CacheListView CacheCount: " + Database.Data.Query.size());
    }


    public void resort() {
        synchronized (Database.Data.Query) {
            CacheWithWP nearstCacheWp = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

            if (nearstCacheWp != null)
                GlobalCore.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
            setSelectedCacheVisible();
        }
    }

    private void setSelectedCacheVisible() {
        //TODO
    }

    @Override
    public void dispose() {

    }
}
