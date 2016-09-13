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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.views.listview.ListView.SelectableType.NONE;
import static de.longri.cachebox3.gui.views.listview.ListView.SelectableType.SINGLE;


/**
 * Created by Longri on 12.08.2016.
 */
public class ListView extends WidgetGroup {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(ListView.class);

    private VisScrollPane scrollPane;
    private Drawable backgroundDrawable;
    private Adapter adapter;
    private boolean needsLayout = true;
    private final ListView.ListViewStyle style;
    private float completeHeight = 0;
    final private Array<ListViewItem> selectedItemList = new Array<ListViewItem>();
    SelectableType selectionType = NONE;
    private FloatArray itemHeights = new FloatArray();
    private FloatArray itemYPos = new FloatArray();
    private SnapshotArray<ListViewItem> itemViews = new SnapshotArray<ListViewItem>();

    public SnapshotArray<ListViewItem> items() {
        return itemViews;
    }

    public void setSelectedItem(int selectedIndex) {
        selectedItemList.add(itemViews.get(selectedIndex));
    }

    public void dispose() {

        for (ListViewItem item:itemViews){
            if(item instanceof Disposable){
                ((Disposable) item).dispose();
            }
        }

        itemViews.clear();
        selectedItemList.clear();
        adapter=null;
        backgroundDrawable=null;
        scrollPane.clearActions();
        scrollPane.clearListeners();
        scrollPane.clearChildren();
        scrollPane.clear();
        scrollPane=null;
    }

    public enum SelectableType {
        NONE, SINGLE, MULTI
    }

    public interface SelectionChangedEvent {
        public void selectionChanged();
    }

    private final Array<SelectionChangedEvent> changedEventListeners = new Array<SelectionChangedEvent>();

    public void addSelectionChangedEventListner(SelectionChangedEvent event) {
        if (!changedEventListeners.contains(event, true))
            changedEventListeners.add(event);
    }

    public void removeSelectionChangedEventListner(SelectionChangedEvent event) {
        changedEventListeners.removeValue(event, true);
    }

    public ListView() {
        this(VisUI.getSkin().get("default", ListView.ListViewStyle.class));
    }

    private ListView(ListView.ListViewStyle style) {
        this.style = style;
        setLayoutEnabled(true);
    }

    public ListView(Adapter listViewAdapter) {
        this(VisUI.getSkin().get("default", ListView.ListViewStyle.class));
        this.adapter = listViewAdapter;
    }

    public void setAdapter(Adapter listViewAdapter) {
        this.adapter = listViewAdapter;
        layout(true);
    }

    public void setSelectable(SelectableType selectionType) {
        this.selectionType = selectionType;
        // TODO: add clicklistener
    }

    public void setBackground(Drawable background) {
        this.backgroundDrawable = background;
    }


    public synchronized void layout(boolean force) {
        if (force) needsLayout = true;
        layout();
    }

    @Override
    public synchronized void layout() {
        if (!this.needsLayout) {
            super.layout();
            return;
        }

        if (adapter == null) return;

        this.clear();
        itemHeights.clear();
        itemYPos.clear();
        itemViews.clear();

        ScrollViewContainer itemGroup = new ScrollViewContainer();
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
                view = new ListViewItem(i);
            }

            //set on drawListner
            view.setOnDrawListener(onDrawListener);
            view.addListener(onListItemClickListener);
            view.setPrefWidth(this.getWidth() - (padLeft + padRight));
            view.pack();
            view.layout();
            itemViews.add(view);
            itemHeights.add(view.getHeight() + padBottom + padTop);
            itemGroup.addActor(view);
//            itemGroup.setTouchable(Touchable.enabled);
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
        if (this.getHeight() > completeHeight) {
            //set on Top
            paneHeight = completeHeight;
            paneYPos = this.getHeight() - completeHeight;
        }

        scrollPane.setBounds(0, paneYPos, this.getWidth(), paneHeight);
    }

    @Override
    public void pack() {
        needsLayout = true;
        layout();
    }


    private ListViewItem.OnDrawListener onDrawListener = new ListViewItem.OnDrawListener() {
        @Override
        public void onDraw(ListViewItem item) {
            if (adapter != null) {
                if (selectionType != NONE) {
                    boolean isSelected = false;
                    if (selectionType == SINGLE) {
                        isSelected = selectedItemList.size == 1 && selectedItemList.contains(item, false);
                    } else {
                        isSelected = selectedItemList.contains(item, false);
                    }

                    if (isSelected) {
                        item.setBackground(style.selectedItem);
                    } else {
                        if (style.secondItem != null && item.getListIndex() % 2 == 1) {
                            item.setBackground(style.secondItem);
                        } else {
                            item.setBackground(style.firstItem);
                        }
                    }

                    //add ClickListener
                    item.addListener(onListItemClickListener);
                }
                adapter.update(item);
            }
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


    ClickListener onListItemClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (event.getType() == InputEvent.Type.touchUp) {
                if (selectionType != NONE) {
                    ListViewItem item = ((ListViewItem) event.getListenerActor());

                    if (selectionType == SINGLE) {
                        if (!selectedItemList.contains(item, false)) {
                            selectedItemList.clear();
                            selectedItemList.add(item);
                        }
                    } else {
                        if (selectedItemList.contains(item, false)) {
                            selectedItemList.removeValue(item, true);
                        } else {
                            selectedItemList.add(item);
                        }
                    }
                    Gdx.graphics.requestRendering();

                    //call selection changed event
                    for (int i = 0, n = changedEventListeners.size; i < n; i++) {
                        changedEventListeners.get(i).selectionChanged();
                    }
                }
            }
        }
    };


    public float getScrollPos() {
        return scrollPane.getScrollY();
    }

    public void setScrollPos(float scrollPos) {
        scrollPane.setScrollY(scrollPos);
    }

    public void setSelectedItemVisible() {
        //get pos of first selected
        ListViewItem item = this.selectedItemList.size == 0 ? null : this.selectedItemList.get(0);
        float scrollPos = 0;
        if (item != null) {
            int index = item.getListIndex();
            scrollPos = completeHeight - (itemYPos.get(index) + item.getHeight());
        }
        this.setScrollPos(scrollPos);
    }

    public void setSelection(int index) {
        if (this.selectionType == NONE) return;
        this.selectedItemList.clear();
        ListViewItem item = itemViews.get(index);
        this.selectedItemList.add(item);
        Gdx.graphics.requestRendering();
    }

    public ListViewItem getSelectedItem() {
        if (this.selectedItemList.size == 0) return null;
        return this.selectedItemList.first();
    }

    public void dataSetChanged() {
        layout(true);
        Gdx.graphics.requestRendering();
    }


    public static class ListViewStyle extends ScrollPane.ScrollPaneStyle {
        public Drawable firstItem, secondItem, selectedItem;
        public float pad, padLeft, padRight, padTop, padBottom;
    }
}
