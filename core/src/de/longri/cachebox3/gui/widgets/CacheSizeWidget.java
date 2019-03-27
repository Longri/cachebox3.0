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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.gui.skin.styles.CacheSizeStyle;
import de.longri.cachebox3.types.CacheSizes;

/**
 * Created by Longri on 08.09.2016.
 */
public class CacheSizeWidget extends AbstractIntValueChangedWidget {
    private Drawable sizeDrawable;
    private final CacheSizeStyle style;

    public CacheSizeWidget(CacheSizes size, CacheSizeStyle style) {

        this.style = style;
        if (style != null)
            switch (size) {
                case other:
                    sizeDrawable = style.other;
                    break;
                case micro:
                    sizeDrawable = style.micro;
                    break;
                case small:
                    sizeDrawable = style.small;
                    break;
                case regular:
                    sizeDrawable = style.regular;
                    break;
                case large:
                    sizeDrawable = style.large;
                    break;
                case notChosen:
                    sizeDrawable = style.notChosen;
                    break;
                case virtual:
                    sizeDrawable = style.virtualSize;
                    break;
                default:
                    sizeDrawable = style.other; // unknown
            }
    }

    public CacheSizeWidget(int value, CacheSizeStyle style) {
        this.style = style;
        setValue(value);
    }

    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        Color color = batch.getColor();
        batch.setColor(1, 1, 1, 1);
        sizeDrawable.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        batch.setColor(color);
    }

    @Override
    public float getPrefWidth() {
        return sizeDrawable == null ? 0 : sizeDrawable.getMinWidth();
    }

    @Override
    public float getPrefHeight() {
        return sizeDrawable == null ? 0 : sizeDrawable.getMinHeight();
    }

    @Override
    public void setValue(int value) {
        switch (value) {
            case 0:
                sizeDrawable = style.micro;
                break;
            case 1:
                sizeDrawable = style.small;
                break;
            case 2:
                sizeDrawable = style.regular;
                break;
            case 3:
                sizeDrawable = style.large;
                break;
            case 4:
                sizeDrawable = style.other;
                break;
            case 5:
                sizeDrawable = style.virtualSize;
                break;
            case 6:
                sizeDrawable = style.notChosen;
                break;
            default:
                sizeDrawable = style.other; // unknown
        }
    }
}
