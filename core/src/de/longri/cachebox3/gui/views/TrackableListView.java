package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.serializable.BitStore;

/**
 * Created by Longri on 24.07.16.
 */
public class TrackableListView extends AbstractView {

    public TrackableListView(BitStore reader) {
        super(reader);
    }

    public TrackableListView() {
        super("TrackableListView");
        // todo implement
        MessageBox.show("Not implemented yet", "Not implemented", MessageBoxButtons.Cancel, MessageBoxIcon.Information, null);
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
