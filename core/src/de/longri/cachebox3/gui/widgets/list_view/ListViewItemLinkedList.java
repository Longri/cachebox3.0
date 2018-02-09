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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ScrollViewContainer;
import de.longri.cachebox3.utils.NamedRunnable;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.HORIZONTAL;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 03.02.18.
 */
public class ListViewItemLinkedList extends ScrollViewContainer {

    final static int OVERLOAD = 10;

    private final ListViewType type;
    ListViewItem first;

    private ListViewItem firstVisibleItem;
    private ListViewItem lastVisibleItem;
    private float lastVisibleScrollSearch = 0;
    private float lastVisibleSearchSize = 0;

    private ListViewAdapter adapter;
    private float completeSize = 0;

    private final float padLeft, padRight, padTop, padBottom;
    private OnDrawListener onDrawListener;
    private final ListView.ListViewStyle style;
    ListViewItem[] itemArray;

    ListViewItemLinkedList(ListViewType type, ListView.ListViewStyle style, float padLeft, float padRight, float padTop, float padBottom) {
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
        this.adapter = adapter;
        if (!checkCount()) return;

        itemArray = new ListViewItem[count];

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


        for (int i = 1, n = adapter.getCount(); i < n; i++) {
            ListViewItem item = new DummyListViewItem(i);
            if (type == VERTICAL) item.setHeight(size);
            else item.setWidth(size);
            itemArray[i] = item;
        }

        calcCompleteSize();
    }

    private void calcCompleteSize() {
        completeSize = (type == VERTICAL) ? padTop : padLeft;
        for (int i = itemArray.length - 1; i >= 0; i--) {
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
    }

    float getCompleteSize() {
        return completeSize;
    }

    private int count = -1;
    private boolean countChk = false;

    private boolean checkCount() {
        if (count < 0) {
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
        lastVisibleSearchSize = size;
        lastVisibleScrollSearch = scroll;


        float search = completeSize - scroll;

        ListViewItem firstVisible = search(this.type, this.itemArray, search - size, search);

        if (firstVisible == null) return;

        //search first visible

//        if (this.type == VERTICAL) {
//            for (int i = firstVisible.index, n = itemArray.length; i < n; i++) {
//                firstVisible = itemArray[i];
//                if (firstVisible.getY() >= search) {
//                    break;
//                }
//            }
//        } else {
//            for (int i = firstVisible.index, n = itemArray.length; i < n; i++) {
//                firstVisible = itemArray[i];
//                if (firstVisible.getX() >= search) {
//                    break;
//                }
//            }
//        }


        //search last visible
        ListViewItem lastVisible = firstVisible;
        float lastPos = search - size;

        if (this.type == VERTICAL) {
            for (int i = lastVisible.index, n = 0; i >= n; i--) {
                lastVisible = itemArray[i];
                if (lastVisible.getY() <= lastPos) {
                    break;
                }
            }
        } else {
            for (int i = lastVisible.index, n = 0; i >= n; i--) {
                lastVisible = itemArray[i];
                if (lastVisible.getX() <= lastPos) {
                    break;
                }
            }
        }

        firstVisibleItem = firstVisible;
        lastVisibleItem = lastVisible;

        //set overload
//        int idx = firstVisible.index - OVERLOAD;
//        if (idx < 0) idx = 0;
//        firstVisible = itemArray[idx];
//        idx = lastVisible.index + OVERLOAD;
//        if (idx > itemArray.length - 1) idx = itemArray.length - 1;
//        lastVisible = itemArray[idx];

        //add visible child items on glThread
        final int firstItemIdx = firstVisible.index;
        final int lastItemIdx = firstVisible.index;//lastVisible.index;

        CB.postOnGlThread(new NamedRunnable("add visible child items") {
            @Override
            public void run() {
                //TODO dispose old Item's, if it will not added any more
                ListViewItemLinkedList.this.clearChildren();
                for (int i = firstItemIdx; i <= lastItemIdx; i++) {
                    ListViewItemLinkedList.this.addActor(itemArray[i]);
                }
            }
        });

    }

    public void setOnDrawListener(OnDrawListener onDrawListener) {
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
        while (n-- > 0) {
            if (childs[n] instanceof DummyListViewItem) {
                anyChanges = true;
                //replace item from adapter
                DummyListViewItem old = (DummyListViewItem) childs[n];
                ListViewItem newItem = adapter.getView(old.index);
                if (style.secondItem != null && old.index % 2 == 1) {
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
                childs[n] = newItem;
                replaceItems(old, newItem);


//                //set pos of following items
//                for (int i = newItem.index + 1; i < itemArray.length; i++) {
//                    if (type == VERTICAL) {
//                        itemArray[i].setY(newItem.getY() + changedSize);
//                    } else {
//                        itemArray[i].setX(newItem.getX() + changedSize);
//                    }
//                }
//                this.completeSize += changedSize;
//                if (type == VERTICAL) this.setHeight(completeSize);
//                else this.setWidth(completeSize);
            }
        }
        this.getChildren().end();

        if (anyChanges) {
            setVisibleBounds(lastVisibleScrollSearch, this.lastVisibleSearchSize);
        }
    }

    void replaceItems(ListViewItem old, ListViewItem newItem) {
        //replace linked list items
        if (old == this.first) {
            first = newItem;
        }

        if (lastVisibleItem == old)
            lastVisibleItem = newItem;

        if (firstVisibleItem == old)
            firstVisibleItem = newItem;

        itemArray[old.getListIndex()] = newItem;
    }

    static int NOT_FOUND = -1;

    private static ListViewItem search(ListViewType type, ListViewItem[] arr, float searchValue, float range) {
        int left = 0;
        int right = arr.length - 1;
        int idx = binarySearch(type, arr, searchValue, range, left, right);

        if (idx < 0 || idx >= arr.length) return null;
        return arr[idx];
    }

    private static int binarySearch(ListViewType type, ListViewItem[] arr, float searchValue, float range, int left, int right) {
        if (right < left) {
            return NOT_FOUND;
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


}
