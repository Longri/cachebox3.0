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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by Longri on 27.01.17.
 */
public class ButtonDialogStyle {
    public Drawable title;
    public Drawable header;
    public Drawable center;
    public Drawable footer;
    public Drawable stageBackground;
    public BitmapFont titleFont;
    public Color titleFontColor;

    public ButtonDialogStyle() {
    }

    public ButtonDialogStyle(ButtonDialogStyle other) {
        title = other.title;
        header = other.header;
        center = other.center;
        footer = other.footer;
        stageBackground = other.stageBackground;
        titleFont = other.titleFont;
        titleFontColor = other.titleFontColor;
    }
}
