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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.ListViewStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.CircularProgressWidget;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_WidgetGroup;
import de.longri.cachebox3.utils.CB_RectF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.HORIZONTAL;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectionType.NONE;
import static de.longri.cachebox3.gui.widgets.list_view.SelectionType.SINGLE;

/**
 * Created by Longri on 03.02.18.
 */
public class ListView extends Catch_WidgetGroup {

    private final static Logger log = LoggerFactory.getLogger(ListView.class);
    public final ListViewStyle listViewStyle;
    protected final ListViewType listViewType;
    protected final ListViewItemLinkedList itemList;
    final VisScrollPane scrollPane;
    final CB_RectF tempClickRec = new CB_RectF();
    private final boolean canDisposeItems;
    private final Array<SelectionChangedEvent> changedEventListeners = new Array<>();
    private final Array<ListViewItemInterface> selectedItemList = new Array<>();
    private final ClickLongClickListener scrollpaneCaptureListener = new ClickLongClickListener() {
        @Override
        public boolean clicked(InputEvent event, float x, float y) {
            log.debug("ListViewItem clicked on x:{}  y:{}", x, y);
            SnapshotArray<Actor> childs = ((ListViewItemLinkedList) scrollPane.getChildren().get(0)).getChildren();

            for (int i = 0, n = childs.size; i < n; i++) {
                if (!(childs.get(i) instanceof ListViewItem)) continue; // DummyListViewItem can't be cast to ListViewItem
                ListViewItem item = (ListViewItem) childs.get(i);
                Vector2 vec = item.localToStageCoordinates(new Vector2());
                tempClickRec.set(vec.x, vec.y, item.getWidth(), item.getHeight());
                if (tempClickRec.contains(event.getStageX(), event.getStageY())) {

                    event.setListenerActor(item);

                    // item Clicked
                    log.debug("ListViewItem {} clicked | item => {}", i, item.toString());
                    Array<EventListener> listeners = item.getListeners();
                    boolean handled = false;
                    for (EventListener listener : listeners) {
                        if (listener instanceof ClickListener) {
                            ((ClickListener) listener).clicked(event, x, y);
                        } else if (listener instanceof ClickLongClickListener) {
                            if (((ClickLongClickListener) listener).clicked(event, x, y)) {
                                event.cancel();
                                handled = true;
                            }
                        }
                    }
                    return handled;
                }
            }
            return false;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
            log.debug("ListViewItem longClicked on x:{}  y:{}", x, y);
            SnapshotArray<Actor> childs = ((ListViewItemLinkedList) scrollPane.getChildren().get(0)).getChildren();
            for (int i = 0, n = childs.size; i < n; i++) {
                ListViewItem item = (ListViewItem) childs.get(i);
                Vector2 vec = item.localToStageCoordinates(new Vector2());
                tempClickRec.set(vec.x, vec.y, item.getWidth(), item.getHeight());
                if (tempClickRec.contains(touchDownStageX, touchDownStageY)) {
                    item.setBackground(ListView.this.listViewStyle.selectedItem);
                    // item Clicked
                    log.debug("ListViewItem {} LongClicked", i);
                    Array<EventListener> listeners = item.getListeners();
                    boolean handled = false;
                    for (EventListener listener : listeners) {
                        if (listener instanceof ClickLongClickListener) {
                            if (((ClickLongClickListener) listener).longClicked(actor, x, y, touchDownStageX, touchDownStageY)) {
                                handled = true;
                            }
                        }
                    }
                    return handled;
                }
            }
            return false;
        }
    };
    CircularProgressWidget circPro;
    Array<EventListener> originalCapturelistener = new Array<>();
    boolean isDisabled = false;
    private float maxScrollChange = 0;
    private SelectionType selectionType;
    private final ClickLongClickListener onListItemClickListener = new ClickLongClickListener() {
        public boolean clicked(InputEvent event, float x, float y) {
            if (event.isCancelled()) return true;
            if (event.getType() == InputEvent.Type.touchUp) {
                if (selectionType != NONE) {
                    ListViewItem item = ((ListViewItem) event.getListenerActor());

                    if (selectionType == SINGLE) {
                        if (!selectedItemList.contains(item, false)) {
                            if (selectedItemList.size > 0) {
                                ListViewItemInterface actSelected = selectedItemList.pop();
                                actSelected.setSelected(false);
                            }
                            selectedItemList.add(item);
                            item.setSelected(true);
                        }
                    } else {
                        if (selectedItemList.contains(item, true)) {
                            selectedItemList.removeValue(item, true);
                            item.setSelected(false);
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
            return true;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
            return false;
        }
    };
    private ListViewAdapter adapter;
    private Drawable backgroundDrawable;
    private float lastFiredScrollX = 0;
    private float lastFiredScrollY = 0;
    private long frameID = Long.MIN_VALUE;
    private VisLabel emptyLabel;
    private ScrollChangedEvent scrollChangedEventListener;
    private float lastScrollX = -1;
    private float lastScrollY = -1;
    private float preferredWidth = 0;
    private float preferredHeight = 0;

    public ListView(ListViewType _listViewType) {
        this(_listViewType, VisUI.getSkin().get(ListViewStyle.class), true);
    }

    public ListView(ListViewType _listViewType, ListViewStyle _listViewStyle) {
        this(_listViewType, _listViewStyle, true);
    }

    public ListView(ListViewType _listViewType, boolean _canDisposeItems) {
        this(_listViewType, VisUI.getSkin().get(ListViewStyle.class), _canDisposeItems);
    }

    public ListView(ListViewType _listViewType, ListViewStyle _listViewStyle, boolean _canDisposeItems) {

        if (_listViewStyle == null) throw new RuntimeException("style can't be NULL");

        canDisposeItems = _canDisposeItems;
        listViewType = _listViewType;
        listViewStyle = _listViewStyle;
        itemList = new ListViewItemLinkedList(_listViewType, _listViewStyle,
                CB.getScaledFloat(_listViewStyle.pad > 0 ? _listViewStyle.pad : _listViewStyle.padLeft),
                CB.getScaledFloat(_listViewStyle.pad > 0 ? _listViewStyle.pad : _listViewStyle.padRight),
                CB.getScaledFloat(_listViewStyle.pad > 0 ? _listViewStyle.pad : _listViewStyle.padTop),
                CB.getScaledFloat(_listViewStyle.pad > 0 ? _listViewStyle.pad : _listViewStyle.padBottom),
                this.canDisposeItems) {
            @Override
            public void sizeChanged() {
                CB.postOnNextGlThread(() -> setScrollPaneBounds());
            }

        };
        selectionType = SINGLE;
        OnDrawListener onDrawListener = item -> {

            if (adapter != null && selectionType != NONE && !Gdx.input.isTouched()) {
                boolean isSelected;

                if (selectionType == SINGLE) {
                    isSelected = selectedItemList.size == 1 && selectedItemList.contains(item, true);
                } else {
                    isSelected = selectedItemList.contains(item, true);
                }

                if (isSelected) {
                    item.setBackground(ListView.this.listViewStyle.selectedItem);
                } else {
                    if (ListView.this.listViewStyle.secondItem != null && item.getListIndex() % 2 == 1) {
                        item.setBackground(ListView.this.listViewStyle.secondItem);
                    } else {
                        item.setBackground(ListView.this.listViewStyle.firstItem);
                    }
                }

                //add ClickListener
                item.addListener(onListItemClickListener);
            }
            try {
                if (adapter != null)
                    adapter.update(item);
            } catch (Exception e) {
                log.error("Update:", e);
            }
        };
        this.itemList.setOnDrawListener(onDrawListener);
        scrollPane = new VisScrollPane(itemList, _listViewStyle) {


            @Override
            public Actor hit(float x, float y, boolean touchable) {
                Actor actor = super.hit(x, y, touchable);
                if (actor == scrollPane) {
                    actor = itemList.hit(x, (itemList.getHeight() - (scrollPane.getScrollY() + scrollPane.getHeight())) + y, touchable);
                }
                return actor;
            }


            @Override
            public void sizeChanged() {
                super.sizeChanged();
                if (ListView.this.listViewType == VERTICAL) {
                    float width = ListView.this.getWidth();
                    if (ListView.this.backgroundDrawable != null) {
                        width -= ListView.this.backgroundDrawable.getLeftWidth() + ListView.this.backgroundDrawable.getRightWidth();
                    }
                    itemList.setWidth(width);
                } else {
                    float height = ListView.this.getHeight();
                    if (ListView.this.backgroundDrawable != null) {
                        height -= ListView.this.backgroundDrawable.getTopHeight() + ListView.this.backgroundDrawable.getBottomHeight();
                    }
                    itemList.setHeight(height);
                }
            }

            /** If currently scrolling by tracking a touch down, stop scrolling. */
            public void cancel() {
                super.cancel();
                log.debug("ScrollPane.cancel()");
            }
        };
        scrollPane.addCaptureListener(scrollpaneCaptureListener);
        if (this.listViewType == VERTICAL) {
            scrollPane.setOverscroll(false, true);
        } else {
            scrollPane.setOverscroll(true, false);
        }
        scrollPane.setFlickScroll(true);
        scrollPane.setVariableSizeKnobs(false);
        scrollPane.setCancelTouchFocus(true);
        scrollPane.setupFadeScrollBars(1f, 0.5f);

    }

    public void setPreferredWidth(float _width) {
        preferredWidth = _width;}

    public void setPreferredHeight(float _height) {
        preferredHeight = _height;}

    public float getPrefWidth () {
        return preferredWidth;
    }

    public float getPrefHeight () {
        return preferredHeight;
    }


    protected void setScrollingDisabled(boolean value, EventListener inputListener) {
        if (value) {
            if (isDisabled) return;
            isDisabled = true;
            scrollPane.setScrollingDisabled(true, true);

//            for (EventListener listener : scrollPane.getCaptureListeners()) {
//                originalCapturelistener.add(listener);
//            }
//
//            for (EventListener listener : originalCapturelistener) {
//                scrollPane.removeCaptureListener(listener);
//            }
//            scrollPane.addCaptureListener(inputListener);

//            scrollPane.setFlickScroll(false);
        } else {
            if (!isDisabled) return;
            isDisabled = false;
            scrollPane.setScrollingDisabled(this.listViewType == VERTICAL, this.listViewType == HORIZONTAL);
            scrollPane.setScrollingDisabled(false, false);
            setScrollPaneBounds();
//            scrollPane.removeCaptureListener(inputListener);
//            for (EventListener listener : originalCapturelistener) {
//                scrollPane.addCaptureListener(listener);
//            }
//            for (EventListener listener : scrollPane.getCaptureListeners()) {
//                originalCapturelistener.removeValue(listener, true);
//            }
//            originalCapturelistener.clear();
//            scrollPane.setFlickScroll(true);
        }
        log.debug("isScrollingDisabledX: {} / isScrollingDisabledY:{}", scrollPane.isScrollingDisabledX(), scrollPane.isScrollingDisabledY());
    }

    public void setAdapter(ListViewAdapter adapter) {
        frameID = Gdx.graphics == null ? frameID++ : Gdx.graphics.getFrameId();
        this.adapter = adapter;

        if (this.adapter == null || this.adapter.getCount() <= 0) {
            this.removeActor(scrollPane);
            if (emptyLabel != null) this.addActor(emptyLabel);
        } else {
            if (circPro != null) this.removeActor(circPro);
            circPro = null;
            if (emptyLabel != null) this.removeActor(emptyLabel);
            this.addActor(scrollPane);
            itemList.setAdapter(adapter);
            setScrollPaneBounds();
        }
    }

    @Override
    public void layout() {
        scrollPane.layout();
    }

    private void setItemVisibleBounds() {
        if (this.listViewType == VERTICAL) {
            itemList.setVisibleBounds(scrollPane.getScrollY(), scrollPane.getHeight());
        } else {
            itemList.setVisibleBounds(scrollPane.getScrollX(), scrollPane.getWidth());
        }
    }

    @Override
    protected void sizeChanged() {
        if (scrollPane != null) {
            maxScrollChange = listViewType == VERTICAL ? this.getHeight() / 4 : this.getWidth() / 4;
            setScrollPaneBounds();
        } else {
            invalidate();
            layout();
        }
    }

    private void setScrollPaneBounds() {
        if (circPro != null) {
            float x = (this.getWidth() - circPro.getWidth()) / 2;
            float y = (this.getHeight() - circPro.getHeight()) / 2;
            circPro.setPosition(x, y);
        }

        if (emptyLabel != null) {
            float labelHeight = this.getHeight();
            float labelWidth = this.getWidth();
            float labelX = 0, labelY = 0;

            if (this.backgroundDrawable != null) {
                labelWidth -= this.backgroundDrawable.getLeftWidth() + this.backgroundDrawable.getRightWidth();
                labelHeight -= this.backgroundDrawable.getBottomHeight() + this.backgroundDrawable.getTopHeight();
                labelX = this.backgroundDrawable.getLeftWidth();
                labelY = this.backgroundDrawable.getBottomHeight();
            }
            emptyLabel.setBounds(labelX, labelY, labelWidth, labelHeight);
        }

        float paneHeight = this.getHeight();
        float paneWidth = this.getWidth();

        if (this.backgroundDrawable != null) {
            paneWidth -= this.backgroundDrawable.getLeftWidth() + this.backgroundDrawable.getRightWidth();
            paneHeight -= this.backgroundDrawable.getBottomHeight() + this.backgroundDrawable.getTopHeight();
        }
        float paneYPos = this.backgroundDrawable != null ? this.backgroundDrawable.getTopHeight() : 0;
        float completeSize = itemList.getCompleteSize();
        if (this.listViewType == VERTICAL) {
            if (this.getHeight() > completeSize) {
                //set on Top
                paneHeight = completeSize;
                paneYPos = this.getHeight() - completeSize;
            }
        } else {
            if (this.getWidth() > completeSize) {
                //set on Top
                paneWidth = completeSize;
            }
        }

        scrollPane.setBounds(this.backgroundDrawable != null ? this.backgroundDrawable.getLeftWidth() : 0, paneYPos,
                paneWidth, paneHeight);
        scrollPane.layout();
        setItemVisibleBounds();
    }

    public float getScrollPos() {
        if (this.listViewType == VERTICAL) {
            return scrollPane.getScrollY();
        }
        return scrollPane.getScrollX();
    }

    public void setScrollPos(float scrollPos) {
        this.setScrollPos(scrollPos, true);
    }

    public void setScrollPos(float scrollPos, boolean withAnimation) {
        if (scrollPane != null) {
            if (this.listViewType == VERTICAL) {
                scrollPane.setScrollY(scrollPos);
            } else {
                scrollPane.setScrollX(scrollPos);
            }

            if (!withAnimation) scrollPane.updateVisualScroll();
        }
        setItemVisibleBounds();

    }

    public void addSelectionChangedEventListner(SelectionChangedEvent event) {
        if (!changedEventListeners.contains(event, true))
            changedEventListeners.add(event);
    }

    public void removeSelectionChangedEventListner(SelectionChangedEvent event) {
        changedEventListeners.removeValue(event, true);
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(SelectionType selectionType) {
        this.selectionType = selectionType;
    }

    /*
    public void setSelection(ListViewItemInterface item) {
        if (item == null) return;
        this.selectedItemList.add(item);
        item.setSelected(true);
        CB.requestRendering();
    }
     */

    public void setSelection(int index) {

        if (frameID == (Gdx.graphics == null ? frameID++ : Gdx.graphics.getFrameId())) {
            throw new RuntimeException("Adapter is set on this frame, call selection later like:\n " +
                    "            CB.postOnNextGlThread(new Runnable() {\n" +
                    "                @Override\n" +
                    "                public void run() {\n" +
                    "                    listView.setSelection(index);\n" +
                    "                }\n" +
                    "            });");
        }

        if (this.selectionType == NONE) return;
        log.debug("Set selected item to index {}", index);
        this.selectedItemList.clear();
        ListViewItemInterface item = itemList.getItem(index);
        if (item == null) return;
        this.selectedItemList.add(item);
        item.setSelected(true);
        CB.requestRendering();
    }

    public ListViewItemInterface getSelectedItem() {
        if (this.selectedItemList.size == 0) return null;
        return this.selectedItemList.first();
    }

    public Array<ListViewItemInterface> getSelectedItems() {
        if (this.selectedItemList.size == 0) return null;
        return this.selectedItemList;
    }

    public void setSelectedItemVisible(boolean withScroll) {

        if (scrollPane == null) return;

        //get pos of first selected
        ListViewItemInterface item = this.selectedItemList.size == 0 ? null : this.selectedItemList.get(0);
        float scrollPos = 0;
        if (item != null) {
            int index = item.getListIndex() - 1;
            scrollPos = index < 0 ? 0 : itemList.getCompleteSize() - ((listViewType == VERTICAL) ? (item.getY() + item.getHeight()) : (item.getX() + item.getWidth()));
        }
        this.setScrollPos(scrollPos);
        if (!withScroll) scrollPane.updateVisualScroll();
        CB.requestRendering();
        if (item != null) log.debug("Scroll to selected item {} at position {}", item.getListIndex(), scrollPos);
    }

    public void setBackground(Drawable background) {
        this.backgroundDrawable = background;
    }

    public void draw(Batch batch, float parentAlpha) {
        if (this.backgroundDrawable != null) {
            backgroundDrawable.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        if (getStage() != null) super.draw(batch, parentAlpha);
    }

    public void setScrollChangedListener(ScrollChangedEvent listener) {
        this.scrollChangedEventListener = listener;
    }

    @Override
    public void act(float delta) {
        try {
            super.act(delta);
        } catch (Exception e) {
            log.error("act()", e);
        }

        if (this.scrollChangedEventListener != null) {
            boolean scrollChanged = false;
            if (lastScrollX != scrollPane.getScrollX()) {
                scrollChanged = true;
                lastScrollX = scrollPane.getScrollX();
            }

            if (lastScrollY != scrollPane.getScrollY()) {
                scrollChanged = true;
                lastScrollY = scrollPane.getScrollY();
            }

            if (scrollChanged) {
                this.scrollChangedEventListener.scrollChanged(lastScrollX, lastScrollY);
            }
        }

        float scroll;
        if (listViewType == VERTICAL) {
            scroll = scrollPane.getScrollY();
            if (Math.abs(lastFiredScrollY - scroll) > maxScrollChange) {
                lastFiredScrollY = scroll;
                setItemVisibleBounds();
            }
        } else {
            scroll = scrollPane.getScrollX();
            if (Math.abs(lastFiredScrollX - scroll) > maxScrollChange) {
                lastFiredScrollX = scroll;
                setItemVisibleBounds();
            }
        }

    }

    public void setEmptyString(CharSequence emptyString) {
        if (emptyString == null || emptyString.length() == 0) {
            this.removeActor(this.emptyLabel);
            this.emptyLabel = null;
            return;
        }
        CB.assertGlThread();
        this.emptyLabel = new VisLabel(emptyString);
        emptyLabel.setWrap(true);
        emptyLabel.setAlignment(Align.center);
    }

    public SnapshotArray<Actor> items() {
        return itemList.getChildren();
    }

    public void dispose() {
        //TODO implement
    }

    public void dataSetChanged() {
        CB.postOnNextGlThread(() -> setAdapter(ListView.this.adapter));
    }

    public void showWorkAnimationUntilSetAdapter() {
        circPro = new CircularProgressWidget();
        circPro.setProgress(-1);
        this.addActor(circPro);
    }

    public ListViewItemInterface getListItem(int idx) {
        if (this.itemList.itemArray == null || this.itemList.itemArray.length <= idx) return null;
        return this.itemList.itemArray[idx];
    }
}
