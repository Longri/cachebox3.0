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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 05.07.2017.
 */
public class CB_ProgressBar extends VisProgressBar {
    private final static Logger log = LoggerFactory.getLogger(CB_ProgressBar.class);

    boolean round = true;

    public CB_ProgressBar(float min, float max, float stepSize, boolean vertical) {
        super(min, max, stepSize, vertical);
    }

    public CB_ProgressBar(float min, float max, float stepSize, boolean vertical, String styleName) {
        super(min, max, stepSize, vertical, styleName);
    }

    public CB_ProgressBar(float min, float max, float stepSize, boolean vertical, ProgressBarStyle style) {
        super(min, max, stepSize, vertical, style);
    }

    public void setRound(boolean round) {
        super.setRound(round);
        this.round = round;
    }

    public boolean setValue(float value) {
        boolean ret = false;
        try {
            ret = super.setValue(value);
        } catch (Exception e) {
        }
        return ret;
    }

    public float getPrefWidth() {
        ProgressBarStyle style = this.getStyle();
        boolean disabled = this.isDisabled();
        boolean vertical = this.isVertical();
        if (vertical) {
            final Drawable knob = getKnobDrawable();
            final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
            return Math.max(knob == null ? 0 : getDrawableMinWidth(knob), getDrawableMinWidth(bg));
        } else
            return 140;
    }

    public float getPrefHeight() {
        ProgressBarStyle style = this.getStyle();
        boolean disabled = this.isDisabled();
        boolean vertical = this.isVertical();
        if (vertical)
            return 140;
        else {
            final Drawable knob = getKnobDrawable();
            final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
            return Math.max(knob == null ? 0 : getKnobMinHeight(knob), bg == null ? 0 : getKnobMinHeight(bg));
        }
    }

    private float getDrawableMinWidth(Drawable drawable) {
        if (drawable instanceof SvgNinePatchDrawable) {
            return ((SvgNinePatchDrawable) drawable).getPatch().getTotalWidth();
        }
        return drawable.getMinWidth();
    }

    private float getKnobMinHeight(Drawable drawable) {
        if (drawable instanceof SvgNinePatchDrawable) {
            return ((SvgNinePatchDrawable) drawable).getPatch().getTotalHeight();
        }
        return drawable.getMinHeight();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ProgressBarStyle style = this.getStyle();
        boolean disabled = this.isDisabled();
        boolean vertical = this.isVertical();
        float min = this.getMinValue();
        float max = this.getMaxValue();
        float position = this.getKnobPosition();
        final Drawable knob = getKnobDrawable();
        final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
        final Drawable knobBefore = (disabled && style.disabledKnobBefore != null) ? style.disabledKnobBefore : style.knobBefore;
        final Drawable knobAfter = (disabled && style.disabledKnobAfter != null) ? style.disabledKnobAfter : style.knobAfter;

        Color color = getColor();
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
        float knobHeight = knob == null ? 0 : knob.getMinHeight();
        float knobWidth = knob == null ? 0 : knob.getMinWidth();
        float percent = getVisualPercent();

        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        if (vertical) {
            float positionHeight = height;

            float bgTopHeight = 0;
            if (bg != null) {
                if (round)
                    bg.draw(batch, Math.round(x + (width - getDrawableMinWidth(bg)) * 0.5f), y, Math.round(getDrawableMinWidth(bg)), height);
                else
                    bg.draw(batch, x + width - getDrawableMinWidth(bg) * 0.5f, y, getDrawableMinWidth(bg), height);
                bgTopHeight = bg.getTopHeight();
                positionHeight -= bgTopHeight + bg.getBottomHeight();
            }

            if (this.getMinValue() != this.getMaxValue()) {
                float knobHeightHalf = 0;
                if (min != max) {
                    if (knob == null) {
                        knobHeightHalf = knobBefore == null ? 0 : getKnobMinHeight(knobBefore) * 0.5f;
                        position = (positionHeight - knobHeightHalf) * percent;
                        position = Math.min(positionHeight - knobHeightHalf, position);
                    } else {
                        knobHeightHalf = knobHeight * 0.5f;
                        position = (positionHeight - knobHeight) * percent;
                        position = Math.min(positionHeight - knobHeight, position) + bg.getBottomHeight();
                    }
                    position = Math.max(0, position);
                }

                if (knobBefore != null) {
                    float offset = 0;
                    if (bg != null) offset = bgTopHeight;
                    if (round)
                        knobBefore.draw(batch, Math.round(x + (width - getDrawableMinWidth(knobBefore)) * 0.5f), Math.round(y + offset), Math.round(getDrawableMinWidth(knobBefore)),
                                Math.round(position + knobHeightHalf));
                    else
                        knobBefore.draw(batch, x + (width - getDrawableMinWidth(knobBefore)) * 0.5f, y + offset, getDrawableMinWidth(knobBefore),
                                position + knobHeightHalf);
                }
                if (knobAfter != null) {
                    if (round)
                        knobAfter.draw(batch, Math.round(x + (width - getDrawableMinWidth(knobAfter)) * 0.5f), Math.round(y + position + knobHeightHalf),
                                Math.round(getDrawableMinWidth(knobAfter)), Math.round(height - position - knobHeightHalf));
                    else
                        knobAfter.draw(batch, x + (width - getDrawableMinWidth(knobAfter)) * 0.5f, y + position + knobHeightHalf,
                                getDrawableMinWidth(knobAfter), height - position - knobHeightHalf);
                }
                if (knob != null) {
                    if (round)
                        knob.draw(batch, Math.round(x + (width - knobWidth) * 0.5f), Math.round(y + position), Math.round(knobWidth), Math.round(knobHeight));
                    else
                        knob.draw(batch, x + (width - knobWidth) * 0.5f, y + position, knobWidth, knobHeight);
                }
            }
        } else {
            float positionWidth = width;

            float bgLeftWidth = 0;
            if (bg != null) {
                if (round)
                    bg.draw(batch, x, Math.round(y + (height - getKnobMinHeight(bg)) * 0.5f), width, Math.round(getKnobMinHeight(bg)));
                else
                    bg.draw(batch, x, y + (height - getKnobMinHeight(bg)) * 0.5f, width, getKnobMinHeight(bg));
                bgLeftWidth = bg.getLeftWidth();
                positionWidth -= bgLeftWidth + bg.getRightWidth();
            }

            if (this.getMinValue() != this.getMaxValue()) {
                float knobWidthHalf = 0;
                if (min != max) {
                    if (knob == null) {
                        knobWidthHalf = knobBefore == null ? 0 : getDrawableMinWidth(knobBefore) /** 0.5f*/;
                        position = (positionWidth - knobWidthHalf) * percent;
                        position = Math.min(positionWidth - knobWidthHalf, position);
                    } else {
                        knobWidthHalf = knobWidth * 0.5f;
                        position = (positionWidth - knobWidth) * percent;
                        position = Math.min(positionWidth - knobWidth, position) + bgLeftWidth;
                    }
                    position = Math.max(0, position);
                }

                if (knobBefore != null) {
                    float offset = 0;
                    if (bg != null) offset = bgLeftWidth;
                    if (round)
                        knobBefore.draw(batch, Math.round(x + offset), Math.round(y + (height - getKnobMinHeight(knobBefore)) * 0.5f),
                                Math.round(position + knobWidthHalf), Math.round(getKnobMinHeight(knobBefore)));
                    else
                        knobBefore.draw(batch, x + offset, y + (height - getKnobMinHeight(knobBefore)) * 0.5f,
                                position + knobWidthHalf, getKnobMinHeight(knobBefore));
                }
                if (knobAfter != null) {
                    if (round)
                        knobAfter.draw(batch, Math.round(x + position + knobWidthHalf), Math.round(y + (height - getKnobMinHeight(knobAfter)) * 0.5f),
                                Math.round(width - position - knobWidthHalf), Math.round(getKnobMinHeight(knobAfter)));
                    else
                        knobAfter.draw(batch, x + position + knobWidthHalf, y + (height - getKnobMinHeight(knobAfter)) * 0.5f,
                                width - position - knobWidthHalf, getKnobMinHeight(knobAfter));
                }
                if (knob != null) {
                    if (round)
                        knob.draw(batch, Math.round(x + position), Math.round(y + (height - knobHeight) * 0.5f), Math.round(knobWidth), Math.round(knobHeight));
                    else
                        knob.draw(batch, x + position, y + (height - knobHeight) * 0.5f, knobWidth, knobHeight);
                }
            }
        }
    }

}
