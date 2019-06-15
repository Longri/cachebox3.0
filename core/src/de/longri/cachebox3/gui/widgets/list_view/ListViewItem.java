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
    private OnItemSizeChangedListener onItemSizeChangedListener;

    private float prefHeight = -1f;
    private float prefWidth = -1f;
    protected int index;
    private boolean isSelected = false;
    private float finalWidth, finalHeight;
    private boolean hasFinalSize = false;

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
        this.onItemSizeChangedListener = null;
        this.onDrawListener = null;
    }

    private OnDrawListener getOnDrawListener() {
        return onDrawListener;
    }

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    @Override
    public void setOnItemSizeChangedListener(OnItemSizeChangedListener onItemSizeChangedListener) {
        this.onItemSizeChangedListener = onItemSizeChangedListener;
    }

    @Override
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    @Override
    public void removeOnItemSizeChangedListener(OnItemSizeChangedListener onItemSizeChangedListener) {
        this.onItemSizeChangedListener = null;
    }

    @Override
    public void removeOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = null;
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

    @Override
    public void setWidth(float width) {
        if (this.hasFinalSize) {
            return;
        }
        if (this.getWidth() != width) {
            onItemSizeChanged(width - this.getWidth(), 0);
            super.setWidth(width);
        }
    }

    @Override
    public void setHeight(float height) {
        if (this.hasFinalSize) {
            return;
        }
        if (this.getHeight() != height) {
            onItemSizeChanged(0, height - this.getHeight());
            super.setHeight(height);
        }
    }

    @Override
    public void setSize(float width, float height) {
        if (this.hasFinalSize) {
            return;
        }
        if (this.getWidth() != width || this.getHeight() != height) {
            onItemSizeChanged(width - this.getWidth(), height - this.getHeight());
            super.setSize(width, height);
        }
    }

    @Override
    public void sizeBy(float size) {
        if (this.hasFinalSize) {
            return;
        }
        if (size != 0) {
            float width = getWidth() + size;
            float height = getHeight() + size;
            onItemSizeChanged(width - this.getWidth(), height - this.getHeight());
            super.setSize(width, height);
        }
    }

    @Override
    public void sizeBy(float widthSize, float heightSize) {
        if (this.hasFinalSize) {
            return;
        }
        if (widthSize != 0 || heightSize != 0) {
            float width = getWidth() + widthSize;
            float height = getHeight() + heightSize;
            onItemSizeChanged(width - this.getWidth(), height - this.getHeight());
            super.setSize(width, height);
        }
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        if (this.hasFinalSize) {
            super.setPosition(x, y);
            return;
        }
        if (this.getWidth() != width || this.getHeight() != height) {
            onItemSizeChanged(width - this.getWidth(), height - this.getHeight());
        }
        super.setBounds(x, y, width, height);
    }

    private void onItemSizeChanged(float changedWidth, float changedHeight) {
        if (onItemSizeChangedListener != null) {
            onItemSizeChangedListener.onSizeChanged(this, changedWidth, changedHeight);
        }
    }

    @Override
    public void setFinalHeight(float finalHeight) {
        this.hasFinalSize = false;
        this.setHeight(finalHeight);
        this.hasFinalSize = true;
    }

    @Override
    public void setFinalWidth(float finalWidth) {
        this.hasFinalSize = false;
        this.setWidth(finalWidth);
        this.hasFinalSize = true;
    }
}
