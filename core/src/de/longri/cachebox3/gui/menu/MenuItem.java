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
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.HSV_Color;
import de.longri.cachebox3.utils.SizeF;
import org.slf4j.LoggerFactory;

public class MenuItem extends VisTable {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(MenuItem.class);

    private MenuItemStyle style;

    private final String name;
    private Label mLabel;
    private Image checkImage;
    private Drawable mIcon;

    private String mTitle;
    private boolean mIsEnabled = true;

    protected boolean mIsCheckable = false;
    protected boolean mIsChecked = false;
    protected boolean mLeft = false;

    private final int mID;

    protected boolean isPressed = false;
    private Image iconImage;
    private Object data;
    private OnItemClickListener onItemClickListener;

    public MenuItem(SizeF size, int Index, int ID, String name) {
        this.name = name;
        mID = ID;
        setDefaultStyle();
    }

    public MenuItem(int Index, int ID, String name) {
        this.name = name;
        mID = ID;
        setDefaultStyle();
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

//			Drawable drawable = null;
//			if (mIsChecked) {
//				drawable = new SpriteDrawable(Sprites.ChkIcons.get(1));
//			} else {
//				drawable = new SpriteDrawable(Sprites.ChkIcons.get(0));
//			}
//
//			checkImage.setDrawableDrawable(drawable);
        }
    }


    protected void initial() {

        this.reset();


        boolean hasIcon = (mIcon != null);
        if (hasIcon) {

            Image iconImage = new Image(mIcon);
            this.add(iconImage).width(iconImage.getWidth()).top().pad(CB.scaledSizes.MARGIN);
            if (!mIsEnabled) {
                // TODO iconImage.setColor(COLOR.getDisableFontColor());
            }
        }

        mLabel = new Label(mTitle, new Label.LabelStyle(style.font, style.fontColor));
        mLabel.setWrap(true);
        this.add(mLabel).expandX().fillX();
//        if (mIsCheckable) {
//            CB_RectF rec;
//            if (hasIcon) {
//                rec = new CB_RectF(this.getWidth() - 2 * this.getHeight(), 0, this.getHeight(), this.getHeight()).ScaleCenter(0.75f);
//            } else {
//                rec = new CB_RectF(this.getWidth() - this.getHeight(), 0, this.getHeight(), this.getHeight()); // .ScaleCenter(0.75f);
//            }
//
//            rec.setHeight(rec.getWidth());
//
//            checkImage = new Image(rec, "MenuItemCheckImage", false);
//
//            Drawable drawable = null;
//            if (mIsChecked) {
//                drawable = new SpriteDrawable(Sprites.ChkIcons.get(1));
//            } else {
//                drawable = new SpriteDrawable(Sprites.ChkIcons.get(0));
//            }
//
//            checkImage.setDrawable(drawable);
//            this.addChild(checkImage);
//        }


        if (!mIsEnabled) {
            //TODO
        }


        this.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if(MenuItem.this.onItemClickListener!=null){
                    MenuItem.this.onItemClickListener.onItemClick(MenuItem.this);
                }
            }
        });

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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener=onItemClickListener;
    }


    public static class MenuItemStyle {
        public BitmapFont font;
        public Color fontColor;
    }



}
