/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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

import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_VisTextButton;

/**
 * Created by Longri on 27.10.2017.
 */
public class CB_Button extends Catch_VisTextButton {

    public CB_Button(CharSequence text) {
        super(EMPTY);
        getLabel().setText(text);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public CB_Button(CharSequence text, String styleName) {
        super(EMPTY, styleName);
        getLabel().setText(text);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public CB_Button(CharSequence text, VisTextButtonStyle buttonStyle) {
        super(EMPTY, buttonStyle);
        getLabel().setText(text);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public void setText(CharSequence sequence) {
        getLabel().setText(sequence);
        setSize(getPrefWidth(), getPrefHeight());
    }
}
