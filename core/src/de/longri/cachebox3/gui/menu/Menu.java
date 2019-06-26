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
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 13.08.16.
 */
public class Menu extends Window {
    public final static float MORE_MENU_ANIMATION_TIME = 0.3f;
    final static Logger log = LoggerFactory.getLogger(Menu.class);
    final static boolean ALL = true;
    final CharSequence name;
    public MenuStyle style;
    protected ListView listView;
    protected Menu parentMenu;
    protected boolean hideWithItemClick;
    CB_List<ListViewItem> mItems = new CB_List();
    OnItemClickListener onItemClickListener;
    private Menu compoundMenu;
    private VisLabel titleLabel, parentTitleLabel;
    private WidgetGroup titleGroup;
    private WidgetGroup mainMenuWidgetGroup;
    private boolean isShowing = false;
    private OnHideListener onHideListener;
    private final ClickListener backClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            hide(false);
            CB.stageManager.unRegisterForBackKey(this);
        }
    };

    public Menu(CharSequence name) {
        super(name.toString());
        this.style = VisUI.getSkin().get("default", MenuStyle.class);
        this.name = name;
        this.setStageBackground(style.stageBackground);
        hideWithItemClick = true;
    }

    public Menu(CharSequence name, MenuStyle style) {
        super(name.toString());
        this.style = style;
        this.name = name;
        this.setStageBackground(style.stageBackground);
        hideWithItemClick = true;
    }

    public Menu(CharSequence name, String styleName) {
        this(name, VisUI.getSkin().get(styleName, MenuStyle.class));
    }

    public void setCompoundMenu(Menu compoundMenu) {
        this.compoundMenu = compoundMenu;
    }

    public void setHideWithItemClick(boolean heideWithItemClick) {
        this.hideWithItemClick = heideWithItemClick;
    }

    public MenuItem addMenuItem(CharSequence titleTranlationId, String titleExtension, boolean withoutTranslation, Drawable icon, ClickListener clickListener) {
        MenuItem item = new MenuItem(0, 738, "Menu Item@" + titleTranlationId.toString() + "[" + "" + "]", this);
        if (titleTranlationId.length() == 0) withoutTranslation = true;
        item.setTitle((withoutTranslation ? titleTranlationId : Translation.get(titleTranlationId.toString())) + titleExtension);
        if (icon != null)
            item.setIcon(icon);
        item.addListener(clickListener);
        mItems.add(item);
        return item;
    }

    private MenuItem addMenuItem(CharSequence titleTranlationId, String titleExtension, boolean withoutTranslation, Drawable icon, Runnable runnable) {
        MenuItem item = new MenuItem(0, 738, "Menu Item@" + titleTranlationId.toString() + "[" + "" + "]", this);
        if (titleTranlationId.length() == 0) withoutTranslation = true;
        item.setTitle((withoutTranslation ? titleTranlationId : Translation.get(titleTranlationId.toString())) + titleExtension);
        if (icon != null)
            item.setIcon(icon);
        item.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (mustHandle(event)) {
                    runnable.run();
                }
            }
        });
        mItems.add(item);
        return item;
    }

    public MenuItem addMenuItem(CharSequence titleTranlationId, String titleExtension, Drawable icon, Runnable runnable) {
        return addMenuItem(titleTranlationId, titleExtension, false, icon, runnable);
    }

    public MenuItem addMenuItem(CharSequence titleTranlationId, Drawable icon, Runnable runnable) {
        return addMenuItem(titleTranlationId, "", false, icon, runnable);
    }

    public MenuItem addCheckableMenuItem(CharSequence titleTranlationId, boolean checked, boolean withoutTranslation, Runnable runnable) {
        MenuItem item = addMenuItem(titleTranlationId, "", withoutTranslation, null, runnable);
        item.setCheckable(true);
        item.setChecked(checked);
        return item;
    }

    public MenuItem addCheckableMenuItem(CharSequence titleTranlationId, boolean checked, Runnable runnable) {
        return addCheckableMenuItem(titleTranlationId, checked, false, runnable);
    }

    public void addItem(final MenuItem menuItem) {

        menuItem.addListener(new ClickLongClickListener() {
            @Override
            public boolean clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return true;

                // if the clicked item disabled, ignore click event!
                if (!menuItem.mIsEnabled) {
                    //TODO toast message say's way is disabled
                    // must implement a property for text

                    CB.viewmanager.toast("Item is disabled");
                    return true;
                }


                // have the clicked item a moreMenu, just show it
                if (menuItem.hasMoreMenu()) {
                    menuItem.getMoreMenu(Menu.this.compoundMenu != null ? Menu.this.compoundMenu : Menu.this).show();
                    return true;
                }
                //close Menu with sub menu's
                if (hideWithItemClick) hide(ALL);
                return onItemClickListener.onItemClick(menuItem);
            }

            @Override
            public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                return false;
            }
        });

        mItems.add(menuItem);
    }

    public MenuItem addItem(int ID, CharSequence StringId) {
        return addItem(ID, StringId, "", false);
    }

    public MenuItem addItem(int ID, CharSequence StringId, boolean withoutTranslation) {
        return addItem(ID, StringId, "", withoutTranslation);
    }

    public MenuItem addItem(int ID, CharSequence StringId, String appendix, Sprite icon) {
        MenuItem item = addItem(ID, StringId, appendix);
        if (icon != null)
            item.setIcon(new SpriteDrawable(icon));
        return item;
    }

    public MenuItem addItem(int ID, CharSequence StringId, String appendix, Drawable icon) {
        MenuItem item = addItem(ID, StringId, appendix);
        if (icon != null)
            item.setIcon(icon);
        return item;
    }

    public MenuItem addItem(int ID, CharSequence StringId, String appendix) {
        return addItem(ID, StringId, appendix, false);
    }

    public MenuItem addItem(int index, String text, Drawable drawable, boolean withoutTranslation) {
        MenuItem item = addItem(index, text, "", withoutTranslation);
        if (drawable != null)
            item.setIcon(drawable);
        return item;
    }

    public MenuItem addItem(int ID, CharSequence StringId, String appendix, boolean withoutTranslation) {
        String trans;
        if (StringId == null || StringId.equals("")) {
            trans = appendix;
        } else {
            if (withoutTranslation)
                trans = StringId + appendix;
            else
                trans = Translation.get(StringId.toString()) + appendix;
        }

        MenuItem item = new MenuItem(0, ID, "Menu Item@" + ID + "[" + trans + "]", this);
        item.setTitle(trans);
        addItem(item);

        return item;
    }

    public MenuItem addCheckableItem(int ID, CharSequence StringId, boolean checked) {
        return addCheckableItem(ID, StringId, checked, false);
    }

    public MenuItem addCheckableItem(int ID, CharSequence StringId, boolean checked, boolean withoutTranslation) {
        MenuItem item = addItem(ID, StringId, "", withoutTranslation);
        item.setCheckable(true);
        item.setChecked(checked);
        return item;
    }

    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void show() {
        reorganizeListIndexes();
        initialLayout();
        showWidgetGroup();
        if (this.parentMenu != null) {
            showAsChild();
        }
        this.setTouchable(Touchable.enabled);
        CB.stageManager.registerForBackKey(backClickListener);
        log.debug("show menu: " + this.name);
    }

    private void showWidgetGroup() {
        clearActions();
        pack();

        mainMenuWidgetGroup = new WidgetGroup();
        mainMenuWidgetGroup.setName(this.name.toString());
        mainMenuWidgetGroup.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        mainMenuWidgetGroup.addActor(this);

        if (this.parentMenu == null) {
            showingStage = CB.stageManager.showOnNewStage(mainMenuWidgetGroup);
        } else {
            showingStage = CB.stageManager.showOnActStage(mainMenuWidgetGroup);
        }

        if (this.parentMenu == null)
            addAction(sequence(Actions.alpha(0), Actions.fadeIn(CB.WINDOW_FADE_TIME, Interpolation.fade)));
        isShowing = true;
    }


    private void showAsChild() {
        float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;
        parentMenu.mainMenuWidgetGroup.addAction(Actions.moveTo(0 - nextXPos, 0, MORE_MENU_ANIMATION_TIME));

        // remove stageBackground
        this.setStageBackground(null);

        mainMenuWidgetGroup.setPosition(nextXPos, 0);
        mainMenuWidgetGroup.addAction(Actions.moveTo(0, 0, MORE_MENU_ANIMATION_TIME));
        log.debug("show child menu: " + this.name);

        isShowing = true;
    }

    public void hide() {
        hide(false);
    }

    public void hide(boolean all) {
        if (this.onHideListener != null) {
            this.onHideListener.onHide();
        }
        if (!isShowing) return;
        if (this.parentMenu != null) {
            if (all) {
                CB.stageManager.removeAllWithActStage(showingStage);
                CB.stageManager.unRegisterForBackKey(backClickListener);
            } else {
                float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;
                mainMenuWidgetGroup.addAction(Actions.sequence(Actions.moveTo(0 + nextXPos, 0, MORE_MENU_ANIMATION_TIME), Actions.removeActor()));
                parentMenu.mainMenuWidgetGroup.addAction(Actions.moveTo(0, 0, MORE_MENU_ANIMATION_TIME));
            }
        } else {
            super.hide();
            CB.stageManager.unRegisterForBackKey(backClickListener);
        }
        log.debug("Hide menu: " + this.name);
        isShowing = false;
    }

    public void addOnHideListener(OnHideListener listener) {
        this.onHideListener = listener;
    }

    private void initialLayout() {

        //remove all child's
        this.clear();

        float topY = Gdx.graphics.getHeight() - CB.scaledSizes.MARGIN_HALF;
        float xPos = CB.scaledSizes.MARGIN_HALF;

        // add the titleLabel on top
        titleGroup = new WidgetGroup();


        if (style.menu_back != null) {
            Image backImage = new Image(style.menu_back);
            backImage.setPosition(xPos, 0);
            xPos += backImage.getWidth() + CB.scaledSizes.MARGIN;
            titleGroup.addActor(backImage);
        }

        String title = getName(); // !!! name is here(local access) and in actor(public getter/setter)
        if (title.length() > 0) {
            if (title.startsWith("-"))
                title = title.substring(1);
            else
                title = Translation.get(title).toString();
        } else title = " ";
        titleLabel = new VisLabel(title, "menu_title_act");

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
        this.reorganizeListIndexes();
        listView = new ListView(VERTICAL, false);
        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(new ListViewAdapter() {
                    @Override
                    public int getCount() {
                        return mItems.size;
                    }

                    @Override
                    public ListViewItem getView(int index) {
                        return mItems.get(index);
                    }

                    @Override
                    public void update(ListViewItem view) {

                    }
                });
            }
        });
        listView.setBackground(this.style.background);
        this.addActor(listView);
    }

    @Override
    public void pack() {
        super.pack();
        float maxListViewHeight = CB.scaledSizes.WINDOW_HEIGHT - (titleGroup.getHeight());
        listView.setBounds(((Gdx.graphics.getWidth() - CB.scaledSizes.WINDOW_WIDTH) / 2f), CB.scaledSizes.MARGIN,
                CB.scaledSizes.WINDOW_WIDTH, maxListViewHeight);

        float itemWidth = listView.getWidth() - (Math.max(listView.style.padLeft, listView.style.pad) + Math.max(listView.style.padRight, listView.style.pad));

        for (ListViewItem item : mItems) {
            item.setPrefWidth(itemWidth);
            item.setWidth(itemWidth);
            ((MenuItem) item).initial();
            item.layout();
            item.pack();
            item.layout();
        }
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
            addItem(new DividerItem(listIndex, this, this.style));
        }
    }

    public void reorganizeListIndexes() {
        for (int i = 0; i < mItems.size; i++) {
            mItems.get(i).setNewIndex(i);
        }
    }

    public ListView getListview() {
        if (listView == null) initialLayout();
        return listView;
    }

    public boolean mustHandle(InputEvent event) {
        if (event.isHandled()) {
            return false;
        } else {
            event.cancel(); // to set event is handled, ...
            MenuItem menuItem = (MenuItem) event.getListenerActor();
            if (menuItem.hasMoreMenu()) {
                menuItem.getMoreMenu(compoundMenu != null ? compoundMenu : this).show();
                return false;
            } else {
                if (hideWithItemClick)
                    hide(ALL);
                return true;
            }
        }
    }

    public interface OnHideListener {
        void onHide();
    }

    public static class MenuStyle {
        public BitmapFont font;
        public Color fontColor;
        public Drawable background, stageBackground, menu_back, menu_for, divider;
    }

}
