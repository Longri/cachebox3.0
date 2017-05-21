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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.types.CacheSizes;

/**
 * Created by Longri on 08.09.2016.
 */
public class CacheSizeWidget extends Widget {
    private final Drawable sizeDrawable;

    public CacheSizeWidget(CacheSizes size) {
        Skin skin = VisUI.getSkin();

        switch (size) {
            case other:
                sizeDrawable = skin.getDrawable("other");
                break;
            case micro:
                sizeDrawable = skin.getDrawable("micro");
                break;
            case small:
                sizeDrawable = skin.getDrawable("small");
                break;
            case regular:
                sizeDrawable = skin.getDrawable("regular");
                break;
            case large:
                sizeDrawable = skin.getDrawable("large");
                break;
            default:
                sizeDrawable = skin.getDrawable("other"); // unknown
        }
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
        return sizeDrawable.getMinWidth();
    }

    @Override
    public float getPrefHeight() {
        return sizeDrawable.getMinHeight();
    }

}
