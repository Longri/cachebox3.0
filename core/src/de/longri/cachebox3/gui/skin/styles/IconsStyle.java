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
package de.longri.cachebox3.gui.skin.styles;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 27.01.17.
 */
public class IconsStyle extends AbstractIconStyle {

    // Button Dialog Icons
    public Drawable Asterisk; //	Das Meldungsfeld enth�lt ein Symbol, das aus dem Kleinbuchstaben i in einem Kreis besteht.
    public Drawable Error; //	Das Meldungsfeld enth�lt ein Symbol, das aus einem wei�en X in einem Kreis mit rotem Hintergrund besteht.
    public Drawable Exclamation; //	Das Meldungsfeld enth�lt ein Symbol, das aus einem Ausrufezeichen in einem Dreieck mit gelbem Hintergrund besteht.
    public Drawable Hand; //	Das Meldungsfeld enth�lt ein Symbol, das aus einem wei�en X in einem Kreis mit rotem Hintergrund besteht.
    public Drawable Information; //	Das Meldungsfeld enth�lt ein Symbol, das aus dem Kleinbuchstaben i in einem Kreis besteht.
    public Drawable Question; //	Das Meldungsfeld enth�lt ein Symbol, das aus einem Fragezeichen in einem Kreis besteht.
    public Drawable Stop; //	Das Meldungsfeld enth�lt ein Symbol, das aus einem wei�en X in einem Kreis mit rotem Hintergrund besteht.
    public Drawable Warning; //	Das Meldungsfeld enth�lt ein Symbol, das aus einem Ausrufezeichen in einem Dreieck mit gelbem Hintergrund besteht.
    public Drawable Powerd_by_GC_Live;
    public Drawable GC_Live;
    public Drawable Close;
    public Drawable Help;
    public Drawable ExpiredApiKey;
    public Drawable Database;


    @Override
    public int getPrefWidth() {
        return CB.getScaledInt(51);
    }

    @Override
    public int getPrefHeight() {
        return CB.getScaledInt(51);
    }
}
