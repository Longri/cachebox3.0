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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ScrollViewContainer;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.HORIZONTAL;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 03.02.18.
 */
public class ListViewItemLinkedList extends ScrollViewContainer {
    private final Logger log = LoggerFactory.getLogger(ListViewItemLinkedList.class);

    final static int OVERLOAD = 5;

    private final ListViewType type;
    ListViewItemInterface first;

    private ListViewItemInterface firstVisibleItem;
    private ListViewItemInterface lastVisibleItem;
    private float lastVisibleScrollSearch = Float.MIN_VALUE;
    private float lastVisibleSearchSize = Float.MIN_VALUE;

    private ListViewAdapter adapter;
    private float completeSize = 0;

    private final float padLeft, padRight, padTop, padBottom;
    private OnDrawListener onDrawListener;
    private final ListView.ListViewStyle style;
    ListViewItemInterface[] itemArray;
    private final Array<ListViewItemInterface> oldItems = new Array<>();
    private final Array<ListViewItemInterface> newItems = new Array<>();


    ListViewItemLinkedList(ListViewType type, ListView.ListViewStyle style, float padLeft,
                           float padRight, float padTop, float padBottom) {
        this.type = type;
        this.style = style;
        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padTop = padTop;
        this.padBottom = padBottom;
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        if (itemArray != null && type == VERTICAL) {
            float itemWidth = width - (padLeft + padRight);
            int n = itemArray.length;
            while (--n >= 0) {
                itemArray[n].setPrefWidth(itemWidth);
                itemArray[n].setWidth(itemWidth);
            }
        }
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        if (itemArray != null && type == HORIZONTAL) {
            float itemHeight = height - (padTop + padBottom);
            int n = itemArray.length;
            while (--n >= 0) {
                itemArray[n].setPrefHeight(itemHeight);
                itemArray[n].setHeight(itemHeight);
            }
        }
    }

    public void setAdapter(ListViewAdapter adapter) {
        CB.assertGlThread();

        this.adapter = adapter;
        if (!checkCount()) return;

        itemArray = new ListViewItemInterface[count];

        //create linked dummy list with size of first item
        first = adapter.getView(0);
        first.setBackground(style.firstItem);
        itemArray[0] = first;
        //set default sizes
        float size;
        if (type == VERTICAL) {
            first.setPrefWidth(this.getWidth() - (padLeft + padRight));
            first.pack();
            first.setX(padLeft);
            size = first.getHeight();
        } else {
            first.setPrefHeight(this.getHeight() - (padBottom + padTop));
            first.pack();
            first.setY(padTop);
            size = first.getWidth();
        }


        for (int i = 0, n = adapter.getCount(); i < n; i++) {
            ListViewItemInterface item = new DummyListViewItem(i);
            if (type == VERTICAL) item.setHeight(size);
            else item.setWidth(size);
            itemArray[i] = item;
        }

        first = itemArray[0];

        calcCompleteSize();
    }

    private void calcCompleteSize() {
        log.debug("calc complete size");
        completeSize = (type == VERTICAL) ? padTop : padLeft;
        for (int i = itemArray.length - 1; i >= 0; i--) {
            if (!itemArray[i].isVisible()) {
                continue;
            }
            if (type == VERTICAL) itemArray[i].setY(completeSize);
            else itemArray[i].setX(completeSize);

            float size = (type == VERTICAL)
                    ? itemArray[i].getHeight() + padTop + padBottom
                    : itemArray[i].getWidth() + padLeft + padRight;
            completeSize += size;

        }
        completeSize += (type == VERTICAL) ? padBottom : padRight;

        if (type == VERTICAL) this.setHeight(completeSize);
        else this.setWidth(completeSize);
        log.debug("complete size: {}", completeSize);
    }

    float getCompleteSize() {
        return completeSize;
    }

    private int count = -1;
    private boolean countChk = false;

    private boolean checkCount() {
        if (count < 0) {
            if (adapter == null) return false;
            count = adapter.getCount();
            countChk = count > 0;
        }
        return countChk;
    }

    void setVisibleBounds(float scroll, float size) {
        if (!checkCount()) return;
        if (size == 0) {
            CB.postOnGlThread(new NamedRunnable("LinkedList clear child items") {
                @Override
                public void run() {
                    ListViewItemLinkedList.this.clearChildren();
                }
            });
            return;
        }


        if (lastVisibleScrollSearch == scroll && lastVisibleSearchSize == size) {
            log.debug("RETURN TWICE CALL");
            return;
        }
        lastVisibleSearchSize = size;
        lastVisibleScrollSearch = scroll;

        float search = completeSize - scroll;

        log.debug("setVisibleBounds scroll: {} size: {}", scroll, size);

        ListViewItemInterface firstVisible = search(this.type, this.itemArray, search - size, search);
        if (firstVisible == null) {
            firstVisible = first;
        }

        log.debug("Founded item index: {}", firstVisible.getListIndex());

        //search first visible
        final int findIdx = firstVisible.getListIndex();
        if (this.type == VERTICAL) {
            for (int i = findIdx; i >= 0; i--) {
                firstVisible = itemArray[i];
                if (firstVisible.getY() >= search) {
                    break;
                }
            }
        } else {
            for (int i = firstVisible.getListIndex(); i >= 0; i--) {
                firstVisible = itemArray[i];
                if (firstVisible.getX() >= search) {
                    break;
                }
            }
        }


        //search last visible
        ListViewItemInterface lastVisible = firstVisible;
        float lastPos = search - size;

        if (this.type == VERTICAL) {
            for (int i = findIdx, n = itemArray.length; i < n; i++) {
                lastVisible = itemArray[i];
                if (lastVisible.getY() <= lastPos) {
                    break;
                }
            }
        } else {
            for (int i = findIdx, n = itemArray.length; i < n; i++) {
                lastVisible = itemArray[i];
                if (lastVisible.getX() <= lastPos) {
                    break;
                }
            }
        }

        firstVisibleItem = firstVisible;
        lastVisibleItem = lastVisible;

        log.debug("firstVisible: {} lastVisible: {}", firstVisible.getListIndex(), lastVisibleItem.getListIndex());

        //set overload
        int idx = firstVisible.getListIndex() - (OVERLOAD * 2);
        if (idx < 0) idx = 0;
        firstVisible = itemArray[idx];
        idx = lastVisible.getListIndex() + OVERLOAD;
        if (idx > itemArray.length - 1) idx = itemArray.length - 1;
        lastVisible = itemArray[idx];

        //add visible child items on glThread
        final int firstItemIdx = firstVisible.getListIndex();
        final int lastItemIdx = lastVisible.getListIndex();

        CB.postOnGlThread(new NamedRunnable("add visible child items") {
            @Override
            public void run() {
                Actor[] childs = ListViewItemLinkedList.this.getChildren().begin();
                Array<ListViewItemInterface> clearList = new Array<>();
                IntArray addedItems = new IntArray();
                for (int i = 0, n = ListViewItemLinkedList.this.getChildren().size; i < n; i++) {
                    ListViewItemInterface item = (ListViewItemInterface) childs[i];
                    int idx = item.getListIndex();
                    if (idx >= firstItemIdx && idx <= lastItemIdx) {
                        addedItems.add(idx);
                    } else {
                        if (!item.isSelected())
                            clearList.add(item);
                    }
                }
                ListViewItemLinkedList.this.getChildren().end();

                for (int i = 0; i < clearList.size; i++) {
                    ListViewItemInterface old = clearList.get(i);
                    ListViewItemLinkedList.this.removeActor((Actor) old);
                    int idx = old.getListIndex();
                    ListViewItemInterface dummy = new DummyListViewItem(idx);
                    dummy.setX(old.getX());
                    dummy.setY(old.getY());
                    dummy.setWidth(old.getWidth());
                    dummy.setHeight(old.getHeight());
                    dummy.setVisible(old.isVisible());
                    itemArray[idx] = dummy;

                    old.dispose();
                }


                int addCount = 0;
                for (int i = firstItemIdx; i <= lastItemIdx; i++) {
                    if (!addedItems.contains(i)) {
                        ListViewItemLinkedList.this.addActor((Actor) itemArray[i]);
                        addCount++;
                    }

                }

                log.debug("Remove {} items | add {} items", clearList.size, addCount);
            }
        });

    }

    void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        replaceDummy();
    }

    void replaceDummy() {
        Actor[] childs = this.getChildren().begin();
        int n = this.getChildren().size;
        boolean anyChanges = false;
        oldItems.clear();
        newItems.clear();
        boolean mustReCalcCompleteSize = false;
        while (n-- > 0) {
            if (childs[n] instanceof DummyListViewItem) {
                anyChanges = true;
                //replace item from adapter
                DummyListViewItem old = (DummyListViewItem) childs[n];
                ListViewItemInterface newItem = adapter.getView(old.getListIndex());
                if (replaceItems(oldItems, newItems, old, newItem)) {
                    mustReCalcCompleteSize = true;
                }
            }
        }
        this.getChildren().end();

        if (anyChanges) {
            //remove old
            for (ListViewItemInterface old : oldItems)
                this.removeActor((Actor) old);

            //add new
            for (ListViewItemInterface n_ew : newItems)
                this.addActor((Actor) n_ew);

            if (mustReCalcCompleteSize) {
                calcCompleteSize();
            }
            setVisibleBounds(lastVisibleScrollSearch, this.lastVisibleSearchSize);
        }
    }

    private boolean replaceItems(Array<ListViewItemInterface> oldItems, Array<ListViewItemInterface> newItems,
                                 ListViewItemInterface old, ListViewItemInterface newItem) {
        if (style.secondItem != null && old.getListIndex() % 2 == 1) {
            newItem.setBackground(style.secondItem);
        } else {
            newItem.setBackground(style.firstItem);
        }
        //set default sizes
        float changedSize;
        if (type == VERTICAL) {
            newItem.setPrefWidth(this.getWidth() - (padLeft + padRight));
            newItem.pack();
            newItem.setX(padLeft);
            newItem.setY(old.getY());
            changedSize = newItem.getHeight() - old.getHeight();
        } else {
            newItem.setPrefHeight(this.getHeight() - (padBottom + padTop));
            newItem.pack();
            newItem.setY(padTop);
            newItem.setX(old.getX());
            changedSize = newItem.getWidth() - old.getWidth();
        }

        newItem.setOnDrawListener(this.onDrawListener);
        if (oldItems != null && newItems != null) {
            oldItems.add(old);
            newItems.add(newItem);
        }
        if (old == this.first) {
            first = newItem;
        }
        if (lastVisibleItem == old)
            lastVisibleItem = newItem;

        if (firstVisibleItem == old)
            firstVisibleItem = newItem;
        itemArray[old.getListIndex()] = newItem;


        if (changedSize != 0) {
            //set pos of items that are before
            for (int i = 0; i < newItem.getListIndex(); i++) {
                if (type == VERTICAL) {
                    itemArray[i].setY(itemArray[i].getY() + changedSize);
                } else {
                    itemArray[i].setX(itemArray[i].getX() + changedSize);
                }
            }
            this.completeSize += changedSize;
            if (type == VERTICAL) this.setHeight(completeSize);
            else this.setWidth(completeSize);
        }
        return old.isVisible() != newItem.isVisible();
    }

    private static ListViewItemInterface search(ListViewType type, ListViewItemInterface[] arr, float searchValue, float range) {
        int left = 0;
        int right = arr.length - 1;
        int idx = binarySearch(type, arr, searchValue, range, left, right);

        if (idx < 0 || idx >= arr.length) return null;
        return arr[idx];
    }

    private static int binarySearch(ListViewType type, ListViewItemInterface[] arr, float searchValue, float range, int left, int right) {
        if (right < left) {
            return -1;
        }
        int mid = (left + right) >>> 1;
        float pos = (type == VERTICAL) ? arr[mid].getY() : arr[mid].getX();
        if (searchValue < pos && range < pos) {
            return binarySearch(type, arr, searchValue, range, mid + 1, right);
        } else if (searchValue > pos && range > pos) {
            return binarySearch(type, arr, searchValue, range, left, mid - 1);
        } else {
            return mid;
        }
    }


    ListViewItemInterface getItem(int index) {
        ListViewItemInterface item = itemArray[index];
        if (item instanceof DummyListViewItem) {
            ListViewItem newItem = adapter.getView(index);
            replaceItems(null, null, item, newItem);
            return newItem;
        }
        return item;
    }
}
