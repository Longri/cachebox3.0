package de.longri.cachebox3.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;

import static de.longri.cachebox3.CB.addClickHandler;

public abstract class Activity extends ActivityBase {
    protected static Activity activity;
    protected Catch_Table mainContent;
    protected CB_Button btnOK, btnCancel;
    private CB_Label lblTitle;
    private Image imgTitle;
    private boolean needLayout = true;
    private ClickListener cancelClickListener;

    public Activity(String title, Drawable icon) {
        super(title);
        lblTitle = new CB_Label(Translation.get(title));
        imgTitle = new Image(icon);
        mainContent = new Catch_Table(true);
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        setTableAndCellDefaults();
        center();
        init();
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
        addNext(imgTitle, -1.2f);
        addLast(lblTitle, -0.8f);
        addLast(new ScrollPane(createMainContent()));
        addNext(btnOK);
        addLast(btnCancel);

        super.layout();
        needLayout = false;
    }

    protected abstract Catch_Table createMainContent();

    protected abstract void runAtOk();

    protected abstract void runAtCancel();

    private void init() {
        addClickHandler(btnOK, this::runAtOk);
        cancelClickListener = addClickHandler(btnCancel, this::runAtCancel);
        CB.stageManager.registerForBackKey(cancelClickListener);
    }

    @Override
    public void dispose() {
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
        activity = null;
        super.dispose();
    }

}
