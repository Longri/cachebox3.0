package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.gui.menu.Menu;
import de.longri.serializable.BitStore;

/**
 * Created by Longri on 24.07.16.
 */
public class TrackListView extends AbstractView {

    public TrackListView(BitStore reader) {
        super(reader);
    }

    public TrackListView() {
        super("TrackListView");
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
