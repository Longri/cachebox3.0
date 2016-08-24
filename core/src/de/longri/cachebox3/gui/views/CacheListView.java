package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.CB;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView {

    public CacheListView() {
        super("CacheListView");
    }

    @Override
    public void reloadState() {

    }

    @Override
    public void saveState() {

    }

    @Override
    public void dispose() {

    }

    public void resort(){

        CB.viewmanager.toast("CacheListView.resort() NOT IMPLEMENTED now");


//        synchronized (Database.Data.Query) {
//            CacheWithWP nearstCacheWp = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
//
//            if (nearstCacheWp != null)
//                GlobalCore.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
//            if (TabMainView.cacheListView != null)
//                TabMainView.cacheListView.setSelectedCacheVisible();
//        }
    }
}
