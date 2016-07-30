package de.longri.cachebox3.gui.actions;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.CacheListView;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_CacheList extends Action {

    public Action_Show_CacheList() {
        //TODO add db size to name super("cacheList", "  (" + String.valueOf(CacheboxDatabase.Data.Query.size()) + ")", MenuID.AID_SHOW_CACHELIST);
        super("cacheList", "  (" + String.valueOf("xxx") + ")", MenuID.AID_SHOW_CACHELIST);
    }


    @Override
    protected void Execute() {
        if (CB.viewmanager.getActView() instanceof CacheListView) return;

        CacheListView view = new CacheListView();
        CB.viewmanager.showView(view);

    }
}
