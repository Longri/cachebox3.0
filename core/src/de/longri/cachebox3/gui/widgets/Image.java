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

    public Image(ImageLoader img, String name, boolean reziseHeight) {
        super(name);
        this.imageLoader = img;
        if (imageLoader == null) return;// in case of JUnitTest
        this.imageLoader.reziseHeight = reziseHeight;
        if (this.imageLoader.reziseHeight && this.imageLoader.getResizeListener() == null) {
            this.imageLoader.setResizeListener((newWidth, newHeight) -> {
                Image.this.prefWidth = newWidth;
                Image.this.prefHeight = newHeight;
                Image.this.invalidateHierarchy();
                Image.this.layout();
            }, 0);
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        if (imageLoader != null) {
            if (imageLoader.getAnimDelay() > 0) {
                isAsRenderViewRegisted.set(true);
            }
        }
    }

    private AtomicBoolean isAsRenderViewRegisted = new AtomicBoolean(false);

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


                drawX += this.getX();
                drawY += this.getY();

                imageLoader.getDrawable(Gdx.graphics.getDeltaTime()).draw(batch, drawX, drawY, drawwidth, drawHeight);

                if (!isAsRenderViewRegisted.get() && imageLoader.getAnimDelay() > 0) {
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
