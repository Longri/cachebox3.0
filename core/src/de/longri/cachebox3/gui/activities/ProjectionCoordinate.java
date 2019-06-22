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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_CheckBox;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.gui.widgets.NumPad;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 14.11.2017.
 */
public class ProjectionCoordinate extends ActivityBase {
    private static final char BACKSPACE = 8;
    private static final char DELETE = 127;

    private final CoordinateButton cordButton;
    private final VisTextField textFieldBearing = new VisTextField("0");
    private final VisTextField textFieldDistance = new VisTextField("0");

    private final NumPad.IKeyEventListener keyEventListener = new NumPad.IKeyEventListener() {
        @Override
        public void KeyPressed(String value) {

            if ("C".equals(value)) {
                cancelListener.clicked(StageManager.BACK_KEY_INPUT_EVENT, -1, -1);
                return;
            }

            if (actFocusField != null) {

                getStage().setKeyboardFocus(actFocusField);
                actFocusField.focusGained();
                VisTextField.TextFieldClickListener listener = (VisTextField.TextFieldClickListener) actFocusField.getDefaultInputListener();

                if (value.equals("<") || value.equals(">")) {
                    if (value.equals("<")) {
                        listener.keyDown(null, Input.Keys.LEFT);
                    } else {
                        listener.keyDown(null, Input.Keys.RIGHT);
                    }
                    return;
                }
                if (value.equals("D")) {
                    listener.keyTyped(null, DELETE);
                    return;
                }
                if (value.equals("B")) {
                    listener.keyTyped(null, BACKSPACE);
                    return;
                }
                listener.keyTyped(null, value.charAt(0));
            }
        }
    };

    private final float targetWidth = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx4;
    private final NumPad numPad = new NumPad(keyEventListener, targetWidth, NumPad.OptionalButton.DelBack);
    private VisTextField actFocusField;
    private VisCheckBox invertCheckBox;

    protected ProjectionCoordinate(Coordinate coordinate) {
        super("project");
        this.cordButton = new CoordinateButton(coordinate);

        this.defaults().padLeft(CB.scaledSizes.MARGIN);
        this.defaults().padRight(CB.scaledSizes.MARGIN);

        this.row();
        this.add(cordButton);


        createInputFields();
        this.row();

        Table invertLine = new VisTable();
        invertLine.defaults().pad(CB.scaledSizes.MARGIN);
        invertCheckBox = new CB_CheckBox(Translation.get("Invert"));
        invertLine.add(invertCheckBox).left();
        invertLine.add((Actor) null).expandX().fillX();

        this.add(invertLine).expandX().fillX();

        this.row();
        this.add((Actor) null).expand().fill();
        createNumPad();
        createOkCancel();

        textFieldBearing.focusField();

        Gdx.graphics.setContinuousRendering(true);


        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                ProjectionCoordinate.this.getStage().setKeyboardFocus(textFieldBearing);
                textFieldBearing.selectAll();
            }
        });
    }

    //for override call back
    public void callBack(Coordinate coordinate) {
    }


    private final ClickListener cancelListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            callBack(null);
            finish();
        }
    };
    private final ClickListener okListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            finish();
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float distance = 0;
                    float direction = 0;
                    try {
                        distance = Float.parseFloat(textFieldDistance.getText());
                        direction = Float.parseFloat(textFieldBearing.getText());
                    } catch (NumberFormatException e) {
                        //TODO handle ( give feedback for wrong input)
                        callBack(null);
                    }
                    if (invertCheckBox.isChecked()) direction -= 180;
                    Coordinate project = Coordinate.Project(cordButton.getCoordinate(), direction, distance);
                    callBack(project);
                }
            });
        }
    };

    private void createInputFields() {
        addEditLine(Translation.get("Bearing"), textFieldBearing, "°");
        addEditLine(Translation.get("Distance"), textFieldDistance,
                Config.ImperialUnits.getValue() ? "yd" : "m");

    }

    private void addEditLine(CharSequence name, final VisTextField textField, CharSequence unity) {
        Table line = new VisTable();
        line.defaults().pad(CB.scaledSizes.MARGIN);
        line.add(name).left();


        //disable onScreenKeyboard
        textField.setOnscreenKeyboard(new TextField.OnscreenKeyboard() {
            @Override
            public void show(boolean visible) {
                // do nothing
                // we use own NumPad
            }
        });

        textField.addListener(new FocusListener() {
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (focused == true) {
                    if (actor == textField) {
                        actFocusField = textField;
                    }
                }
            }
        });
        line.add(textField).expandX().fillX();
        line.add(unity);
        this.row();
        this.add(line).expandX().fillX();
    }


    private void createNumPad() {
        this.row();
        this.add(numPad);
    }

    private void createOkCancel() {
        this.row();
        Table cancelOkTable = new Table();

        CB_Button btnOk = new CB_Button(Translation.get("ok"));
        CB_Button btnCancel = new CB_Button(Translation.get("cancel"));

        btnOk.addListener(okListener);
        btnCancel.addListener(cancelListener);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN_HALF * 3) / 2;

        cancelOkTable.add(btnOk).width(new Value.Fixed(btnWidth));
        cancelOkTable.add(btnCancel).width(new Value.Fixed(btnWidth));

        this.add(cancelOkTable).bottom().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
    }

}
