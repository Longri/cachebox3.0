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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.activities.EditFieldNotes;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.popUps.QuickFieldNoteFeedbackPopUp;
import de.longri.cachebox3.gui.skin.styles.FieldNoteListItemStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.utils.TemplateFormatter;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheDAO;
import de.longri.cachebox3.sqlite.dao.CacheListDAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by Longri on 14.09.2016.
 */
public class FieldNotesView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(FieldNotesView.class);

    private static FieldNotesView THAT;
    private static FieldNoteEntry aktFieldNote;
    private static FieldNoteList fieldNoteEntries;


    private final ListView listView = new ListView();
    private final FieldNoteListItemStyle itemStyle;

    private Array<ListViewItem> items;

    public FieldNotesView() {
        super("FieldNotesView");
        THAT = this;
        itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", FieldNoteListItemStyle.class);

        fieldNoteEntries = new FieldNoteList();
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
            // set listener on Update, because Item is remove all listener with Layout
            view.addListener(clickLongClickListener);
        }

        @Override
        public float getItemSize(int index) {
            return items == null ? 0 : items.get(index).getHeight();
        }
    };

    private final ClickLongClickListener clickLongClickListener = new ClickLongClickListener() {
        @Override
        public boolean clicked(InputEvent event, float x, float y) {
            return false;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y) {

            int listIndex = ((ListViewItem) actor).getListIndex();
            aktFieldNote = fieldNoteEntries.get(listIndex);

            Menu cm = new Menu("FieldNoteItem-Menu");

            cm.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public boolean onItemClick(MenuItem item) {
                    switch (item.getMenuItemId()) {
                        case MenuID.MI_SELECT_CACHE:
                            selectCacheFromFieldNote();
                            return true;
                        case MenuID.MI_EDIT_FIELDNOTE:
                            editFieldNote();
                            return true;
                        case MenuID.MI_DELETE_FIELDNOTE:
                            deleteFieldNote();
                            return true;

                    }
                    return false;
                }
            });

            cm.addItem(MenuID.MI_SELECT_CACHE, "SelectCache", ":\n" + aktFieldNote.CacheName, aktFieldNote.cacheType.getDrawable());
            cm.addItem(MenuID.MI_EDIT_FIELDNOTE, "edit",CB.getSkin().getMenuIcon.edit);
            cm.addItem(MenuID.MI_DELETE_FIELDNOTE, "delete", CB.getSkin().getMenuIcon.deleteAllFieldNotes);

            cm.show();
            return true;
        }
    };

    @Override
    public void dispose() {
        EventHandler.remove(this);
        fieldNoteEntries.clear();
        fieldNoteEntries = null;
        aktFieldNote = null;
        THAT = null;
        listView.dispose();
        for (ListViewItem item : items) {
            item.dispose();
        }
        items.clear();
        items = null;
    }

    private void loadFieldNotes(FieldNoteList.LoadingType type) {
        fieldNoteEntries.loadFieldNotes("", type);

        if (items == null) {
            items = new Array<>();
        }
        items.clear();

        int idx = 0;
        for (FieldNoteEntry entry : fieldNoteEntries) {
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

    private void uploadFieldNotes() {
        //TODO uploadFieldNotes
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


    public static void addNewFieldnote(LogTypes type) {
        addNewFieldnote(type, false);
    }

    public static void addNewFieldnote(LogTypes type, boolean witoutShowEdit) {
        Cache cache = EventHandler.getSelectedCache();

        if (cache == null) {
            MessageBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            return;
        }

        // chk car found?
        if (cache.getGcCode().equalsIgnoreCase("CBPark")) {
            if (type == LogTypes.found) {
                MessageBox.Show(Translation.Get("My_Parking_Area_Found"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            } else if (type == LogTypes.didnt_find) {
                MessageBox.Show(Translation.Get("My_Parking_Area_DNF"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            }
            return;
        }

        // kein GC Cache
        if (!cache.getGcCode().toLowerCase().startsWith("gc")) {

            if (type == LogTypes.found || type == LogTypes.attended || type == LogTypes.webcam_photo_taken) {
                // Found it! -> fremden Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                    QuickFieldNoteFeedbackPopUp pop = new QuickFieldNoteFeedbackPopUp(true);
                    pop.show();
                    PlatformConnector.vibrate();
                }
            } else if (type == LogTypes.didnt_find) {
                // DidNotFound -> fremden Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                    QuickFieldNoteFeedbackPopUp pop2 = new QuickFieldNoteFeedbackPopUp(false);
                    pop2.show();
                    PlatformConnector.vibrate();
                }
            }

            if (THAT != null)
                THAT.notifyDataSetChanged();
            return;
        }

        FieldNoteList tmpFieldNotes = new FieldNoteList();
        tmpFieldNotes.loadFieldNotes("", FieldNoteList.LoadingType.LOAD_ALL);

        FieldNoteEntry newFieldNote = null;
        if ((type == LogTypes.found) //
                || (type == LogTypes.attended) //
                || (type == LogTypes.webcam_photo_taken) //
                || (type == LogTypes.didnt_find)) {
            // nachsehen, ob für diesen Cache bereits eine FieldNote des Types angelegt wurde
            // und gegebenenfalls diese ändern und keine neue anlegen
            // gilt nur für Found It! und DNF.
            // needMaintance oder Note können zusätzlich angelegt werden

            for (FieldNoteEntry nfne : tmpFieldNotes) {
                if ((nfne.CacheId == cache.Id) && (nfne.type == type)) {
                    newFieldNote = nfne;
                    newFieldNote.deleteFromDatabase();
                    newFieldNote.timestamp = new Date();
                    aktFieldNote = newFieldNote;
                }
            }
        }

        if (newFieldNote == null) {
            newFieldNote = new FieldNoteEntry(type);
            newFieldNote.CacheName = cache.getName();
            newFieldNote.gcCode = cache.getGcCode();
            newFieldNote.foundNumber = Config.FoundOffset.getValue();
            newFieldNote.timestamp = new Date();
            newFieldNote.CacheId = cache.Id;
            newFieldNote.comment = "";
            newFieldNote.CacheUrl = cache.getUrl();
            newFieldNote.cacheType = cache.Type;
            newFieldNote.fillType();
            // aktFieldNoteIndex = -1;
            aktFieldNote = newFieldNote;
        } else {
            tmpFieldNotes.removeValue(newFieldNote, false);

        }

        switch (type) {
            case found:
                if (!cache.isFound())
                    newFieldNote.foundNumber++; //
                newFieldNote.fillType();
                if (newFieldNote.comment.equals(""))
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newFieldNote);
                // wenn eine FieldNote Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case attended:
                if (!cache.isFound())
                    newFieldNote.foundNumber++; //
                newFieldNote.fillType();
                if (newFieldNote.comment.equals(""))
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.AttendedTemplate.getValue(), newFieldNote);
                // wenn eine FieldNote Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case webcam_photo_taken:
                if (!cache.isFound())
                    newFieldNote.foundNumber++; //
                newFieldNote.fillType();
                if (newFieldNote.comment.equals(""))
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.WebcamTemplate.getValue(), newFieldNote);
                // wenn eine FieldNote Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case didnt_find:
                if (newFieldNote.comment.equals(""))
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.DNFTemplate.getValue(), newFieldNote);
                break;
            case needs_maintenance:
                if (newFieldNote.comment.equals(""))
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.NeedsMaintenanceTemplate.getValue(), newFieldNote);
                break;
            case note:
                if (newFieldNote.comment.equals(""))
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), newFieldNote);
                break;
            default:
                break;
        }

        if (!witoutShowEdit) {
            efnActivity = new EditFieldNotes(newFieldNote, returnListener, true);
            efnActivity.show();
        } else {

            // new FieldNote
            tmpFieldNotes.add(newFieldNote);
            newFieldNote.writeToDatabase();
            aktFieldNote = newFieldNote;
            if (newFieldNote.type == LogTypes.found || newFieldNote.type == LogTypes.attended || newFieldNote.type == LogTypes.webcam_photo_taken) {
                // Found it! -> Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                    Config.FoundOffset.setValue(aktFieldNote.foundNumber);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene FieldNote DNF löschen
                tmpFieldNotes.deleteFieldNoteByCacheId(EventHandler.getSelectedCache().Id, LogTypes.didnt_find);
            } else if (newFieldNote.type == LogTypes.didnt_find) {
                // DidNotFound -> Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                    Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene FieldNote FoundIt löschen
                tmpFieldNotes.deleteFieldNoteByCacheId(EventHandler.getSelectedCache().Id, LogTypes.found);
            }

            FieldNoteList.createVisitsTxt(Config.FieldNotesGarminPath.getValue());

            if (THAT != null)
                THAT.notifyDataSetChanged();

        }
    }

    private static EditFieldNotes.ReturnListener returnListener = new EditFieldNotes.ReturnListener() {

        @Override
        public void returnedFieldNote(FieldNoteEntry fieldNote, boolean isNewFieldNote, boolean directlog) {
            addOrChangeFieldNote(fieldNote, isNewFieldNote, directlog);
        }

    };

    private static void addOrChangeFieldNote(FieldNoteEntry fieldNote, boolean isNewFieldNote, boolean directLog) {

        if (directLog) {
            // try to direct upload
            logOnline(fieldNote, isNewFieldNote);
            return;
        }


        if (fieldNote != null) {

            if (isNewFieldNote) {
                // nur, wenn eine FieldNote neu angelegt wurde
                // new FieldNote
                fieldNoteEntries.add(fieldNote);

                // eine evtl. vorhandene FieldNote /DNF löschen
                if (fieldNote.type == LogTypes.attended //
                        || fieldNote.type == LogTypes.found //
                        || fieldNote.type == LogTypes.webcam_photo_taken //
                        || fieldNote.type == LogTypes.didnt_find) {
                    fieldNoteEntries.deleteFieldNoteByCacheId(fieldNote.CacheId, LogTypes.found);
                    fieldNoteEntries.deleteFieldNoteByCacheId(fieldNote.CacheId, LogTypes.didnt_find);
                }
            }

            fieldNote.writeToDatabase();
            aktFieldNote = fieldNote;

            if (isNewFieldNote) {
                // nur, wenn eine FieldNote neu angelegt wurde
                // wenn eine FieldNote neu angelegt werden soll dann kann hier auf SelectedCache zugegriffen werden, da nur für den
                // SelectedCache eine fieldNote angelegt wird
                if (fieldNote.type == LogTypes.found //
                        || fieldNote.type == LogTypes.attended //
                        || fieldNote.type == LogTypes.webcam_photo_taken) {
                    // Found it! -> Cache als gefunden markieren
                    if (!EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(true);
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                        Config.FoundOffset.setValue(aktFieldNote.foundNumber);
                        Config.AcceptChanges();
                    }

                } else if (fieldNote.type == LogTypes.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
                    if (EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(false);
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                        Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                        Config.AcceptChanges();
                    } // und eine evtl. vorhandene FieldNote FoundIt löschen
                    fieldNoteEntries.deleteFieldNoteByCacheId(EventHandler.getSelectedCache().Id, LogTypes.found);
                }
            }
            FieldNoteList.createVisitsTxt(Config.FieldNotesGarminPath.getValue());

            // Reload List
            if (isNewFieldNote) {
                fieldNoteEntries.loadFieldNotes("", FieldNoteList.LoadingType.LOAD_NEW);
            } else {
                fieldNoteEntries.loadFieldNotes("", FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);
            }
        }
        THAT.notifyDataSetChanged();
    }

    private static void logOnline(final FieldNoteEntry fieldNote, final boolean isNewFieldNote) {

        throw new RuntimeException("TODO");

//        wd = CancelWaitDialog.ShowWait("Upload Log", DownloadAnimation.GetINSTANCE(), new IcancelListener() {
//
//            @Override
//            public void isCanceld() {
//
//            }
//        }, new cancelRunnable() {
//
//            @Override
//            public void run() {
//                GroundspeakAPI.LastAPIError = "";
//
//                boolean dl = fieldNote.isDirectLog;
//                int result = GroundspeakAPI.CreateFieldNoteAndPublish(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, dl, this);
//
//                if (result == GroundspeakAPI.IO) {
//                    fieldNote.uploaded = true;
//
//                    // after direct Log create a fieldNote with uploded state
//                    addOrChangeFieldNote(fieldNote, isNewFieldNote, false);
//                }
//
//                if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
//                    GL.that.Toast(ConnectionError.INSTANCE);
//                    if (wd != null)
//                        wd.close();
//                    MessageBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
//
//                        @Override
//                        public boolean onClick(int which, Object data) {
//                            switch (which) {
//                                case ButtonDialog.BUTTON_NEGATIVE:
//                                    addOrChangeFieldNote(fieldNote, isNewFieldNote, true);// try again
//                                    return true;
//
//                                case ButtonDialog.BUTTON_NEUTRAL:
//                                    return true;
//
//                                case ButtonDialog.BUTTON_POSITIVE:
//                                    addOrChangeFieldNote(fieldNote, isNewFieldNote, false);// create Fieldnote
//                                    return true;
//                            }
//                            return true;
//                        }
//                    });
//                    return;
//                }
//                if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
//                    GL.that.Toast(ApiUnavailable.INSTANCE);
//                    if (wd != null)
//                        wd.close();
//                    MessageBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
//
//                        @Override
//                        public boolean onClick(int which, Object data) {
//                            switch (which) {
//                                case ButtonDialog.BUTTON_NEGATIVE:
//                                    addOrChangeFieldNote(fieldNote, isNewFieldNote, true);// try again
//                                    return true;
//
//                                case ButtonDialog.BUTTON_NEUTRAL:
//                                    return true;
//
//                                case ButtonDialog.BUTTON_POSITIVE:
//                                    addOrChangeFieldNote(fieldNote, isNewFieldNote, false);// create Fieldnote
//                                    return true;
//                            }
//                            return true;
//                        }
//                    });
//                    return;
//                }
//
//                if (GroundspeakAPI.LastAPIError.length() > 0) {
//                    Gdx.app.postRunnable(new Runnable() {
//                        @Override
//                        public void run() {
//                            MessageBox.Show(GroundspeakAPI.LastAPIError, Translation.Get("Error"), MessageBoxIcon.Error);
//                        }
//                    });
//                }
//
//                if (wd != null)
//                    wd.close();
//            }
//
//            @Override
//            public boolean cancel() {
//                // TODO handle cancel
//                return false;
//            }
//        });

    }

    private static EditFieldNotes efnActivity;

    private void editFieldNote() {
        if (efnActivity != null && !efnActivity.isDisposed()) {
            efnActivity.setFieldNote(aktFieldNote, returnListener, false);
        } else {
            efnActivity = new EditFieldNotes(aktFieldNote, returnListener, false);
        }

        efnActivity.show();
    }

    private void deleteFieldNote() {
        // aktuell selectierte FieldNote löschen
        if (aktFieldNote == null)
            return;
        // final Cache cache =
        // Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);

        Cache tmpCache = null;
        // suche den Cache aus der DB.
        // Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(lCaches, "Id = " + aktFieldNote.CacheId, false, false);
        if (lCaches.size > 0)
            tmpCache = lCaches.get(0);
        final Cache cache = tmpCache;

        if (cache == null && !aktFieldNote.isTbFieldNote) {
            String message = Translation.Get("cacheOtherDb", aktFieldNote.CacheName);
            message += "\n" + Translation.Get("fieldNoteNoDelete");
            MessageBox.Show(message);
            return;
        }

        OnMsgBoxClickListener dialogClickListener = new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                switch (which) {
                    case ButtonDialog.BUTTON_POSITIVE:
                        // Yes button clicked
                        // delete aktFieldNote
                        if (cache != null) {
                            if (cache.isFound()) {
                                cache.setFound(false);
                                CacheDAO cacheDAO = new CacheDAO();
                                cacheDAO.WriteToDatabase_Found(cache);
                                Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                                Config.AcceptChanges();
                                // jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
                                // damit das Smiley Symbol aus der Map und der CacheList verschwindet
                                synchronized (Database.Data.Query) {
                                    Cache tc = Database.Data.Query.GetCacheById(cache.Id);
                                    if (tc != null) {
                                        tc.setFound(false);
                                    }
                                }
                            }
                        }
                        fieldNoteEntries.deleteFieldNote(aktFieldNote.Id, aktFieldNote.type);

                        aktFieldNote = null;

                        fieldNoteEntries.loadFieldNotes("", FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);

                        loadFieldNotes(FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);

                        FieldNoteList.createVisitsTxt(Config.FieldNotesGarminPath.getValue());

                        break;
                    case ButtonDialog.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        break;
                }
                return true;
            }

        };

        String message = "";
        if (aktFieldNote.isTbFieldNote) {
            message = Translation.Get("confirmFieldnoteDeletionTB", aktFieldNote.typeString, aktFieldNote.TbName);
        } else {
            message = Translation.Get("confirmFieldnoteDeletion", aktFieldNote.typeString, aktFieldNote.CacheName);
            if (aktFieldNote.type == LogTypes.found || aktFieldNote.type == LogTypes.attended || aktFieldNote.type == LogTypes.webcam_photo_taken)
                message += Translation.Get("confirmFieldnoteDeletionRst");
        }

        MessageBox.Show(message, Translation.Get("deleteFieldnote"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, dialogClickListener);

    }

    private void deleteAllFieldNotes() {
        final OnMsgBoxClickListener dialogClickListener = new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                switch (which) {
                    case ButtonDialog.BUTTON_POSITIVE:
                        // Yes button clicked
                        // delete all FieldNotes
                        // reload all Fieldnotes!
                        fieldNoteEntries.loadFieldNotes("", FieldNoteList.LoadingType.LOAD_ALL);

                        for (FieldNoteEntry entry : fieldNoteEntries) {
                            entry.deleteFromDatabase();

                        }

                        fieldNoteEntries.clear();
                        aktFieldNote = null;

                        loadFieldNotes(FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);
                        break;

                    case ButtonDialog.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        break;
                }
                return true;

            }
        };
        final String message = Translation.Get("DeleteAllFieldNotesQuestion");
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                MessageBox.Show(message, Translation.Get("DeleteAllNotes"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning, dialogClickListener);

            }
        });
    }

    private void selectCacheFromFieldNote() {
        if (aktFieldNote == null)
            return;

        // suche den Cache aus der DB.
        // Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(lCaches, "Id = " + aktFieldNote.CacheId, false, false);
        Cache tmpCache = null;
        if (lCaches.size > 0)
            tmpCache = lCaches.get(0);
        Cache cache = tmpCache;

        if (cache == null) {
            String message = Translation.Get("cacheOtherDb", aktFieldNote.CacheName);
            message += "\n" + Translation.Get("fieldNoteNoSelect");
            MessageBox.Show(message, Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            return;
        }

        synchronized (Database.Data.Query) {
            cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
        }

        if (cache == null) {
            Database.Data.Query.add(tmpCache);
            cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
        }

        Waypoint finalWp = null;
        if (cache != null) {
            if (cache.HasFinalWaypoint())
                finalWp = cache.GetFinalWaypoint();
            else if (cache.HasStartWaypoint())
                finalWp = cache.GetStartWaypoint();
            EventHandler.setSelectedWaypoint(cache, finalWp);
        }
    }

    private final OnItemClickListener itemLogClickListener = new OnItemClickListener() {


        @Override
        public boolean onItemClick(MenuItem item) {

            int index = item.getListIndex();

            aktFieldNote = fieldNoteEntries.get(index);

            Menu cm = new Menu("CacheListContextMenu");

            cm.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public boolean onItemClick(MenuItem item) {
                    switch (item.getMenuItemId()) {
                        case MenuID.MI_SELECT_CACHE:
                            selectCacheFromFieldNote();
                            return true;
                        case MenuID.MI_EDIT_FIELDNOTE:
                            editFieldNote();
                            return true;
                        case MenuID.MI_DELETE_FIELDNOTE:
                            deleteFieldNote();
                            return true;

                    }
                    return false;
                }
            });

            cm.addItem(MenuID.MI_SELECT_CACHE, "SelectCache");
            cm.addItem(MenuID.MI_EDIT_FIELDNOTE, "edit");
            cm.addItem(MenuID.MI_DELETE_FIELDNOTE, "delete");

            cm.show();
            return true;
        }
    };


    public void notifyDataSetChanged() {
        loadFieldNotes(FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);
    }


}
