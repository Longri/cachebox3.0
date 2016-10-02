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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;

import java.util.ArrayList;

/**
 * Created by Longri on 24.07.16.
 */
public class ButtonBar extends WidgetGroup {


    public static class ButtonBarStyle {
        Drawable background;

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
        background.draw(batch, 0, 0, this.getWidth(), this.getHeight());
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
        float completeWidth = 0;
        for (Button button : buttonList) {
            completeWidth += button.getWidth();
        }

        float remaind = this.getWidth() - completeWidth;
        float margin = remaind / (buttonList.size() + 1);

        this.clear();
        float xPos = margin;
        for (Button button : buttonList) {
            button.setPosition(xPos, CB.scaledSizes.MARGIN_HALF);
            this.addActor(button);
            xPos += button.getWidth() + margin;
        }
    }

    public float getPrefHeight() {
        return buttonList.get(0).getHeight() + CB.scaledSizes.MARGIN;
    }

}
