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
package de.longri.cachebox3.gui.widgets;

import com.kotcrab.vis.ui.widget.VisCheckBox;

/**
 * Created by Longri on 30.10.2017.
 * add left() arbor95 26.07.2019
 */
public class CB_CheckBox extends VisCheckBox {

    private final static String EMPTY = "";

    public CB_CheckBox(CharSequence text) {
        super(EMPTY);
        left();
        getLabel().setText(text);
    }

    public CB_CheckBox(CharSequence text, String styleName) {
        super(EMPTY, styleName);
        left();
        getLabel().setText(text);
    }

    public void setText(CharSequence sequence) {
        left();
        getLabel().setText(sequence);
    }
}
