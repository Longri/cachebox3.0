/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.utils.NamedRunnable;


/**
 * Created by Longri on 04.10.17
 */
public class CancelProgressDialog extends ButtonDialog {

    private final ProgressCancelRunnable progressCancelRunnable;
    private final ProgressTable progressTable;

    public CancelProgressDialog(String name, String title, final ProgressCancelRunnable progressCancelRunnable) {
        super(name, getProgressContentTable(), title, MessageBoxButtons.Cancel, null);
        this.msgBoxClickListener = new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                progressCancelRunnable.cancel();
                return true;
            }
        };
        this.progressCancelRunnable = progressCancelRunnable;
        this.progressTable = (ProgressTable) getContentTable();
    }


    private static Table getProgressContentTable() {

        ProgressTable contentTable = new ProgressTable();
        float contentWidth = (Gdx.graphics.getWidth() * 0.75f);

        contentTable.label = new VisLabel();
        contentTable.add(contentTable.label).width(new Value.Fixed(contentWidth)).pad(20);
        contentTable.row();

        contentTable.progress = new CB_ProgressBar(0, 100, 1, false, "default");
        contentTable.add(contentTable.progress).width(new Value.Fixed(contentWidth)).pad(20);
        contentTable.row();

        return contentTable;
    }

    @Override
    public void show() {

        super.show();

        //start runnable async
        CB.postAsync(new NamedRunnable("CancelProgressDialog") {
            @Override
            public void run() {
                progressCancelRunnable.run();

                //after finish, close Dialog
                CB.postOnGlThread(new NamedRunnable("CancelProgressDialog") {
                    @Override
                    public void run() {
                        CancelProgressDialog.this.hide();
                    }
                });

            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.progressTable.progress.setValue(this.progressCancelRunnable.getProgress());
        this.progressTable.label.setText(this.progressCancelRunnable.getProgressMsg());
        super.draw(batch, parentAlpha);
        CB.requestRendering();
    }


    private static class ProgressTable extends Catch_Table {
        CB_ProgressBar progress;
        VisLabel label;
    }

}
