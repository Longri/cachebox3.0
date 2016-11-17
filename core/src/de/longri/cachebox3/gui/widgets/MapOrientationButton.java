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

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 14.10.2016.
 */
public class MapOrientationButton extends Button {

    public enum State {NORTH, COMPASS, USER}

    private boolean block;
    private State state;

    public MapOrientationButton() {
        super(VisUI.getSkin().get("toggle", ButtonStyle.class));
    }

    @Override
    public float getPrefWidth() {
        return CB.scaledSizes.BUTTON_WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return CB.scaledSizes.BUTTON_HEIGHT;
    }

    public void setOrientation(float bearing) {

    }

    public void setChecked(boolean checked) {
        if (this.block) return;
        super.setChecked(checked);
    }

    public void block(boolean block) {
        this.block = block;
    }
}
