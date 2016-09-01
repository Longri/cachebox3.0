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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;


/**
 * Created by Longri on 12.08.2016.
 */
public class ListView extends WidgetGroup {

    private VisScrollPane scrollPane;
    private Drawable backgroundDrawable;
    private Adapter adapter;
    private boolean needsLayout = true;
    private final ListViewStyle style;
    private ScrollViewContainer itemGroup;
    float completeHeight = 0;
    boolean isDraggable = false;

    public ListView() {
        this(VisUI.getSkin().get("default", ListViewStyle.class));
    }

    public ListView(ListViewStyle style) {
        this.style = style;
        setLayoutEnabled(true);
    }

    public ListView(Adapter listViewAdapter) {
        this(VisUI.getSkin().get("default", ListViewStyle.class));
        this.adapter = listViewAdapter;
    }

    public void setAdapter(Adapter listViewAdapter) {
        this.adapter = listViewAdapter;
    }

    public void setBackground(Drawable background) {
        this.backgroundDrawable = background;
    }


    private FloatArray itemHeights = new FloatArray();
    private FloatArray itemYPos = new FloatArray();
    private Array<VisTable> itemViews = new Array<VisTable>();

    public void layout(boolean force) {
        needsLayout = true;
        layout();
    }

    @Override
    public void layout() {
        if (!this.needsLayout) {
            super.layout();
            return;
        }

        if (adapter == null) return;

        this.clear();
        itemHeights.clear();
        itemYPos.clear();
        itemViews.clear();

        itemGroup = new ScrollViewContainer();
        itemGroup.setWidth(this.getWidth());
        itemGroup.clear();

        float padLeft = style.padLeft > 0 ? style.padLeft : style.pad;
        float padRight = style.padRight > 0 ? style.padRight : style.pad;
        float padTop = style.padTop > 0 ? style.padTop : style.pad;
        float padBottom = style.padBottom > 0 ? style.padBottom : style.pad;


        int length = adapter.getCount();

        for (int i = 0; i < length; i++) {
            // set Item background
            ListViewItem view = adapter.getView(i);

            if (view != null) {
                if (style.secondItem != null && i % 2 == 1) {
                    view.setBackground(style.secondItem);
                } else {
                    view.setBackground(style.firstItem);
                }
            } else {
                // add a empty table
                view = new ListViewItem();
            }

            //set on drawListner
            view.setOnDrawListener(onDrawListener);

            view.setPrefWidth(this.getWidth() - (padLeft + padRight));
            view.pack();
            view.layout();
            itemViews.add(view);
            itemHeights.add(view.getHeight() + padBottom + padTop);
            itemGroup.addActor(view);
        }


        //layout itemGroup

        completeHeight = 0;
        for (int i = 0; i < itemHeights.size; i++) { //calculate complete hight of all Items
            completeHeight += itemHeights.items[i];
        }

        itemGroup.setWidth(this.getWidth());
        itemGroup.setHeight(completeHeight);
        itemGroup.setPrefWidth(this.getWidth());
        itemGroup.setPrefHeight(completeHeight);


        float yPos = completeHeight;

        Actor[] actors = itemGroup.getChildren().items;
        for (int i = 0; i < itemGroup.getChildren().size; i++) {// calculate Y position of all Items
            yPos -= itemHeights.get(i);
            itemYPos.add(yPos);
            actors[i].setBounds(padLeft, yPos, this.getWidth() - (padLeft + padRight), itemHeights.get(i) - (padTop + padBottom));
        }

        scrollPane = new VisScrollPane(itemGroup, style);
        scrollPane.setOverscroll(false, true);
        scrollPane.setFlickScroll(true);
        scrollPane.setVariableSizeKnobs(false);
        setScrollPaneBounds();
        this.addActor(scrollPane);
        scrollPane.layout();
        needsLayout = false;
    }

    @Override
    protected void sizeChanged() {
        if (scrollPane != null) {
            setScrollPaneBounds();
        } else {
            needsLayout = true;
            layout();
        }
    }

    private void setScrollPaneBounds() {
        float paneHeight = this.getHeight();
        float paneYPos = 0;
        isDraggable = true;
        if (this.getHeight() > completeHeight) {
            //set on Top
            paneHeight = completeHeight;
            paneYPos = this.getHeight() - completeHeight;
            isDraggable = false;
        }

        scrollPane.setBounds(0, paneYPos, this.getWidth(), paneHeight);
    }

    @Override
    public void pack() {
        needsLayout = true;
        layout();
    }


    ListViewItem.OnDrawListener onDrawListener = new ListViewItem.OnDrawListener() {
        @Override
        public void onDraw(ListViewItem item) {
            if (adapter != null) adapter.update(item);
        }
    };

    public void draw(Batch batch, float parentAlpha) {
        if (this.backgroundDrawable != null) {
            backgroundDrawable.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        synchronized (this) {
            super.draw(batch, parentAlpha);
        }
    }

    /**
     * Draws all children. {@link #applyTransform(Batch, Matrix4)} should be called before and {@link #resetTransform(Batch)}
     * after this method if {@link #setTransform(boolean) transform} is true. If {@link #setTransform(boolean) transform} is false
     * these methods don't need to be called, children positions are temporarily offset by the group position when drawn. This
     * method avoids drawing children completely outside the {@link #setCullingArea(Rectangle) culling area}, if set.
     */
    protected void drawChildren(Batch batch, float parentAlpha) {
        parentAlpha *= this.getColor().a;
        SnapshotArray<Actor> children = this.getChildren();
        Actor[] actors = children.begin();
        Rectangle cullingArea = this.getCullingArea();
        if (cullingArea != null) {
            // Draw children only if inside culling area.
            float cullLeft = cullingArea.x;
            float cullRight = cullLeft + cullingArea.width;
            float cullBottom = cullingArea.y;
            float cullTop = cullBottom + cullingArea.height;
            if (this.isTransform()) {
                for (int i = 0, n = children.size; i < n; i++) {
                    Actor child = actors[i];
                    if (!child.isVisible()) continue;
                    float cx = child.getX(), cy = child.getY();
                    if (cx <= cullRight && cy <= cullTop && cx + child.getWidth() >= cullLeft && cy + child.getHeight() >= cullBottom)
                        child.draw(batch, parentAlpha);
                }
            } else {
                // No transform for this group, offset each child.
                float offsetX = getX(), offsetY = getY();
                setPosition(0, 0);
                for (int i = 0, n = children.size; i < n; i++) {
                    Actor child = actors[i];
                    if (!child.isVisible()) continue;
                    float cx = child.getX(), cy = child.getY();
                    if (cx <= cullRight && cy <= cullTop && cx + child.getWidth() >= cullLeft && cy + child.getHeight() >= cullBottom) {
                        child.setX(cx + offsetX);
                        child.setY(cy + offsetY);
                        child.draw(batch, parentAlpha);
                        child.setX(cx);
                        child.setY(cy);
                    }
                }
                setPosition(offsetX, offsetY);
            }
        } else {
            // No culling, draw all children.
            if (this.isTransform()) {
                for (int i = 0, n = children.size; i < n; i++) {
                    Actor child = actors[i];
                    if (!child.isVisible()) continue;
                    child.draw(batch, parentAlpha);
                }
            } else {
                // No transform for this group, offset each child.
                float offsetX = getX(), offsetY = getY();
                setPosition(0, 0);
                for (int i = 0, n = children.size; i < n; i++) {
                    Actor child = actors[i];
                    if (!child.isVisible()) continue;
                    float cx = child.getX(), cy = child.getY();
                    child.setX(cx + offsetX);
                    child.setY(cy + offsetY);
                    child.draw(batch, parentAlpha);
                    child.setX(cx);
                    child.setY(cy);
                }
                setPosition(offsetX, offsetY);
            }
        }
        children.end();
    }


    public boolean isDraggable() {
        return isDraggable;
    }

    public float getScrollPos() {
        return scrollPane.getScrollY();
    }

    public void setScrollPos(float scrollPos) {
        scrollPane.setScrollY(scrollPos);
    }


    public static class ListViewStyle extends ScrollPane.ScrollPaneStyle {
        public Drawable firstItem, secondItem, selectedItem;
        public float pad, padLeft, padRight, padTop, padBottom;
    }
}
