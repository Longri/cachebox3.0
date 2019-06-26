/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.apis.gcvote_api.GCVote;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.activities.EditDrafts;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.popUps.QuickDraftFeedbackPopUp;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.utils.TemplateFormatter;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;

/**
 * Created by Longri on 14.09.2016.
 */
public class DraftsView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(DraftsView.class);

    private static DraftsView THAT;
    private static DraftEntry aktDraft;
    private static DraftList draftEntries;
    private static EditDrafts.ReturnListener returnListener = (fieldNote, isNewDraft, directlog) -> addOrChangeDraft(fieldNote, isNewDraft, directlog);
    private static EditDrafts efnActivity;
    private ListView listView = new ListView(VERTICAL);
    private DraftListItemStyle itemStyle;
    private Array<ListViewItem> items;
    private ListViewAdapter listViewAdapter = new ListViewAdapter() {

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
    };
    private final ClickLongClickListener clickLongClickListener = new ClickLongClickListener() {
        @Override
        public boolean clicked(InputEvent event, float x, float y) {
            return false;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
            if (!(actor instanceof ListViewItem)) return false;
            Menu cm = new Menu("DraftItemMenuTitle");
            aktDraft = draftEntries.get(((ListViewItem) actor).getListIndex());
            cm.addMenuItem("SelectCache", ":\n" + aktDraft.CacheName, aktDraft.cacheType.getDrawable(), () -> selectCacheFromDraft());
            cm.addMenuItem("edit", CB.getSkin().getMenuIcon.edit, () -> editDraft());
            cm.addMenuItem("delete", CB.getSkin().getMenuIcon.deleteAllDrafts, () -> deleteDraft());
            cm.show();
            return true;
        }
    };


    public DraftsView(BitStore reader) {
        super(reader);
    }

    public DraftsView() {
        super("DraftsView");
        create();
    }

    public static void addNewFieldnote(LogTypes type) {
        addNewFieldnote(type, false);
    }

    public static void addNewFieldnote(LogTypes type, boolean witoutShowEdit) {
        AbstractCache abstractCache = EventHandler.getSelectedCache();

        if (abstractCache == null) {
            MessageBox.show(Translation.get("NoCacheSelect"), Translation.get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            return;
        }

        // chk car found?
        if (abstractCache.getGcCode().toString().equalsIgnoreCase("CBPark")) {
            if (type == LogTypes.found) {
                MessageBox.show(Translation.get("My_Parking_Area_Found"), Translation.get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            } else if (type == LogTypes.didnt_find) {
                MessageBox.show(Translation.get("My_Parking_Area_DNF"), Translation.get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            }
            return;
        }

        // kein GC Cache
        if (!abstractCache.getGcCode().toString().toLowerCase().startsWith("gc")) {

            if (type == LogTypes.found || type == LogTypes.attended || type == LogTypes.webcam_photo_taken) {
                // Found it! -> fremden Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    EventHandler.getSelectedCache().updateBooleanStore(Database.Data);
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(true);
                    pop.show();
                }
            } else if (type == LogTypes.didnt_find) {
                // DidNotFound -> fremden Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    EventHandler.getSelectedCache().updateBooleanStore(Database.Data);
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    QuickDraftFeedbackPopUp pop2 = new QuickDraftFeedbackPopUp(false);
                    pop2.show();
                }
            }

            if (THAT != null)
                THAT.notifyDataSetChanged();
            return;
        }

        DraftList tmpDrafts = new DraftList();
        tmpDrafts.loadDrafts("", DraftList.LoadingType.LOAD_ALL);

        DraftEntry newDraft = null;
        if ((type == LogTypes.found) //
                || (type == LogTypes.attended) //
                || (type == LogTypes.webcam_photo_taken) //
                || (type == LogTypes.didnt_find)) {
            // nachsehen, ob für diesen Cache bereits eine Draft des Types angelegt wurde
            // und gegebenenfalls diese ändern und keine neue anlegen
            // gilt nur für Found It! und DNF.
            // needMaintance oder Note können zusätzlich angelegt werden

            for (DraftEntry nfne : tmpDrafts) {
                if ((nfne.CacheId == abstractCache.getId()) && (nfne.type == type)) {
                    newDraft = nfne;
                    newDraft.deleteFromDatabase();
                    newDraft.timestamp = new Date();
                    aktDraft = newDraft;
                }
            }
        }

        if (newDraft == null) {
            newDraft = new DraftEntry(type);
            newDraft.CacheName = abstractCache.getName();
            newDraft.gcCode = abstractCache.getGcCode().toString();
            newDraft.foundNumber = Config.FoundOffset.getValue();
            newDraft.timestamp = new Date();
            newDraft.CacheId = abstractCache.getId();
            newDraft.comment = "";
            CharSequence url = abstractCache.getUrl();
            newDraft.CacheUrl = url == null ? null : url.toString();
            newDraft.cacheType = abstractCache.getType();
            newDraft.fillType();
            // aktDraftIndex = -1;
            aktDraft = newDraft;
        } else {
            tmpDrafts.removeValue(newDraft, false);

        }

        switch (type) {
            case found:
                if (!abstractCache.isFound())
                    newDraft.foundNumber++; //
                newDraft.fillType();
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case attended:
                if (!abstractCache.isFound())
                    newDraft.foundNumber++; //
                newDraft.fillType();
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.AttendedTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case webcam_photo_taken:
                if (!abstractCache.isFound())
                    newDraft.foundNumber++; //
                newDraft.fillType();
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.WebcamTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case didnt_find:
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.DNFTemplate.getValue(), newDraft);
                break;
            case needs_maintenance:
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.NeedsMaintenanceTemplate.getValue(), newDraft);
                break;
            case note:
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), newDraft);
                break;
            default:
                break;
        }

        if (!witoutShowEdit) {
            efnActivity = new EditDrafts(newDraft, returnListener, true);
            efnActivity.show();
        } else {

            // new Draft
            tmpDrafts.add(newDraft);
            newDraft.writeToDatabase();
            aktDraft = newDraft;
            if (newDraft.type == LogTypes.found || newDraft.type == LogTypes.attended || newDraft.type == LogTypes.webcam_photo_taken) {
                // Found it! -> Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    EventHandler.getSelectedCache().updateBooleanStore(Database.Data);
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    Config.FoundOffset.setValue(aktDraft.foundNumber);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft DNF löschen
                tmpDrafts.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogTypes.didnt_find);
            } else if (newDraft.type == LogTypes.didnt_find) {
                // DidNotFound -> Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    EventHandler.getSelectedCache().updateBooleanStore(Database.Data);
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft FoundIt löschen
                tmpDrafts.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogTypes.found);
            }

            DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());

            if (THAT != null)
                THAT.notifyDataSetChanged();

        }
    }

    private static void addOrChangeDraft(DraftEntry fieldNote, boolean isNewDraft, boolean directLog) {

        if (directLog) {
            // try to direct upload
            logOnline(fieldNote, isNewDraft);
            return;
        }


        if (fieldNote != null) {

            if (isNewDraft) {
                // nur, wenn eine Draft neu angelegt wurde
                // new Draft
                draftEntries.add(fieldNote);

                // eine evtl. vorhandene Draft /DNF löschen
                if (fieldNote.type == LogTypes.attended //
                        || fieldNote.type == LogTypes.found //
                        || fieldNote.type == LogTypes.webcam_photo_taken //
                        || fieldNote.type == LogTypes.didnt_find) {
                    draftEntries.deleteDraftByCacheId(fieldNote.CacheId, LogTypes.found);
                    draftEntries.deleteDraftByCacheId(fieldNote.CacheId, LogTypes.didnt_find);
                }
            }

            fieldNote.writeToDatabase();
            aktDraft = fieldNote;

            if (isNewDraft) {
                // nur, wenn eine Draft neu angelegt wurde
                // wenn eine Draft neu angelegt werden soll dann kann hier auf SelectedCache zugegriffen werden, da nur für den
                // SelectedCache eine fieldNote angelegt wird
                if (fieldNote.type == LogTypes.found //
                        || fieldNote.type == LogTypes.attended //
                        || fieldNote.type == LogTypes.webcam_photo_taken) {
                    // Found it! -> Cache als gefunden markieren
                    if (!EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(true);
                        EventHandler.getSelectedCache().updateBooleanStore(Database.Data);
                        DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                        Config.FoundOffset.setValue(aktDraft.foundNumber);
                        Config.AcceptChanges();
                    }

                } else if (fieldNote.type == LogTypes.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
                    if (EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(false);
                        EventHandler.getSelectedCache().updateBooleanStore(Database.Data);
                        AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                        EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                        Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                        Config.AcceptChanges();
                    } // und eine evtl. vorhandene Draft FoundIt löschen
                    draftEntries.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogTypes.found);
                }
            }
            DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());

            // Reload List
            if (isNewDraft) {
                draftEntries.loadDrafts("", DraftList.LoadingType.LOAD_NEW);
            } else {
                draftEntries.loadDrafts("", DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
            }
        }
        THAT.notifyDataSetChanged();
    }

    private static void logOnline(final DraftEntry fieldNote, final boolean isNewFieldNote) {

        if (Config.GcVotePassword.getEncryptedValue().length() > 0 && !fieldNote.isTbDraft) {
            if (fieldNote.gc_Vote > 0) {
                // Stimme abgeben
                try {
                    if (!GCVote.sendVote(Config.GcLogin.getValue(), Config.GcVotePassword.getValue(), fieldNote.gc_Vote, fieldNote.CacheUrl, fieldNote.gcCode)) {
                        log.error(fieldNote.gcCode + " GC-Vote");
                    }
                } catch (Exception e) {
                    log.error(fieldNote.gcCode + " GC-Vote");
                }
            }
        }

        if (GroundspeakAPI.OK == GroundspeakAPI.UploadDraftOrLog(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, fieldNote.isDirectLog)) {
            // after direct Log change state to uploaded
            fieldNote.uploaded = true;
            addOrChangeDraft(fieldNote, isNewFieldNote, false);
        } else {
            // Error handling
            MessageBox.show(Translation.get("CreateFieldnoteInstead"), Translation.get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, (which, data) -> {
                switch (which) {
                    case ButtonDialog.BUTTON_NEGATIVE:
                        addOrChangeDraft(fieldNote, isNewFieldNote, true);// try again
                        break;
                    case ButtonDialog.BUTTON_NEUTRAL:
                        break;
                    case ButtonDialog.BUTTON_POSITIVE:
                        addOrChangeDraft(fieldNote, isNewFieldNote, false);// create Fieldnote
                }
                return true;
            });
        }
        if (GroundspeakAPI.LastAPIError.length() > 0) {
            MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
        }

    }

    @Override
    protected void create() {
        THAT = this;
        itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", DraftListItemStyle.class);

        draftEntries = new DraftList();
        loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);

        listView.setEmptyString(Translation.get("EmptyDrafts"));
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
        CB.postOnGlThread(new NamedRunnable("DraftsView") {
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

    @Override
    public void dispose() {
        EventHandler.remove(this);
        if (draftEntries != null) draftEntries.clear();
        draftEntries = null;
        aktDraft = null;
        THAT = null;
        if (listView != null) listView.dispose();
        listView = null;
        if (items != null) {
            for (ListViewItem item : items) {
                item.dispose();
            }
            items.clear();
        }
        items = null;
    }

    private void loadDrafts(DraftList.LoadingType type) {
        draftEntries.loadDrafts("", type);

        if (items == null) {
            items = new Array<>();
        }
        items.clear();

        int idx = 0;
        for (DraftEntry entry : draftEntries) {
            items.add(new DraftsViewItem(idx++, entry, itemStyle));
        }
        CB.postOnNextGlThread(() -> listView.setAdapter(listViewAdapter));
    }

    private void uploadDrafts() {
        new CancelProgressDialog("UploadDraftsDialog", "",
                new ProgressCancelRunnable() {

                    ICancel iCancel = this::isCanceled;

                    @Override
                    public void canceled() {
                        log.debug("cancel clicked");
                    }

                    @Override
                    public void run() {
                        DraftList lDrafts = new DraftList();
                        lDrafts.loadDrafts("(Uploaded=0 or Uploaded is null)", DraftList.LoadingType.LOAD_ALL);

                        int count = 0;
                        int anzahl = 0;
                        for (DraftEntry fieldNote : lDrafts) {
                            if (!fieldNote.uploaded)
                                anzahl++;
                        }

                        boolean sendGCVote = !Config.GcVotePassword.getEncryptedValue().equalsIgnoreCase("");

                        StringBuilder UploadMeldung = new StringBuilder();
                        if (anzahl > 0) {

                            for (DraftEntry fieldNote : lDrafts) {
                                if (isCanceled())
                                    break;

                                if (fieldNote.uploaded)
                                    continue;

                                // Progress status Melden
                                setProgress((100 * count) / anzahl, fieldNote.CacheName);

                                if (sendGCVote && !fieldNote.isTbDraft) {
                                    try {
                                        if (!GCVote.sendVote(Config.GcLogin.getValue(), Config.GcVotePassword.getValue(), fieldNote.gc_Vote, fieldNote.CacheUrl, fieldNote.gcCode)) {
                                            UploadMeldung.append(fieldNote.gcCode).append("\n").append("GC-Vote Error").append("\n");
                                        }
                                    } catch (Exception e) {
                                        UploadMeldung.append(fieldNote.gcCode).append("\n").append("GC-Vote Error").append("\n");
                                    }
                                }

                                int result;

                                if (fieldNote.isTbDraft) {
                                    result = GroundspeakAPI.uploadTrackableLog(fieldNote.TravelBugCode, fieldNote.TrackingNumber, fieldNote.gcCode, LogTypes.CB_LogType2GC(fieldNote.type), fieldNote.timestamp, fieldNote.comment);
                                } else {
                                    boolean dl = fieldNote.isDirectLog;
                                    result = GroundspeakAPI.UploadDraftOrLog(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, dl);
                                }

                                if (result == GroundspeakAPI.ERROR) {
                                    UploadMeldung.append(fieldNote.gcCode).append("\n").append(GroundspeakAPI.LastAPIError).append("\n");
                                } else {
                                    // set Draft as uploaded
                                    fieldNote.uploaded = true;
                                }
                                fieldNote.writeToDatabase();
                                count++;
                            }
                        }

                        if (!UploadMeldung.toString().equals("")) {
                            final String finalUploadMeldung = UploadMeldung.toString();
                            CB.scheduleOnGlThread(new NamedRunnable("DraftsView") {
                                @Override
                                public void run() {
                                    MessageBox.show(finalUploadMeldung, Translation.get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
                                    log.debug("Show MessageBox for ERROR on upload Draft");
                                }
                            }, 300);
                        } else {
                            CB.scheduleOnGlThread(new NamedRunnable("DraftsView") {
                                @Override
                                public void run() {
                                    MessageBox.show(Translation.get("uploadFinished"), Translation.get("uploadDrafts"), MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);
                                    log.debug("Show MessageBox for uploaded Draft");
                                }
                            }, 300);
                        }
                        DraftsView.this.notifyDataSetChanged();
                    }
                }
        ).show();
    }

    private Menu getSecondMenu() {
        Menu sm = new Menu("OwnerLogTypesTitle");
        boolean IM_owner = EventHandler.getSelectedCache().ImTheOwner();
        sm.addMenuItem("enabled", itemStyle.typeStyle.enabled, () -> addNewFieldnote(LogTypes.enabled)).setEnabled(IM_owner);
        sm.addMenuItem("temporarilyDisabled", itemStyle.typeStyle.temporarily_disabled, () -> addNewFieldnote(LogTypes.temporarily_disabled)).setEnabled(IM_owner);
        sm.addMenuItem("ownerMaintenance", itemStyle.typeStyle.owner_maintenance, () -> addNewFieldnote(LogTypes.owner_maintenance)).setEnabled(IM_owner);
        // todo check if needed: addNewFieldnote(LogTypes.reviewer_note)
        return sm;
    }

    private void editDraft() {
        if (efnActivity != null && !efnActivity.isDisposed()) {
            efnActivity.setDraft(aktDraft, returnListener, false);
        } else {
            efnActivity = new EditDrafts(aktDraft, returnListener, false);
        }

        efnActivity.show();
    }

    private void deleteDraft() {
        // aktuell selectierte Draft löschen
        if (aktDraft == null)
            return;
        // final Cache cache =
        // Database.Data.cacheList.GetCacheByGcCode(aktDraft.gcCode);

        AbstractCache tmpAbstractCache = null;
        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();

        String statement = "SELECT * FROM CacheCoreInfo core WHERE Id = " + aktDraft.CacheId;

        DaoFactory.CACHE_LIST_DAO.readCacheList(Database.Data, lCaches, statement, false, false);
        if (lCaches.size > 0)
            tmpAbstractCache = lCaches.get(0);
        final AbstractCache abstractCache = tmpAbstractCache;

        if (abstractCache == null && !aktDraft.isTbDraft) {
            CharSequence message = new CompoundCharSequence(Translation.get("cacheOtherDb", aktDraft.CacheName.toString())
                    , "\n", Translation.get("draftNoDelete"));

            MessageBox.show(message, null, MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
            return;
        }

        OnMsgBoxClickListener dialogClickListener = (which, data) -> {
            switch (which) {
                case ButtonDialog.BUTTON_POSITIVE:
                    // Yes button clicked
                    // delete aktDraft
                    if (abstractCache != null) {
                        if (abstractCache.isFound()) {
                            abstractCache.setFound(false);
                            abstractCache.updateBooleanStore(Database.Data);
                            DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, abstractCache);
                            Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                            Config.AcceptChanges();
                            // jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
                            // damit das Smiley Symbol aus der Map und der CacheList verschwindet
                            synchronized (Database.Data.cacheList) {
                                AbstractCache tc = Database.Data.cacheList.GetCacheById(abstractCache.getId());
                                if (tc != null) {
                                    tc.setFound(false);
                                    tc.updateBooleanStore(Database.Data);
                                }
                            }
                        }
                    }
                    draftEntries.deleteDraft(aktDraft.Id, aktDraft.type);
                    aktDraft = null;
                    draftEntries.loadDrafts("", DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
                    loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
                    DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());

                    break;
                case ButtonDialog.BUTTON_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;
        };

        CharSequence message;
        if (aktDraft.isTbDraft) {
            message = Translation.get("confirmDraftDeletionTB", aktDraft.typeString, aktDraft.TbName);
        } else {
            message = Translation.get("confirmDraftDeletion", aktDraft.typeString, aktDraft.CacheName.toString());
        }

        MessageBox.show(message, Translation.get("deleteDraft"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, dialogClickListener);

    }

    private void deleteAllDrafts() {
        final OnMsgBoxClickListener dialogClickListener = (which, data) -> {
            switch (which) {
                case ButtonDialog.BUTTON_POSITIVE:
                    // Yes button clicked
                    // delete all Drafts
                    // reload all Drafts!
                    draftEntries.loadDrafts("", DraftList.LoadingType.LOAD_ALL);

                    for (DraftEntry entry : draftEntries) {
                        entry.deleteFromDatabase();

                    }

                    draftEntries.clear();
                    aktDraft = null;

                    loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
                    break;

                case ButtonDialog.BUTTON_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;

        };
        final CharSequence message = Translation.get("DelDrafts?");
        Gdx.app.postRunnable(() -> MessageBox.show(message, Translation.get("DeleteAllDrafts"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning, dialogClickListener));
    }

    private void selectCacheFromDraft() {
        if (aktDraft == null)
            return;

        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        String statement = "SELECT * FROM CacheCoreInfo core WHERE Id = " + aktDraft.CacheId;
        DaoFactory.CACHE_LIST_DAO.readCacheList(Database.Data, lCaches, statement, false, false);
        AbstractCache tmpCache = null;
        if (lCaches.size > 0)
            tmpCache = lCaches.get(0);
        AbstractCache cache = tmpCache;

        if (cache == null) {
            CharSequence message = Translation.get("cacheOtherDb", aktDraft.CacheName.toString());
            //TODO message += "\n" + Translation.get("DraftNoSelect");
            MessageBox.show(message, Translation.get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            return;
        }

        synchronized (Database.Data.cacheList) {
            cache = Database.Data.cacheList.GetCacheByGcCode(aktDraft.gcCode);
        }

        if (cache == null) {
            Database.Data.cacheList.add(tmpCache);
            cache = Database.Data.cacheList.GetCacheByGcCode(aktDraft.gcCode);
        }

        AbstractWaypoint finalWp = null;
        if (cache != null) {
            if (cache.HasFinalWaypoint())
                finalWp = cache.GetFinalWaypoint();
            else if (cache.HasStartWaypoint())
                finalWp = cache.GetStartWaypoint();
            EventHandler.setSelectedWaypoint(cache, finalWp);
        }
    }

    public void notifyDataSetChanged() {
        CB.postOnGlThread(new NamedRunnable("DraftsView") {
            @Override
            public void run() {
                loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
            }
        });
    }

    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {


        final Menu cm = new Menu("DraftsContextMenuTitle");

        AbstractCache abstractCache = EventHandler.getSelectedCache();
        if (abstractCache != null) {

            // Found je nach CacheType
            if (abstractCache.getType() == null)
                return null;
            switch (abstractCache.getType()) {
                case Giga:
                case MegaEvent:
                case Event:
                case CITO:
                    cm.addMenuItem("will-attended", itemStyle.typeStyle.will_attend, () -> addNewFieldnote(LogTypes.will_attend));
                    cm.addMenuItem("attended", itemStyle.typeStyle.attended, () -> addNewFieldnote(LogTypes.attended));
                    break;
                case Camera:
                    cm.addMenuItem("webCamFotoTaken", itemStyle.typeStyle.webcam_photo_taken, () -> addNewFieldnote(LogTypes.webcam_photo_taken));
                    break;
                default:
                    cm.addMenuItem("found", itemStyle.typeStyle.found, () -> addNewFieldnote(LogTypes.found));
                    break;
            }

            cm.addMenuItem("DNF", itemStyle.typeStyle.didnt_find, () -> addNewFieldnote(LogTypes.didnt_find));
        }

        // Aktueller Cache ist von geocaching.com dann weitere Menüeinträge freigeben
        if (abstractCache != null && abstractCache.getGcCode().toString().toLowerCase().startsWith("gc")) {
            cm.addMenuItem("maintenance", itemStyle.typeStyle.needs_maintenance, () -> addNewFieldnote(LogTypes.needs_maintenance));
            cm.addMenuItem("writenote", itemStyle.typeStyle.note, () -> addNewFieldnote(LogTypes.note));
        }

        cm.addMenuItem("uploadDrafts", CB.getSkin().getMenuIcon.uploadDraft, this::uploadDrafts);
        cm.addMenuItem("DeleteAllDrafts", CB.getSkin().getMenuIcon.deleteAllDrafts, this::deleteAllDrafts);

        if (abstractCache != null && !abstractCache.ImTheOwner()) {
            cm.addMenuItem("ownerLogTypes", CB.getSkin().getMenuIcon.ownerLogTypes, () -> {
            }).setMoreMenu(getSecondMenu());
        }

        return cm;
    }

    @Override
    public void saveInstanceState(BitStore writer) {
        // we save only listView scroll pos
        float pos = listView == null ? -1 : listView.getScrollPos();
        writer.write(pos);
    }

    @Override
    protected void restoreInstanceState(BitStore reader) {
        // we save only listView scroll pos
        float pos = reader.readFloat();
        if (pos > 0) {
            final float finalPos = pos;
            CB.postOnGLThreadDelayed(300, new NamedRunnable("restore ScrollPos") {
                @Override
                public void run() {
                    listView.setScrollPos(finalPos);
                }
            });
        }
    }
}
