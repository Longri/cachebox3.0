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
package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;

/**
 * Created by Longri on 24.01.2017.
 */
public abstract class ValidationTask {

    public abstract String getName();

    public abstract void runValidation();


    final protected SkinEditorGame game;
    final protected SavableSvgSkin validationSkin;
    final private Stage stage;
    private Cell cell;
    protected String errorMsg, warnMsg;

    public ValidationTask(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        this.game = game;
        this.validationSkin = validationSkin;
        this.stage = stage;
    }

    public boolean hasError() {
        return errorMsg != null && !errorMsg.isEmpty();
    }

    public boolean hasWarn() {
        return warnMsg != null && !warnMsg.isEmpty();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getWarnMsg() {
        return warnMsg;
    }

    public void setResultCell(Cell cell) {
        this.cell = cell;
    }

    public void setReadyIcon() {
        //Ready , set icon on render Thread
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (hasWarn() || hasError()) {
                    if (hasError()) {
                        Image errorImage = new Image(game.skin.getRegion("error"));
                        ValidationTask.this.cell.setActor(errorImage);
                    } else {
                        Image warnImage = new Image(game.skin.getRegion("warn"));
                        ValidationTask.this.cell.setActor(warnImage);
                    }
                } else {
                    Image readyImage = new Image(game.skin.getRegion("valid"));
                    ValidationTask.this.cell.setActor(readyImage);
                }
                ValidationTask.this.cell.getActor().addListener(clickListener);
                ValidationTask.this.cell.getActor().setTouchable(Touchable.enabled);
                Gdx.graphics.requestRendering();
            }
        });
    }

    InputListener clickListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (hasWarn() || hasError()) {

                getWarnMsg();

                Dialog dlg = new Dialog(getName(), game.skin);
                dlg.pad(20);

                TextArea taError = new TextArea(getErrorMsg(), game.skin, "error") {
                    public float getPrefHeight() {
                        calculateOffsets();
                        float prefHeight = (getLines() + 1) * getStyle().font.getLineHeight(); // Work around
                        TextFieldStyle style = getStyle();
                        if (style.background != null) {
                            prefHeight = Math.max(prefHeight + style.background.getBottomHeight() + style.background.getTopHeight(), style.background.getMinHeight());
                        }
                        return prefHeight;
                    }
                };

                TextArea taWarn = new TextArea(getWarnMsg(), game.skin, "warn"){
                    public float getPrefHeight() {
                        calculateOffsets();
                        float prefHeight = (getLines() + 1) * getStyle().font.getLineHeight(); // Work around
                        TextFieldStyle style = getStyle();
                        if (style.background != null) {
                            prefHeight = Math.max(prefHeight + style.background.getBottomHeight() + style.background.getTopHeight(), style.background.getMinHeight());
                        }
                        return prefHeight;
                    }
                };


                Table ta = new Table();

                if (hasError()) {
                    ta.add(taError).expand().fill();

                    if (hasWarn()) ta.row();
                }
                if (hasWarn()) {
                    ta.add(taWarn).expand().fill();
                }


                ScrollPane scrollPane = new ScrollPane(ta, game.skin);
                scrollPane.setFlickScroll(false);
                scrollPane.setFadeScrollBars(false);
                scrollPane.setScrollbarsOnTop(true);

                dlg.getContentTable().add(scrollPane).width(720).height(420).pad(20);


                dlg.button("OK", true);
                dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
                dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, true);
                dlg.show(stage);
            } else {
                game.showMsgDlg(getName(), "All tests are ok!", stage);
            }
            return false;
        }
    };
}
