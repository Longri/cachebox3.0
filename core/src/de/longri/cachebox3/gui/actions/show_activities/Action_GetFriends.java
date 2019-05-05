package de.longri.cachebox3.gui.actions.show_activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;

import static de.longri.cachebox3.gui.menu.MenuID.AID_GET_FRIENDS;

public class Action_GetFriends extends AbstractAction {

    public Action_GetFriends() {
        super(IMPLEMENTED, "Friends", AID_GET_FRIENDS);
    }

    @Override
    public void execute() {
        String friends = GroundspeakAPI.fetchFriends();
        if (GroundspeakAPI.APIError == GroundspeakAPI.OK) {
            Config.Friends.setValue(friends);
            Config.AcceptChanges();
            MessageBox.show(Translation.get("ok") + ":\n" + friends, Translation.get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
        } else {
            MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
        }
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.friends;
    }
}
