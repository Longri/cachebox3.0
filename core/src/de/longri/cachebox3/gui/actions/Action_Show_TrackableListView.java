package de.longri.cachebox3.gui.actions;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.TrackableListView;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_TrackableListView extends Action {
    public Action_Show_TrackableListView() {
        super("TBList", MenuID.AID_SHOW_TRACKABLELIST);
    }

    @Override
    protected void Execute() {
        if (CB.viewmanager.getActView() instanceof TrackableListView) return;

        TrackableListView view = new TrackableListView();
        CB.viewmanager.showView(view);

    }
}
