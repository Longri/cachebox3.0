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

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.ListViewStyle;
import de.longri.cachebox3.utils.MathUtils;
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
    private GalleryItem firstVisibleItem;
    private float lastSearchPos;
    private EventListener inputListener;

    public GalleryListView(ListViewStyle listViewStyle) {
        super(ListViewType.HORIZONTAL, listViewStyle);
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
                // it's over zoomedScroll, don't snapIn
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

    public void snapIn() {
        // don't snap in with end of ListView
        log.debug("SnapIn");

        // get first visible item and zoomedScroll to Center
        firstVisibleItem = (GalleryItem) getVisibleItem();
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

    public ListViewItemInterface getVisibleItem() {
        float size = getWidth();
        float searchPos = getScrollPos();
        if (lastSearchPos == searchPos && firstVisibleItem != null) return firstVisibleItem;
        lastSearchPos = searchPos;
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

    public void zoomedScroll(float x, float y, int amount) {
        // check if item snapped in
        float scrollPos = getScrollPos();
        if (firstVisibleItem == null) {
            snapIn();
        }

        //set zoom to Item
        float amountScaleFactor = 0.25f;
        float scale = amount * amountScaleFactor;
        firstVisibleItem.zoom(x, y, scale);

        //disable scrolling
        if (firstVisibleItem.getZoom() > 1.0f) {
            log.debug("DISABLE scrolling");
            setScrollingDisabled(true, this.inputListener);
        } else {
            log.debug("ENABLE scrolling");
            setScrollingDisabled(false, this.inputListener);
        }


    }

    public void zoomedFling(float velocityX, float velocityY) {
        if (firstVisibleItem != null) {
            firstVisibleItem.setInputListener(this.inputListener);
            firstVisibleItem.fling(velocityX, velocityY);
        }
    }

    public void zoomedDrag(float x, float y) {
        if (firstVisibleItem != null) {
            firstVisibleItem.setInputListener(this.inputListener);
            firstVisibleItem.drag(x, y);
        }
    }

    public void addInputListener(EventListener inputListener) {
        this.inputListener = inputListener;
        this.addListener(inputListener);
    }

    @Override
    public void setScrollPos(float scrollPos, boolean withAnimation) {
        if (firstVisibleItem != null) {
            //reset zoomed item
            firstVisibleItem.resetZoom();
            setScrollingDisabled(false, this.inputListener);
        }
        super.setScrollPos(scrollPos, withAnimation);
    }

    public void zoom(float x, float y, float pixelChange) {

        if (firstVisibleItem == null) {
            snapIn();
        }


        float actZoom = firstVisibleItem.getZoom();

        if (actZoom == 1) firstVisibleItem.clamp();

        float itemWidth = firstVisibleItem.drwWidth;
        float itemHeight = firstVisibleItem.drwHeight;

        if (itemWidth <= 0 || itemHeight <= 0) {
            itemWidth = firstVisibleItem.getImgWidth();
            itemHeight = firstVisibleItem.getImgHeight();
        }

        if (itemWidth <= 0 || itemHeight <= 0) {
            log.debug("don't zoom a item with unvisible size of width:{}, height:{}", itemWidth, itemHeight);
            return;
        }

        // calculate zoom from Pixel change
        log.debug("itemWidth:{} , itemHeight:{}", itemWidth, itemHeight);
        float hypotenuseAlt = MathUtils.hypo(itemWidth, itemHeight);
        float hypotenuseNew = hypotenuseAlt + pixelChange;


        float zoom = (1 - (hypotenuseAlt / hypotenuseNew)) * CB.getScalefactor() * actZoom * 3f;
        log.debug("a:{} , c:{} ,n:{}, z:{}", hypotenuseAlt, pixelChange, hypotenuseNew, zoom);

        //set zoom to Item
        firstVisibleItem.zoom(x, y, zoom);

        //disable scrolling
        if (firstVisibleItem.getZoom() > 1.0f) {
            log.debug("DISABLE scrolling");
            setScrollingDisabled(true, this.inputListener);
        } else {
            log.debug("ENABLE scrolling");
            setScrollingDisabled(false, this.inputListener);
        }

    }
}
