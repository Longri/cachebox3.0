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
package de.longri.cachebox3.gui.widgets.list_view;

import com.badlogic.gdx.graphics.g2d.Batch;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryListView extends ListView {
    public GalleryListView() {
//        super(ListViewType.HORIZONTAL);
        super(ListViewType.VERTICAL);
        this.setDebug(true, true);
    }


    //    @Override
//    public void setWidth(float newWidth) {
//        super.setWidth(newWidth);
//    }
//
//    @Override
//    public void setHeight(float newHeight) {
//        super.setHeight(newHeight);
//    }
//
//    public void setSize(float width, float height) {
//        super.setSize(width, height);
//    }
//
//    /**
//     * Adds the specified size to the current size.
//     */
//    public void sizeBy(float size) {
//        super.sizeBy(size);
//    }
//
//    /**
//     * Adds the specified size to the current size.
//     */
//    public void sizeBy(float width, float height) {
//        super.sizeBy(width, height);
//    }
//
//    /**
//     * Set bounds the x, y, width, and height.
//     */
//    public void setBounds(float x, float y, float width, float height) {
//        super.setBounds(x, y, width, height);
//    }
//
//
//    @Override
//    public void draw(Batch batch, float parentAlpha) {
//        super.draw(batch, parentAlpha);
//    }
//
//    @Override
//    public float getPrefWidth(){
//        return 100;
//    }
//
//
    @Override
    public float getPrefHeight() {
        return CB.getScaledFloat(75);
    }
}
