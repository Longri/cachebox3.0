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
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.CB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Extends SpriteBatch for throw exception or log warn with scaled AtlasTextureRegion drawings in Debug mode.
 * <p>
 * Or, at normal mode do nothing else !!
 * <p>
 * Created by longri on 16.07.17.
 */
public class CB_SpriteBatch extends PolygonSpriteBatch {

    private final Logger log = LoggerFactory.getLogger(CB_SpriteBatch.class);
    private final float DRAW_SCALE_TOLERANCE = Math.max(1.5f, Math.min(3.0f, CB.getScaledFloat(1.2f)));
    private final Array<String> scaledDrawables = new Array<>();

    public enum Mode {
        THROW, WARN, NORMAL
    }

    private final boolean NORMAL, THROW;
    private final StringBuilder stringBuilder = new StringBuilder(100);

    public CB_SpriteBatch(Mode mode) {
        switch (mode) {

            case THROW:
                THROW = true;
                NORMAL = false;
                break;
            case WARN:
                THROW = false;
                NORMAL = false;
                break;
            case NORMAL:
                THROW = false;
                NORMAL = true;
                break;
            default:
                THROW = true;
                NORMAL = false;
        }
    }

    /**
     * Add a drawable that can scaled, without exception or warning! (Like Compass)
     *
     * @param name
     */
    public void registerScaledDrawable(String name) {
        scaledDrawables.add(name);
    }


    @Override
    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
                     float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        if (!NORMAL) {
            float drwWidth = width * scaleX, drwHeight = height * scaleY;
            checkDrawingSize("Texture", srcWidth, srcHeight, drwWidth, drwHeight);
        }
        super.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
                     int srcHeight, boolean flipX, boolean flipY) {
        if (!NORMAL) {
            checkDrawingSize("Texture", srcWidth, srcHeight, width, height);
        }
        super.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        if (region == null) return;
        if (!NORMAL) {
            if (region instanceof TextureAtlas.AtlasRegion) {
                String name = ((TextureAtlas.AtlasRegion) region).name;
                checkDrawingSize(name, region.regionWidth, region.regionHeight, width, height);
            }
        }
        super.draw(region, x, y, width, height);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation) {
        if (!NORMAL) {
            if (region instanceof TextureAtlas.AtlasRegion) {
                String name = ((TextureAtlas.AtlasRegion) region).name;
                checkDrawingSize(name, region.regionWidth * scaleX, region.regionHeight * scaleY, width, height);
            }
        }
        super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation, boolean clockwise) {
        if (!NORMAL) {
            if (region instanceof TextureAtlas.AtlasRegion) {
                String name = ((TextureAtlas.AtlasRegion) region).name;
                checkDrawingSize(name, region.regionWidth * scaleX, region.regionHeight * scaleY, width, height);
            }
        }
        super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }


    private void checkDrawingSize(String name, float srcWidth, float srcHeight, float drwWidth, float drwHeight) {

        if (this.scaledDrawables.contains(name, false)) return;

        float widthDiv = (float) ((int) (Math.abs(srcWidth - drwWidth)));
        float heightDiv = (float) ((int) (Math.abs(srcHeight - drwHeight)));

        if (widthDiv > DRAW_SCALE_TOLERANCE || heightDiv > DRAW_SCALE_TOLERANCE) {
            stringBuilder.length = 0;
            Arrays.fill(stringBuilder.chars, Character.MIN_VALUE);

            stringBuilder.append("Batch draw scaled Texture '");
            stringBuilder.append(name);
            stringBuilder.append("' | size: ");
            stringBuilder.append(srcWidth);
            stringBuilder.append("/");
            stringBuilder.append(srcHeight);
            stringBuilder.append(" draw size: ");
            stringBuilder.append(drwWidth);
            stringBuilder.append("/");
            stringBuilder.append(drwHeight);
            stringBuilder.append(" difference: ");
            stringBuilder.append(widthDiv);
            stringBuilder.append("/");
            stringBuilder.append(heightDiv);

            if (THROW) {
                throw new RuntimeException(stringBuilder.toString());
            } else {
                log.warn(stringBuilder.toString());
            }
        }
    }

}
