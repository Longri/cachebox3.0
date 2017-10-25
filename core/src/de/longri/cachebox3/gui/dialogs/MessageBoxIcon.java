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
import de.longri.cachebox3.gui.skin.styles.IconsStyle;

public enum MessageBoxIcon implements SelectBoxItem {
    Asterisk, //	Das Meldungsfeld enth�lt ein Symbol, das aus dem Kleinbuchstaben i in einem Kreis besteht.
    Error, //	Das Meldungsfeld enth�lt ein Symbol, das aus einem wei�en X in einem Kreis mit rotem Hintergrund besteht.
    Exclamation, //	Das Meldungsfeld enth�lt ein Symbol, das aus einem Ausrufezeichen in einem Dreieck mit gelbem Hintergrund besteht.
    Hand, //	Das Meldungsfeld enth�lt ein Symbol, das aus einem wei�en X in einem Kreis mit rotem Hintergrund besteht.
    Information, //	Das Meldungsfeld enth�lt ein Symbol, das aus dem Kleinbuchstaben i in einem Kreis besteht.
    None, //	Das Meldungsfeld enth�lt keine Symbole.
    Question, //	Das Meldungsfeld enth�lt ein Symbol, das aus einem Fragezeichen in einem Kreis besteht.
    Stop, //	Das Meldungsfeld enth�lt ein Symbol, das aus einem wei�en X in einem Kreis mit rotem Hintergrund besteht.
    Warning, //	Das Meldungsfeld enth�lt ein Symbol, das aus einem Ausrufezeichen in einem Dreieck mit gelbem Hintergrund besteht.
    Powerd_by_GC_Live,
    GC_Live,
    ExpiredApiKey,
    Database;

    private static IconsStyle iconStyle;

    @Override
    public Drawable getDrawable() {
        // for select Box interface, use 'cacheList' style
        if (iconStyle == null) iconStyle = VisUI.getSkin().get("default", IconsStyle.class);
        return getDrawable(iconStyle);
    }

    private Drawable getDrawable(IconsStyle iconStyle) {
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
            case Powerd_by_GC_Live:
                return iconStyle.Powerd_by_GC_Live;
            case GC_Live:
                return iconStyle.GC_Live;
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
