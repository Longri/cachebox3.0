/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_WidgetGroup;

import java.util.ArrayList;

/**
 * Created by Longri on 24.07.16.
 */
public class ButtonBar extends Catch_WidgetGroup {

    private boolean needsLayout = true;

    public static class ButtonBarStyle {
        public Drawable background;

        public ButtonBarStyle() {
        }
    }


    private final Drawable background;
    private final ArrayList<Button> buttonList = new ArrayList<Button>(5);

    public ButtonBar(ButtonBarStyle style) {
        this.background = style.background;
    }

    public void draw(Batch batch, float parentAlpha) {
        Color color = batch.getColor();
        batch.setColor(1, 1, 1, 1);


        float drawableWidth = (background instanceof TextureRegionDrawable) ?
                ((TextureRegionDrawable) background).getRegion().getRegionWidth() : getWidth();
        float drawX = (getWidth() - drawableWidth) / 2;
        background.draw(batch, drawX, 0, drawableWidth, getHeight());
        batch.setColor(color);
        super.draw(batch, parentAlpha);
    }

    public void addButton(Button button) {
        buttonList.add(button);
    }

    public ArrayList<Button> getButtons() {
        return buttonList;
    }

    public void layout() {
        if (!needsLayout) return;
        float completeWidth = 0;
        for (Button button : buttonList) {
            completeWidth += button.getWidth();
        }

        float remaind = this.getWidth() - completeWidth;
        float margin = remaind / (buttonList.size() + 1);

        this.clear();
        float xPos = margin;
        for (Button button : buttonList) {
            button.setPosition(xPos, CB.scaledSizes.MARGIN_HALF / 2);
            this.addActor(button);
            xPos += button.getWidth() + margin;
        }
        needsLayout = false;
    }

    public float getPrefHeight() {
        return buttonList.get(0).getHeight() + CB.scaledSizes.MARGIN_HALF;
    }

}
