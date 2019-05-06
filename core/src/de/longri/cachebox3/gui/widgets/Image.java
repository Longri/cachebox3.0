/*
 * Copyright (C) 2011-2017 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.building.utilities.Alignment;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 *
 * @author Longri
 */
public class Image extends CB_View_Base {
    final static Logger log = LoggerFactory.getLogger(Image.class);
    private ImageLoader imageLoader;
    private float prefWidth, prefHeight;

    private Color mColor = new Color(1, 1, 1, 1);
    private Alignment hAlignment = Alignment.CENTER;

    public Image(float x, float y, float width, float height, String name, boolean reziseHeight) {
        super(x, y, width, height, name);
        this.imageLoader = new ImageLoader();
        this.imageLoader.reziseHeight = reziseHeight;
        if (this.imageLoader.reziseHeight && this.imageLoader.getResizeListener() == null) {
            this.imageLoader.setResizeListener(new ImageLoader.resize() {

                @Override
                public void sizechanged(float newWidth, float newHeight) {
                    Image.this.setSize(newWidth, newHeight);
                }
            }, this.getWidth());
        }

    }

    public Image(CB_RectF rec, String name, boolean reziseHeight) {
        super(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight(), name);
        this.imageLoader = new ImageLoader();
        this.imageLoader.reziseHeight = reziseHeight;
        if (this.imageLoader.reziseHeight && this.imageLoader.getResizeListener() == null) {
            this.imageLoader.setResizeListener(new ImageLoader.resize() {

                @Override
                public void sizechanged(float newWidth, float newHeight) {
                    Image.this.setSize(newWidth, newHeight);
                }
            }, this.getWidth());
        }
    }

    public Image(ImageLoader img, CB_RectF rec, String name, boolean reziseHeight) {
        super(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight(), name);
        this.imageLoader = img;
        this.imageLoader.reziseHeight = reziseHeight;
        if (this.imageLoader.reziseHeight && this.imageLoader.getResizeListener() == null) {
            this.imageLoader.setResizeListener(new ImageLoader.resize() {

                @Override
                public void sizechanged(float newWidth, float newHeight) {
                    Image.this.prefWidth = newWidth;
                    Image.this.prefHeight = newHeight;
                    Image.this.invalidateHierarchy();
                    Image.this.layout();
                }
            }, rec.getWidth());
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        if (imageLoader != null) {
            if (imageLoader.getAnimDelay() > 0) {
                //GL.that.addRenderView(this, imageLoader.getAnimDelay());
                isAsRenderViewRegisted.set(true);
            }
        }
    }

    AtomicBoolean isAsRenderViewRegisted = new AtomicBoolean(false);

    @Override
    public void onHide() {
        super.onHide();
        if (imageLoader != null) {
            if (imageLoader.getAnimDelay() > 0) {
                isAsRenderViewRegisted.set(false);
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (imageLoader == null)
            return;

        Color altColor = batch.getColor().cpy();
        batch.setColor(mColor);
        try {
            if (!imageLoader.isDrawableNULL()) {
                imageLoader.inLoad = false;
                float drawwidth = getWidth();
                float drawHeight = getHeight();
                float drawX = 0;
                float drawY = 0;

                if (imageLoader.getSpriteWidth() > 0 && imageLoader.getSpriteHeight() > 0) {
                    float proportionWidth = getWidth() / imageLoader.getSpriteWidth();
                    float proportionHeight = getHeight() / imageLoader.getSpriteHeight();

                    float proportion = Math.min(proportionWidth, proportionHeight);

                    drawwidth = imageLoader.getSpriteWidth() * proportion;
                    drawHeight = imageLoader.getSpriteHeight() * proportion;

                    switch (hAlignment) {
                        case CENTER:
                            drawX = (getWidth() - drawwidth) / 2;
                            break;
                        case LEFT:
                            drawX = 0;
                            break;
                        case RIGHT:
                            drawX = getWidth() - drawwidth;
                            break;
                        default:
                            drawX = (getWidth() - drawwidth) / 2;
                            break;

                    }

                    drawY = (getHeight() - drawHeight) / 2;
                }

                imageLoader.getDrawable(Gdx.graphics.getDeltaTime()).draw(batch, drawX, drawY, drawwidth, drawHeight);

                if (!isAsRenderViewRegisted.get() && imageLoader.getAnimDelay() > 0) {
                    //GL.that.addRenderView(this, imageLoader.getAnimDelay());
                    isAsRenderViewRegisted.set(true);
                }
            } else if (imageLoader.inLoad & !imageLoader.ImageLoadError) {
                //TODO add work animation
            } else if (imageLoader.ImageLoadError) {

                //set error image
                //Fixme use style
                this.setSprite(CB.getSkin().getSprite("error"), false);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        batch.setColor(altColor);

    }

    public void setImage(String Path) {
        if (imageLoader != null)
            imageLoader.setImage(Path);
    }

    public void setDrawable(Drawable drawable) {
        if (imageLoader != null)
            imageLoader.setDrawable(drawable);
    }

    @Override
    public void dispose() {
        isAsRenderViewRegisted.set(false);
        if (imageLoader != null)
            imageLoader.dispose();
        imageLoader = null;
    }

    public void setColor(Color color) {
        if (color == null)
            mColor = new Color(1, 1, 1, 1);
        else
            mColor = color;
    }

    /**
     * Sets a Image URl and Downlowd this Image if this don't exist on Cache
     *
     * @param iconUrl
     */
    public void setImageURL(final String iconUrl) {
        if (imageLoader != null)
            imageLoader.setImageURL(iconUrl);
    }

//    public void clearImage() {
//        if (imageLoader != null)
//            imageLoader.clearImage();
//        mColor = new Color(1, 1, 1, 1);
//        mScale = 1;
//        setOriginCenter();
//    }

    public void setHAlignment(Alignment alignment) {
        this.hAlignment = alignment;
    }

    public void setSprite(Sprite sprite, boolean reziseHeight) {
        if (imageLoader != null)
            imageLoader.setSprite(sprite, reziseHeight);
    }

    public Drawable getDrawable() {
        if (imageLoader == null)
            return null;
        return imageLoader.getDrawable(Gdx.graphics.getDeltaTime());
    }

    @Override
    public float getPrefWidth() {
        return this.prefWidth;
    }

    @Override
    public float getPrefHeight() {
        return this.prefHeight;
    }

}
