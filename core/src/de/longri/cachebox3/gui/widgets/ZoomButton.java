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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Group;

/**
 * Created by Longri on 15.10.16.
 */
public class ZoomButton extends Catch_Group {

    private final float TIME_TO_FADE_OUT = 5;
    private float actTime;
    private long lastTime = -1;
    private boolean fadeOutRuns = false;

    private final ZoomButtonStyle style;
    private final Button plus;
    private final Button minus;

    private final ValueChangeListener valueChangeListener;

    public interface ValueChangeListener {
        public void valueChanged(int changeValue);
    }

    private class ZoomClickListener extends ClickListener {
        final int value;

        protected ZoomClickListener(int value) {
            this.value = value;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            if (fadeOutRuns) {
                ZoomButton.this.clearActions();
                actTime = 0;
                fadeOutRuns = false;
            }
            ZoomButton.this.addAction(Actions.alpha(style.alphaOn));
            valueChangeListener.valueChanged(value);
        }
    }


    public ZoomButton(ValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
        style = VisUI.getSkin().get("default", ZoomButtonStyle.class);

        Button.ButtonStyle btnStylePlus = new Button.ButtonStyle();
        btnStylePlus.up = style.plus;
        btnStylePlus.down = style.plusDown;
        btnStylePlus.disabled = style.plusDisabled;
        plus = new Button(btnStylePlus);

        Button.ButtonStyle btnStyleMinus = new Button.ButtonStyle();
        btnStyleMinus.up = style.minus;
        btnStyleMinus.down = style.minusDown;
        btnStyleMinus.disabled = style.minusDisabled;
        minus = new Button(btnStyleMinus);


        plus.addListener(new ZoomClickListener(1));
        minus.addListener(new ZoomClickListener(-1));

        this.addActor(plus);
        this.addActor(minus);

    }

    public void pack() {
        if (style == null || style.minus == null || style.plus == null) return;
        float widthMinus = style.minus.getMinWidth();
        float widthPlus = style.plus.getMinWidth();
        float height = style.minus.getMinHeight();

        this.setSize(widthMinus + widthPlus, height);
        this.minus.setBounds(0, 0, widthMinus, height);
        this.plus.setBounds(widthMinus, 0, widthPlus, height);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (lastTime == -1) lastTime = System.currentTimeMillis();


        delta = ((System.currentTimeMillis() - lastTime) / 1000f);
        lastTime = System.currentTimeMillis();
        actTime += delta;

        if (!fadeOutRuns && actTime > TIME_TO_FADE_OUT) {
            this.addAction(Actions.alpha(style.alphaOff, 2f));
            this.fadeOutRuns = true;
        }

        if (!fadeOutRuns) {
            //wait for fade out
            Gdx.graphics.requestRendering();
        }
    }


    public static class ZoomButtonStyle {
        public Drawable plus, minus, plusDown, minusDown, plusDisabled, minusDisabled;
        public float alphaOff = 0.5f, alphaOn = 1.0f;
    }

    public void setDisabledPlus(boolean disabeld) {
        this.plus.setDisabled(disabeld);
    }

    public void setDisabledMinus(boolean disabeld) {
        this.minus.setDisabled(disabeld);
    }
}
