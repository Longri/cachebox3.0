/*
 * Copyright (C) 2018 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Actor;

/**
 * Created by Longri on 03.02.18.
 */
public class DummyListViewItem extends Catch_Actor implements ListViewItemInterface {

    private final int index;
    private float prefHeight, prefWidth;
    private boolean isSelected;

    DummyListViewItem(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Dummy Item: " + Integer.toString(this.getListIndex());
    }

    @Override
    public void dispose() {

    }

    @Override
    public int getListIndex() {
        return this.index;
    }

    @Override
    public float getPrefHeight() {
        return prefHeight;
    }

    @Override
    public float getPrefWidth() {
        return prefWidth;
    }

    @Override
    public void setBackground(Drawable drawable) {

    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setPrefWidth(float prefWidth) {
        this.prefWidth = prefWidth;
    }

    @Override
    public void pack() {

    }

    @Override
    public void setPrefHeight(float prefHeight) {
        this.prefHeight = prefHeight;
    }

    @Override
    public void setOnDrawListener(OnDrawListener onDrawListener) {
        //ignore
    }

    @Override
    public void setOnItemSizeChangedListener(OnItemSizeChangedListener onItemSizeChangedListener) {
        //ignore
    }

    @Override
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    @Override
    public void removeOnItemSizeChangedListener(OnItemSizeChangedListener onItemSizeChangedListener) {
        //ignore
    }

    @Override
    public void removeOnDrawListener(OnDrawListener onDrawListener) {
        //ignore
    }

    @Override
    public void setFinalHeight(float finalHeight) {
        //ignore
    }

    @Override
    public void setFinalWidth(float finalWidth) {
        //ignore
    }

    private float finalWidth, finalHeight;
    private boolean hasFinalSize = false;

    public void setFinalSize(float width, float height) {
        this.finalWidth = width;
        this.finalHeight = height;
        hasFinalSize = true;
    }

    public boolean hasFinalSize() {
        return hasFinalSize;
    }

    public float getFinalWidth() {
        return this.finalWidth;
    }

    public float getFinalHeight() {
        return this.finalHeight;
    }

    @Override
    public void setX(float value) {
        super.setX(value);
    }
}
