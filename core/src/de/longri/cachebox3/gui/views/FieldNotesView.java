/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.actions.ShowImportMenu;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.skin.styles.FieldNoteListItemStyle;
import de.longri.cachebox3.gui.skin.styles.LogListItemStyle;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.FieldNoteEntry;
import de.longri.cachebox3.types.FieldNoteList;
import de.longri.cachebox3.types.LogTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 14.09.2016.
 */
public class FieldNotesView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(FieldNotesView.class);
    private final ListView listView = new ListView();
    private final FieldNoteList fieldNotes;
    private final FieldNoteListItemStyle itemStyle;

    private Array<ListViewItem> items;

    public FieldNotesView() {
        super("FieldNotesView");

        itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", FieldNoteListItemStyle.class);

        fieldNotes = new FieldNoteList();
        loadFieldNotes(FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);

        listView.setEmptyString(Translation.Get("EmptyFieldNotes"));
        this.addActor(listView);
    }

    @Override
    public void onShow() {
        setListViewAdapter();
    }

    @Override
    public void onHide() {
        super.onHide();
    }

    private void setListViewAdapter() {
        CB.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(listViewAdapter);
            }
        });
    }

    @Override
    public void sizeChanged() {
        listView.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    private Adapter listViewAdapter = new Adapter() {

        @Override
        public int getCount() {
            return items == null ? 0 : items.size;
        }

        @Override
        public ListViewItem getView(int index) {
            return items == null ? null : items.get(index);
        }

        @Override
        public void update(ListViewItem view) {
            // nothing to do
        }

        @Override
        public float getItemSize(int index) {
            return items == null ? 0 : items.get(index).getHeight();
        }
    };

    @Override
    public void dispose() {
        EventHandler.remove(this);
    }

    private void loadFieldNotes(FieldNoteList.LoadingType type) {
        fieldNotes.loadFieldNotes("", type);

        if (items == null) {
            items = new Array<>();
        }
        items.clear();

        int idx = 0;
        for (FieldNoteEntry entry : fieldNotes) {
            items.add(new FieldNotesViewItem(idx++, entry, itemStyle));
        }

        listView.setAdapter(listViewAdapter);

    }

    public Menu getContextMenu() {

        Cache cache = EventHandler.getSelectedCache();

        final Menu cm = new Menu("FieldNoteContextMenu");

        cm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case MenuID.MI_FOUND:
                        addNewFieldnote(LogTypes.found);
                        return true;
                    case MenuID.MI_ATTENDED:
                        addNewFieldnote(LogTypes.attended);
                        return true;
                    case MenuID.MI_WEBCAM_FOTO_TAKEN:
                        addNewFieldnote(LogTypes.webcam_photo_taken);
                        return true;
                    case MenuID.MI_WILL_ATTENDED:
                        addNewFieldnote(LogTypes.will_attend);
                        return true;
                    case MenuID.MI_NOT_FOUND:
                        addNewFieldnote(LogTypes.didnt_find);
                        return true;
                    case MenuID.MI_MAINTANCE:
                        addNewFieldnote(LogTypes.needs_maintenance);
                        return true;
                    case MenuID.MI_NOTE:
                        addNewFieldnote(LogTypes.note);
                        return true;
                    case MenuID.MI_UPLOAD_FIELDNOTE:
                        uploadFieldNotes();
                        return true;
                    case MenuID.MI_DELETE_ALL_FIELDNOTES:
                        deleteAllFieldNotes();
                        return true;
                }
                return false;
            }
        });

        if (cache != null) {

            // Found je nach CacheType
            if (cache.Type == null)
                return null;
            switch (cache.Type) {
                case Giga:
                case MegaEvent:
                case Event:
                case CITO:
                    cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", itemStyle.typeStyle.will_attend);
                    cm.addItem(MenuID.MI_ATTENDED, "attended", itemStyle.typeStyle.attended);
                    break;
                case Camera:
                    cm.addItem(MenuID.MI_WEBCAM_FOTO_TAKEN, "webCamFotoTaken", itemStyle.typeStyle.webcam_photo_taken);
                    break;
                default:
                    cm.addItem(MenuID.MI_FOUND, "found", itemStyle.typeStyle.found);
                    break;
            }

            cm.addItem(MenuID.MI_NOT_FOUND, "DNF", itemStyle.typeStyle.didnt_find);
        }

        // Aktueller Cache ist von geocaching.com dann weitere Menüeinträge freigeben
        if (cache != null && cache.getGcCode().toLowerCase().startsWith("gc")) {
            cm.addItem(MenuID.MI_MAINTANCE, "maintenance", itemStyle.typeStyle.needs_maintenance);
            cm.addItem(MenuID.MI_NOTE, "writenote", itemStyle.typeStyle.note);
        }

        cm.addItem(MenuID.MI_UPLOAD_FIELDNOTE, "uploadFieldNotes", CB.getSkin().getMenuIcon.uploadFieldNote);
        cm.addItem(MenuID.MI_DELETE_ALL_FIELDNOTES, "DeleteAllNotes", CB.getSkin().getMenuIcon.deleteAllFieldNotes);

         if (cache != null) {
             MenuItem mi = cm.addItem(MenuID.MI_IMPORT, "ownerLogTypes", CB.getSkin().getMenuIcon.ownerLogTypes);
             mi.setMoreMenu(getSecondMenu());
         }

        return cm;
    }

    private void deleteAllFieldNotes() {
        //TODO deleteAllFieldNotes
    }

    private void uploadFieldNotes() {
        //TODO uploadFieldNotes
    }

    private void addNewFieldnote(LogTypes logType) {
        //TODO addNewFieldnote(LogTypes logType)
    }

    private Menu getSecondMenu() {
        Menu sm = new Menu("FieldNoteContextMenu/2");
        MenuItem mi;
        boolean IM_owner = EventHandler.getSelectedCache().ImTheOwner();
        sm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case MenuID.MI_ENABLED:
                        addNewFieldnote(LogTypes.enabled);
                        return true;
                    case MenuID.MI_TEMPORARILY_DISABLED:
                        addNewFieldnote(LogTypes.temporarily_disabled);
                        return true;
                    case MenuID.MI_OWNER_MAINTENANCE:
                        addNewFieldnote(LogTypes.owner_maintenance);
                        return true;
                    case MenuID.MI_ATTENDED:
                        addNewFieldnote(LogTypes.attended);
                        return true;
                    case MenuID.MI_WEBCAM_FOTO_TAKEN:
                        addNewFieldnote(LogTypes.webcam_photo_taken);
                        return true;
                    case MenuID.MI_REVIEWER_NOTE:
                        addNewFieldnote(LogTypes.reviewer_note);
                        return true;
                }
                return false;
            }
        });

        mi = sm.addItem(MenuID.MI_ENABLED, "enabled", itemStyle.typeStyle.enabled);
        mi.setEnabled(IM_owner);
        mi = sm.addItem(MenuID.MI_TEMPORARILY_DISABLED, "temporarilyDisabled", itemStyle.typeStyle.temporarily_disabled);
        mi.setEnabled(IM_owner);
        mi = sm.addItem(MenuID.MI_OWNER_MAINTENANCE, "ownerMaintenance", itemStyle.typeStyle.owner_maintenance);
        mi.setEnabled(IM_owner);

        return sm;
    }
}
