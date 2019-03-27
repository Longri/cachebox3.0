/*
 * Copyright (C) 2017-2018 team-cachebox.de
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
import de.longri.cachebox3.gui.skin.styles.CacheTypeStyle;

/**
 * Created by Longri on 22.06.2017.
 */
public class CacheWidget extends Widget {


    private final Drawable typeIcon, leftInfoIcon, rightInfoIcon, leftTopIcon, rightTopIcon;
    private boolean needsLayout = true, hasInfoIcon;
    private float typeIconX, leftIconX, rightIconX, leftTopIconX, rightTopIconX;
    private float typeIconY, leftIconY, rightIconY, leftTopIconY, rightTopIconY;
    private float typeIconWidth, infoIconWidth;
    private float typeIconHeight, infoIconHeight;
    private float logTypeIconSize;
    private float prefSize;


    public CacheWidget(CacheTypes cacheType, CacheTypeStyle style,
                       Drawable leftInfoIcon, Drawable rightInfoIcon,
                       Drawable leftTopIcon, Drawable rightTopIcon) {
        this.typeIcon = cacheType.getDrawable(style);
        this.leftInfoIcon = leftInfoIcon;
        this.rightInfoIcon = rightInfoIcon;
        this.leftTopIcon = leftTopIcon;
        this.rightTopIcon = rightTopIcon;

        if (typeIcon == null) return;
        this.prefSize = typeIcon.getMinHeight();
        if (leftInfoIcon != null) {
            hasInfoIcon = true;
            logTypeIconSize = leftInfoIcon.getMinWidth();
        } else if (rightInfoIcon != null) {
            hasInfoIcon = true;
            logTypeIconSize = rightInfoIcon.getMinWidth();
        } else if (leftTopIcon != null) {
            hasInfoIcon = true;
            logTypeIconSize = leftTopIcon.getMinWidth();
        } else if (rightTopIcon != null) {
            hasInfoIcon = true;
            logTypeIconSize = rightTopIcon.getMinWidth();
        } else {
            hasInfoIcon = false;
            logTypeIconSize = 0;
        }
    }

    @Override
    public void layout() {
        if (!needsLayout) {
            super.layout();
            return;
        }
        if (typeIcon == null) return;
        typeIconWidth = typeIcon.getMinWidth();
        typeIconHeight = typeIcon.getMinHeight();

        infoIconWidth = hasInfoIcon ? logTypeIconSize : 0;
        infoIconHeight = hasInfoIcon ? logTypeIconSize : 0;

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
            leftIconX = leftTopIconX = getX();
            rightIconY = leftIconY = getY();
            rightIconX = rightTopIconX = getX() + getWidth() - infoIconWidth;
            leftTopIconY = rightTopIconY = getY() + getHeight() - infoIconHeight;
        } else {
            typeIconX = getX();
            typeIconY = getY();
        }

        if (typeIcon != null) {
            if (typeIcon instanceof TransformDrawable) {
                float rotation = getRotation();
                if (rotation != 0) {
                    ((TransformDrawable) typeIcon).draw(batch, typeIconX, typeIconY, getOriginX(), getOriginY(),
                            typeIconWidth, typeIconHeight, 1, 1, rotation);
                } else {
                    typeIcon.draw(batch, typeIconX, typeIconY, typeIconWidth, typeIconHeight);
                }
            } else {
                typeIcon.draw(batch, typeIconX, typeIconY, typeIconWidth, typeIconHeight);
            }
        }

        if (hasInfoIcon) {
            if (leftInfoIcon != null) {
                if (leftInfoIcon instanceof TransformDrawable) {
                    float rotation = getRotation();
                    if (rotation != 0) {
                        ((TransformDrawable) leftInfoIcon).draw(batch, leftIconX, leftIconY, getOriginX(), getOriginY(),
                                infoIconWidth, infoIconHeight, 1, 1, rotation);
                    } else {
                        leftInfoIcon.draw(batch, leftIconX, leftIconY, infoIconWidth, infoIconHeight);
                    }
                } else {
                    leftInfoIcon.draw(batch, leftIconX, leftIconY, infoIconWidth, infoIconHeight);
                }
            }

            if (rightInfoIcon != null) {
                if (rightInfoIcon instanceof TransformDrawable) {
                    float rotation = getRotation();
                    if (rotation != 0) {
                        ((TransformDrawable) rightInfoIcon).draw(batch, rightIconX, rightIconY, getOriginX(), getOriginY(),
                                infoIconWidth, infoIconHeight, 1, 1, rotation);
                    } else {
                        rightInfoIcon.draw(batch, rightIconX, rightIconY, infoIconWidth, infoIconHeight);
                    }
                } else {
                    rightInfoIcon.draw(batch, rightIconX, rightIconY, infoIconWidth, infoIconHeight);
                }
            }

            if (rightTopIcon != null) {
                if (rightTopIcon instanceof TransformDrawable) {
                    float rotation = getRotation();
                    if (rotation != 0) {
                        ((TransformDrawable) rightTopIcon).draw(batch, rightTopIconX, rightTopIconY, getOriginX(), getOriginY(),
                                infoIconWidth, infoIconHeight, 1, 1, rotation);
                    } else {
                        rightTopIcon.draw(batch, rightTopIconX, rightTopIconY, infoIconWidth, infoIconHeight);
                    }
                } else {
                    rightTopIcon.draw(batch, rightTopIconX, rightTopIconY, infoIconWidth, infoIconHeight);
                }
            }

            if (leftTopIcon != null) {
                if (leftTopIcon instanceof TransformDrawable) {
                    float rotation = getRotation();
                    if (rotation != 0) {
                        ((TransformDrawable) leftTopIcon).draw(batch, leftTopIconX, leftTopIconY, getOriginX(), getOriginY(),
                                infoIconWidth, infoIconHeight, 1, 1, rotation);
                    } else {
                        leftTopIcon.draw(batch, leftTopIconX, leftTopIconY, infoIconWidth, infoIconHeight);
                    }
                } else {
                    leftTopIcon.draw(batch, leftTopIconX, leftTopIconY, infoIconWidth, infoIconHeight);
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
