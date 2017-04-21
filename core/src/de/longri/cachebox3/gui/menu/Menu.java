/*
 * Copyright (C) 2016-2017 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Longri on 13.08.16.
 */
public class Menu extends Window {
    final static Logger log = LoggerFactory.getLogger(Menu.class);
    final static boolean ALL = true;
    public final static float MORE_MENU_ANIMATION_TIME = 0.3f;

    CB_List<ListViewItem> mItems = new CB_List();
    MenuStyle style;
    final String name;
    ListView listView;
    OnItemClickListener onItemClickListener;
    private VisLabel titleLabel, parentTitleLabel;
    protected Menu parentMenu;
    private WidgetGroup titleGroup;

    public Menu(String name) {
        super(name);
        this.style = VisUI.getSkin().get("default", MenuStyle.class);
        this.name = name;
        this.setStageBackground(style.stageBackground);
    }

    public Menu(String name, MenuStyle style) {
        super(name);
        this.style = style;
        this.name = name;
        this.setStageBackground(style.stageBackground);
    }

    public Menu(String name, String styleName) {
        this(name, VisUI.getSkin().get(styleName, MenuStyle.class));
    }

    public MenuItem addItem(int ID, String StringId, Drawable icon) {
        MenuItem item = addItem(ID, StringId);
        if (icon != null)
            item.setIcon(icon);
        return item;
    }

    public void addItem(MenuItem menuItem) {
        mItems.add(menuItem);
    }

    public MenuItem addItem(int ID, String StringId) {
        return addItem(ID, StringId, "", false);
    }

    public MenuItem addItem(int ID, String StringId, boolean withoutTranslation) {
        return addItem(ID, StringId, "", withoutTranslation);
    }

    public MenuItem addItem(int ID, String StringId, String appendix, Sprite icon) {
        MenuItem item = addItem(ID, StringId, appendix);
        if (icon != null)
            item.setIcon(new SpriteDrawable(icon));
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String appendix, Drawable icon) {
        MenuItem item = addItem(ID, StringId, appendix);
        if (icon != null)
            item.setIcon(icon);
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String appendix) {
        return addItem(ID, StringId, appendix, false);
    }

    public MenuItem addItem(int index, String text, Drawable drawable, boolean withoutTranslation) {
        MenuItem item = addItem(index, text, "", withoutTranslation);
        if (drawable != null)
            item.setIcon(drawable);
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String appendix, boolean withoutTranslation) {
        String trans;
        if (StringId == null || StringId.equals("")) {
            trans = appendix;
        } else {
            if (withoutTranslation)
                trans = StringId + appendix;
            else
                trans = Translation.Get(StringId) + appendix;
        }

        MenuItem item = new MenuItem(0, ID, "Menu Item@" + ID + "[" + trans + "]", this);
        item.setTitle(trans);
        addItem(item);

        return item;
    }

    public MenuItem addCheckableItem(int ID, String StringId, boolean checked) {
        MenuItem item = addItem(ID, StringId, "", false);
        item.setCheckable(true);
        item.setChecked(checked);
        return item;
    }

    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void show() {
        initialLayout();
        showWidgetGroup();
        if (this.parentMenu != null) {
            showAsChild();
        }
        this.setTouchable(Touchable.enabled);
        log.debug("Show menu: " + this.name);
    }


    protected WidgetGroup mainMenuWidgetGroup;

    private void showWidgetGroup() {
        clearActions();
        pack();

        mainMenuWidgetGroup = new WidgetGroup();
        mainMenuWidgetGroup.setName(this.name);
        mainMenuWidgetGroup.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        mainMenuWidgetGroup.addActor(this);

        if (this.parentMenu == null) {
            StageManager.showOnNewStage(mainMenuWidgetGroup);
        } else {
            StageManager.showOnActStage(mainMenuWidgetGroup);
        }

        if (this.parentMenu == null)
            addAction(sequence(Actions.alpha(0), Actions.fadeIn(CB.WINDOW_FADE_TIME, Interpolation.fade)));
    }


    private void showAsChild() {
        float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;
        parentMenu.mainMenuWidgetGroup.addAction(Actions.moveTo(0 - nextXPos, 0, MORE_MENU_ANIMATION_TIME));

        // remove stageBackground
        this.setStageBackground(null);

        mainMenuWidgetGroup.setPosition(nextXPos, 0);
        mainMenuWidgetGroup.addAction(Actions.moveTo(0, 0, MORE_MENU_ANIMATION_TIME));
        log.debug("Show child menu: " + this.name);
    }

    public void hide() {
        hide(false);
    }

    public void hide(boolean all) {
        if (this.parentMenu != null) {
            if (all) {
                StageManager.removeAllWithActStage();
            } else {
                float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;
                mainMenuWidgetGroup.addAction(Actions.sequence(Actions.moveTo(0 + nextXPos, 0, MORE_MENU_ANIMATION_TIME), Actions.removeActor()));
                parentMenu.mainMenuWidgetGroup.addAction(Actions.moveTo(0, 0, MORE_MENU_ANIMATION_TIME));
            }
        } else {
            super.hide();
        }
        log.debug("Hide menu: " + this.name);
    }


    private void initialLayout() {

        //remove all child's
        this.clear();

        float topY = Gdx.graphics.getHeight() - CB.scaledSizes.MARGIN_HALF;
        float xPos = CB.scaledSizes.MARGIN_HALF;

        // add the titleLabel on top
        titleGroup = new WidgetGroup();
        ClickListener backClickListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                hide(false);
            }
        };

        if (style.menu_back != null) {
            Image backImage = new Image(style.menu_back);
            backImage.setPosition(xPos, 0);
            xPos += backImage.getWidth() + CB.scaledSizes.MARGIN;
            titleGroup.addActor(backImage);
        }

        titleLabel = new VisLabel(this.name, "menu_title_act");

        if (parentMenu != null) {
            parentTitleLabel = new VisLabel(parentMenu.name, "menu_title_parent");
            parentTitleLabel.setPosition(xPos, 0);
            xPos += parentTitleLabel.getWidth() + CB.scaledSizes.MARGINx2;
            titleGroup.addActor(parentTitleLabel);
        } else {
            //center titleLabel
            xPos = (Gdx.graphics.getWidth() - titleLabel.getWidth()) / 2;
        }

        titleLabel.setPosition(xPos, 0);
        titleGroup.addActor(titleLabel);


        float titleHeight = titleLabel.getHeight() + CB.scaledSizes.MARGIN;
        titleGroup.setBounds(0, Gdx.graphics.getHeight() - (titleHeight), Gdx.graphics.getWidth(), titleHeight);
        titleGroup.addListener(backClickListener);

        this.addActor(titleGroup);

        final OnItemClickListener clickListener = new OnItemClickListener() {
            @Override
            public boolean onItemClick(final MenuItem item) {

                // have the clicked item a moreMenu, just show it
                if (item.hasMoreMenu()) {
                    item.getMoreMenu(Menu.this).show();
                    return true;
                }
                //close Menu with sub menu's
                hide(ALL);
                return onItemClickListener.onItemClick(item);
            }
        };


        Adapter listViewAdapter = new Adapter() {
            @Override
            public int getCount() {
                return mItems.size;
            }

            @Override
            public ListViewItem getView(int index) {
                MenuItem item = (MenuItem) mItems.get(index);
                item.setOnItemClickListener(clickListener);
                return item;
            }

            @Override
            public void update(ListViewItem view) {

            }

            @Override
            public float getItemSize(int index) {
                return mItems.get(index).getHeight();
            }
        };
        listView = new ListView(listViewAdapter);
        listView.setBackground(this.style.background);
        this.addActor(listView);
    }

    @Override
    public void pack() {
        for (ListViewItem item : mItems) {
            ((MenuItem) item).initial();
            item.pack();
        }


        super.pack();

        float maxListViewHeight = CB.scaledSizes.WINDOW_HEIGHT - (titleGroup.getHeight());
        listView.setBounds(((Gdx.graphics.getWidth() - CB.scaledSizes.WINDOW_WIDTH) / 2f), CB.scaledSizes.MARGIN,
                CB.scaledSizes.WINDOW_WIDTH, maxListViewHeight);
        listView.pack();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemClickListener getOnItemClickListeners() {
        return this.onItemClickListener;
    }

    public CB_List<ListViewItem> getItems() {
        return mItems;
    }

    public void addItems(CB_List<ListViewItem> items) {
        mItems.addAll(items);
    }

    public void addDivider(int listIndex) {

        if (this.style.divider != null) {
            MenuItem item = new MenuItem(listIndex, this);
            item.overrideBackground(this.style.divider);
            addItem(item);
        }

        log.debug("add Divider");
        //TODO add divider item
    }

    public void reorganizeListIndexes() {
        for (int i = 0; i < mItems.size; i++) {
            mItems.get(i).setNewIndex(i);
        }
    }

    public static class MenuStyle {
        public BitmapFont font;
        public Color fontColor;
        public Drawable background, stageBackground, menu_back, menu_for, divider;

    }

}
