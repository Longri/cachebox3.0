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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;
import de.longri.cachebox3.gui.skin.styles.MessageBoxIconStyle;

public enum MessageBoxIcon implements SelectBoxItem {
    Asterisk, //
    Error, //
    Exclamation, //
    Hand, //
    Information, //
    None, //
    Question, //
    Stop, //
    Warning, //
    ExpiredApiKey,
    Database;

    private static MessageBoxIconStyle iconStyle;

    @Override
    public Drawable getDrawable() {
        // for select Box interface, use 'cacheList' style
        if (iconStyle == null) iconStyle = VisUI.getSkin().get(MessageBoxIconStyle.class);
        return getDrawable(iconStyle);
    }

    private Drawable getDrawable(MessageBoxIconStyle iconStyle) {
        switch (this) {

            case Asterisk:
                return iconStyle.Asterisk;
            case Error:
                return iconStyle.Error;
            case Exclamation:
                return iconStyle.Exclamation;
            case Hand:
                return iconStyle.Hand;
            case Information:
                return iconStyle.Information;
            case None:
                return null;
            case Question:
                return iconStyle.Question;
            case Stop:
                return iconStyle.Stop;
            case Warning:
                return iconStyle.Warning;
            case ExpiredApiKey:
                return iconStyle.ExpiredApiKey;
            case Database:
                return iconStyle.Database;
        }
        return null;
    }

    public String getName() {
        return this.name();
    }


}
