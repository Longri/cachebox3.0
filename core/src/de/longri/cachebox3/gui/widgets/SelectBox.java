/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.skin.styles.SelectBoxStyle;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 15.05.2017.
 */
public class SelectBox<T extends SelectBoxItem> extends IconButton {

    private Array<T> entries;
    private SelectBoxStyle style;
    private final com.badlogic.gdx.scenes.scene2d.ui.Image selectIcon = new com.badlogic.gdx.scenes.scene2d.ui.Image();
    private T selectedItem;

    public SelectBox() {
        super("");
        setStyle(VisUI.getSkin().get("default", SelectBoxStyle.class));
        this.addActor(selectIcon);
        setSize(getPrefWidth(), getPrefHeight());
        this.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                //show select menu
                showMenu();
            }
        });
    }

    private void showMenu() {
        Menu menu = new Menu(Translation.Get("select"));
        for (int i = 0, n = entries.size; i < n; i++) {
            menu.addItem(getMenuItem(i, entries.get(i)));
        }
        menu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                T entry = (T) item.getData();
                select(entry);
                return true;
            }
        });
        menu.show();
    }

    private MenuItem getMenuItem(int index, T entry) {
        String text = entry.getName();
        Drawable icon = entry.getDrawable();

        MenuItem menuItem = new MenuItem(index, null) {
            protected synchronized void initial() {
                this.reset();
                this.removeListener(clickListener);


                boolean hasIcon = (mIcon != null);
                if (hasIcon) {
                    com.badlogic.gdx.scenes.scene2d.ui.Image iconImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(mIcon);
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

                if (mIsChecked) {
                    checkImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(style.option_select);
                } else {
                    checkImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(style.option_back);
                }
                this.add(checkImage).width(checkImage.getWidth()).pad(CB.scaledSizes.MARGIN / 2);
                this.addListener(clickListener);
            }
        };

        menuItem.setTitle(text);
        menuItem.setIcon(icon);
        menuItem.setCheckable(true);
        menuItem.setData(entry);
        if (entry.equals(selectedItem)) menuItem.setChecked(true);
        return menuItem;
    }

    public void set(Array<T> list) {
        this.entries = list;
        select(0);
    }

    public void setStyle(SelectBoxStyle style) {
        this.style = style;
        VisTextButtonStyle buttonStyle = new VisTextButtonStyle();
        buttonStyle.up = style.up;
        buttonStyle.down = style.down;
        buttonStyle.font = style.font;
        buttonStyle.fontColor = style.fontColor;
        super.setStyle(buttonStyle);
        this.setIcon(style.selectIcon);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                SelectBox.this.layout();
            }
        });
    }

    @Override
    public void layout() {
        super.layout();
        Drawable imageDrawable = image.getDrawable();
        if (imageDrawable != null) {
            float x = this.getWidth() - (imageDrawable.getMinHeight() + CB.scaledSizes.MARGINx2 + style.selectIcon.getMinWidth());
            float y = (this.getHeight() - imageDrawable.getMinHeight()) / 2;
            image.setBounds(x, y, imageDrawable.getMinHeight(), imageDrawable.getMinHeight());
        }
        selectIcon.setBounds(this.getWidth() - (style.selectIcon.getMinWidth() + CB.scaledSizes.MARGIN),
                (this.getHeight() - style.selectIcon.getMinHeight()) / 2,
                style.selectIcon.getMinWidth(),
                style.selectIcon.getMinHeight());

    }

    public void select(int index) {
        selectedItem = entries.get(index);
        this.setText(selectedItem.getName());
        this.setIcon(selectedItem.getDrawable());
        this.setSelectIcon(style.selectIcon);
        this.layout();
        this.fire(new ChangeListener.ChangeEvent());
    }

    public void select(T item) {
        selectedItem = item;
        this.setText(item.getName());
        this.setIcon(item.getDrawable());
        this.setSelectIcon(style.selectIcon);
        this.layout();
        this.fire(new ChangeListener.ChangeEvent());
    }

    private void setSelectIcon(Drawable drawable) {
        selectIcon.setDrawable(drawable);
    }

    @Override
    public float getPrefWidth() {
        if (style == null || style.selectIcon == null) {
            return super.getPrefWidth();
        }
        return super.getPrefWidth() + style.selectIcon.getMinWidth() + CB.scaledSizes.MARGINx2;
    }

    @Override
    public float getPrefHeight() {
        if (style == null || style.selectIcon == null) {
            return super.getPrefHeight();
        }
        return Math.max(super.getPrefHeight(), style.selectIcon.getMinHeight() + CB.scaledSizes.MARGINx2 * 2);
    }

    public T getSelected() {
        return selectedItem;
    }
}