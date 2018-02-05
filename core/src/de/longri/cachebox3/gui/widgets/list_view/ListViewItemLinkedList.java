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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.ListView;
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
    float lastVisibleSearchSize = 0;

    private ListViewAdapter adapter;
    private float completeSize = 0;

    private final float padLeft, padRight, padTop, padBottom;
    private OnDrawListener onDrawListener;
    private final ListView.ListViewStyle style;

    ListViewItemLinkedList(ListViewType type, ListView.ListViewStyle style, float padLeft, float padRight, float padTop, float padBottom) {

        this.setDebug(true);

        this.type = type;
        this.style = style;
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
        for (int i = 1, n = adapter.getCount() - 1; i < n; i++) {
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

    void setVisibleBounds(float scroll, float size) {

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
                //TODO dispose old Item's, if it will not added any more
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
        int n = childs.length;
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

    private void replaceItems(ListViewItem old, ListViewItem newItem) {
        //replace linked list items
        if (old == this.first) {
            first = newItem;
        } else {
            old.before.next = newItem;
        }

        if (lastVisibleItem == old)
            lastVisibleItem = newItem;

        if (firstVisibleItem == old)
            firstVisibleItem = newItem;

        newItem.next = old.next;
        old.before = null;
        old.next = null;
    }

//    protected void drawDebugBounds(ShapeRenderer shapes) {
//        shapes.set(ShapeRenderer.ShapeType.Filled);
//        shapes.setColor(Color.CYAN);
//        shapes.rect(getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
//    }
}
