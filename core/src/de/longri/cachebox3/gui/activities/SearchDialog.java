package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;

public class SearchDialog extends Activity {
    private final CB_Label lblCachetitle, lblGcCode, lblOwner;
    private final EditTextField edtCachetitle, edtGcCode, edtOwner;
    private final CB_Button btnSearch, btnFilter, btnNext;

    private SearchDialog(String title, Drawable icon) {
        super(title,icon);
        lblCachetitle = new CB_Label(Translation.get("Title"));
        lblGcCode = new CB_Label(Translation.get("GCCode"));
        lblOwner = new CB_Label(Translation.get("Owner"));
        edtCachetitle = new EditTextField("");
        edtGcCode = new  EditTextField("");
        edtOwner = new  EditTextField("");
        btnSearch = new CB_Button(Translation.get("Search"));
        btnFilter = new CB_Button(Translation.get("Filter"));
        btnNext = new CB_Button(Translation.get("Next"));
    }

    public static Activity getInstance(String title, Drawable icon) {
        if (activity == null) {
            activity = new SearchDialog(title, icon);
            activity.top();
            activity.setHeight(activity.getPrefHeight() / 3);
        }
        return activity;
    }

    protected Catch_Table createMainContent() {
        mainContent.addNext(lblCachetitle, -0.4f);
        mainContent.addLast(edtCachetitle);
        mainContent.addNext(lblGcCode, -0.4f);
        mainContent.addLast(edtGcCode);
        mainContent.addNext(lblOwner, -0.4f);
        mainContent.addLast(edtOwner);
        mainContent.addNext(btnFilter);
        mainContent.addNext(btnSearch);
        mainContent.addNext(btnNext);
        return mainContent;
    }

    protected void runAtOk() {
        btnOK.setDisabled(true);
    }

    public void runAtCancel() {
        finish();
    }
}
