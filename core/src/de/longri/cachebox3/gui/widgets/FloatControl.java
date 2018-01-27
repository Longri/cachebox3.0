/*
 * Copyright (C) 2018 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.FloatControlStyle;

/**
 * Created by Longri on 27.01.18.
 */
public class FloatControl extends Table {


    private float sliderPos;

    public interface ValueChangeListener {
        void valueChanged(int value);
    }

    private final ProgressBar progressBar;
    private final Button slideButton;
    private final ValueChangeListener changeListener;


    public FloatControl(float min, float max, float step, ValueChangeListener changeListener) {
        FloatControlStyle style = VisUI.getSkin().get(FloatControlStyle.class);
        this.progressBar = new ProgressBar(min, max, step, false, style.progressBarStyle);
        this.slideButton = new Button(style.buttonStyle);
        this.changeListener = changeListener;
        this.defaults().pad(CB.scaledSizes.MARGIN);
        this.add(progressBar).fillX().expandX();
        this.addActor(slideButton);
        float s = progressBar.getPrefHeight() + CB.scaledSizes.MARGINx2;
        slideButton.setSize(s, s);
        setSliderPosition();
//        this.setDebug(true);
    }

    private void setSliderPosition() {
        sliderPos = (this.getWidth() - slideButton.getWidth()) * progressBar.getVisualPercent();
        float y = progressBar.getY() - CB.scaledSizes.MARGIN;
        slideButton.setPosition(sliderPos, y);
    }

    public void setValue(float value) {
        progressBar.setValue(value);
        invalidate();
        layout();
    }

    @Override
    public void layout() {
        super.layout();
        if (this.needsLayout()) {
            setSliderPosition();
        }
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        layout();
    }


}
