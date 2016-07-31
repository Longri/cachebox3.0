package de.longri.cachebox3.gui.actions;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.TrackListView;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_TrackListView extends Action {
    public Action_Show_TrackListView() {
        super("Tracks", MenuID.AID_SHOW_TRACKLIST);
    }

    @Override
    protected void Execute() {
        if (CB.viewmanager.getActView() instanceof TrackListView) return;

        TrackListView view = new TrackListView();
        CB.viewmanager.showView(view);
    }
}
