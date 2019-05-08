/*
 * Copyright (C) 2019 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewItemLinkedList.search;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryListView extends ListView {

    private static Logger log = LoggerFactory.getLogger(GalleryListView.class);

    private boolean isMoving = false;

    public GalleryListView() {
        super(ListViewType.HORIZONTAL);
        this.setDebug(true, true);
    }

    public void act(float delta) {
        super.act(delta);

        boolean dontSnapIn = false;
        if (isMoving && !scrollPane.isPanning()) {
            float maxY = getScrollPos() + scrollPane.getWidth();
            ListViewItemInterface lastItem = this.itemList.itemArray[this.itemList.itemArray.length - 1];
            float maxWidth = lastItem.getX() + lastItem.getWidth();
            if (maxY > maxWidth) {
                // it's over scroll, don't snapIn
                dontSnapIn = true;
            }
        }


        if (scrollPane.isPanning() || scrollPane.isDragging() || scrollPane.isFlinging()) {
            isMoving = true;
        } else {
            if (isMoving) {
                isMoving = false;
                if (!dontSnapIn)
                    CB.postAsync(new NamedRunnable("GalleryListViewSnapIn") {
                        @Override
                        public void run() {
                            snapIn();
                        }
                    });
            }
        }
    }

    private void snapIn() {
        // don't snap in with end of ListView
        log.debug("SnapIn");

        // get first visible item and scroll to Center
        ListViewItemInterface firstVisibleItem = getVisibleItem();
        float scrollPos = 0;
        if (firstVisibleItem != null) {
            int index = firstVisibleItem.getListIndex() - 1;
            scrollPos = index < 0 ? 0 : firstVisibleItem.getX();
        }
        this.setScrollPos(scrollPos);
        CB.requestRendering();
        if (firstVisibleItem != null)
            log.debug("Scroll to selected item {} at position {}", firstVisibleItem.getListIndex(), scrollPos);
    }

    private ListViewItemInterface getVisibleItem() {
        float size = getWidth();
        float searchPos = getScrollPos();
        ListViewItemInterface[] itemArray = this.itemList.itemArray;
        ListViewItemInterface firstItem = search(this.type, itemArray, searchPos, size);
        ListViewItemInterface lastItem = ListViewItemLinkedList.search(this.type, itemArray, searchPos + size, size);

        float visualFirst = ListViewItemLinkedList.getVisualSize(ListViewType.HORIZONTAL, firstItem, searchPos, size);
        float visualLast = ListViewItemLinkedList.getVisualSize(ListViewType.HORIZONTAL, lastItem, searchPos, size);

        if (visualFirst >= visualLast) return firstItem;
        return lastItem;
    }

    @Override
    public float getPrefHeight() {
        return CB.getScaledFloat(75);
    }
}
