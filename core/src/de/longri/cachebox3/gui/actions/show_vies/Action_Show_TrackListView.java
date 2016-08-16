package de.longri.cachebox3.gui.actions.show_vies;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.TrackListView;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_TrackListView extends Abstract_Action_ShowView {
    public Action_Show_TrackListView() {
        super("Tracks", MenuID.AID_SHOW_TRACKLIST);
    }

    @Override
    protected void Execute() {
        if (isActVisible()) return;

        TrackListView view = new TrackListView();
        CB.viewmanager.showView(view);
    }

    @Override
    public boolean hasContextMenu() {
        return false;
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof TrackListView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(TrackListView.class.getName());
    }
}
