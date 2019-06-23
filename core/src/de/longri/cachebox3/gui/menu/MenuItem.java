/*
 * Copyright (C) 2015-2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.gui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuItem extends ListViewItem {
    final static Logger log = LoggerFactory.getLogger(MenuItem.class);
    private final String name;
    private final int mID;
    private final Menu parentMenu;
    protected Label mLabel;
    protected Image checkImage;
    protected Drawable mIcon;
    protected String mTitle;
    protected boolean mIsEnabled = true;
    protected boolean mIsChecked = false;
    protected boolean isPressed = false;
    private MenuItemStyle style;
    private boolean mIsCheckable = false;
    private boolean mLeft = false;
    private Image iconImage;
    private Object data;
    private Menu moreMenu;
    private boolean backgroundOverrides = false;

    public MenuItem(int Index, int ID, String name, Menu parentMenu) {
        super(Index);
        this.name = name;
        mID = ID;
        this.parentMenu = parentMenu;
        setDefaultStyle();
    }


    public MenuItem(int Index, Menu parentMenu) {
        super(Index);
        mID = -1;
        name = "";
        this.parentMenu = parentMenu;
    }

    private void setDefaultStyle() {
        this.style = VisUI.getSkin().get("default", MenuItemStyle.class);
    }

    public int getMenuItemId() {
        return mID;
    }

    @Override
    public void pack() {
        // calc pref Label width
        float prefLabelWidth = this.getWidth();
        if (checkImage != null) {
            prefLabelWidth -= checkImage.getWidth() + CB.scaledSizes.MARGINx2;
        } else {
            prefLabelWidth -= CB.scaledSizes.MARGIN;
        }
        if (mIcon != null) {
            prefLabelWidth -= mIcon.getMinWidth() + CB.scaledSizes.MARGINx2;
        } else {
            prefLabelWidth -= CB.scaledSizes.MARGIN;
        }
        // calc prefHeight
        float prefHeight = 0;
        if (checkImage != null) prefHeight = checkImage.getHeight();
        if (mIcon != null) prefHeight = Math.max(prefHeight, mIcon.getMinHeight());
        if (mLabel != null) {
            mLabel.setWidth(prefLabelWidth);
            mLabel.invalidate();
            prefHeight = Math.max(prefHeight, mLabel.getPrefHeight());
        }
        prefHeight += CB.scaledSizes.MARGINx4;
        this.setPrefHeight(prefHeight);
        this.setHeight(prefHeight);
    }

    @Override
    public boolean addListener(EventListener listener) {
        return super.addListener(listener);
    }

    protected synchronized void initial() {
        this.reset();

        boolean hasIcon = (mIcon != null);
        if (hasIcon) {
            Image iconImage = new Image(mIcon, Scaling.none);
            this.add(iconImage).center().padRight(CB.scaledSizes.MARGIN_HALF);
            if (!mIsEnabled) {
                // TODO iconImage.setColor(COLOR.getDisableFontColor());
            }
        }

        if (moreMenu != null && parentMenu.style.menu_for != null) {
            checkImage = new Image(parentMenu.style.menu_for, Scaling.none);
        } else if (mIsCheckable) { // ignore checkable hav this item a moreMenu
            if (mIsChecked) {
                if (mIsEnabled) {
                    checkImage = new Image(new TextureRegionDrawable(CB.getSprite("check_on")), Scaling.none, Align.center);
                } else {
                    checkImage = new Image(new TextureRegionDrawable(CB.getSprite("check_disabled")), Scaling.none, Align.center);
                }
            } else {
                checkImage = new Image(new TextureRegionDrawable(CB.getSprite("check_off")), Scaling.none, Align.center);
            }
        }


        if (mTitle != null) {
            mLabel = new Label(mTitle, new Label.LabelStyle(style.font, style.fontColor));
            mLabel.setWrap(true);
            this.add(mLabel).expandX().fillX().padTop(CB.scaledSizes.MARGIN).padBottom(CB.scaledSizes.MARGIN);
        }

        if (checkImage != null) {
            this.add(checkImage).width(checkImage.getWidth()).pad(CB.scaledSizes.MARGIN / 2);
        }


        if (!mIsEnabled) {
            //TODO
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (mLabel != null && mLabel.getHeight() > this.getHeight()) {
            this.setHeight(mLabel.getHeight() + CB.scaledSizes.MARGINx4);
            if (parentMenu != null && parentMenu.listView != null) {
                parentMenu.listView.invalidate();
                parentMenu.listView.layout();
            }
        }

        // set alpha if item disabled
        if (!this.mIsEnabled)
            parentAlpha *= 0.25f;

        super.draw(batch, parentAlpha);
    }

    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {

        Drawable background = this.getBackground();

        if (background == null) return;
        Color color = getColor();

        // restore alpha if item disabled, draw background never with alpha
        if (!this.mIsEnabled)
            parentAlpha *= 4f;

        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        background.draw(batch, x, y, getWidth(), getHeight());
    }

    /**
     * Retrieve the current title of the item.
     *
     * @return The title.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Change the title associated with this item.
     *
     * @param title The new text to be displayed.
     * @return This Item so additional setters can be called.
     */
    public MenuItem setTitle(String title) {
        mTitle = title;
        initial();
        return this;
    }

    /**
     * Change the icon associated with this item. This icon will not always be shown, so the title should be sufficient in describing this
     * item. See {@link Menu} for the menu types that support icons.
     *
     * @param icon The new icon (as a Sprite) to be displayed.
     * @return This Item so additional setters can be called.
     */
    public MenuItem setIcon(Drawable icon) {
        mIcon = icon;
        initial();
        return this;
    }

    public boolean isEnabled() {
        initial();
        return mIsEnabled;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
        initial();
    }

    public boolean isCheckable() {
        return mIsCheckable;
    }

    public void setCheckable(boolean isCheckable) {
        mIsCheckable = isCheckable;
        initial();
    }

    public void setLeft(boolean value) {
        mLeft = value;
        initial();
    }

    @Override
    public String toString() {
        return "MenuItem " + name;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setMoreMenu(Menu moreMenu) {
        this.moreMenu = moreMenu;
        initial();
    }

    public boolean hasMoreMenu() {
        return this.moreMenu != null;
    }

    public Menu getMoreMenu(Menu menu) {
        this.moreMenu.parentMenu = menu;
        this.moreMenu.reorganizeListIndexes();
        return this.moreMenu;
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
    }

    public void overrideBackground(Drawable drawable) {
        setBackground(drawable);
        backgroundOverrides = drawable != null;
    }

    @Override
    public void setBackground(Drawable drawable) {
        if (backgroundOverrides) return;
        super.setBackground(drawable);
    }

    @Override
    public void dispose() {
        style = null;
        mLabel = null;
        checkImage = null;
        mIcon = null;
        mTitle = null;
        iconImage = null;
        data = null;
        moreMenu = null;
    }

    @Override
    public String getName() {
        return name;
    }

    public static class MenuItemStyle {
        public BitmapFont font;
        public Color fontColor;
        public Drawable option_select, option_back;
    }
}
