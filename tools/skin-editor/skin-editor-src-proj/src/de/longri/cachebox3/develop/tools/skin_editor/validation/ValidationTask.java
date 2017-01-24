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
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;

/**
 * Created by Longri on 24.01.2017.
 */
public abstract class ValidationTask {

    public abstract String getName();

    public abstract void runValidation();


    final protected SkinEditorGame game;
    final protected SavableSvgSkin validationSkin;
    private Cell cell;
    protected String errorMsg, warnMsg;

    public ValidationTask(SkinEditorGame game, SavableSvgSkin validationSkin) {
        this.game = game;
        this.validationSkin = validationSkin;
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

                } else {
                    Image readyImage = new Image(game.skin.getRegion("check-on"));
                    ValidationTask.this.cell.setActor(readyImage);
                }
                Gdx.graphics.requestRendering();
            }
        });


    }
}
