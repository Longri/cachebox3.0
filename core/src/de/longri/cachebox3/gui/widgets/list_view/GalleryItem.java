/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets.list_view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.DoubleClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.building.utilities.Alignment;
import de.longri.cachebox3.gui.widgets.Image;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryItem extends ListViewItem {

    private static final Logger log = LoggerFactory.getLogger(GalleryItem.class);

    private static final float zoomMax = 8;
    private static final float zoomMin = 1;

    private final ImageLoader iloader;
    private final Image img;
    private float zoom = 1.0f;
    private float proportion = -1;
    private EventListener eventListener;


    public GalleryItem(int index, ImageLoader loader) {
        super(index);
        iloader = loader;
        img = new Image(iloader, "", false);
        img.setHAlignment(Alignment.CENTER);
        this.add(img);
    }


    @Override
    public void layout() {
        super.layout();

        if (this.getWidth() > 0) {
            iloader.setResizeListener((width, height) -> {
                log.debug("GalleryItem: resized");
                GalleryItem.this.invalidateHierarchy();
                GalleryItem.this.layout();
            }, this.getWidth());
        }

        CB_RectF rec = new CB_RectF(0, 0, getWidth(), getHeight()).scaleCenter(0.95f);
        img.setBounds(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
    }

    @Override
    public float getPrefHeight() {
        return this.hasParent() ? ((GalleryListView) this.getParent()).getPrefHeight() : super.getPrefHeight();
    }

    @Override
    public float getPrefWidth() {
        return this.hasParent() ? ((GalleryListView) this.getParent()).getPrefHeight() : super.getPrefHeight();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Item: ");
        sb.append(Integer.toString(this.index));
        sb.append(" x: ").append(Float.toString(this.getX()));
        sb.append(" w: ").append(Float.toString(this.getWidth()));
        sb.append(" v: ").append(Boolean.toString(this.isVisible()));
        return sb.toString();
    }

    public String getImagePath() {
        return this.iloader.getOriginalImagePath();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (zoom > 1.0f) {
            Drawable imgDrawable = this.iloader.getDrawable(Gdx.graphics.getDeltaTime());

            if (!this.iloader.inLoad) {
                //calculate proportional width/height and pos
                if (proportion < 0) {// calc once
                    float proportionWidth = getWidth() / this.iloader.getSpriteWidth();
                    float proportionHeight = getHeight() / this.iloader.getSpriteHeight();
                    proportion = Math.min(proportionWidth, proportionHeight);
                    drwWidth = this.iloader.getSpriteWidth() * proportion * zoom;
                    drwHeight = this.iloader.getSpriteHeight() * proportion * zoom;
                }

                float drwX = this.getX() - amountX;
                float drwY = this.getY() - amountY;


                //draw with scissor
                Rectangle scissors = new Rectangle();
                Rectangle clipBounds = new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
                getStage().calculateScissors(clipBounds, scissors);
                if (ScissorStack.pushScissors(scissors)) {
                    imgDrawable.draw(batch, drwX, drwY, drwWidth, drwHeight);
                    batch.flush();
                    ScissorStack.popScissors();
                } else {
                    imgDrawable.draw(batch, drwX, drwY, drwWidth, drwHeight);
                }
            }
        } else {
            super.draw(batch, parentAlpha);
        }
    }

    public void zoom(float x, float y, float scale) {
        this.zoom += scale;
        log.debug("zoom {} to actScale {}", scale, this.zoom);

        if (this.zoom > zoomMax) this.zoom = zoomMax;
        if (this.zoom < zoomMin) this.zoom = zoomMin;

        //calculate over dragging bounds
        drwWidth = this.iloader.getSpriteWidth() * proportion * zoom;
        drwHeight = this.iloader.getSpriteHeight() * proportion * zoom;

        maxX = (drwWidth - this.getWidth());
        maxY = (drwHeight - this.getHeight());
        if (this.zoom > 1.0f) clamp();

        log.debug("zoomed size w:{} , h:{}", drwWidth, drwHeight);
    }

    public float getZoom() {
        return this.zoom;
    }

    float drwWidth;
    float drwHeight;

    float velocityX, velocityY;
    float flingTimer;
    boolean animating = false;
    float amountX, amountY;
    float maxX, maxY;
    float flingTime = 1f;
    private boolean panning = false;
    private float overscrollDistance = 50, overscrollSpeedMin = 30, overscrollSpeedMax = 200;

    public void cancelTouchFocus() {
        if (eventListener == null) return;
        Stage stage = getStage();
        if (stage != null) stage.cancelTouchFocusExcept(eventListener, this);
    }

    public void drag(float deltaX, float deltaY) {
        amountX -= deltaX;
        amountY -= deltaY;
        clamp();
//        if ((deltaX != 0) || (deltaY != 0)) cancelTouchFocus();
    }

    public void fling(float x, float y) {
        if (this.zoom > 1.0f) {
            if (Math.abs(x) > 150) {
                flingTimer = flingTime;
                velocityX = x;
//                if (cancelTouchFocus) cancelTouchFocus();
            }
            if (Math.abs(y) > 150) {
                flingTimer = flingTime;
                velocityY = y;
//                if (cancelTouchFocus) cancelTouchFocus();
            }
        }
    }


    public void act(float delta) {
        super.act(delta);

        panning = this.eventListener != null && (((DoubleClickListener) (this.eventListener)).getGestureDetector().isPanning());
        animating = false;
        if (flingTimer > 0) {

            float alpha = flingTimer / flingTime;
            amountX -= velocityX * alpha * delta;
            amountY -= velocityY * alpha * delta;
            clamp();

            // Stop zoomedFling if hit overscroll distance.
            if (amountX == -overscrollDistance) velocityX = 0;
            if (amountX >= maxX + overscrollDistance) velocityX = 0;
            if (amountY == -overscrollDistance) velocityY = 0;
            if (amountY >= maxY + overscrollDistance) velocityY = 0;

            flingTimer -= delta;
            if (flingTimer <= 0) {
                velocityX = 0;
                velocityY = 0;
            }

            animating = true;
        }

        if (!panning) {
            if (this.zoom > 1.0f) {
                if (drwWidth < this.getWidth()) {
                    amountX = -(getWidth() - drwWidth) / 2.0f;
                    animating = true;
                } else if (amountX < 0) {   //todo <== center x Pos

                    amountX += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountX / overscrollDistance)
                            * delta;
                    if (amountX > 0) scrollX(0);
                    animating = true;
                } else if (amountX > maxX) {

                    amountX -= (overscrollSpeedMin
                            + (overscrollSpeedMax - overscrollSpeedMin) * -(maxX - amountX) / overscrollDistance) * delta;
                    if (amountX < maxX) scrollX(maxX);
                    animating = true;
                }

                if (drwHeight < this.getHeight()) {// center image
                    amountY = -(getHeight() - drwHeight) / 2.0f;
                    animating = true;
                } else if (amountY < 0) { //todo  <== center Y Pos
                    amountY += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountY / overscrollDistance)
                            * delta;
                    if (amountY > 0) scrollY(0);
                    animating = true;
                } else if (amountY > maxY) {

                    amountY -= (overscrollSpeedMin
                            + (overscrollSpeedMax - overscrollSpeedMin) * -(maxY - amountY) / overscrollDistance) * delta;
                    if (amountY < maxY) scrollY(maxY);
                    animating = true;
                }
            }

            if (animating) {
                Stage stage = getStage();
                if (stage != null && stage.getActionsRequestRendering()) Gdx.graphics.requestRendering();
            }
        } else {
            //center
            if (this.zoom > 1.0f) {
                if (drwWidth < this.getWidth()) {
                    amountX = -(getWidth() - drwWidth) / 2.0f;
                }
                if (drwHeight < this.getHeight()) {
                    amountY = -(getHeight() - drwHeight) / 2.0f;
                }
            }
        }
    }

    public void clamp() {
        //maybe center
        float drwWidth = this.iloader.getSpriteWidth() * proportion * zoom;
        float drwHeight = this.iloader.getSpriteHeight() * proportion * zoom;
        if (drwWidth < this.getWidth()) {
            // center x
            amountX = (getWidth() - drwWidth) / 2;
        }
        if (drwHeight < this.getHeight()) {
            // center y
            amountY = (getHeight() - drwHeight) / 2;
        }

        //set
        scrollX(MathUtils.clamp(amountX, -overscrollDistance, maxX + overscrollDistance));
        scrollY(MathUtils.clamp(amountY, -overscrollDistance, maxY + overscrollDistance));
    }

    /**
     * Called whenever the x zoomedScroll amount is changed.
     */
    protected void scrollX(float pixelsX) {
        this.amountX = pixelsX;
        log.debug("setAmountX:{}", amountX);
    }

    /**
     * Called whenever the y zoomedScroll amount is changed.
     */
    protected void scrollY(float pixelsY) {
        this.amountY = pixelsY;
        log.debug("setAmountY:{}", amountY);
    }

    public void setInputListener(EventListener inputListener) {
        this.eventListener = inputListener;
    }

    public void resetZoom() {
        this.zoom = 1.0f;
    }

    public float getImgWidth() {
        return iloader.getSpriteWidth();
    }

    public float getImgHeight() {
        return iloader.getSpriteHeight();
    }
}
