package de.longri.cachebox3.gui.actions.show_activities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.activities.EditFilterSettings;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.utils.IconNames;

public class Action_ShowFilterSettings extends AbstractAction {

    public Action_ShowFilterSettings() {
        super("filter", MenuID.AID_SHOW_FILTER_DIALOG);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return CB.getSprite(IconNames.filter.name());
    }

    @Override
    public void Execute() {
        EditFilterSettings edFi = new EditFilterSettings("Filter");
        edFi.show();

    }
}
