package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.QuickActions;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.MoveableList;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonList extends Group {

    final QuickButtonListStyle style;
    final ScrollPane scrollPane;
    final Group scrollPaneContent = new Group();
    MoveableList<QuickButtonItem> quickButtonList;

    public QuickButtonList() {
        style = VisUI.getSkin().get("default", QuickButtonListStyle.class);
        scrollPane = new ScrollPane(scrollPaneContent);
        scrollPane.setOverscroll(true, false);
        scrollPane.setFlickScroll(true);
        this.addActor(scrollPane);
        readQuickButtonItemsList();
        this.setTouchable(Touchable.childrenOnly);
    }


    private void readQuickButtonItemsList() {
        if (quickButtonList == null) {
            String configActionList = Config.quickButtonList.getValue();
//TODO make quick buttons configurable att SettingsView
            configActionList = "5,0,1,21,3,2,4,15,25";

            String[] configList = configActionList.split(",");
            quickButtonList = QuickActions.getListFromConfig(configList, CB.scaledSizes.BUTTON_HEIGHT, style.button);
        }

        scrollPaneContent.clear();
        float buttonMargin = CB.scaledSizes.MARGIN_HALF;
        float xPos = buttonMargin;
        float buttonSqare = CB.scaledSizes.BUTTON_HEIGHT;

        for (QuickButtonItem item : quickButtonList) {
            item.setBounds(xPos, 0, buttonSqare, buttonSqare);
            scrollPaneContent.addActor(item);
            xPos += buttonSqare + buttonMargin;
        }
        float completeWidth = xPos + buttonMargin;
        scrollPaneContent.setBounds(0, 0, completeWidth, buttonSqare);
    }


    @Override
    public void sizeChanged() {
        super.sizeChanged();
        scrollPane.setBounds(0, 0, getWidth(), getHeight());
        scrollPane.layout();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isTransform()) {
            applyTransform(batch, computeTransform());
            drawBackground(batch, parentAlpha, 0, 0);
            drawChildren(batch, parentAlpha);
            resetTransform(batch);
        } else {
            drawBackground(batch, parentAlpha, getX(), getY());
            super.draw(batch, parentAlpha);
        }
    }

    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        if (style.background == null) return;
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        style.background.draw(batch, x, y, getWidth(), getHeight());
    }


    public static class QuickButtonListStyle {
        public Drawable background, button;
    }
}
