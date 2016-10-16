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

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;

/**
 * Created by Longri on 15.10.16.
 */
public class ZoomButton extends Group {

    private final ZoomButtonStyle style;
    private final Button plus;
    private final Button minus;

    public ZoomButton() {
        style = VisUI.getSkin().get("default", ZoomButtonStyle.class);

        Button.ButtonStyle btnStylePlus = new Button.ButtonStyle();
        btnStylePlus.up = style.plus;
        btnStylePlus.down = style.plusDown;
        plus = new Button(btnStylePlus);

        Button.ButtonStyle btnStyleMinus = new Button.ButtonStyle();
        btnStyleMinus.up = style.minus;
        btnStyleMinus.down = style.minusDown;
        minus = new Button(btnStyleMinus);

        this.addActor(plus);
        this.addActor(minus);

    }

    public void pack() {
        float widthMinus = style.minus.getMinWidth();
        float widthPlus = style.plus.getMinWidth();
        float height = style.minus.getMinHeight();

        this.setSize(widthMinus + widthPlus, height);
        this.minus.setBounds(0, 0, widthMinus, height);
        this.plus.setBounds(widthMinus, 0, widthPlus, height);
    }


    public static class ZoomButtonStyle {
        Drawable plus, minus, plusDown, minusDown;
        float alphaOff = 0.2f, alphaOn = 1.0f;
    }
}
