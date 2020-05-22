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
import de.longri.cachebox3.gui.menu.QuickAction;
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

    final QuickButtonListStyle quickButtonListStyle;
    final ScrollPane scrollPane;
    final Group scrollPaneContent = new Group();
    final CB_RectF tempClickRec = new CB_RectF();
    private MoveableList<QuickButtonItem> quickButtonList;
    private ClickLongClickListener captureListener;

    public QuickButtonList() {
        quickButtonListStyle = VisUI.getSkin().get(QuickButtonListStyle.class);
        scrollPane = new ScrollPane(scrollPaneContent);
        scrollPane.setOverscroll(true, false);
        scrollPane.setFlickScroll(true);
        addActor(scrollPane);
        captureListener = new ClickLongClickListener() {
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
            public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                return true;
            }
        };
        readQuickButtonItemsList();
        setTouchable(Touchable.childrenOnly);
        Config.quickButtonList.addChangedEventListener(() -> {
            int oldCount = quickButtonList.size;
            quickButtonList = null;
            readQuickButtonItemsList();
            if (oldCount != quickButtonList.size) {
                sizeChanged();
            }
        });
    }


    private void readQuickButtonItemsList() {
        if (quickButtonList == null) {
            String configActionList = Config.quickButtonList.getValue();
            String[] configList = configActionList.split(",");
            quickButtonList = new MoveableList<>();
            if (configList.length != 0) {
                boolean invalidEnumId = false;
                int index = 0;

                for (String s : configList) {
                    try {
                        s = s.replace(",", "");
                        int ordinal = Integer.parseInt(s);
                        if (ordinal > -1) {
                            QuickAction quickAction = QuickAction.values()[ordinal];
                            if (quickAction != null && quickAction.getAction() != null) {
                                quickButtonList.add(new QuickButtonItem(index++, quickButtonListStyle.button, quickAction));
                            } else
                                invalidEnumId = true;
                        }
                    } catch (Exception e) {// wenn ein Fehler auftritt, gib die bis dorthin gelesenen Items zurück
                        log.error("getListFromConfig", e);
                        invalidEnumId = true;
                    }
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
        float buttonSquare = CB.scaledSizes.BUTTON_HEIGHT;

        for (QuickButtonItem item : quickButtonList) {
            item.setBounds(xPos, 0, buttonSquare, buttonSquare);
            scrollPaneContent.addActor(item);
            xPos += buttonSquare + buttonMargin;
        }
        float completeWidth = xPos + buttonMargin;
        scrollPaneContent.setBounds(0, 0, completeWidth, buttonSquare);
        scrollPaneContent.addCaptureListener(captureListener);
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
        if (quickButtonListStyle.background == null) return;
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        float drawableWidth = (quickButtonListStyle.background instanceof TextureRegionDrawable) ?
                ((TextureRegionDrawable) quickButtonListStyle.background).getRegion().getRegionWidth() : getWidth();
        float drawX = ((getWidth() - drawableWidth) / 2) + x;
        quickButtonListStyle.background.draw(batch, drawX, y, drawableWidth, getHeight());
    }

    public MoveableList<QuickButtonItem> getQuickButtonList() {
        return quickButtonList;
    }


    public static class QuickButtonListStyle {
        public Drawable background, button;
    }
}
