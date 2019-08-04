package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;

import static de.longri.cachebox3.CB.addClickHandler;

public class SearchDialog extends ActivityBase {
    private static SearchDialog searchDialog;
    private CB_Label lblTitle;
    private Image imgTitle;
    private CB_Button btnOK, btnCancel;
    private Catch_Table box;
    private boolean needLayout = true;
    Drawable icon;

    private SearchDialog(String title, Drawable icon) {
        super(title);
        this.icon = icon;
        lblTitle = new CB_Label(Translation.get(title));
        imgTitle = new Image(icon);
        box = new Catch_Table(true);
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        setTableAndCellDefaults();
        center();
        initClickHandlersAndContent();
    }

    public static SearchDialog getInstance(String title, Drawable icon) {
        if (searchDialog == null) searchDialog = new SearchDialog(title, icon);
        return searchDialog;
    }

    @Override
    public void layout() {
        if (!needLayout) {
            super.layout();
            return;
        }
        SnapshotArray<Actor> actors = getChildren();
        for (Actor actor : actors)
            removeActor(actor);
        setFillParent(true);
        addNext(imgTitle).left().fill(false);
        addLast(lblTitle, -0.8f);
        addLast(new ScrollPane(box));
        addNext(btnOK);
        addLast(btnCancel);

        super.layout();
        needLayout = false;

        this.setStageBackground(icon);
    }

    private void initClickHandlersAndContent() {

        addClickHandler(btnOK, () -> {
            btnOK.setDisabled(true);
        });

        addClickHandler(btnCancel, () -> {
            btnOK.setDisabled(false);
            finish();
        });

    }

}
