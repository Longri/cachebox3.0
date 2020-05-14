package de.longri.cachebox3.gui.menu.menuBtn1;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractAction;

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
        return CB.getSkin().menuIcon.me1ShareInfos;
    }
}
