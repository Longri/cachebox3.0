/*
 * Copyright (C) 2020 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 01.06.17
 */
public class HintDialog extends DialogBox {
    private final CharSequence hintFromDB;
    private final VisLabel hintLabel;
    private boolean encoded = false;

    public HintDialog() {
        super("Hint", createContentBox(), Translation.get("hint"), MessageBoxButton.RetryCancel, null);
        hintFromDB = EventHandler.getSelectedCache() == null ? ""
                : UnitFormatter.rot13(EventHandler.getSelectedCache().getHint());

        hintLabel = (VisLabel) ((ScrollPane) center.getCells().get(0).getActor()).getActor();

        hintLabel.setWrap(true);
        setButtonText("decode", null, "close");

        result(BUTTON_POSITIVE);
    }

    private static Catch_Table createContentBox() {
        Catch_Table contentBox = new Catch_Table();
        VisScrollPane scrollPane = new VisScrollPane(new VisLabel());
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
                hintLabel.setText(UnitFormatter.rot13(hintFromDB));
                setButtonText(Translation.get("encode"), BUTTON_POSITIVE);
            } else {
                encoded = false;
                hintLabel.setText(UnitFormatter.rot13(hintFromDB));
                setButtonText(Translation.get("decode"), BUTTON_POSITIVE);
            }
            return;
        }
        this.hide();
    }
}
