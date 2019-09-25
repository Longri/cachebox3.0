/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Group;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_WidgetGroup;
import de.longri.cachebox3.settings.Config;

/**
 * Created by Longri on 09.09.16.
 */
public class Slider extends Catch_WidgetGroup {

    public final static float ANIMATION_TIME = 0.3f;

    private SliderStyle style;
    private boolean needsLayout = true;
    private final NameWidget nameWidget;
    private final Group content;
    private float sliderPos, maxSliderPos, minSliderPos;
    private final float nameWidgetHeight;
    private boolean swipeUp;
    private boolean swipeDown;
    private float quickButtonHeight;
    private float quickButtonMaxHeight;
    private final QuickButtonList quickButtonList;
    private ScrollPane logTextField;

    public Slider() {
        style = VisUI.getSkin().get("default", SliderStyle.class);
        quickButtonMaxHeight = CB.scaledSizes.BUTTON_HEIGHT;
        quickButtonList = new QuickButtonList();
        nameWidget = new NameWidget();
        nameWidgetHeight = CB.scaledSizes.BUTTON_HEIGHT / 2;
        content = new Group() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                validate();
                drawBackground(batch, parentAlpha, getX(), getY());
                super.draw(batch, parentAlpha);
            }

            private void drawBackground(Batch batch, float parentAlpha, float x, float y) {
                if (style.background == null) return;
                Color color = getColor();
                batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
                style.background.draw(batch, x, y, getWidth(), getHeight());
            }

        };

        super.addActor(nameWidget);
        super.addActor(content);
        super.addActor(quickButtonList);
        super.setTouchable(Touchable.childrenOnly);
        super.setLayoutEnabled(true);

        fillContent();
    }

    private void fillContent() {

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }


    @Override
    public void layout() {
        if (!needsLayout) {
            super.layout();
            return;
        }

        float quickButtonPos = sliderPos + nameWidgetHeight;
        if (quickButtonPos < this.getHeight() - quickButtonMaxHeight) {
            quickButtonPos = this.getHeight() - quickButtonMaxHeight;
        }

        quickButtonList.setBounds(0, quickButtonPos, this.getWidth(), quickButtonMaxHeight);
        nameWidget.setBounds(0, sliderPos, this.getWidth(), nameWidgetHeight);
        content.setBounds(0, sliderPos + nameWidgetHeight, this.getWidth(), this.getHeight());
        needsLayout = false;
        super.layout();
        viewHeightChanged(quickButtonPos - nameWidgetHeight);
    }

    public void viewHeightChanged(float height) {
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        needsLayout = true;

        maxSliderPos = getHeight() - nameWidgetHeight;
        minSliderPos = 0;

        //set slider pos to top
        setSliderPos(getHeight() - nameWidgetHeight);

        //set size for Log text field
//        logTextField.setBounds(0, 0, getWidth(), getHeight() - (nameWidgetHeight + quickButtonMaxHeight));

        //call layout for set size direct
        layout();
    }

    public void setCacheName(CharSequence cacheName) {
        nameWidget.setCacheName(cacheName);
    }

    private void setSliderPos(float pos) {
        if (sliderPos == pos) return;

        sliderPos = Math.max(Math.min(maxSliderPos, pos), minSliderPos);
        needsLayout = true;
        super.invalidate();
    }

    private void checkSlideBack() {
        boolean quickButtonShow = Config.quickButtonLastShow.getValue();

        quickButtonShow = true;

        // check if QuickButtonList snap in
        if (this.getHeight() - (nameWidgetHeight + sliderPos) >= (quickButtonMaxHeight * 0.5) && quickButtonShow) {
            quickButtonHeight = quickButtonMaxHeight;
            Config.quickButtonLastShow.setValue(true);
            Config.AcceptChanges();
        } else {
            quickButtonHeight = 0;
            Config.quickButtonLastShow.setValue(false);
            Config.AcceptChanges();
        }

        if (swipeUp || swipeDown) {
            if (swipeUp) {
                startAnimationTo(quickButtonShow ? quickButtonHeight : 0);
            } else {
                startAnimationTo((int) (getHeight() - nameWidgetHeight));
            }
            swipeUp = swipeDown = false;

        } else {
            if (sliderPos > getHeight() * 0.5) {
                startAnimationTo((int) (getHeight() - nameWidgetHeight - (quickButtonShow ? quickButtonHeight : 0)));
            } else {
                startAnimationTo(0);

            }
        }
    }

    private void startAnimationTo(float targetPos) {
        nameWidget.addAction(Actions.moveTo(0, targetPos, ANIMATION_TIME, Interpolation.exp10Out));
    }

    public void setQuickButtonVisible() {
        quickButtonHeight = quickButtonMaxHeight;
        nameWidget.setPosition(0, getHeight() - nameWidgetHeight - quickButtonHeight);
    }


    /**
     * the touchable slider with self scrolling selected CacheName if the name to long
     */
    private class NameWidget extends Catch_Group {


        private Drawable background;
        private final ScrollLabel nameLabel;

        private NameWidget() {
            background = style.slider;
            this.addListener(new DragListener() {
                public void drag(InputEvent event, float x, float y, int pointer) {
                    setSliderPos(getY() + y - getTouchDownY());
                }

                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    super.touchUp(event, x, y, pointer, button);
                    checkSlideBack();
                }
            });
            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = style.font;
            labelStyle.fontColor = style.fontColor;
            nameLabel = new ScrollLabel("", labelStyle);
            this.addActor(nameLabel);
        }

        private void setCacheName(final CharSequence name) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    nameLabel.setText(name);
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            validate();
            drawBackground(batch, parentAlpha, getX(), getY());
            super.draw(batch, parentAlpha);
        }


        /**
         * Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
         * drawable.
         */
        protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
            if (background == null) return;
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            background.draw(batch, x, y, getWidth(), getHeight());
        }


        @Override
        protected void positionChanged() {
            setSliderPos(this.getY());
        }

        @Override
        protected void sizeChanged() {
            nameLabel.setBounds(CB.scaledSizes.MARGIN, 0, this.getWidth() - CB.scaledSizes.MARGINx2, getHeight());
        }
    }


    public static class SliderStyle {
        public Drawable background, slider;
        public BitmapFont font;
        public Color fontColor;
    }
}
