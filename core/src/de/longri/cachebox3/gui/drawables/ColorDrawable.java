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
package de.longri.cachebox3.gui.drawables;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.ColorDrawableStyle;

public class ColorDrawable extends EmptyDrawable {


    /**
     * Da beim Zeichnen dieses Sprites, dieses nicht Manipuliert wird, brauchen wir hier nur eine einmalige Statische Instanz
     */
    private static Sprite pixelSprite;

    private Texture tex;
    private Pixmap pix;
    private Color mColor;

    public ColorDrawable(Color color) {
        setColor(color);
    }

    public ColorDrawable(ColorDrawableStyle style) {
        setColor(style.color);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {

        if (pixelSprite == null) {
            pixelSprite = CB.getSprite("color");
            if (pixelSprite == null) {
                setSpriteFromPixMap();
            }
        }

        if (pixelSprite != null) {
            Color altColor = batch.getColor();

            float r = altColor.r;
            float g = altColor.g;
            float b = altColor.b;
            float a = altColor.a;

            batch.setColor(mColor);
            batch.draw(pixelSprite, x, y, width, height);
            batch.setColor(r, g, b, a);
        }

    }

    private void setSpriteFromPixMap() {
        int w = 2;
        int h = 2;
        pix = new Pixmap(w, h, Pixmap.Format.RGB565);
        pix.setColor(Color.WHITE);

        pix.fillRectangle(0, 0, w, h);

        try {
            tex = new Texture(pix);
        } catch (Exception e) {
            tex = null;
        }

        pixelSprite = new Sprite(tex);

        pix.dispose();
    }

    public void setColor(Color color) {
        mColor = color;
    }
}
