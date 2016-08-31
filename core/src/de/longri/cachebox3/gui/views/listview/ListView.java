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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
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


            view.setPrefWidth(this.getWidth() - (padLeft + padRight));
            view.pack();
            view.layout();
            itemViews.add(view);
            itemHeights.add(view.getHeight() + padBottom + padTop);
            itemGroup.addActor(view);
        }


        //layout itemGroup

        float completeHeight = 0;

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

        scrollPane = new VisScrollPane(itemGroup);
        scrollPane.setOverscroll(false, true);
        scrollPane.setFlickScroll(true);
        scrollPane.setFadeScrollBars(true);


        float paneHeight = this.getHeight();
        float paneYPos = 0;
        if (this.getHeight() > completeHeight) {
            //set on Top
            paneHeight = completeHeight;
            paneYPos = this.getHeight() - completeHeight;
        }

        scrollPane.setBounds(0, paneYPos, this.getWidth(), paneHeight);
        this.addActor(scrollPane);
        scrollPane.layout();
        needsLayout = false;
    }

    @Override
    protected void sizeChanged() {
        needsLayout = true;
        layout();
    }

    @Override
    public void pack() {
        needsLayout = true;
        layout();
    }


    public void draw(Batch batch, float parentAlpha) {
        if (this.backgroundDrawable != null) {
            backgroundDrawable.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        super.draw(batch, parentAlpha);
    }

    public float getScrollPos() {
        return scrollPane.getScrollY();
    }

    public void setScrollPos(float scrollPos) {
        scrollPane.setScrollY(scrollPos);
    }


    public static class ListViewStyle {
        public Drawable background, firstItem, secondItem, selectedItem;
        public float pad, padLeft, padRight, padTop, padBottom;
    }
}
