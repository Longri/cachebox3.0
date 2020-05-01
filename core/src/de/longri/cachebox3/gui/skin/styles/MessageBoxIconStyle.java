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
package de.longri.cachebox3.gui.skin.styles;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 27.01.17.
 */
public class MessageBoxIconStyle extends AbstractIconStyle {

    // Button Dialog Icons
    public Drawable Asterisk;
    public Drawable Error;
    public Drawable Exclamation;
    public Drawable Hand;
    public Drawable Information;
    public Drawable Question;
    public Drawable Stop;
    public Drawable Warning;
    public Drawable Close;
    public Drawable Help;
    public Drawable ExpiredApiKey;
    public Drawable Database;


    @Override
    public int getPrefWidth() {
        return CB.getScaledInt(47);
    }

    @Override
    public int getPrefHeight() {
        return CB.getScaledInt(47);
    }
}
