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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by Longri on 30.11.2017.
 */
public class CircularProgressStyle {
    public Drawable progressTexture, readyDrawable;
    public Color borderColor, backgroundColor, textBackgroundColor, textBorderColor;
    public Color unknownColor;
    public BitmapFont textFont;
    public Color textFontColor;
    public float scaledPreferedRadius;
    public Color progressColor;

    public CircularProgressStyle() {
    }

    public CircularProgressStyle(CircularProgressStyle other) {
        readyDrawable = other.readyDrawable;
        borderColor = other.borderColor;
        backgroundColor = other.backgroundColor;
        textBackgroundColor = other.textBackgroundColor;
        textBorderColor = other.textBorderColor;
        unknownColor = other.unknownColor;
        textFont = other.textFont;
        textFontColor = other.textFontColor;
        progressColor = other.progressColor;
    }
}
