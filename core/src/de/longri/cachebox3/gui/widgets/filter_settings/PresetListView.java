/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.activities.EditFilterSettings;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.skin.styles.FilterStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.SettingString;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.cachebox3.utils.CharSequenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.SINGLE;

/**
 * Created by Longri on 16.11.2017.
 */
public class PresetListView extends Catch_Table implements EditFilterSettings.OnShow {

    private final Logger log = LoggerFactory.getLogger(PresetListView.class);

    private final ListView presetListView;
    private final FilterStyle style;
    private Array<PresetEntry> presetEntries;
    private Array<ListViewItem> presetListItems;
    private final EditFilterSettings filterSettings;

    public PresetListView(EditFilterSettings editFilterSettings, FilterStyle style) {
        this.style = style;
        this.filterSettings = editFilterSettings;
        fillPresetList();

        presetListView = new ListView(VERTICAL);
        presetListView.setSelectable(SINGLE);
        this.add(presetListView).expand().fill();
        presetListView.setEmptyString("EmptyList");
        Gdx.app.postRunnable(() -> {
            setListViewAdapter();
            presetListView.pack();
            presetListView.invalidateHierarchy();
            setSelected();
        });

        //selection changed listener
        presetListView.addSelectionChangedEventListner(new SelectionChangedEvent() {
            @Override
            public void selectionChanged() {
                ListViewItemInterface item = presetListView.getSelectedItem();
                if (item instanceof PresetItem) {
                    filterSettings.filterProperties.set(((PresetItem) item).entry.filterProperties);
                    log.debug("Set EditFilterSettings to Preset {}", filterSettings.filterProperties.toString());
                }
            }
        });
    }

    private void setSelected() {
        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                //setSelection
                int n = presetListItems.size;
                FilterProperties actFilter = filterSettings.filterProperties;
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

                if (selectedIndex > -1) {
                    presetListView.setSelection(selectedIndex);
                    presetListView.setSelectedItemVisible(true);

                    //maybe set filter name
                    String name = ((PresetItem) presetListItems.get(selectedIndex)).entry.name.toString();
                    filterSettings.filterProperties.setName(name);

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            CB.requestRendering();
                        }
                    });
                }
            }
        });
    }

    private void setListViewAdapter() {

        presetListView.setAdapter(new ListViewAdapter() {
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

        });

    }

    protected void sizeChanged() {
        this.invalidate();
        this.layout();
    }

    private void fillPresetList() {
        if (presetEntries != null)
            presetEntries.clear();
        else
            presetEntries = new Array<>();

        if (presetListItems != null)
            presetListItems.clear();
        else
            presetListItems = new Array<>();


        FilterInstances.HISTORY.isHistory = true;

        presetEntriesAdd("myfinds", style.finds, FilterInstances.MYFOUNDS);
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
                    presetEntries.add(new PresetEntry(true, name, style.userFilter, new FilterProperties(name, filter)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //add listViewItems
        for (int i = 0, n = presetEntries.size; i < n; i++) {
            PresetEntry entry = presetEntries.get(i);
            final PresetItem item = new PresetItem(i, entry);
            presetListItems.add(item);

            if (entry.userPreset) {
                item.addListener(new ClickLongClickListener() {

                    @Override
                    public boolean clicked(InputEvent event, float x, float y) {
                        return false;
                    }

                    @Override
                    public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {

                        String name = "";

                        if (actor instanceof PresetItem) {
                            name = ((PresetItem) actor).entry.name.toString();
                        }

                        MessageBox.show(Translation.get("?DelUserPreset", "\n'" + name + "'"),
                                Translation.get("DelUserPreset"), MessageBoxButtons.YesNo,
                                MessageBoxIcon.Warning, new OnMsgBoxClickListener() {
                                    @Override
                                    public boolean onClick(int which, Object data) {

                                        if (which == ButtonDialog.BUTTON_POSITIVE) {
                                            presetListItems.removeValue(item, true);

                                            CharSequence itemName = item.entry.name;

                                            String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);

                                            String newUserEntris = "";
                                            for (String entry : userEntrys) {
                                                int pos = entry.indexOf(";");
                                                String name = entry.substring(0, pos);
                                                if (!CharSequenceUtil.equals(name, itemName))
                                                    newUserEntris += entry + SettingString.STRING_SPLITTER;
                                            }
                                            Config.UserFilter.setValue(newUserEntris);
                                            Config.AcceptChanges();
                                            fillPresetList();
                                            Gdx.app.postRunnable(new Runnable() {
                                                @Override
                                                public void run() {
                                                    presetListView.dataSetChanged();
                                                }
                                            });
                                        }

                                        return true;
                                    }
                                }
                        );
                        return false;
                    }
                });
            }
        }

        // add a Button of the end of list, to create UserItem
        presetListItems.add(new ButtonListViewItem(presetListItems.size));
    }

    private void presetEntriesAdd(String name, Drawable icon, FilterProperties filter) {
        presetEntries.add(new PresetEntry(false, Translation.get(name), icon, filter));
    }

    @Override
    public void onShow() {
        fillPresetList();
        setSelected();
    }

    class ButtonListViewItem extends ListViewItem {

        public ButtonListViewItem(int listIndex) {
            super(listIndex);
            CB_Button btn = new CB_Button(Translation.get("AddOwnFilterPreset"));

            btn.getLabel().setWrap(true);

            this.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    addUserPreset();
                }
            });
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


    private void addUserPreset() {

        // Check if Preset exist
        boolean exist = false;
        CharSequence existName = "";
        for (ListViewItem v : presetListItems) {
            if (v instanceof PresetItem) {
                if (((PresetItem) v).entry.filterProperties.equals(filterSettings.filterProperties)) {
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

                //set name of filter
                filterSettings.filterProperties.setName(text);

                String uF = Config.UserFilter.getValue();
                String aktFilter = filterSettings.filterProperties.getJsonString();

                // Category Filterungen aus Filter entfernen
                int pos = aktFilter.indexOf("^");
                if (pos >= 0)
                    aktFilter = aktFilter.substring(0, pos);

                uF += text + ";" + aktFilter + SettingString.STRING_SPLITTER;
                Config.UserFilter.setValue(uF);
                Config.AcceptChanges();
                fillPresetList();
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        presetListView.dataSetChanged();
                        setSelected();
                    }
                });
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
        final boolean userPreset;

        PresetEntry(boolean userPreset, CharSequence name, Drawable icon, FilterProperties filterProperties) {
            this.name = name;
            this.icon = icon;
            this.filterProperties = filterProperties;
            this.userPreset = userPreset;
        }
    }

}
