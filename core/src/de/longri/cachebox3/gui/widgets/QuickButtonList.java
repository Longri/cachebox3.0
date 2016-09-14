package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.QuickActions;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.MoveableList;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonList extends WidgetGroup {

    final QuickButtonListStyle style;
    final ScrollPane scrollPane;
    final Group scrollPaneContent = new Group();
    MoveableList<QuickButtonItem> quickButtonList;
    private boolean needsLayout = true;


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

            configActionList = "5,0,1,3,2,4,7,8,9";

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
        this.invalidate();
        needsLayout = true;
    }

    @Override
    public void layout() {
        if (needsLayout || super.needsLayout()) {
            scrollPane.setBounds(0, 0, getWidth(), getHeight());
            scrollPane.layout();
        }
        needsLayout = false;
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        needsLayout = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        style.background.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        super.draw(batch, parentAlpha);
    }

    public static class QuickButtonListStyle {
        Drawable background, button;
    }
}
