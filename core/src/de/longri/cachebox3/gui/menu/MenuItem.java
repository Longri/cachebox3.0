/* 
 * Copyright (C) 2015-2016 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.utils.SizeF;
import org.slf4j.LoggerFactory;

public class MenuItem extends ListViewItem {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(MenuItem.class);

    private MenuItemStyle style;

    private final String name;
    private Label mLabel;
    private Image checkImage;
    private Drawable mIcon;
    private float imageScaleValue = 1;
    ;

    private String mTitle;
    private boolean mIsEnabled = true;

    protected boolean mIsCheckable = false;
    protected boolean mIsChecked = false;
    protected boolean mLeft = false;

    private final int mID;

    protected boolean isPressed = false;
    private Image iconImage;
    private Object data;

    private Menu moreMenu;
    private final Menu parentMenu;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    ClickListener clickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (MenuItem.this.onItemClickListener != null && event.getType() == InputEvent.Type.touchUp) {
                MenuItem.this.onItemClickListener.onItemClick(MenuItem.this);
            }
        }
    };

    public MenuItem(SizeF size, int Index, int ID, String name, Menu parentMenu) {
        super(Index);
        this.name = name;
        mID = ID;
        this.parentMenu = parentMenu;
        setDefaultStyle();
    }

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

    public void toggleCheck() {
        if (isCheckable()) {
            mIsChecked = !mIsChecked;
            //  initial();
        }
    }

    @Override
    public void pack() {
        try {
            super.pack();
            initial();
        } catch (Exception e) {
        }

    }


    protected synchronized void initial() {
        this.reset();
        this.removeListener(clickListener);


        boolean hasIcon = (mIcon != null);
        if (hasIcon) {
            Image iconImage = new Image(mIcon);
            iconImage.setWidth(iconImage.getWidth() * imageScaleValue);
            iconImage.setHeight(iconImage.getHeight() * imageScaleValue);
            this.add(iconImage).width(iconImage.getWidth()).height(iconImage.getHeight()).center().padRight(CB.scaledSizes.MARGIN_HALF);
            if (!mIsEnabled) {
                // TODO iconImage.setColor(COLOR.getDisableFontColor());
            }
        }

        if (mTitle != null) {
            mLabel = new Label(mTitle, new Label.LabelStyle(style.font, style.fontColor));
            mLabel.setWrap(true);
            this.add(mLabel).expandX().fillX().padTop(CB.scaledSizes.MARGIN).padBottom(CB.scaledSizes.MARGIN);
        }

        if (moreMenu != null && parentMenu.style.menu_for != null) {
            checkImage = new Image(parentMenu.style.menu_for);
            this.add(checkImage).width(checkImage.getWidth()).pad(CB.scaledSizes.MARGIN / 2);
        } else if (mIsCheckable) { // ignore checkable hav this item a moreMenu
            if (mIsChecked) {
                if (mIsEnabled) {
                    checkImage = new Image(CB.getSprite("check_on"));
                } else {
                    checkImage = new Image(CB.getSprite("check_disabled"));
                }
            } else {
                checkImage = new Image(CB.getSprite("check_off"));
            }
            this.add(checkImage).width(checkImage.getWidth()).pad(CB.scaledSizes.MARGIN / 2);
        }


        if (!mIsEnabled) {
            //TODO
        }


        this.addListener(clickListener);
    }


    /**
     * Change the title associated with this item.
     *
     * @param title The new text to be displayed.
     * @return This Item so additional setters can be called.
     */
    public MenuItem setTitle(String title) {
        mTitle = title;
        //  initial();
        return this;
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
     * Change the icon associated with this item. This icon will not always be shown, so the title should be sufficient in describing this
     * item. See {@link Menu} for the menu types that support icons.
     *
     * @param icon The new icon (as a Sprite) to be displayed.
     * @return This Item so additional setters can be called.
     */
    public MenuItem setIcon(Drawable icon) {
        CB.scaledSizes.checkMaxIconSize();
        if (icon.getMinWidth() > CB.scaledSizes.ICON_HEIGHT) {
            // set scaled icon size!
            imageScaleValue = Math.min(CB.scaledSizes.ICON_WIDTH / icon.getMinWidth(), CB.scaledSizes.ICON_HEIGHT / icon.getMinHeight());
            log.debug("Menu Icon size not otimal for item: " + this.getName() + "! Maby use scalefactor = " + imageScaleValue);
        } else {
            imageScaleValue = 1f;
        }
        mIcon = icon;
        return this;
    }


    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;

    }


    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setCheckable(boolean isCheckable) {
        mIsCheckable = isCheckable;

    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;

    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public boolean isCheckable() {
        return mIsCheckable;
    }

    public void setLeft(boolean value) {
        mLeft = value;

    }

    @Override
    public String toString() {
        return "MenuItem " + name;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }


    public void setMoreMenu(Menu moreMenu) {
        this.moreMenu = moreMenu;
    }

    public boolean hasMoreMenu() {
        return this.moreMenu != null;
    }

    public Menu getMoreMenu(Menu menu) {
        this.moreMenu.parentMenu = menu;
        return this.moreMenu;
    }


    private boolean backgroundOverrides = false;

    public void overrideBackground(Drawable drawable) {
        setBackground(drawable);
        backgroundOverrides = drawable != null;
    }

    @Override
    public void setBackground(Drawable drawable) {
        if (backgroundOverrides) return;
        super.setBackground(drawable);
    }

    public static class MenuItemStyle {
        public BitmapFont font;
        public Color fontColor;
    }

    @Override
    public String getName() {
        return name;
    }
}
