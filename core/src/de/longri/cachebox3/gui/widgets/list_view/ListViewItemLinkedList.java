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

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.ScrollViewContainer;
import de.longri.cachebox3.utils.NamedRunnable;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 03.02.18.
 */
public class ListViewItemLinkedList extends ScrollViewContainer {

    final static int OVERLOAD = 10;

    private final ListViewType type;
    private ListViewItem first;

    ListViewItem firstVisibleItem;
    ListViewItem lastVisibleItem;
    float lastVisibleScrollSearch = 0;

    private ListViewAdapter adapter;
    private float completeSize = 0;

    private final float padLeft, padRight, padTop, padBottom;


    ListViewItemLinkedList(ListViewType type, float padLeft, float padRight, float padTop, float padBottom) {
        this.type = type;
        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padTop = padTop;
        this.padBottom = padBottom;
    }

    public void setAdapter(ListViewAdapter adapter) {
        this.adapter = adapter;

        //create linked dummy list with size of first item
        float size = adapter.getDefaultItemSize();
        ListViewItem act = first = new DummyListViewItem(0);
        if (type == VERTICAL) act.setHeight(size);
        else act.setWidth(size);
        for (int i = 1, n = adapter.getCount() + 1; i < n; i++) {
            ListViewItem item = new DummyListViewItem(i);
            act.setNext(item);
            item.setBefore(act);
            act = item;
            if (type == VERTICAL) act.setHeight(size);
            else act.setWidth(size);
        }
        calcCompleteSize();
    }

    private void calcCompleteSize() {
        completeSize = (type == VERTICAL) ? padTop : padLeft;
        ListViewItem act = first;
        if (type == VERTICAL) act.setX(0);
        else act.setY(0);
        do {
            completeSize += (type == VERTICAL)
                    ? act.getHeight() + padTop + padBottom
                    : act.getWidth() + padLeft + padRight;
            act = act.next;
            if (type == VERTICAL) act.setY(completeSize);
            else act.setX(completeSize);
        } while (act.next != null);

        if (type == VERTICAL) this.setHeight(completeSize);
        else this.setWidth(completeSize);
    }

    float getCompleteSize() {
        return completeSize;
    }

    void setVisibleBounds(float scroll, float size) {

        if (size == 0) {
            CB.postOnGlThread(new NamedRunnable("add visible child items") {
                @Override
                public void run() {
                    ListViewItemLinkedList.this.clearChildren();
                }
            });
            return;
        }


        //search first visible
        ListViewItem firstVisible = (firstVisibleItem == null || lastVisibleItem == null) ? first
                : ((lastVisibleScrollSearch < scroll) ? lastVisibleItem : firstVisibleItem);

        if (lastVisibleScrollSearch < scroll) {
            if (this.type == VERTICAL) {
                while (firstVisible.before != null) {
                    if (firstVisible.getY() <= scroll) {
                        break;
                    }
                    firstVisible = firstVisible.before;
                }
            } else {
                while (firstVisible.before != null) {
                    if (firstVisible.getX() <= scroll) {
                        break;
                    }
                    firstVisible = firstVisible.before;
                }
            }
        } else {
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
        }

        lastVisibleScrollSearch = scroll;


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
                ListViewItemLinkedList.this.clearChildren();
                ListViewItem act = firstItem;
                do {
                    ListViewItemLinkedList.this.addActor(act);
                    if (act == lastItem) break;
                    act = act.next;
                } while (act != null);
            }
        });

    }
}
