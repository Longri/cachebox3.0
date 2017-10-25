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
package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 01.06.17
 */
public class HintDialog extends ButtonDialog {
    private final String hintTextDecoded, hintTextEncoded;
    private static final VisLabel hintLabel = new VisLabel();
    private final VisTextButton encodeButton;
    private boolean encoded = false;

    public HintDialog() {
        super("Hint", createContentBox(), Translation.Get("hint"), MessageBoxButtons.RetryCancel, null);

        String hintFromDB = EventHandler.getSelectedCache() == null ? "" : EventHandler.getSelectedCache().getHint(Database.Data);
        this.hintTextDecoded = UnitFormatter.rot13(hintFromDB) + "\n ";
        this.hintTextEncoded = hintFromDB + "\n ";

        hintLabel.setWrap(true);

        SnapshotArray<Actor> childs = this.buttonTable.getChildren();
        encodeButton = ((VisTextButton) childs.get(0));
        encodeButton.setText(Translation.Get("decode"));
        ((VisTextButton) childs.get(1)).setText(Translation.Get("close"));
        result(BUTTON_POSITIVE);
    }

    private static Table createContentBox() {
        Table contentBox = new Table();
        VisScrollPane scrollPane = new VisScrollPane(hintLabel);
        contentBox.defaults().pad(CB.scaledSizes.MARGIN);
        contentBox.add(scrollPane).expand().fill();
        contentBox.pack();
        contentBox.layout();
        return contentBox;
    }


    protected void result(Object which) {
        boolean decodeClicked = ((Integer) which) == BUTTON_POSITIVE;
        if (decodeClicked) {
            if (!encoded) {
                encoded = true;
                hintLabel.setText(this.hintTextEncoded);
                encodeButton.setText(Translation.Get("encode"));
            } else {
                encoded = false;
                hintLabel.setText(this.hintTextDecoded);
                encodeButton.setText(Translation.Get("decode"));
            }
            return;
        }
        this.hide();
    }
}
