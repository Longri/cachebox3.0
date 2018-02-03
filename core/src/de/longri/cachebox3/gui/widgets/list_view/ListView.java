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
import de.longri.cachebox3.gui.views.listview.ScrollViewContainer;

/**
 * Created by Longri on 03.02.18.
 */
public class ListView extends WidgetGroup {

    final ListViewType type;
    private final ScrollViewContainer itemGroup = new ScrollViewContainer();
    private final VisScrollPane scrollPane;
    private final de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle style;

    private ListViewItemLinkedList itemList;

    public ListView(ListViewType type) {
        this.type = type;
        this.style = VisUI.getSkin().get("default", de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle.class);
        scrollPane = new VisScrollPane(itemGroup, style) {
            @Override
            public Actor hit(float x, float y, boolean touchable) {
                Actor actor = super.hit(x, y, touchable);
                if (actor == scrollPane) {
                    actor = itemGroup.hit(x, (itemGroup.getHeight() - (scrollPane.getScrollY() + scrollPane.getHeight())) + y, touchable);
                }
                return actor;
            }
        };
        scrollPane.setOverscroll(false, true);
        scrollPane.setFlickScroll(true);
        scrollPane.setVariableSizeKnobs(false);
        scrollPane.setCancelTouchFocus(true);
        scrollPane.setupFadeScrollBars(1f, 0.5f);
        setScrollPaneBounds();
    }

    public void setAdapter(ListViewAdapter adapter) {
        itemList = new ListViewItemLinkedList(adapter);
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
        if (this.getHeight() > itemList.getCompleteSize()) {
            //set on Top
            paneHeight = itemList.getCompleteSize();
            paneYPos = this.getHeight() - itemList.getCompleteSize();
        }
        scrollPane.setBounds(0, paneYPos, this.getWidth(), paneHeight);
    }

}
