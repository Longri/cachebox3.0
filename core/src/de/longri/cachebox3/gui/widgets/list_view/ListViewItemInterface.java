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

/**
 * Created by Longri on 10.02.18.
 */
public interface ListViewItemInterface {
    void draw(Batch batch, float parentAlpha);

    void dispose();

    int getListIndex();

    float getPrefHeight();

    float getPrefWidth();

    void setBackground(Drawable drawable);

    boolean isSelected();

    void setPrefWidth(float prefWidth);

    void pack();

    void setX(float x);

    float getHeight();

    void setPrefHeight(float prefHeight);

    void setY(float y);

    float getWidth();

    boolean isVisible();

    float getY();

    float getX();

    void setOnDrawListener(OnDrawListener onDrawListener);

    void setOnItemSizeChangedListener(OnItemSizeChangedListener onItemSizeChangedListener);

    void setWidth(float width);

    void setHeight(float height);

    void setSelected(boolean selected);

    void setVisible(boolean visible);

    void removeOnItemSizeChangedListener(OnItemSizeChangedListener onItemSizeChangedListener);

    void removeOnDrawListener(OnDrawListener onDrawListener);

    void setFinalHeight(float finalHeight);

    void setFinalWidth(float finalWidth);
}
