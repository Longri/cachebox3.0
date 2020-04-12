/*
 * Copyright (C) 2016 team-cachebox.de
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
import de.longri.cachebox3.gui.widgets.CB_CheckBox;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 03.09.16.
 */
public class NewDB_InputBox {
    private ButtonDialog dialog;
    private Catch_Table contentBox;
    private CB_Label lblDBName;
    private EditTextField edtDBName;
    private CB_CheckBox checkBox;
    private OnMsgBoxClickListener listener;

    public NewDB_InputBox(OnMsgBoxClickListener listener) {
        lblDBName = new CB_Label(Translation.get("InsNewDBName"));
        edtDBName = new EditTextField("");
        checkBox = new CB_CheckBox(Translation.get("UseDefaultRep"));
        contentBox = new Catch_Table(true);
        contentBox.addLast(lblDBName);
        contentBox.addLast(edtDBName);
        contentBox.addLast(checkBox);
        contentBox.pack();
        contentBox.layout();
        this.listener = listener;
        dialog = new ButtonDialog("NewDB", contentBox, Translation.get("NewDB"), MessageBoxButton.OKCancel, listener);
    }

    public void show() {
        dialog.show();
        // enable continues rendering for cursor blink
        Gdx.graphics.setContinuousRendering(true);
    }

    public void hide() {
        dialog.hide();
        // disable continues rendering
        Gdx.graphics.setContinuousRendering(false);
    }

    public String getNewDB_Name() {
        return edtDBName.getText();
    }

    public boolean ownRepository() {
        return !checkBox.isChecked();
    }
}
