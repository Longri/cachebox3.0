package de.longri.cachebox3.gui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;

public abstract class Activity extends ActivityBase {
    protected static Activity activity;
    private final ClickListener cancelListener;
    private final ClickListener okListener;
    protected Catch_Table mainContent;
    protected CB_Button btnOK, btnCancel;
    protected CB_Label lblTitle;
    protected Image imgTitle;

    public Activity(String title, Drawable icon) {
        super(title);
        lblTitle = new CB_Label(Translation.get(title));
        imgTitle = new Image(icon);
        mainContent = new Catch_Table(true);
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        setTableAndCellDefaults();
        top().left();
        okListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                runAtOk(event, x, y);
                finish();
            }
        };
        btnOK.addListener(okListener);
        cancelListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                runAtCancel(event, x, y);
                finish();
            }
        };
        btnCancel.addListener(cancelListener);
        CB.stageManager.registerForBackKey(cancelListener);
    }

    @Override
    public void layout() {
        if (!needLayout) {
            super.layout();
            return;
        }
        getChildren().clear();
        setFillParent(true);
        addNext(imgTitle, -1.2f).padBottom(0);
        addLast(lblTitle, -0.8f).padBottom(0);
        createMainContent();
        // hack to make the widget area bigger, to have ok and cancel at the bottom
        for (int i = 0; i < 25; i++) {
            mainContent.addLast(new CB_Label(""));
        }
        addLast(new ScrollPane(mainContent)).pad(0);
        addNext(btnOK).padTop(0);
        addLast(btnCancel).padTop(0);
        super.layout();
        needLayout = false;
    }

    protected abstract void createMainContent();

    protected abstract void runAtOk(InputEvent event, float x, float y);

    protected void runAtCancel(InputEvent event, float x, float y) {
    }

    @Override
    public void dispose() {
        CB.stageManager.unRegisterForBackKey(cancelListener);
        btnOK.removeListener(okListener);
        btnCancel.removeListener(cancelListener);
        activity = null;
        getChildren().clear();
        super.dispose();
    }

}
