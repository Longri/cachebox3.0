package de.longri.cachebox3.gui.actions.extendsAbstractAction;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.menu.MenuID;

import static de.longri.cachebox3.PlatformConnector.shareInfos;

public class Action_Share extends AbstractAction {

    public Action_Share() {
        super("Share", MenuID.SHARE_INFOS);
    }

    @Override
    public void execute() {
        shareInfos();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.shareInfos;
    }
}
