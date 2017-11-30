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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import de.longri.cachebox3.gui.drawables.geometry.GeometryDrawable;

/**
 * Created by Longri on 29.11.2017.
 */
public class GeometryDrawableWidget extends Widget {

    final GeometryDrawable drawable;

    public GeometryDrawableWidget(GeometryDrawable drawable) {
        this.drawable = drawable;
        this.setSize(drawable.getWidth(), drawable.getHeight());
        this.setColor(drawable.getColor());
    }

    public void draw(Batch batch, float parentAlpha) {
        drawable.setColor(this.getColor());
        drawable.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }


    @Override
    public float getPrefWidth() {
        return drawable.getMinWidth();
    }

    @Override
    public float getPrefHeight() {
        return drawable.getMinHeight();
    }

    @Override
    public void sizeChanged() {
        drawable.setSize(this.getWidth(), this.getHeight());
        this.invalidateHierarchy();
        super.sizeChanged();
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        drawable.setColor(color);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        drawable.setColor(r, g, b, a);
    }

}
