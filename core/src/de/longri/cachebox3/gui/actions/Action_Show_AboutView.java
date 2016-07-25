package de.longri.cachebox3.gui.actions;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.AboutView;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_AboutView extends Action {

    public Action_Show_AboutView() {
        super("AboutView", MenuID.AID_SHOW_CACHELIST);
    }


    @Override
    protected void Execute() {
        if (CB.viewmanager.getActView() instanceof AboutView) return;

        AboutView view = new AboutView();
        CB.viewmanager.showView(view);

        view.reloadState();
    }
}
