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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 23.07.16.
 */
public class ColorWidget extends Widget {

    final Color color;
    final Sprite texture;

    public ColorWidget(Color color) {
        this.color = color;
        texture = CB.getSkin().getSprite("color");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        Color batchColor = batch.getColor();
        batch.setColor(color);
        batch.draw(texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        batch.setColor(batchColor);
    }

}
