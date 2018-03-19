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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;

/**
 * Created by Longri on 03.02.18.
 */
public class ListViewItem extends Catch_Table implements Disposable, ListViewItemInterface {

    private OnDrawListener onDrawListener;

    private float prefHeight = -1f;
    private float prefWidth = -1f;
    private int index;
    private boolean isSelected = false;

    public ListViewItem(int index) {
        this.index = index;
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (getOnDrawListener() != null) {
            if (CB.viewmanager != null && CB.viewmanager.isTop(this.getStage()))
                getOnDrawListener().onDraw(this);
        }
    }

    @Override
    public void dispose() {

    }

    private OnDrawListener getOnDrawListener() {
        return onDrawListener;
    }

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    @Override
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }


    @Override
    public int getListIndex() {
        return index;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Item: ");
        sb.append(Integer.toString(this.index));
        sb.append(" y: ").append(Float.toString(this.getY()));
        sb.append(" h: ").append(Float.toString(this.getHeight()));
        sb.append(" v: ").append(Boolean.toString(this.isVisible()));
        return sb.toString();
    }

    public void setPrefHeight(float height) {
        prefHeight = height;
    }

    public void setPrefWidth(float width) {
        prefWidth = width;
    }

    @Override
    public float getPrefHeight() {
        if (prefHeight == -1) return super.getPrefHeight();
        return prefHeight;
    }

    @Override
    public float getPrefWidth() {
        if (prefWidth == -1) return super.getPrefWidth();
        return prefWidth;
    }

    @Override
    public void setBackground(Drawable drawable) {
        super.setBackground(drawable);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ListViewItem && (this.index == ((ListViewItem) o).index);
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    public void setNewIndex(int index) {
        this.index = index;
    }
}
