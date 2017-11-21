/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets.filter_settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.gui.activities.EditFilterSettings;
import de.longri.cachebox3.gui.skin.styles.FilterStyle;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.IntProperty;

/**
 * Created by Longri on 16.11.2017.
 */
public class FilterSetListView extends Table {

    private final ListView setListView;
    private final FilterStyle style;
    private final EditFilterSettings filterSettings;
    private final Array<ListViewItem> listViewItems = new Array<>();

    public FilterSetListView(EditFilterSettings editFilterSettings, FilterStyle style) {
        this.style = style;
        this.filterSettings = editFilterSettings;

        Adapter listViewAdapter = new Adapter() {
            @Override
            public int getCount() {
                return listViewItems.size;
            }

            @Override
            public ListViewItem getView(int index) {
                return listViewItems.get(index);
            }

            @Override
            public void update(ListViewItem view) {

            }

            @Override
            public float getItemSize(int index) {
                return listViewItems.get(index).getHeight();
            }
        };

        setListView = new ListView(listViewAdapter, true, false);
        setListView.setSelectable(ListView.SelectableType.NONE);

        this.add(setListView).expand().fill();
        setListView.setEmptyString("EmptyList");

        fillList();

    }

    private void fillList() {
        listViewItems.clear();
        addGeneralItems();
        addDTGcVoteItems();
        addCachTypeItems();
        addAttributeItems();
    }

    private void addGeneralItems() {

        ClickListener listener = new ClickListener() {

        };

        listViewItems.add(new ButtonListViewItem(listViewItems.size, Translation.get("General"), listener));
    }

    private void addDTGcVoteItems() {
        ClickListener listener = new ClickListener() {

        };

        listViewItems.add(new ButtonListViewItem(listViewItems.size, "D / T" + String.format("%n") + "GC-Vote", listener));
    }

    private void addCachTypeItems() {
        ClickListener listener = new ClickListener() {

        };

        listViewItems.add(new ButtonListViewItem(listViewItems.size, Translation.get("CacheTypes"), listener));
    }

    private void addAttributeItems() {
        ClickListener listener = new ClickListener() {

        };

        listViewItems.add(new ButtonListViewItem(listViewItems.size, Translation.get("Attributes"), listener));
    }

    class ButtonListViewItem extends ListViewItem {

        public ButtonListViewItem(int listIndex, CharSequence text, ClickListener clickListener) {
            super(listIndex);
            CharSequenceButton btn = new CharSequenceButton(text);
            btn.getLabel().setWrap(true);
            this.addListener(clickListener);
            this.add(btn).expand().fill();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            //clear Background
            this.setBackground((Drawable) null);
            super.draw(batch, parentAlpha);
        }

        @Override
        public void dispose() {

        }
    }

    class IntPropertyListView extends ListViewItem {

        public IntPropertyListView(int listIndex, IntProperty property, Drawable icon, CharSequence name) {
            super(listIndex);
        }

        @Override
        public void dispose() {

        }
    }

}
