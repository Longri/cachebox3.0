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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 09.09.16.
 */
public class Slider extends WidgetGroup {

    private SliderStyle style;
    private boolean needsLayout = true;
    private final NameWidget nameWidget;
    private final WidgetGroup content;
    private float sliderPos, maxSliderPos, minSliderPos;
    private final float nameWidgetHeight;

    public Slider() {
        style = VisUI.getSkin().get("default", SliderStyle.class);
        nameWidget = new NameWidget();
        nameWidgetHeight = CB.scaledSizes.BUTTON_HEIGHT;
        content = new WidgetGroup() {
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
        super.setTouchable(Touchable.childrenOnly);
        super.setLayoutEnabled(true);
    }


    @Override
    public void layout() {
        if (!needsLayout) {
            super.layout();
            return;
        }
        nameWidget.setBounds(0, sliderPos, this.getWidth(), nameWidgetHeight);
        content.setBounds(0, sliderPos + nameWidgetHeight, this.getWidth(), this.getHeight());
        needsLayout = false;
        super.layout();
    }

    /**
     * Called when the actor's size has been changed.
     */
    protected void sizeChanged() {
        needsLayout = true;
        super.invalidate();


        maxSliderPos = getHeight() - nameWidgetHeight;
        minSliderPos = 0;

        //set slider pos to top
        setSliderPos(getHeight() - nameWidgetHeight);
    }

    private void setSliderPos(float pos) {
        sliderPos = Math.max(Math.min(maxSliderPos, pos), minSliderPos);


        needsLayout = true;
        super.invalidate();
    }


    /**
     * the touchable slider with self scrolling selected CacheName if the name to long
     */
    private class NameWidget extends WidgetGroup {

        private CharSequence name;
        private Drawable background;

        private NameWidget() {
            background = style.slider;
            this.addListener(new DragListener() {
                public void drag(InputEvent event, float x, float y, int pointer) {
                    setSliderPos(getY() + y - getTouchDownY());
                }
            });
        }

        private void setName(CharSequence name) {
            this.name = name;
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

    }


    public static class SliderStyle {
        Drawable background, slider;
        BitmapFont font;
        Color fontColor;
    }
}
