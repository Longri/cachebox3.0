/*
 * Copyright (C) 2020 - 2018 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by Longri on 27.10.2017.
 */
public class CB_Button extends IconButton {

    public CB_Button(CharSequence text) {
        super(text);
    }

    public CB_Button(CharSequence text, String styleName) {
        super(text, styleName);
    }

    public CB_Button(CharSequence text, VisTextButtonStyle buttonStyle) {
        super(text, buttonStyle);
    }

    public CB_Button(CharSequence text, Drawable icon) {
        super(text, icon);
    }

    public CB_Button(Drawable icon) {
        super(icon);
    }

    public void setText(CharSequence text) {
        getLabel().setText(text);
    }

    public void setTextAndSize(CharSequence text) {
        getLabel().setText(text);
        setSize(getPreferredWidth(), getPreferredHeight());
    }

    public void setState(int i) {
        setChecked(i == 0 ? false : true);
    }

    public void enable() { setDisabled(false);}

    public void disable() { setDisabled(true);}

}
