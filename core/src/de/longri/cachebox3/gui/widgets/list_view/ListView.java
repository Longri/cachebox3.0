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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 03.02.18.
 */
public class ListView extends WidgetGroup {

    final ListViewType type;
    final VisScrollPane scrollPane;
    final de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle style;
    final ListViewItemLinkedList itemList;
    float maxScrollChange = 0;


    public ListView(ListViewType type) {
        this(type, VisUI.getSkin().get("default", de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle.class));
    }

    public ListView(ListViewType type, de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle style) {
        this.type = type;
        this.itemList = new ListViewItemLinkedList(type,
                style.pad > 0 ? style.pad : style.padLeft,
                style.pad > 0 ? style.pad : style.padRight,
                style.pad > 0 ? style.pad : style.padTop,
                style.pad > 0 ? style.pad : style.padBottom);
        this.style = style;
        scrollPane = new VisScrollPane(itemList, style) {


            float lastFiredScrollX = 0;
            float lastFiredScrollY = 0;

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
                if (Math.abs(lastFiredScrollX - scroll) > maxScrollChange) {
                    lastFiredScrollX = scroll;
                    setItemVisibleBounds();
                }
            }

            @Override
            public void setScrollY(float scroll) {
                super.setScrollY(scroll);
                if (Math.abs(lastFiredScrollY - scroll) > maxScrollChange) {
                    lastFiredScrollY = scroll;
                    setItemVisibleBounds();
                }
            }

        };
        scrollPane.setOverscroll(false, true);
        scrollPane.setFlickScroll(true);
        scrollPane.setVariableSizeKnobs(false);
        scrollPane.setCancelTouchFocus(true);
        scrollPane.setupFadeScrollBars(1f, 0.5f);

        this.addActor(scrollPane);

    }

    public void setAdapter(ListViewAdapter adapter) {
        itemList.setAdapter(adapter);
        maxScrollChange = adapter.getDefaultItemSize() * ListViewItemLinkedList.OVERLOAD;
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
}
