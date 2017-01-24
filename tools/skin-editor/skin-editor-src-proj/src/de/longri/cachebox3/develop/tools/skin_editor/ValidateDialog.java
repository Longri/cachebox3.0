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
package de.longri.cachebox3.develop.tools.skin_editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.develop.tools.skin_editor.validation.Validate_MapWayPointItemStyle;
import de.longri.cachebox3.develop.tools.skin_editor.validation.Validate_UnusedSvgFiles;
import de.longri.cachebox3.develop.tools.skin_editor.validation.ValidationTask;

/**
 * Display a dialog that validate the Skin
 *
 * @author Longri
 */
public class ValidateDialog extends Dialog {

    private SkinEditorGame game;
    private Table tableOutput;
    private final Array<ValidationTask> tasks = new Array<ValidationTask>();
    private final SavableSvgSkin validationSkin;

    public ValidateDialog(final SkinEditorGame game, SavableSvgSkin skin) {
        super("Validate Skin", game.skin);
        this.game = game;
        this.validationSkin = skin;
        tableOutput = new Table(game.skin);
        tableOutput.left().top().pad(5);
        tableOutput.defaults().pad(5);

        ScrollPane scrollPane = new ScrollPane(tableOutput, game.skin);
        scrollPane.setFlickScroll(false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(true);

        getContentTable().add(scrollPane).width(720).height(420).pad(20);
        getButtonTable().padBottom(15);
        button("Cancel", false);
        key(com.badlogic.gdx.Input.Keys.ESCAPE, false);

        addValidationTasks();


        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                runValidate();
            }
        });
    }

    private void addValidationTasks() {
        tasks.add(new Validate_MapWayPointItemStyle(game, validationSkin));
        tasks.add(new Validate_UnusedSvgFiles(game, validationSkin));
    }

    public void runValidate() {

        // Most CB classes use VisUI, so set Skin first!
        VisUI.dispose();
        VisUI.load(validationSkin);


        //fill Table
        for (ValidationTask task : tasks) {
            Label taskLabel = new Label(task.getName(), game.skin);
            tableOutput.add(taskLabel).expandX().fillX();
            Image workImage = new Image(game.skin.getRegion("check-off"));
            Cell cell = tableOutput.add(workImage);
            task.setResultCell(cell);
            tableOutput.row();
        }

        //run every task on new Thread
        for (final ValidationTask task : tasks) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    task.runValidation();
                    task.setReadyIcon();
                }
            });
            thread.start();
        }

    }
}
