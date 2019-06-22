/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * Created by Longri on 02.04.2019.
 */
public class CB_Label extends VisLabel {
    public CB_Label() {
        super();
    }

    public CB_Label(CharSequence text,LabelStyle style) {
        super(text,style);
    }

    public CB_Label(CharSequence text, Color textColor) {
        super(text, textColor);
        setColor(textColor);
    }

    public CB_Label(CharSequence text, int alignment) {
        super(text, alignment);
    }

    public CB_Label(CharSequence name) {
        super(name);
    }

    public void draw (Batch batch, float parentAlpha) {
        try {
            super.draw(batch,parentAlpha);
        } catch (Exception e) {
            this.setText("can't draw text");
        }
    }
}
