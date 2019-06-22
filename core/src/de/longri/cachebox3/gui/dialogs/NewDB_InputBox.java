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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTextField;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.CB_CheckBox;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 03.09.16.
 */
public class NewDB_InputBox extends ButtonDialog {
    public NewDB_InputBox(OnMsgBoxClickListener listener) {
        super("NewDB", createContentBox(), Translation.get("NewDB"), MessageBoxButtons.OKCancel, listener);
    }

    private static Table createContentBox() {
        Table contentBox = new Table();

        VisTextField textField = new VisTextField();
        textField.setMessageText(Translation.get("InsNewDBName").toString());//TODO change to CharSequence

        CB_CheckBox checkBox = new CB_CheckBox(Translation.get("UseDefaultRep"));

        float pad = CB.scaledSizes.MARGIN;
        contentBox.add(textField).pad(pad).left().fillX();
        contentBox.row();
        contentBox.add(checkBox).pad(pad).left().fillX();
        contentBox.pack();
        contentBox.layout();
        return contentBox;
    }


    protected void result(Object which) {
        if (msgBoxClickListener != null) {

            //get TextFiled and Checkbox from input
            VisTextField textField = null;
            VisCheckBox checkBox = null;
            for (Actor actor : this.contentBox.getChildren()) {
                if (actor instanceof VisCheckBox) checkBox = (VisCheckBox) actor;
                else if (actor instanceof VisTextField) textField = (VisTextField) actor;
            }

            String newDbName = textField.getText();
            boolean okClicked = ((Integer) which) == BUTTON_POSITIVE;
            if (okClicked && (newDbName == null || newDbName.isEmpty())) {
                CB.viewmanager.toast(Translation.get("MustEnterName"));//TODO Missing translation
                return;
            }

            Object[] objects = new Object[2];
            objects[0] = new Boolean(checkBox.isChecked());
            objects[1] = newDbName;

            msgBoxClickListener.onClick((Integer) which, objects);
            this.hide();
        }
    }


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
}
