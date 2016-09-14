package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.QuickActions;
import de.longri.cachebox3.gui.widgets.QuickButtonItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.MoveableList;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonList extends Widget {

    MoveableList<QuickButtonItem> quickButtonList;

    private void readQuickButtonItemsList() {
        if (quickButtonList == null) {
            String ConfigActionList = Config.quickButtonList.getValue();
            String[] ConfigList = ConfigActionList.split(",");
            quickButtonList = QuickActions.getListFromConfig(ConfigList, CB.scaledSizes.BUTTON_HEIGHT);
        }

    }
}
