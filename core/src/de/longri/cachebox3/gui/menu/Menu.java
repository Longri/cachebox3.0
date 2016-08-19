/*
 * Copyright (C) 2016 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.views.ListView;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 13.08.16.
 */
public class Menu extends Window {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Menu.class);

    CB_List<MenuItem> mItems = new CB_List();
    MenuStyle style;
    final String name;
    ListView listView;
    OnItemClickListener onItemClickListener;


    InputListener clickListener = new InputListener() {

        private final CB_RectF listViewRec = new CB_RectF();

        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            // close menu if outside of listView
            listViewRec.set(listView.getMainTable().getX(), listView.getMainTable().getY(),
                    listView.getMainTable().getWidth(), listView.getMainTable().getHeight());
            if (!listViewRec.contains(x, y)) {
                hide();
                return true;
            }
            return false;
        }
    };


    public Menu(String name) {
        this.style = VisUI.getSkin().get("default", MenuStyle.class);
        this.name = name;
        this.setStageBackground(style.stageBackground);
    }

    public Menu(String name, MenuStyle style) {
        this.style = style;
        this.name = name;
        this.setStageBackground(style.stageBackground);
    }

    public Menu(String name, String styleName) {
        this(name, VisUI.getSkin().get(styleName, MenuStyle.class));
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

        MenuItem item = new MenuItem(0, ID, "Menu Item@" + ID);
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
        super.show();
        this.setTouchable(Touchable.enabled);
        CB.windowStage.addListener(clickListener);
        log.debug("Show menu: " + this.name);
    }

    public void hide() {
        super.hide();
        CB.windowStage.removeListener(clickListener);
        log.debug("Hide menu: " + this.name);
    }

    private void initialLayout() {

        final OnItemClickListener clickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(final MenuItem item) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onItemClickListener.onItemClick(item);
                    }
                });

                //close Menu
                hide();
                thread.start();

            }
        };

        listView = new ListView(mItems.size()) {

            @Override
            public VisTable createView(Integer index) {
                MenuItem item = mItems.get(index);

                item.setOnItemClickListener(clickListener);

                return item;
            }
        };
        listView.getMainTable().setBackground(this.style.background);
        this.add(listView.getMainTable());
    }

    @Override
    public void pack() {
        float height = 0;
        float itemPad = 0;
        for (MenuItem item : mItems) {
            item.initial();
            item.pack();
            itemPad = item.getPadTop();
            height += item.getHeight();
        }
        height += listView.getMainTable().getBackground().getMinHeight() + CB.scaledSizes.MARGIN + itemPad;
        if (height > CB.scaledSizes.WINDOW_HEIGHT) height = CB.scaledSizes.WINDOW_HEIGHT;
        listView.rebuildView();
        super.pack();
        listView.getMainTable().setBounds(((CB.windowStage.getWidth() - CB.scaledSizes.WINDOW_WIDTH) / 2f),
                ((CB.windowStage.getHeight() - height) / 2),
                CB.scaledSizes.WINDOW_WIDTH, height);
    }

    public void addOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemClickListener getOnItemClickListeners() {
        return this.onItemClickListener;
    }

    public CB_List<MenuItem> getItems() {
        return mItems;
    }

    public void addItems(CB_List<MenuItem> items) {
        mItems.addAll(items);
    }

    public void addDivider() {
        //TODO add divider item
    }

    public static class MenuStyle {
        public Drawable background;
        public BitmapFont font;
        public Color fontColor;
        public Drawable stageBackground;
    }


}
