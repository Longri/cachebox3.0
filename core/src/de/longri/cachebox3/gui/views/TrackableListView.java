package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.gui.menu.Menu;

/**
 * Created by Longri on 24.07.16.
 */
public class TrackableListView extends AbstractView {

    public TrackableListView() {
        super("TrackableListView");
    }


    @Override
    public void dispose() {

    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return false;
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }
}
