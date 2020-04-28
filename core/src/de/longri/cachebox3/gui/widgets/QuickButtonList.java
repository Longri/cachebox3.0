/*
 * Copyright (C) 2020 - 2018 team-cachebox.de
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.QuickActions;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Group;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.MoveableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonList extends Catch_Group {

    private final static Logger log = LoggerFactory.getLogger(QuickButtonList.class);


    final QuickButtonListStyle style;
    final ScrollPane scrollPane;
    final Group scrollPaneContent = new Group();
    private MoveableList<QuickButtonItem> quickButtonList;

    final CB_RectF tempClickRec = new CB_RectF();

    public QuickButtonList() {
        style = VisUI.getSkin().get(QuickButtonListStyle.class);
        scrollPane = new ScrollPane(scrollPaneContent);
        scrollPane.setOverscroll(true, false);
        scrollPane.setFlickScroll(true);
        this.addActor(scrollPane);
        readQuickButtonItemsList();
        this.setTouchable(Touchable.childrenOnly);

        scrollPaneContent.addCaptureListener(new ClickLongClickListener() {
            @Override
            public boolean clicked(InputEvent event, float x, float y) {
                log.debug("QuickButton clicked on x:{}  y:{}", x, y);
                SnapshotArray<Actor> childs = scrollPaneContent.getChildren();
                for (int i = 0, n = childs.size; i < n; i++) {
                    QuickButtonItem item = (QuickButtonItem) childs.get(i);
                    tempClickRec.set(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                    if (tempClickRec.contains(x, y)) {
                        // item Clicked
                        log.debug("QuickButtonItem {} clicked", i);
                        item.clicked();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean longClicked(Actor actor, float x, float y,float touchDownStageX, float touchDownStageY) {
                return true;
            }
        });
    }


    private void readQuickButtonItemsList() {
        if (quickButtonList == null) {
            //TODO make quick buttons configurable at SettingsView
            String configActionList = Config.quickButtonList.getValue();
            configActionList = "10,19,11,12,5,0,1,21,3,2,4,15,25,24";
            String[] configList = configActionList.split(",");
            quickButtonList = new MoveableList<>();
            if (configList.length != 0) {
                boolean invalidEnumId = false;
                try {
                    int index = 0;

                    for (String s : configList) {
                        s = s.replace(",", "");
                        int EnumId = Integer.parseInt(s);
                        if (EnumId > -1) {

                            QuickActions type = QuickActions.values()[EnumId];
                            if (type != null) {
                                QuickButtonItem tmp = new QuickButtonItem(index++, style.button, type.getAction(), type.getName(), type);
                                quickButtonList.add(tmp);
                            } else
                                invalidEnumId = true;
                        }
                    }
                } catch (Exception e) {// wenn ein Fehler auftritt, gib die bis dorthin gelesenen Items zurück
                    log.error("getListFromConfig", e);
                }
                if (invalidEnumId) {
                    //	    write valid id's back
                    StringBuilder actionsString = new StringBuilder();
                    int counter = 0;
                    for (int i = 0, n = quickButtonList.size; i < n; i++) {
                        QuickButtonItem tmp = quickButtonList.get(i);
                        actionsString.append(tmp.getAction().ordinal());
                        if (counter < quickButtonList.size - 1) {
                            actionsString.append(",");
                        }
                        counter++;
                    }
                    Config.quickButtonList.setValue(actionsString.toString());
                    Config.AcceptChanges();
                }
            }
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
        float drawableWidth = (style.background instanceof TextureRegionDrawable) ?
                ((TextureRegionDrawable) style.background).getRegion().getRegionWidth() : getWidth();
        float drawX = ((getWidth() - drawableWidth) / 2) + x;
        style.background.draw(batch, drawX, y, drawableWidth, getHeight());
    }

    public MoveableList<QuickButtonItem> getQuickButtonList() {
        return quickButtonList;
    }


    public static class QuickButtonListStyle {
        public Drawable background, button;
    }
}
