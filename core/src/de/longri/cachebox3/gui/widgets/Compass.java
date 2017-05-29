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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.skin.styles.CompassStyle;
import de.longri.cachebox3.utils.CB_RectF;

/**
 * Created by Longri on 21.03.2017.
 */
public class Compass extends WidgetGroup implements Layout {

    private final CompassStyle style;
    private final CB_RectF rec_frame, rec_scale, rec_arrow;
    private final Matrix4 oldTransform = new Matrix4();
    private final Matrix4 transform_scale = new Matrix4();
    private final Matrix4 transform_arrow = new Matrix4();
    private final Matrix4 tmp = new Matrix4();
    private float lastBearing, lastHeadiong;

    public Compass(String style) {
        this(VisUI.getSkin().get(style, CompassStyle.class));
    }

    public Compass(CompassStyle style) {
        this.style = style;
        rec_frame = new CB_RectF();
        rec_scale = new CB_RectF();
        rec_arrow = new CB_RectF();
        calcSizes();
    }

    private float minSize, prefSize, maxSize, scaleRatio, arrowRatio;

    private void calcSizes() {
        prefSize = style.frameNorthOrient.getMinWidth();
        minSize = prefSize * 0.6f;
        maxSize = prefSize * 1.3f;
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
        batch.setColor(1, 1, 1, 1);
        //draw frame
        if (style.frameNorthOrient != null) {
            style.frameNorthOrient.draw(batch, rec_frame.getX(), rec_frame.getY(), rec_frame.getWidth(), rec_frame.getHeight());
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
        setHeading(lastHeadiong);
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
        lastBearing = bearing;
        transform_scale.idt();
        transform_scale.translate(rec_scale.getHalfWidth() + rec_scale.getX(), rec_scale.getHalfHeight() + rec_scale.getY(), 0);
        transform_scale.rotate(0, 0, 1, bearing);
        transform_scale.translate(-(rec_scale.getHalfWidth() + rec_scale.getX()), -(rec_scale.getHalfHeight() + rec_scale.getY()), 0);
    }

    public void setHeading(float heading) {
        lastHeadiong = heading;
        transform_arrow.idt();
        transform_arrow.translate(rec_arrow.getHalfWidth() + rec_arrow.getX(), rec_arrow.getHalfHeight() + rec_arrow.getY(), 0);
        transform_arrow.rotate(0, 0, 1, -heading);
        transform_arrow.translate(-(rec_arrow.getHalfWidth() + rec_arrow.getX()), -(rec_arrow.getHalfHeight() + rec_arrow.getY()), 0);
    }

}
