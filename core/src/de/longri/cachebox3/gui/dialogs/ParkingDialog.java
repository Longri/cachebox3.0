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

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 14.09.2016.
 */
public class ParkingDialog extends ButtonDialog {
    public ParkingDialog() {
        super("NewDB", createContentBox(), Translation.Get("NewDB"), MessageBoxButtons.OKCancel, MessageBoxIcon.None, null);
    }

    private static Table createContentBox() {
        Table contentBox = new Table();
        //TODO fill content box
        return contentBox;
    }
}
