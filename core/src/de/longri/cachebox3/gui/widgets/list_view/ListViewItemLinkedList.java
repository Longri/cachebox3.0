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
        if (type == VERTICAL) {
            ListViewItem act = first;
            float itemWidth = width - (padLeft + padRight);
            while (act != null) {
                act.setPrefWidth(itemWidth);
                act.setWidth(itemWidth);
                act = act.next;
            }
        }
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        if (type == HORIZONTAL) {
            ListViewItem act = first;
            float itemHeight = height - (padTop + padBottom);
            while (act != null) {
                act.setPrefHeight(itemHeight);
                act.setHeight(itemHeight);
                act = act.next;
            }
        }
    }

    public void setAdapter(ListViewAdapter adapter) {
        this.adapter = adapter;
        if (!checkCount()) return;

        itemArray = new ListViewItem[count];

        //create linked dummy list with size of first item
        ListViewItem act = first = adapter.getView(0);
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
                act.setNext(item);
                item.setBefore(act);
                act = item;
                if (type == VERTICAL) act.setHeight(size);
                else act.setWidth(size);
                itemArray[i] = act;
            }

        calcCompleteSize();
    }

    private void calcCompleteSize() {
        completeSize = (type == VERTICAL) ? -padTop : -padLeft;
        ListViewItem act = first;
        while (act != null) {
            completeSize += (type == VERTICAL)
                    ? act.getHeight() + padTop + padBottom
                    : act.getWidth() + padLeft + padRight;
            if (type == VERTICAL) act.setY(completeSize - act.getHeight());
            else act.setX(completeSize - act.getWidth());
            act = act.next;
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

        ListViewItem firstVisible = search(this.itemArray, scroll, scroll + size);

        if (firstVisible == null) return;

        //search first visible


        if (this.type == VERTICAL) {
            while (firstVisible.next != null) {
                if (firstVisible.getY() >= scroll) {
                    break;
                }
                firstVisible = firstVisible.next;
            }
        } else {
            while (firstVisible.next != null) {
                if (firstVisible.getX() >= scroll) {
                    break;
                }
                firstVisible = firstVisible.next;
            }
        }


        //search last visible
        ListViewItem lastVisible = firstVisible;
        float lastPos = scroll + size;

        if (this.type == VERTICAL) {
            while (lastVisible.next != null) {
                if (lastVisible.getY() >= lastPos) {
                    break;
                }
                lastVisible = lastVisible.next;
            }
        } else {
            while (lastVisible.next != null) {
                if (lastVisible.getX() >= lastPos) {
                    break;
                }
                lastVisible = lastVisible.next;
            }
        }

        firstVisibleItem = firstVisible;
        lastVisibleItem = lastVisible;

        //set overload
        for (int i = 0; i < OVERLOAD; i++) {
            if (firstVisible.before == null) {
                break;
            }
            firstVisible = firstVisible.before;
        }

        for (int i = 0; i < OVERLOAD; i++) {
            if (lastVisible.next == null) {
                break;
            }
            lastVisible = lastVisible.next;
        }

        //add visible child items on glThread
        final ListViewItem firstItem = firstVisible;
        final ListViewItem lastItem = lastVisible;

        CB.postOnGlThread(new NamedRunnable("add visible child items") {
            @Override
            public void run() {
                //TODO dispose old Item's, if it will not added any more
                ListViewItemLinkedList.this.clearChildren();
                ListViewItem act = lastItem;
                do {
                    ListViewItemLinkedList.this.addActor(act);
                    if (act == firstItem) break;
                    act = act.before;
                } while (act != null);
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


                //set pos of following items
                while (newItem.next != null) {
                    newItem = newItem.next;
                    if (type == VERTICAL) {
                        newItem.setY(newItem.getY() + changedSize);
                    } else {
                        newItem.setX(newItem.getX() + changedSize);
                    }
                }
                this.completeSize += changedSize;
                if (type == VERTICAL) this.setHeight(completeSize);
                else this.setWidth(completeSize);
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
        if (old.before != null) {
            old.before.next = newItem;
        }

        if (lastVisibleItem == old)
            lastVisibleItem = newItem;

        if (firstVisibleItem == old)
            firstVisibleItem = newItem;

        newItem.next = old.next;
        newItem.before = old.before;
        old.before = null;
        old.next = null;

        itemArray[old.getListIndex()] = newItem;
    }

    int getDebugCount() {
        ListViewItem act = first;
        int count = 0;
        while (act != null) {
            count++;
            act = act.next;
        }
        return count;
    }


    static int NOT_FOUND = -1;

    private static ListViewItem search(ListViewItem[] arr, float searchValue, float range) {
        int left = 0;
        int right = arr.length - 1;
        int idx = binarySearch(arr, searchValue, range, left, right);

        if (idx < 0 || idx >= arr.length) return null;
        return arr[idx];
    }

    private static int binarySearch(ListViewItem[] arr, float searchValue, float range, int left, int right) {
        if (right < left) {
            return NOT_FOUND;
        }
		/*
		int mid = mid = (left + right) / 2;
		There is a bug in the above line;
		Joshua Bloch suggests the following replacement:
		*/
        int mid = (left + right) >>> 1;
        if (searchValue < arr[mid].getY() && range < arr[mid].getY()) {
            return binarySearch(arr, searchValue, range, mid + 1, right);
        } else if (searchValue > arr[mid].getY() && range > arr[mid].getY()) {
            return binarySearch(arr, searchValue, range, left, mid - 1);
        } else {
            return mid;
        }
    }


}
