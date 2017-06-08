/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.utils.CB_RectF;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.views.listview.ListView.SelectableType.NONE;
import static de.longri.cachebox3.gui.views.listview.ListView.SelectableType.SINGLE;


/**
 * Created by Longri on 12.08.2016.
 */
public class ListView extends WidgetGroup {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(ListView.class);

    private final static int OVERLOAD = 5;
    final CB_RectF tempClickRec = new CB_RectF();
    private final ListView.ListViewStyle style;
    private final Array<ListViewItem> selectedItemList = new Array<ListViewItem>();
    private final SnapshotArray<ListViewItem> itemViews = new SnapshotArray<ListViewItem>();
    private final SnapshotArray<ListViewItem> clearList = new SnapshotArray<ListViewItem>();
    private final ScrollViewContainer itemGroup = new ScrollViewContainer();
    private final IntArray drawedIndexList = new IntArray();
    private final IntArray indexList = new IntArray();
    private final FloatArray itemHeights = new FloatArray();
    private final FloatArray itemYPos = new FloatArray();
    private final boolean dontDisposeItems;

    private VisScrollPane scrollPane;
    private Drawable backgroundDrawable;
    private Adapter adapter;
    private SelectableType selectionType = NONE;
    private float completeHeight, padLeft, padRight, padTop, padBottom;
    private int listCount, lastDrawedIndex, first, last, begin, end, idx, min, max;
    private boolean mustAdd = false;
    private boolean needsLayout = true;

    private final static int SAME_HEIGHT_INITIAL_COUNT = 10;
    private final boolean itemsHaveSameHeight;

    public SnapshotArray<ListViewItem> items() {
        return itemViews;
    }

    public ListView() {
        this(false);
    }

    public ListView(boolean dontDisposeItems) {
        this(VisUI.getSkin().get("default", ListView.ListViewStyle.class), dontDisposeItems, false);
    }

    public ListView(Adapter listViewAdapter) {
        this(listViewAdapter, false);
    }

    public ListView(Adapter listViewAdapter, boolean dontDisposeItems) {
        this(VisUI.getSkin().get("default", ListView.ListViewStyle.class), dontDisposeItems, false);
        this.adapter = listViewAdapter;
        this.listCount = adapter.getCount();
    }

    public ListView(Adapter listViewAdapter, boolean dontDisposeItems, boolean itemsHaveSameHeight) {
        this(VisUI.getSkin().get("default", ListView.ListViewStyle.class), dontDisposeItems, itemsHaveSameHeight);
        this.adapter = listViewAdapter;
        this.listCount = adapter.getCount();
    }

    private ListView(ListView.ListViewStyle style, boolean dontDisposeItems, boolean itemsHaveSameHeight) {
        this.style = style;
        this.dontDisposeItems = dontDisposeItems;
        this.itemsHaveSameHeight = itemsHaveSameHeight;
        setLayoutEnabled(true);

        itemGroup.addCaptureListener(captureListener);
    }

    final ClickLongClickListener captureListener = new ClickLongClickListener() {
        @Override
        public boolean clicked(InputEvent event, float x, float y) {
            log.debug("ListView clicked on x:{}  y:{}", x, y);
            SnapshotArray<Actor> childs = itemGroup.getChildren();

            for (int i = 0, n = childs.size; i < n; i++) {
                ListViewItem item = (ListViewItem) childs.get(i);
                tempClickRec.set(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                if (tempClickRec.contains(x, y)) {
                    // item Clicked
                    log.debug("ListViewItem {} clicked", item.getListIndex());

                    Array<EventListener> listeners = item.getListeners();
                    for (int j = 0, m = listeners.size; j < m; j++) {
                        EventListener listener = listeners.get(j);

                        //change Event Actor
                        event.setListenerActor(item);

                        if (listener instanceof ClickLongClickListener) {
                            if(((ClickLongClickListener) listener).clicked(event, x, y)){
                                break;
                            }
                        } else if (listener instanceof ClickListener) {
                            ((ClickListener) listener).clicked(event, x, y);
                        }
                    }
                    event.reset();
                    event.cancel();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y) {
            log.debug("ListView LongClicked on x:{}  y:{}", x, y);
            SnapshotArray<Actor> childs = itemGroup.getChildren();
            for (int i = 0, n = childs.size; i < n; i++) {
                ListViewItem item = (ListViewItem) childs.get(i);
                tempClickRec.set(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                if (tempClickRec.contains(x, y)) {
                    // item Clicked
                    log.debug("ListViewItem {} LongClicked", item.getListIndex());
                    Array<EventListener> listeners = item.getListeners();
                    for (int j = 0, m = listeners.size; j < m; j++) {
                        EventListener listener = listeners.get(j);
                        if (listener instanceof ClickLongClickListener) {
                            return ((ClickLongClickListener) listener).longClicked(item, x, y);
                        }
                    }
                    return false;
                }
            }
            return false;
        }
    };

    public synchronized void dispose() {

        Object[] tmpItems = itemViews.begin();
        for (int i = 0, n = tmpItems.length; i < n; i++) {
            ListViewItem item = (ListViewItem) tmpItems[i];
            if (item instanceof Disposable) {
                item.dispose();
            }
        }
        itemViews.end();

        itemViews.clear();
        clearList.clear();
        selectedItemList.clear();
        indexList.clear();
        drawedIndexList.clear();
        adapter = null;
        backgroundDrawable = null;
        if (scrollPane != null) {
            scrollPane.clearActions();
            scrollPane.clearListeners();
            scrollPane.clearChildren();
            scrollPane.clear();
            scrollPane = null;
        }

        Object[] childrens = itemGroup.getChildren().begin();
        for (int i = 0, n = childrens.length; i < n; i++) {
            Actor item = (Actor) childrens[i];
            itemGroup.removeActor(item);
            if (item instanceof Disposable) {
                ((Disposable) item).dispose();
            }
        }
        itemGroup.getChildren().end();
        itemGroup.clearChildren();
        itemGroup.clearActions();
        itemGroup.clearListeners();
        itemGroup.clear();
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

    public void setAdapter(Adapter listViewAdapter) {
        this.adapter = listViewAdapter;
        this.listCount = adapter.getCount();
        layout(true);
    }

    public void setSelectable(SelectableType selectionType) {
        this.selectionType = selectionType;
    }

    public void setBackground(Drawable background) {
        this.backgroundDrawable = background;
    }


    public synchronized void layout(boolean force) {
        if (force) needsLayout = true;
        layout();
    }


    private boolean atWork = false;


    @Override
    public synchronized void layout() {
        if (atWork) return;
        atWork = true;


        if (!this.needsLayout) {
            super.layout();
            atWork = false;
            return;
        }

        if (adapter == null) return;
        log.debug("Start Layout Items");
        this.clear();
        itemHeights.clear();
        itemYPos.clear();
        itemViews.clear();


        itemGroup.setWidth(this.getWidth());
        itemGroup.clear();

        padLeft = CB.getScaledFloat(style.padLeft > 0 ? style.padLeft : style.pad);
        padRight = CB.getScaledFloat(style.padRight > 0 ? style.padRight : style.pad);
        padTop = CB.getScaledFloat(style.padTop > 0 ? style.padTop : style.pad);
        padBottom = CB.getScaledFloat(style.padBottom > 0 ? style.padBottom : style.pad);


        completeHeight = 0;

        if (itemsHaveSameHeight) {

            //initial with only 10 items, for speedup initial
            for (int i = 0; i < Math.min(SAME_HEIGHT_INITIAL_COUNT, this.listCount); i++) {
                addItem(i, false, -1);
            }


            float itemHeight = itemHeights.items[0];
            completeHeight = itemHeight;
            for (int i = 0; i < this.listCount; i++) {
                if (i < Math.min(SAME_HEIGHT_INITIAL_COUNT, this.listCount)) {
                    completeHeight += itemHeights.items[i];
                } else {
                    completeHeight += itemHeight;
                    itemHeights.add(itemHeight);
                }

            }
        } else {
            for (int i = 0; i < this.listCount; i++) {
                addItem(i, false, -1);
            }
            //layout itemGroup
            for (int i = 0; i < this.listCount; i++) { //calculate complete height of all Items
                completeHeight += itemHeights.items[i];
            }
        }


        itemGroup.setWidth(this.getWidth());
        itemGroup.setHeight(completeHeight);
        itemGroup.setPrefWidth(this.getWidth());
        itemGroup.setPrefHeight(completeHeight);
        itemGroup.addCaptureListener(captureListener);
        float yPos = completeHeight;

        Actor[] actors = itemGroup.getChildren().items;
        for (int i = 0; i < this.listCount; i++) {// calculate Y position of all Items
            yPos -= itemHeights.get(i);
            itemYPos.add(yPos);
            if (itemsHaveSameHeight && i < Math.min(SAME_HEIGHT_INITIAL_COUNT, this.listCount)) {
                actors[i].setBounds(padLeft, yPos, this.getWidth() - (padLeft + padRight), itemHeights.get(i) - (padTop + padBottom));
            } else if (!itemsHaveSameHeight) {
                actors[i].setBounds(padLeft, yPos, this.getWidth() - (padLeft + padRight), itemHeights.get(i) - (padTop + padBottom));
            }

        }

        scrollPane = new VisScrollPane(itemGroup, style);
        scrollPane.setOverscroll(false, true);
        scrollPane.setFlickScroll(true);
        scrollPane.setVariableSizeKnobs(false);
        setScrollPaneBounds();

        if (this.getChildren().size > 0) throw new RuntimeException("Childs not Empty");

        this.addActor(scrollPane);
        scrollPane.layout();
        needsLayout = false;
        atWork = false;
        log.debug("Finish Layout Items");
    }

    private void addItem(final int index, final boolean reAdd, final float yPos) {

        if (CB.isMainThread()) {
            addItemThreadSave(index, reAdd, yPos);
        } else {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    addItemThreadSave(index, reAdd, yPos);
                }
            });
        }
    }

    protected void addItemThreadSave(final int index, boolean reAdd, final float yPos) {

        synchronized (indexList) {

            // set Item background
            ListViewItem view = adapter.getView(index);
            if (view != null) {
                if (style.secondItem != null && index % 2 == 1) {
                    view.setBackground(style.secondItem);
                } else {
                    view.setBackground(style.firstItem);
                }
            } else {
                // add a empty table
                view = new ListViewItem(index) {
                    @Override
                    public void dispose() {
                    }
                };
            }

            //set on drawListner
            view.setOnDrawListener(onDrawListener);
//            view.addListener(onListItemClickListener);
            view.setPrefWidth(this.getWidth() - (padLeft + padRight));
            view.pack();
            view.layout();
            itemViews.add(view);
            itemGroup.addActor(view);
            indexList.add(index);

            // set the position of this item
            if (yPos >= 0) view.setPosition(0, yPos);

            if (!reAdd) itemHeights.add(view.getHeight() + padBottom + padTop);
        }
    }

    @Override
    protected void sizeChanged() {
        if (scrollPane != null) {
            setScrollPaneBounds();
        } else {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    needsLayout = true;
                    layout();
                }
            });

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

            //register item as drawed
            drawedIndexList.add(item.getListIndex());


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
                try {
                    adapter.update(item);
                } catch (Exception e) {
                    log.error("Update:", e);
                }
            }
        }
    };


    public void draw(Batch batch, float parentAlpha) {

        synchronized (indexList) {

            if (listCount != adapter.getCount()) {
                //adapter has changed!
                log.debug("List count has changed! set Adapter new!");
                setAdapter(this.adapter);
            }


            if (scrollPane == null) return;

            drawedIndexList.clear();

            if (this.backgroundDrawable != null) {
                backgroundDrawable.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
            synchronized (this) {
                super.draw(batch, parentAlpha);
            }

            if (dontDisposeItems || this.listCount <= OVERLOAD) return;


            first = 0;
            last = 0;
            begin = 0;
            end = 0;
            mustAdd = false;
            if (drawedIndexList.size > 0) {

                drawedIndexList.sort();

                first = drawedIndexList.first();
                last = drawedIndexList.items[drawedIndexList.size - 1];

                //grow up before
                begin = first - OVERLOAD;
                for (int i = first - 1; i > begin; i--) {
                    if (i < 0) break;
                    drawedIndexList.add(i);
                }

                //grow up after
                end = last + OVERLOAD;
                for (int i = last + 1; i <= end; i++) {
                    if (i >= this.listCount) break;
                    drawedIndexList.add(i);
                }

                //remove non drawed items
                for (ListViewItem item : itemViews) {
                    if (drawedIndexList.indexOf(item.getListIndex()) == -1) {
                        clearList.add(item);
                    }
                }

                for (ListViewItem clearItem : clearList) {
                    itemViews.removeValue(clearItem, true);
                    itemGroup.removeActor(clearItem);
                    indexList.removeValue(clearItem.getListIndex());
                    clearItem.dispose();
                }
                clearList.clear();
            } else {
                // fill drawedIndexList over the scroll position

                float pos = completeHeight - scrollPane.getScrollY();
                idx = -1;
                for (int i = 0, n = itemYPos.size; i < n; i++) {
                    if (pos > itemYPos.get(i)) {
                        idx = i;
                        break;
                    }
                }


                min = idx - OVERLOAD;
                max = idx + OVERLOAD;

                if (min < 0) min = 0;
                if (max > this.listCount) max = this.listCount;

                for (; min < max; min++)
                    drawedIndexList.add(min);

                mustAdd = true;
                last = idx;
            }


            //sometimes indexList is not correct after fast scroll, so we check this
            if (itemGroup.getChildren().size != indexList.size) {
                log.debug("ListIndexList must new");
                indexList.clear();
                for (Actor item : itemGroup.getChildren().begin()) {
                    if (item == null) continue;
                    indexList.add(((ListViewItem) item).listIndex);
                }
            }


            //check for scroll changes and add any item
            if (mustAdd || lastDrawedIndex != last) {
                lastDrawedIndex = last;
                for (int idx : drawedIndexList.toArray()) {
                    if (!indexList.contains(idx)) {
                        // add the Item
                        addItem(idx, true, itemYPos.items[idx]);
                    }
                }
            }
        }
    }


    ClickLongClickListener onListItemClickListener = new ClickLongClickListener() {
        public boolean clicked(InputEvent event, float x, float y) {
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
                    CB.requestRendering();

                    //call selection changed event
                    for (int i = 0, n = changedEventListeners.size; i < n; i++) {
                        changedEventListeners.get(i).selectionChanged();
                    }
                }
            }
            return false;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y) {
            return false;
        }
    };

    public float getScrollPos() {
        return scrollPane.getScrollY();
    }

    public void setScrollPos(float scrollPos) {
        if (scrollPane != null) scrollPane.setScrollY(scrollPos);
        CB.requestRendering();
    }

    public void setSelectedItemVisible() {
        //get pos of first selected
        ListViewItem item = this.selectedItemList.size == 0 ? null : this.selectedItemList.get(0);
        float scrollPos = 0;
        if (itemYPos.size < adapter.getCount())
            layout();
        if (item != null) {
            int index = item.getListIndex() - 1;
            scrollPos = index < 0 ? 0 : completeHeight - (itemYPos.get(index) + item.getHeight());
        }
        this.setScrollPos(scrollPos);
        if (item != null) log.debug("Scroll to selected item {} at position {}", item.getListIndex(), scrollPos);
    }

    public void setSelection(int index) {
        if (this.selectionType == NONE) return;
        log.debug("Set selected item to index {}", index);
        this.selectedItemList.clear();
        ListViewItem item = adapter.getView(index);
        this.selectedItemList.add(item);
        CB.requestRendering();
    }

    public ListViewItem getSelectedItem() {
        if (this.selectedItemList.size == 0) return null;
        return this.selectedItemList.first();
    }

    public void dataSetChanged() {
        layout(true);
        CB.requestRendering();
    }

    public static class ListViewStyle extends ScrollPane.ScrollPaneStyle {
        public Drawable firstItem, secondItem, selectedItem;
        public float pad, padLeft, padRight, padTop, padBottom;
    }
}
