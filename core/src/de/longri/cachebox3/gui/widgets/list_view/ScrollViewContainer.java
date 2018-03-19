/*
 * Copyright (C) 2016 -2018 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets.list_view;

import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Group;

/**
 * Created by Longri on 31.08.2016.
 */
public class ScrollViewContainer extends Catch_Group {

    private float prefWidth = -1, prefHeight = -1;


    public float getPrefWidth() {
        return prefWidth;
    }


    public float getPrefHeight() {
        return prefHeight;
    }

    public void setPrefWidth(float width) {
        this.prefWidth = width;
    }

    public void setPrefHeight(float height) {
        this.prefHeight = height;
    }

}
