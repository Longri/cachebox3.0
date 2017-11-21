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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.activities.EditFilterSettings;
import de.longri.cachebox3.gui.skin.styles.FilterStyle;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.IntProperty;
import de.longri.cachebox3.types.Property;

import java.util.concurrent.atomic.AtomicBoolean;

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
                ListViewItem item = listViewItems.get(index);
                return item.isVisible() ? item.getHeight() : 0;
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

        final AtomicBoolean sectionVisible = new AtomicBoolean(false);

        final IntPropertyListView available = new IntPropertyListView(listViewItems.size + 1,
                filterSettings.filterProperties.NotAvailable, style.Available, Translation.get("disabled"));
        final IntPropertyListView archived = new IntPropertyListView(listViewItems.size + 1,
                filterSettings.filterProperties.Archived, style.PrepareToArchive, Translation.get("archived"));
        final IntPropertyListView finds = new IntPropertyListView(listViewItems.size + 1,
                filterSettings.filterProperties.Finds, style.finds, Translation.get("myfinds"));



        ClickListener listener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                boolean visible = !sectionVisible.get();
                sectionVisible.set(visible);
                available.setVisible(visible);
                archived.setVisible(visible);
                finds.setVisible(visible);

                setListView.invalidate();
                setListView.layout(true);
            }
        };

        listViewItems.add(new ButtonListViewItem(listViewItems.size, Translation.get("General"), listener));
        listViewItems.add(available);
        listViewItems.add(archived);
        listViewItems.add(finds);

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

        public IntPropertyListView(int listIndex, final IntProperty property, Drawable icon, CharSequence name) {
            super(listIndex);

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int value = property.get();

                    if (value == -1)
                        value = 0;
                    else if (value == 0)
                        value = 1;
                    else if (value == 1)
                        value = -1;

                    property.set(value);

                }
            });

            this.setVisible(false);

            //Left icon
            final Image iconImage = new Image(icon, Scaling.none);
            this.add(iconImage).center().padRight(CB.scaledSizes.MARGIN_HALF);

            //Center name text
            final Label label = new VisLabel(name);
            label.setWrap(true);
            this.add(label).expandX().fillX().padTop(CB.scaledSizes.MARGIN).padBottom(CB.scaledSizes.MARGIN);

            //Right checkBox
            final Image checkImage = new Image(style.CheckNo);
            this.add(checkImage).width(checkImage.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

            property.setChangeListener(new Property.PropertyChangedListener() {
                @Override
                public void propertyChanged() {
                    switch (property.get()) {
                        case -1:
                            checkImage.setDrawable(style.CheckOff);
                            break;
                        case 0:
                            checkImage.setDrawable(style.CheckNo);
                            break;
                        case 1:
                            checkImage.setDrawable(style.Check);
                            break;
                        default:
                            checkImage.setDrawable(style.CheckNo);
                    }
                }
            });
        }

        @Override
        public void dispose() {

        }
    }

}
