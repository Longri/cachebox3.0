/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.animations.map.DoubleAnimator;
import de.longri.cachebox3.gui.map.layer.MapOrientationMode;
import de.longri.cachebox3.gui.skin.styles.CompassStyle;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_WidgetGroup;
import de.longri.cachebox3.utils.CB_RectF;

import static de.longri.cachebox3.gui.animations.map.DoubleAnimator.DEFAULT_DURATION;


/**
 * Created by Longri on 21.03.2017.
 */
public class Compass extends Catch_WidgetGroup implements Layout {

    public interface StateChanged {
        void stateChanged(MapOrientationMode state);
    }

    private final DoubleAnimator bearingAnimator = new DoubleAnimator();
    private final DoubleAnimator headingAnimator = new DoubleAnimator();


    private final CompassStyle style;
    private final CB_RectF rec_frame, rec_scale, rec_arrow;
    private final Matrix4 oldTransform = new Matrix4();
    private final Matrix4 transform_scale = new Matrix4();
    private final Matrix4 transform_arrow = new Matrix4();
    private final Matrix4 tmp = new Matrix4();
    private float lastBearing, lastHeading;
    private final boolean CAN_SCALE;

    private MapOrientationMode state = MapOrientationMode.NORTH;
    private StateChanged stateChangedListener;
    private boolean layoutChanged = false;

    public Compass(String style) {
        this(VisUI.getSkin().get(style, CompassStyle.class), false);
    }

    public Compass(String style, boolean useState) {
        this(VisUI.getSkin().get(style, CompassStyle.class), useState, false);
    }

    public Compass(CompassStyle style, boolean canScale) {
        this(style, false, canScale);
    }

    public Compass(CompassStyle style, boolean useState, boolean canScale) {
        this.style = style;
        rec_frame = new CB_RectF();
        rec_scale = new CB_RectF();
        rec_arrow = new CB_RectF();
        calcSizes();
        CAN_SCALE = canScale;
        if (useState) {
            this.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    // change state
                    int ordinal = state.ordinal();
                    ordinal++;
                    if (ordinal > 2)
                        ordinal = 0;

                    setState(MapOrientationMode.values()[ordinal]);
                }
            });
        }
    }

    private float minSize, prefSize, maxSize, scaleRatio, arrowRatio;


    public void setState(MapOrientationMode state) {
        if (this.state == state) return;
        this.state = state;
        CB.requestRendering();
        if (this.stateChangedListener != null) {
            this.stateChangedListener.stateChanged(this.state);
        }
    }

    public MapOrientationMode getState() {
        return this.state;
    }

    void setStateChangedListener(StateChanged listener) {
        this.stateChangedListener = listener;
    }

    private void calcSizes() {
        if (style == null || style.frameNorthOrient == null) return;
        prefSize = style.frameNorthOrient.getMinWidth();
        minSize = prefSize * (CAN_SCALE ? 0.4f : 1f);
        maxSize = prefSize * (CAN_SCALE ? 1.3f : 1f);
        scaleRatio = style.scale.getMinWidth() / prefSize;
        arrowRatio = style.arrow.getMinWidth() / prefSize;
    }

    public void draw(Batch batch, float parentAlpha) {
        validate();
        applyTransform(batch, computeTransform());
        drawBackground(batch, parentAlpha);
        resetTransform(batch);
    }

    private void drawBackground(Batch batch, float parentAlpha) {
        Color color = batch.getColor();
        batch.setColor(1, 1, 1, 1 * parentAlpha);
        //draw frame
        Drawable frame = null;
        switch (state) {
            case COMPASS:
                frame = style.frameCompasAlign;
                break;
            case USER:
                frame = style.frameUserRotate;
                break;
            case NORTH:
                frame = style.frameNorthOrient;
                break;
        }

        if (frame != null) {
            frame.draw(batch, rec_frame.getX(), rec_frame.getY(), rec_frame.getWidth(), rec_frame.getHeight());
        }

        oldTransform.set(batch.getTransformMatrix());

        //draw scale
        if (style.scale != null) {
            tmp.set(oldTransform);
            tmp.mul(transform_scale);
            batch.setTransformMatrix(tmp);
            style.scale.draw(batch, rec_scale.getX(), rec_scale.getY(), rec_scale.getWidth(), rec_scale.getHeight());
        }

        //draw arrow
        if (style.arrow != null) {
            tmp.set(oldTransform);
            tmp.mul(transform_arrow);
            batch.setTransformMatrix(tmp);
            style.arrow.draw(batch, rec_arrow.getX(), rec_arrow.getY(), rec_arrow.getWidth(), rec_arrow.getHeight());
        }

        batch.setTransformMatrix(oldTransform);
        batch.setColor(color);
    }

    @Override
    protected void sizeChanged() {
        layout();

        //set matrix new
        setBearing(lastBearing);
        setHeading(lastHeading);
    }

    public void layout() {
        float size = Math.min(this.getWidth(), this.getHeight());
        rec_frame.setSize(size, size);
        //set to center pos
        rec_frame.setPos(this.getWidth() / 2 - size / 2, this.getHeight() / 2 - size / 2);

        float centerX = rec_frame.getCenterPosX();
        float centerY = rec_frame.getCenterPosY();

        rec_scale.setSize(size * scaleRatio, size * scaleRatio);
        rec_scale.setX(centerX - rec_scale.getHalfWidth());
        rec_scale.setY(centerY - rec_scale.getHalfHeight());

        rec_arrow.setSize(size * arrowRatio, size * arrowRatio);
        rec_arrow.setX(centerX - rec_arrow.getHalfWidth());
        rec_arrow.setY(centerY - rec_arrow.getHalfHeight());
        layoutChanged = true;
    }

    @Override
    public float getMinWidth() {
        return minSize;
    }

    @Override
    public float getMinHeight() {
        return minSize;
    }

    @Override
    public float getPrefWidth() {
        return prefSize;
    }

    @Override
    public float getPrefHeight() {
        return prefSize;
    }

    @Override
    public float getMaxWidth() {
        return maxSize;
    }

    @Override
    public float getMaxHeight() {
        return maxSize;
    }


    public void setBearing(float bearing) {
        if (lastBearing != bearing) {
            bearingAnimator.start(DEFAULT_DURATION, lastBearing, bearing);
            lastBearing = bearing;
        }
    }

    void setHeading(float heading) {
        if (lastHeading != heading) {
            headingAnimator.start(DEFAULT_DURATION, lastHeading, heading);
            lastHeading = heading;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean changes = false;
        if (layoutChanged || bearingAnimator.update(delta)) {
            float animatedBearingValue = (float) bearingAnimator.getAct();
            transform_scale.idt();
            transform_scale.translate(rec_scale.getHalfWidth() + rec_scale.getX(), rec_scale.getHalfHeight() + rec_scale.getY(), 0);
            transform_scale.rotate(0, 0, 1, animatedBearingValue);
            transform_scale.translate(-(rec_scale.getHalfWidth() + rec_scale.getX()), -(rec_scale.getHalfHeight() + rec_scale.getY()), 0);
            changes = true;
        }

        if (layoutChanged || headingAnimator.update(delta)) {
            float animatedHeadingValue = (float) headingAnimator.getAct();
            transform_arrow.idt();
            transform_arrow.translate(rec_arrow.getHalfWidth() + rec_arrow.getX(), rec_arrow.getHalfHeight() + rec_arrow.getY(), 0);
            transform_arrow.rotate(0, 0, 1, -animatedHeadingValue);
            transform_arrow.translate(-(rec_arrow.getHalfWidth() + rec_arrow.getX()), -(rec_arrow.getHalfHeight() + rec_arrow.getY()), 0);
            changes = true;
        }
        layoutChanged = false;
        if (changes) CB.requestRendering();
    }
}
