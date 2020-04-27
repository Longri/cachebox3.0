package de.longri.cachebox3.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
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
    protected CB_Label lblTitle;
    protected Image imgTitle;
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
        addNext(imgTitle, -1.2f).padBottom(0);
        addLast(lblTitle, -0.8f).padBottom(0);
        createMainContent();
        addLast(new ScrollPane(mainContent)).pad(0);
        addNext(btnOK).padTop(0);
        addLast(btnCancel).padTop(0);

        super.layout();
        needLayout = false;
    }

    protected abstract void createMainContent();

    protected abstract void runAtOk();

    protected void runAtCancel() {
        finish();
    }

    private void init() {
        addClickHandler(btnOK, this::runAtOk);
        cancelClickListener = addClickHandler(btnCancel, this::runAtCancel);
        CB.stageManager.registerForBackKey(cancelClickListener);
    }

    @Override
    public void dispose() {
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
        activity = null;

        //dispose all actors
        SnapshotArray<Actor> children = getChildren();
        for (Actor child : children) {
            if (child instanceof Disposable) {
                ((Disposable) child).dispose();
            }
            this.removeActor(child);
        }
        super.dispose();
    }

}
