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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.utils.IgnoreTouchInputListener;
import de.longri.cachebox3.gui.views.ListView;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Longri on 13.08.16.
 */
public class Menu extends Table {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Menu.class);
    static private final Vector2 tmpPosition = new Vector2();
    static private final Vector2 tmpSize = new Vector2();

    CB_List<MenuItem> mItems = new CB_List();
    MenuStyle style;
    final String name;
    ListView listView;

    public Menu(String name) {
        this.style = VisUI.getSkin().get("default", MenuStyle.class);
        this.name = name;
    }

    public Menu(String name, MenuStyle style) {
        this.style = style;
        this.name = name;
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

    public MenuItem addItem(int ID, String StringId, String anhang, Sprite icon) {
        MenuItem item = addItem(ID, StringId, anhang);
        if (icon != null)
            item.setIcon(new SpriteDrawable(icon));
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String anhang, Drawable icon) {
        MenuItem item = addItem(ID, StringId, anhang);
        if (icon != null)
            item.setIcon(icon);
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String anhang) {
        return addItem(ID, StringId, anhang, false);
    }

    public MenuItem addItem(int index, String text, Drawable drawable, boolean withoutTranslation) {
        MenuItem item = addItem(index, text, "", withoutTranslation);
        if (drawable != null)
            item.setIcon(drawable);
        return item;
    }

    public MenuItem addItem(int ID, String StringId, String anhang, boolean withoutTranslation) {
        String trans;
        if (StringId == null || StringId.equals("")) {
            trans = anhang;
        } else {
            if (withoutTranslation)
                trans = StringId + anhang;
            else
                trans = Translation.Get(StringId) + anhang;
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


    public void show() {
        CB.viewmanager.addActor(this);
        CB.viewmanager.setKeyboardFocus(this);
        CB.viewmanager.setScrollFocus(this);
        initialLayout();
        clearActions();
        pack();
        addAction(sequence(Actions.alpha(0), Actions.fadeIn(CB.WINDOW_FADE_TIME, Interpolation.fade)));
//        setPosition(Math.round((CB.viewmanager.getWidth() - getWidth()) / 2), Math.round((CB.viewmanager.getHeight() - getHeight()) / 2));
        log.debug("Show menu: " + this.name);
    }

    private void initialLayout() {
        listView = new ListView(mItems.size()) {
            @Override
            public VisTable createView(Integer index) {
                return mItems.get(index);
            }
        };
        listView.getMainTable().setBackground(this.style.background);
        this.add(listView.getMainTable());
    }

    @Override
    public void pack() {
        super.pack();
        listView.getMainTable().setBounds(((CB.viewmanager.getWidth() - CB.scaledSizes.WINDOW_WIDTH) / 2f),
                ((CB.viewmanager.getHeight() - listView.getMainTable().getHeight()) / 2),
                CB.scaledSizes.WINDOW_WIDTH, listView.getMainTable().getHeight());
    }

    public void hide() {
        clearActions();
        addCaptureListener(IgnoreTouchInputListener.INSTANCE);
        addAction(sequence(Actions.fadeOut(CB.WINDOW_FADE_TIME, Interpolation.fade), Actions.removeActor()));
        log.debug("Hide menu: " + this.name);
    }


    public void draw(Batch batch, float parentAlpha) {
        if (style.stageBackground != null) drawStageBackground(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    private void drawStageBackground(Batch batch, float parentAlpha) {
        Stage stage = getStage();
        if (stage.getKeyboardFocus() == null) stage.setKeyboardFocus(this);

        stageToLocalCoordinates(tmpPosition.set(0, 0));
        stageToLocalCoordinates(tmpSize.set(stage.getWidth(), stage.getHeight()));
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        style.stageBackground.draw(batch, getX() + tmpPosition.x, getY() + tmpPosition.y, getX() + tmpSize.x,
                getY() + tmpSize.y);
    }


    public static class MenuStyle {
        public Drawable background;
        public BitmapFont font;
        public Color fontColor;
        public Drawable stageBackground;
    }


}
