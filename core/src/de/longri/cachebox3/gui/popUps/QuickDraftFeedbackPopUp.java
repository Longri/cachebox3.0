package de.longri.cachebox3.gui.popUps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.stages.ViewManager;

public class QuickDraftFeedbackPopUp extends Actor {

    public QuickDraftFeedbackPopUp(boolean found) {
//TODO
    }

    public void show() {
        if (CB.viewmanager != null) {
            CB.viewmanager.toast(this, ViewManager.ToastLength.NORMAL);
        }
    }
}
