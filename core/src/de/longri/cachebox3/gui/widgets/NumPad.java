/*
 * Copyright (C) 2016 -2018 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 26.08.2016.
 */
public class NumPad extends Catch_Table implements TextField.OnscreenKeyboard, Disposable {
    final static Logger log = LoggerFactory.getLogger(NumPad.class);

    @Override
    public void show(boolean visible) {
        // do nothing
    }

    public enum OptionalButton {
        OK, CANCEL, DOT, DelBack, none
    }

    public interface IKeyEventListener {
        /**
         * Value is 0-9 or "." <br>
         * or <br>
         * "D" for Delete Button<br>
         * "B" for Back Button<br>
         * "O" for Ok Button<br>
         * "C" for Cancel Button<br>
         * "<" for Left Button<br>
         * ">" for Right Button<br>
         *
         * @param value
         */
        public void KeyPressed(String value);
    }

    private final IKeyEventListener keyEventListener;


    private final boolean hasOk, hasCancel, hasDot, hasDelBack;
    private final VisTextButton btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btnDel, btnBack, btnDot, btnLeft, btnRight;
    private final CB_Button btnOk, btnCancel;
    private float targetWidth;


    public NumPad(IKeyEventListener keyEventListener, float targetWidth, OptionalButton... options) {
        this(keyEventListener, options);
        this.targetWidth = targetWidth;
    }

    public NumPad(IKeyEventListener keyEventListener, OptionalButton... options) {
        this.targetWidth = Gdx.graphics.getWidth();
        this.keyEventListener = keyEventListener;

        boolean ok = false, cancel = false, dot = false, delBack = false;


        for (OptionalButton option : options) {
            switch (option) {

                case OK:
                    ok = true;
                    break;
                case CANCEL:
                    cancel = true;
                    break;
                case DOT:
                    dot = true;
                    break;
                case DelBack:
                    delBack = true;
                    break;
            }
        }

        hasOk = ok;
        hasCancel = cancel;
        hasDot = dot;
        hasDelBack = delBack;

        btn0 = new VisTextButton("0");
        btn1 = new VisTextButton("1");
        btn2 = new VisTextButton("2");
        btn3 = new VisTextButton("3");
        btn4 = new VisTextButton("4");
        btn5 = new VisTextButton("5");
        btn6 = new VisTextButton("6");
        btn7 = new VisTextButton("7");
        btn8 = new VisTextButton("8");
        btn9 = new VisTextButton("9");
        btnBack = new VisTextButton("Back");
        btnDel = new VisTextButton("Del");
        btnOk = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        btnDot = new VisTextButton(".");
        btnLeft = new VisTextButton("<");
        btnRight = new VisTextButton(">");


        btn0.addListener(clickListener);
        btn1.addListener(clickListener);
        btn2.addListener(clickListener);
        btn3.addListener(clickListener);
        btn4.addListener(clickListener);
        btn5.addListener(clickListener);
        btn6.addListener(clickListener);
        btn7.addListener(clickListener);
        btn8.addListener(clickListener);
        btn9.addListener(clickListener);
        btnBack.addListener(clickListener);
        btnDel.addListener(clickListener);
        btnOk.addListener(clickListener);
        btnCancel.addListener(clickListener);
        btnDot.addListener(clickListener);
        btnLeft.addListener(clickListener);
        btnRight.addListener(clickListener);
        CB.stageManager.registerForBackKey(clickListener);

    }

    private ClickListener clickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            String keyValue;
            if (event == StageManager.BACK_KEY_INPUT_EVENT) {
                keyValue = "C";
            } else {
                VisTextButton btn = (VisTextButton) event.getListenerActor();
                keyValue = String.valueOf(btn.getText());
                keyValue = keyValue.replace("Del", "D");
                keyValue = keyValue.replace("Back", "B");
                keyValue = keyValue.replace(Translation.get("ok"), "O");
                keyValue = keyValue.replace(Translation.get("cancel"), "C");

            }

            NumPad.this.keyEventListener.KeyPressed(keyValue);
            log.debug("Button [" + keyValue + "] clicked");
        }
    };


    private boolean layoutInvalid = true;

    @Override
    public void layout() {

        if (!layoutInvalid) {
            super.layout();
            return;
        }


        this.clear();

        //calculate pad
        float buttonWidth = CB.scaledSizes.BUTTON_WIDTH;
        float pad = (this.targetWidth - (5 * buttonWidth)) / 10; //max 5 buttons on width

        //row 1
        this.add(btn1).pad(pad).width(buttonWidth);
        this.add(btn2).pad(pad).width(buttonWidth);
        this.add(btn3).pad(pad).width(buttonWidth);
        if (hasOk) this.add(btnOk).colspan(2).pad(pad).expandX().fillX();
        this.row();

        //row 2
        this.add(btn4).pad(pad).width(buttonWidth);
        this.add(btn5).pad(pad).width(buttonWidth);
        this.add(btn6).pad(pad).width(buttonWidth);
        if (hasCancel) this.add(btnCancel).colspan(2).pad(pad).expandX().fillX();
        this.row();

        //row 3
        this.add(btn7).pad(pad).width(buttonWidth);
        this.add(btn8).pad(pad).width(buttonWidth);
        this.add(btn9).pad(pad).width(buttonWidth);
        this.row();

        //row 4
        this.add(new Actor()).pad(pad);
        this.add(btn0).pad(pad).width(buttonWidth);
        this.add(hasDot ? btnDot : new Actor()).pad(pad).width(buttonWidth);
        if (hasDelBack) {
            this.add(btnDel).pad(pad).width(buttonWidth);
            this.add(btnBack).pad(pad).width(buttonWidth);
        }
        this.row();

        //row 5
        Table cursorTable = new Table();
        cursorTable.add(btnLeft).pad(pad).expandX().fillX();
        cursorTable.add(btnRight).pad(pad).expandX().fillX();
        this.add(new Actor()).pad(pad);
        this.add(cursorTable).pad(pad).colspan(3).expandX().fillX();

        super.layout();
        layoutInvalid = false;
    }

    @Override
    public void dispose() {
        btn0.removeListener(clickListener);
        btn1.removeListener(clickListener);
        btn2.removeListener(clickListener);
        btn3.removeListener(clickListener);
        btn4.removeListener(clickListener);
        btn5.removeListener(clickListener);
        btn6.removeListener(clickListener);
        btn7.removeListener(clickListener);
        btn8.removeListener(clickListener);
        btn9.removeListener(clickListener);
        btnBack.removeListener(clickListener);
        btnDel.removeListener(clickListener);
        btnOk.removeListener(clickListener);
        btnCancel.removeListener(clickListener);
        btnDot.removeListener(clickListener);
        btnLeft.removeListener(clickListener);
        btnRight.removeListener(clickListener);
        CB.stageManager.unRegisterForBackKey(clickListener);
        this.clear();
    }


}
