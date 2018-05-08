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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.FloatControlStyle;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 27.01.18.
 */
public class FloatControl extends Catch_Table {

    private final Logger log = LoggerFactory.getLogger(FloatControl.class);


    public interface ValueChangeListener {
        void valueChanged(float value, boolean dragged);
    }

    private final CB_ProgressBar CBProgressBar;
    private final Button slideButton;
    private final ValueChangeListener changeListener;
    private final boolean fireChangedWithDrag;
    private boolean onDrag;


    public FloatControl(float min, float max, float step, boolean fireWithDrag, ValueChangeListener listener) {
        FloatControlStyle style = VisUI.getSkin().get(FloatControlStyle.class);
        this.CBProgressBar = new CB_ProgressBar(min, max, step, false, style.progressBarStyle);
        this.slideButton = new Button(style.buttonStyle);
        this.fireChangedWithDrag = fireWithDrag;
        this.changeListener = listener;
        this.defaults().pad(CB.scaledSizes.MARGIN);
        this.add(CBProgressBar).fillX().expandX();
        this.addActor(slideButton);
        float s = CBProgressBar.getPrefHeight() + CB.scaledSizes.MARGINx2;
        slideButton.setSize(s, s);
        final DragListener dragListener = new DragListener() {

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                event.stop();
                return true;
            }

            public void dragStart(InputEvent event, float x, float y, int pointer) {
                event.stop();
                onDrag = true;
            }

            public void drag(InputEvent event, float x, float y, int pointer) {
                slideButton.moveBy(x - slideButton.getWidth() / 2, 0);
                x = slideButton.getX();

                boolean revert = false;
                if (x < 0) {
                    x = 0;
                    revert = true;
                }

                if (x > FloatControl.this.getWidth() - slideButton.getWidth()) {
                    x = FloatControl.this.getWidth() - slideButton.getWidth();
                    revert = true;
                }

                if (revert) {
                    slideButton.setPosition(x, 0);
                }

                //set progress value by percent
                float percent = x / (getWidth() - slideButton.getWidth());
                float value = (CBProgressBar.getMaxValue() - CBProgressBar.getMinValue()) * percent;
                CBProgressBar.setValue(value);
                if (fireChangedWithDrag && changeListener != null) {
                    changeListener.valueChanged(CBProgressBar.getValue(), true);
                }
                event.stop();
            }

            public void dragStop(InputEvent event, float x, float y, int pointer) {
                log.debug("DRAG stop");
                event.stop();
                onDrag = false;

                //fire changed event
                if (changeListener != null) {
                    changeListener.valueChanged(CBProgressBar.getValue(), false);
                }
            }

        };
        slideButton.addCaptureListener(dragListener);
        setSliderPosition();
//        this.setDebug(true);
    }


    private void setSliderPosition() {
        if (onDrag) return;
        float sliderPos = (this.getWidth() - slideButton.getWidth()) * CBProgressBar.getVisualPercent();
        float y = CBProgressBar.getY() - CB.scaledSizes.MARGIN;
        slideButton.setPosition(sliderPos, y);
    }

    public void setValue(float value) {
        CBProgressBar.setValue(value);
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
