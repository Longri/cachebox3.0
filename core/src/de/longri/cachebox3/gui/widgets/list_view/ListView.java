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
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.ListViewStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.NONE;
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.SINGLE;

/**
 * Created by Longri on 03.02.18.
 */
public class ListView extends WidgetGroup {

    private final static Logger log = LoggerFactory.getLogger(ListView.class);

    private final ListViewType type;
    final VisScrollPane scrollPane;
    private final ListViewStyle style;
    private final ListViewItemLinkedList itemList;
    private final Array<SelectionChangedEvent> changedEventListeners = new Array<>();
    private final Array<ListViewItemInterface> selectedItemList = new Array<>();
    final CB_RectF tempClickRec = new CB_RectF();
    private float maxScrollChange = 0;
    private SelectableType selectionType;
    private ListViewAdapter adapter;
    private Drawable backgroundDrawable;
    private float lastFiredScrollX = 0;
    private float lastFiredScrollY = 0;
    private long frameID = Long.MIN_VALUE;


    public ListView(ListViewType type) {
        this(type, VisUI.getSkin().get("default", ListViewStyle.class));
    }

    public ListView(ListViewType type, ListViewStyle style) {
        this.type = type;
        this.style = style;
        this.itemList = new ListViewItemLinkedList(type, style,
                CB.getScaledFloat(style.pad > 0 ? style.pad : style.padLeft),
                CB.getScaledFloat(style.pad > 0 ? style.pad : style.padRight),
                CB.getScaledFloat(style.pad > 0 ? style.pad : style.padTop),
                CB.getScaledFloat(style.pad > 0 ? style.pad : style.padBottom)) {
            @Override
            public void sizeChanged() {
                CB.postOnNextGlThread(new Runnable() {
                    @Override
                    public void run() {
                        setScrollPaneBounds();
                    }
                });
            }

        };
        OnDrawListener onDrawListener = new OnDrawListener() {
            @Override
            public void onDraw(ListViewItem item) {
                if (adapter != null) {
                    if (selectionType != NONE) {
                        boolean isSelected;
                        if (selectionType == SINGLE) {
                            isSelected = selectedItemList.size == 1 && selectedItemList.contains(item, false);
                        } else {
                            isSelected = selectedItemList.contains(item, false);
                        }

                        if (isSelected) {
                            item.setBackground(ListView.this.style.selectedItem);
                        } else {
                            if (ListView.this.style.secondItem != null && item.getListIndex() % 2 == 1) {
                                item.setBackground(ListView.this.style.secondItem);
                            } else {
                                item.setBackground(ListView.this.style.firstItem);
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
        this.itemList.setOnDrawListener(onDrawListener);
        scrollPane = new VisScrollPane(itemList, style) {


            @Override
            public Actor hit(float x, float y, boolean touchable) {
                Actor actor = super.hit(x, y, touchable);
                if (actor == scrollPane) {
                    actor = itemList.hit(x, (itemList.getHeight() - (scrollPane.getScrollY() + scrollPane.getHeight())) + y, touchable);
                }
                return actor;
            }

            @Override
            public void setScrollX(float scroll) {
                super.setScrollX(scroll);

            }

            @Override
            public void setScrollY(float scroll) {
                super.setScrollY(scroll);

            }

            @Override
            public void sizeChanged() {
                super.sizeChanged();

                if (ListView.this.type == VERTICAL) {
                    itemList.setWidth(ListView.this.getWidth());
                } else {
                    itemList.setHeight(ListView.this.getHeight());
                }
            }
        };
        scrollPane.addCaptureListener(new ClickLongClickListener() {
            @Override
            public boolean clicked(InputEvent event, float x, float y) {
                log.debug("ListViewItem clicked on x:{}  y:{}", x, y);
                SnapshotArray<Actor> childs = ((ListViewItemLinkedList) scrollPane.getChildren().get(0)).getChildren();

                for (int i = 0, n = childs.size; i < n; i++) {
                    ListViewItem item = (ListViewItem) childs.get(i);
                    Vector2 vec = item.localToStageCoordinates(new Vector2());
                    tempClickRec.set(vec.x, vec.y, item.getWidth(), item.getHeight());
                    if (tempClickRec.contains(event.getStageX(), event.getStageY())) {

                        event.setListenerActor(item);

                        // item Clicked
                        log.debug("ListViewItem {} clicked", i);
                        Array<EventListener> listeners = item.getListeners();
                        boolean handeld = false;
                        for (EventListener listener : listeners) {
                            if (listener instanceof ClickListener) {
                                ((ClickListener) listener).clicked(event, x, y);
                            } else if (listener instanceof ClickLongClickListener) {
                                if (((ClickLongClickListener) listener).clicked(event, x, y)) {
                                    event.cancel();
                                    handeld = true;
                                }
                            }
                        }
                        return handeld;
                    }
                }
                return false;
            }

            @Override
            public boolean longClicked(Actor actor, float x, float y) {
                log.debug("ListViewItem longClicked on x:{}  y:{}", x, y);
                SnapshotArray<Actor> childs = ((ListViewItemLinkedList) scrollPane.getChildren().get(0)).getChildren();
                for (int i = 0, n = childs.size; i < n; i++) {
                    ListViewItem item = (ListViewItem) childs.get(i);
                    Vector2 vec = item.localToStageCoordinates(new Vector2());
                    tempClickRec.set(vec.x, vec.y, item.getWidth(), item.getHeight());
                    if (tempClickRec.contains(x, y)) {
                        // item Clicked
                        log.debug("ListViewItem {} LongClicked", i);
                        Array<EventListener> listeners = item.getListeners();
                        boolean handeld = false;
                        for (EventListener listener : listeners) {
                            if (listener instanceof ClickLongClickListener) {
                                if (((ClickLongClickListener) listener).longClicked(actor, x, y)) {
                                    handeld = true;
                                }
                            }
                        }
                        return handeld;
                    }
                }
                return false;
            }
        });
        scrollPane.setOverscroll(false, true);
        scrollPane.setFlickScroll(true);
        scrollPane.setVariableSizeKnobs(false);
        scrollPane.setCancelTouchFocus(true);
        scrollPane.setupFadeScrollBars(1f, 0.5f);

        this.addActor(scrollPane);

    }

    public void setAdapter(ListViewAdapter adapter) {
        frameID = Gdx.graphics == null ? frameID++ : Gdx.graphics.getFrameId();
        this.adapter = adapter;
        itemList.setAdapter(adapter);
        setScrollPaneBounds();
    }

    @Override
    public void layout() {
        scrollPane.layout();
    }

    private void setItemVisibleBounds() {
        if (this.type == VERTICAL) {
            itemList.setVisibleBounds(scrollPane.getScrollY(), scrollPane.getHeight());
        } else {
            itemList.setVisibleBounds(scrollPane.getScrollX(), scrollPane.getWidth());
        }
    }

    @Override
    protected void sizeChanged() {
        if (scrollPane != null) {
            maxScrollChange = type == VERTICAL ? this.getHeight() / 4 : this.getWidth() / 4;
            setScrollPaneBounds();
        } else {
//            if (emptyLabel != null) {
//                emptyLabel.setBounds(0, 0, getWidth(), getHeight());
//                return;
//            }
            invalidate();
            layout();
        }
    }

    private void setScrollPaneBounds() {
        float paneHeight = this.getHeight();
        float paneYPos = 0;
        float completeSize = itemList.getCompleteSize();
        if (this.getHeight() > completeSize) {
            //set on Top
            paneHeight = completeSize;
            paneYPos = this.getHeight() - completeSize;
        }
        scrollPane.setBounds(0, paneYPos, this.getWidth(), paneHeight);
        scrollPane.layout();
        setItemVisibleBounds();
    }

    public float getScrollPos() {
        if (this.type == VERTICAL) {
            return scrollPane.getScrollY();
        }
        return scrollPane.getScrollX();
    }

    public void setScrollPos(float scrollPos) {
        this.setScrollPos(scrollPos, true);
    }

    public void setScrollPos(float scrollPos, boolean withAnimation) {
        if (scrollPane != null) {
            if (this.type == VERTICAL) {
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

    public void setSelectable(SelectableType selectionType) {
        this.selectionType = selectionType;
    }

    private ClickLongClickListener onListItemClickListener = new ClickLongClickListener() {
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
        public boolean longClicked(Actor actor, float x, float y) {
            return false;
        }
    };

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
        this.selectedItemList.add(item);
        item.setSelected(true);
        CB.requestRendering();
    }

    public ListViewItemInterface getSelectedItem() {
        if (this.selectedItemList.size == 0) return null;
        return this.selectedItemList.first();
    }

    public void setSelectedItemVisible(boolean withScroll) {

        if (scrollPane == null) return;

        //get pos of first selected
        ListViewItemInterface item = this.selectedItemList.size == 0 ? null : this.selectedItemList.get(0);
        float scrollPos = 0;
        if (item != null) {
            int index = item.getListIndex() - 1;
            scrollPos = index < 0 ? 0 : itemList.getCompleteSize() - ((type == VERTICAL) ? (item.getY() + item.getHeight()) : (item.getX() + item.getWidth()));
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

    private ScrollChangedEvent scrollChangedEventListener;
    private float lastScrollX = -1;
    private float lastScrollY = -1;

    public void setScrollChangedListener(ScrollChangedEvent listener) {
        this.scrollChangedEventListener = listener;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

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
        if (type == VERTICAL) {
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
        //TODO implement
    }

    public SnapshotArray<Actor> items() {
        return itemList.getChildren();
    }

    public void dispose() {
        //TODO implement
    }

    public void dataSetChanged() {
        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                setAdapter(ListView.this.adapter);
            }
        });
    }
}