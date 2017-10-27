/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.widget.VisTextField;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.NumPad;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.exceptions.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.Input.Keys.*;
import static de.longri.cachebox3.gui.widgets.NumPad.OptionalButton.*;

/**
 * Created by Longri on 27.08.16.
 */
public class NumericInput_Activity<T extends Number> extends ActivityBase {
    final static Logger log = LoggerFactory.getLogger(NumericInput_Activity.class);

    private T value;
    private NumPad numPad;
    private VisTextField textField;

    public NumericInput_Activity(T value) {
        super("NumericInput");
        this.value = value;
        createWidgets();

    }

    public NumericInput_Activity(T value, ActivityBaseStyle style) {
        super("NumericInput", style);
        this.value = value;
        createWidgets();
    }

    private void createWidgets() {

        float width = Gdx.graphics.getWidth();

        if (value instanceof Integer) {
            numPad = new NumPad(numPadKeyListener, width, OK, CANCEL, DelBack);
        } else if (value instanceof Double) {
            numPad = new NumPad(numPadKeyListener, width, OK, CANCEL, DOT, DelBack);
        } else if (value instanceof Float) {
            numPad = new NumPad(numPadKeyListener, width, OK, CANCEL, DOT, DelBack);
        } else throw new NotImplementedException("Ilegal Number type");

        textField = new VisTextField(value.toString());
        textField.setOnscreenKeyboard(numPad);
    }

    NumPad.IKeyEventListener numPadKeyListener = new NumPad.IKeyEventListener() {
        @Override
        public void KeyPressed(String keyValue) {
            if (keyValue.equals("O")) {
                try {
                    T retValue = null;
                    if (value instanceof Integer) {
                        retValue = (T) new Integer(Integer.parseInt(textField.getText()));
                    } else if (value instanceof Double) {
                        retValue = (T) new Double(Double.parseDouble(textField.getText()));
                    } else if (value instanceof Float) {
                        retValue = (T) new Float(Float.parseFloat(textField.getText()));
                    }
                    returnValue(retValue);
                    hide();
                } catch (NumberFormatException e) {
                    CB.viewmanager.toast(Translation.get("wrongValue"));
                }

            } else if (keyValue.equals("C")) {
                returnValue(value);
                hide();
            } else if (keyValue.equals("<") || keyValue.equals(">")) {
                int keycode = ANY_KEY;
                if (keyValue.equals("<")) keycode = LEFT;
                else if (keyValue.equals(">")) keycode = RIGHT;

                //set focus
                Stage stage = getStage();
                FocusManager.switchFocus(stage, textField);
                stage.setKeyboardFocus(textField);

                //send key
                InputListener inputListener = textField.getDefaultInputListener();
                inputListener.keyDown(null, keycode);
                inputListener.keyUp(null, keycode);
            } else {
                char chr = 0;
                if (keyValue.equals("D")) chr = DELETE;
                else if (keyValue.equals("B")) chr = BACKSPACE;
                else chr = keyValue.charAt(0);

                //set focus
                Stage stage = getStage();
                FocusManager.switchFocus(stage, textField);
                stage.setKeyboardFocus(textField);

                //send key
                InputListener inputListener = textField.getDefaultInputListener();
                inputListener.keyTyped(null, chr);
            }


        }
    };

    static private final char DELETE = 127;
    static private final char BACKSPACE = 8;

    @Override
    public void show() {
        super.show();
        // enable continues rendering for cursor blink
        Gdx.graphics.setContinuousRendering(true);
    }

    @Override
    public void hide() {
        super.hide();
        // disable continues rendering
        Gdx.graphics.setContinuousRendering(false);
    }

    @Override
    public void layout() {
        super.layout();
        if (!needsLayout) return;
        clear();

        this.addActor(numPad);
        numPad.pack();
        numPad.setPosition(0, CB.scaledSizes.MARGIN);

        this.addActor(textField);
        textField.setAlignment(Align.right);
        textField.setWidth(CB.scaledSizes.WINDOW_WIDTH);
        textField.setPosition(CB.scaledSizes.MARGIN, numPad.getY() + numPad.getHeight() + CB.scaledSizes.MARGIN);
        textField.setCursorAtTextEnd();


        needsLayout = false;
    }

    public void returnValue(T value) {
    }

    @Override
    public void dispose() {
        numPad.dispose();
        numPad = null;
    }
}