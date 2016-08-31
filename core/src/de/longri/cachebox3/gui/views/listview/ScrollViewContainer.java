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
package de.longri.cachebox3.gui.views.listview;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

/**
 * Created by Longri on 31.08.2016.
 */
public class ScrollViewContainer extends WidgetGroup {

    private float prefWidth = -1, prefHeight = -1;

    @Override
    public float getPrefWidth() {
        if (prefWidth == -1) return super.getPrefWidth();
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        if (prefHeight == -1) return super.getPrefHeight();
        return prefHeight;
    }

    public void setPrefWidth(float width) {
        this.prefWidth = width;
    }

    public void setPrefHeight(float height) {
        this.prefHeight = height;
    }

}
