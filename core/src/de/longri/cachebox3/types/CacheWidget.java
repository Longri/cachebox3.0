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
package de.longri.cachebox3.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CacheTypeStyle;

/**
 * Created by Longri on 22.06.2017.
 */
public class CacheWidget extends Widget {
    private final static float prefSize = CB.getScaledFloat(45);
    private final Drawable typeIcon, leftInfoIcon, rightInfoIcon;
    private boolean needsLayout = true, hasInfoIcon;
    private float typeIconX, leftIconX, rightIconX;
    private float typeIconY, leftIconY, rightIconY;
    private float typeIconWidth, infoIconWidth;
    private float typeIconHeight, infoIconHeight;
    private float scaleX;
    private float scaleY;

    public CacheWidget(CacheTypes cacheType, CacheTypeStyle style, Drawable leftInfoIcon, Drawable rightInfoIcon) {
        this.typeIcon = cacheType.getDrawable(style);
        this.leftInfoIcon = leftInfoIcon;
        this.rightInfoIcon = rightInfoIcon;
        hasInfoIcon = leftInfoIcon != null || rightInfoIcon != null;

    }

    @Override
    public void layout() {
        if (!needsLayout) {
            super.layout();
            return;
        }

        //TODO use aspect ratio
        typeIconWidth = getWidth() / (hasInfoIcon ? 1.2f : 1f);
        typeIconHeight = getHeight() / (hasInfoIcon ? 1.2f : 1f);

        infoIconWidth = hasInfoIcon ? typeIconWidth / 1.5f : 0;
        infoIconHeight = hasInfoIcon ? typeIconHeight / 1.5f : 0;

        scaleX = getScaleX();
        scaleY = getScaleY();

        super.layout();
        needsLayout = true;
    }

    /**
     * Called when the actor's size has been changed.
     */
    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        needsLayout = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        if (hasInfoIcon) {
            typeIconX = getX() + ((getWidth() - typeIconWidth) / 2);
            typeIconY = getY() + (getHeight() - typeIconHeight);
            leftIconX = getX();
            rightIconY = leftIconY = getY();
            rightIconX = getX() + getWidth() - infoIconWidth;
        } else {
            typeIconX = getX();
            typeIconY = getY();
        }

        if (typeIcon != null) {
            if (typeIcon instanceof TransformDrawable) {
                float rotation = getRotation();
                if (scaleX != 1 || scaleY != 1 || rotation != 0) {
                    ((TransformDrawable) typeIcon).draw(batch, typeIconX, typeIconY, getOriginX(), getOriginY(),
                            typeIconWidth, typeIconHeight, scaleX, scaleY, rotation);
                } else {
                    typeIcon.draw(batch, typeIconX, typeIconY, typeIconWidth * scaleX, typeIconHeight * scaleY);
                }
            } else {
                typeIcon.draw(batch, typeIconX, typeIconY, typeIconWidth * scaleX, typeIconHeight * scaleY);
            }
        }

        if (hasInfoIcon) {
            if (leftInfoIcon != null) {
                if (leftInfoIcon instanceof TransformDrawable) {
                    float rotation = getRotation();
                    if (scaleX != 1 || scaleY != 1 || rotation != 0) {
                        ((TransformDrawable) leftInfoIcon).draw(batch, leftIconX, leftIconY, getOriginX(), getOriginY(),
                                infoIconWidth, infoIconHeight, scaleX, scaleY, rotation);
                    } else {
                        leftInfoIcon.draw(batch, leftIconX, leftIconY, infoIconWidth * scaleX, infoIconHeight * scaleY);
                    }
                } else {
                    leftInfoIcon.draw(batch, leftIconX, leftIconY, infoIconWidth * scaleX, infoIconHeight * scaleY);
                }
            }

            if (rightInfoIcon != null) {
                if (rightInfoIcon instanceof TransformDrawable) {
                    float rotation = getRotation();
                    if (scaleX != 1 || scaleY != 1 || rotation != 0) {
                        ((TransformDrawable) rightInfoIcon).draw(batch, rightIconX, rightIconY, getOriginX(), getOriginY(),
                                infoIconWidth, infoIconHeight, scaleX, scaleY, rotation);
                    } else {
                        rightInfoIcon.draw(batch, rightIconX, rightIconY, infoIconWidth * scaleX, infoIconHeight * scaleY);
                    }
                } else {
                    rightInfoIcon.draw(batch, rightIconX, rightIconY, infoIconWidth * scaleX, infoIconHeight * scaleY);
                }
            }
        }

    }


    @Override
    public float getPrefWidth() {
        return prefSize * (hasInfoIcon ? 1.2f : 1f);
    }

    @Override
    public float getPrefHeight() {
        return prefSize * (hasInfoIcon ? 1.2f : 1f);
    }
}
