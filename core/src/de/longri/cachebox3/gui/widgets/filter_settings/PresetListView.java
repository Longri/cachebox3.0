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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.activities.EditFilterSettings;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.dialogs.OnMsgBoxClickListener;
import de.longri.cachebox3.gui.skin.styles.FilterStyle;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.gui.widgets.EditTextBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.SettingString;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;

/**
 * Created by Longri on 16.11.2017.
 */
public class PresetListView extends Table {

    public static final FilterProperties[] presets = new FilterProperties[]{ //
            FilterInstances.HISTORY, //
            FilterInstances.ACTIVE, //
            FilterInstances.QUICK, //
            FilterInstances.BEGINNER, //
            FilterInstances.WITHTB, //
            FilterInstances.DROPTB, //
            FilterInstances.HIGHLIGHTS, //
            FilterInstances.FAVORITES, //
            FilterInstances.TOARCHIVE, //
            FilterInstances.LISTINGCHANGED, //
            FilterInstances.ALL, //
    };

    private final ListView presetListView;
    private final FilterStyle style;
    private Array<PresetEntry> presetEntries;
    private Array<ListViewItem> presetListItems;
    private final EditFilterSettings filterSettings;

    public PresetListView(EditFilterSettings editFilterSettings, FilterStyle style) {
        this.style = style;
        this.filterSettings = editFilterSettings;
        final int selected = fillPresetList();

        presetListView = new ListView();
        presetListView.setSelectable(ListView.SelectableType.SINGLE);
        this.add(presetListView).expand().fill();
        presetListView.setEmptyString("EmptyList");
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                setListViewAdapter();
                presetListView.pack();
                presetListView.invalidateHierarchy();
                if (selected > -1) {
                    presetListView.setSelection(selected);
                    presetListView.setSelectedItemVisible(true);
                }
            }
        });
    }

    private void setListViewAdapter() {

        presetListView.setAdapter(new Adapter() {
            @Override
            public int getCount() {
                return presetListItems.size;
            }

            @Override
            public ListViewItem getView(int index) {
                return presetListItems.get(index);
            }

            @Override
            public void update(ListViewItem view) {

            }

            @Override
            public float getItemSize(int index) {
                return presetListItems.get(index).getPrefHeight();
            }
        });

    }

    protected void sizeChanged() {
        this.invalidate();
        this.layout();
    }

    private int fillPresetList() {
        if (presetEntries != null)
            presetEntries.clear();
        else
            presetEntries = new Array<>();

        if (presetListItems != null)
            presetListItems.clear();
        else
            presetListItems = new Array<>();


        FilterInstances.HISTORY.isHistory = true;

        presetEntriesAdd("HISTORY", style.HISTORY, FilterInstances.HISTORY);
        presetEntriesAdd("AllCachesToFind", style.AllCachesToFind, FilterInstances.ACTIVE);
        presetEntriesAdd("QuickCaches", style.QuickCaches, FilterInstances.QUICK);
        presetEntriesAdd("BEGINNER", style.BEGINNER, FilterInstances.BEGINNER);
        presetEntriesAdd("GrabTB", style.GrabTB, FilterInstances.WITHTB);
        presetEntriesAdd("DropTB", style.DropTB, FilterInstances.DROPTB);
        presetEntriesAdd("Highlights", style.Highlights, FilterInstances.HIGHLIGHTS);
        presetEntriesAdd("Favorites", style.Favorites, FilterInstances.FAVORITES);
        presetEntriesAdd("PrepareToArchive", style.PrepareToArchive, FilterInstances.TOARCHIVE);
        presetEntriesAdd("ListingChanged", style.ListingChanged, FilterInstances.LISTINGCHANGED);
        presetEntriesAdd("AllCaches", style.AllCaches, FilterInstances.ALL);

        // add User Presets from Config.UserFilter
        if (!Config.UserFilter.getValue().equalsIgnoreCase("")) {
            String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);
            try {
                for (String entry : userEntrys) {
                    int pos = entry.indexOf(";");
                    String name = entry.substring(0, pos);
                    String filter = entry.substring(pos + 1);
                    presetEntries.add(new PresetEntry(name, style.userFilter, new FilterProperties(filter)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //add listViewItems
        for (int i = 0, n = presetEntries.size; i < n; i++) {
            PresetEntry entry = presetEntries.get(i);
            PresetItem item = new PresetItem(i, entry);
            presetListItems.add(item);
        }

        // add a Button of the end of list, to create UserItem
        presetListItems.add(new ButtonListViewItem(presetListItems.size));

        //setSelection
        int n = presetListItems.size;
        FilterProperties actFilter = CB.viewmanager.getActFilter();
        int selectedIndex = -1;
        while (n-- > 0) {
            ListViewItem item = presetListItems.get(n);
            if (item instanceof PresetItem) {
                if (((PresetItem) item).entry.filterProperties.equals(actFilter)) {
                    selectedIndex = n;
                    break;
                }
            }
        }
        return selectedIndex;
    }

    private void presetEntriesAdd(String name, Drawable icon, FilterProperties filter) {
        presetEntries.add(new PresetEntry(Translation.get(name), icon, filter));
    }

    class ButtonListViewItem extends ListViewItem {

        public ButtonListViewItem(int listIndex) {
            super(listIndex);
            CharSequenceButton btn = new CharSequenceButton(Translation.get("AddOwnFilterPreset"));

            this.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    final int selected = presetListView.getSelectedItem().getListIndex();
                    addUserPreset();

                    // can't select this item, so select the last
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            presetListView.setSelection(selected);
                            presetListView.setSelectedItemVisible(true);
                        }
                    });
                }
            });
            this.add(btn).expand().fill();
        }

        @Override
        public void dispose() {

        }
    }


    private void addUserPreset() {

        // Check if Preset exist
        boolean exist = false;
        CharSequence existName = "";
        final FilterProperties filterSet = filterSettings.getFilterSet();
        for (ListViewItem v : presetListItems) {
            if (v instanceof PresetItem) {
                if (((PresetItem) v).entry.filterProperties.equals(filterSet)) {
                    exist = true;
                    existName = ((PresetItem) v).entry.name;
                }
            }
        }

        if (exist) {
            CharSequence msg = Translation.get("PresetExist", "\n\n", existName.toString());
            MessageBox.show(msg, null, MessageBoxButtons.OK, MessageBoxIcon.Warning, new OnMsgBoxClickListener() {
                @Override
                public boolean onClick(int which, Object data) {
                    return true;
                }
            });
            return;
        }

        Input.TextInputListener listener = new Input.TextInputListener() {
            @Override
            public void input(String text) {
                String uF = Config.UserFilter.getValue();
                String aktFilter = filterSet.toString();

                // Category Filterungen aus Filter entfernen
                int pos = aktFilter.indexOf("^");
                aktFilter = aktFilter.substring(0, pos);

                uF += text + ";" + aktFilter + "#";
                Config.UserFilter.setValue(uF);
                Config.AcceptChanges();
                fillPresetList();
                presetListView.dataSetChanged();
            }

            @Override
            public void canceled() {
                // do nothing
            }
        };

        PlatformConnector.getSinglelineTextInput(listener, Translation.get("NewUserPreset"), "", Translation.get("InsNewUserPreset"));
    }


    class PresetItem extends ListViewItem {
        final PresetEntry entry;

        public PresetItem(int listIndex, PresetEntry entry) {
            super(listIndex);
            this.entry = entry;
            final Image iconImage = new Image(entry.icon, Scaling.none);
            this.add(iconImage).center().padRight(CB.scaledSizes.MARGIN_HALF);
            final VisLabel label = new VisLabel(entry.name);
            label.setWrap(true);
            this.add(label).expandX().fillX().padTop(CB.scaledSizes.MARGIN).padBottom(CB.scaledSizes.MARGIN);
        }


        @Override
        public void dispose() {

        }
    }

    static class PresetEntry {
        final CharSequence name;
        final Drawable icon;
        final FilterProperties filterProperties;

        PresetEntry(CharSequence name, Drawable icon, FilterProperties filterProperties) {
            this.name = name;
            this.icon = icon;
            this.filterProperties = filterProperties;
        }
    }

}
