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
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GCVote;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.activities.EditDrafts;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.popUps.QuickDraftFeedbackPopUp;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.utils.TemplateFormatter;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.DraftEntry;
import de.longri.cachebox3.types.DraftList;
import de.longri.cachebox3.types.LogTypes;
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
    static DraftList draftEntries;
    private static DraftsView that;
    private static DraftEntry aktDraft;
    private static EditDrafts efnActivity;
    private ListView listView = new ListView(VERTICAL);
    private DraftListItemStyle draftListItemStyle;
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
        }
    };

    public DraftsView(BitStore reader) {
        super(reader);
    }

    private DraftsView() {
        super("DraftsView");
        create();
    }

    private static boolean isInstanceCreated() {
        return that != null;
    }

    public static DraftsView getInstance() {
        if (that == null) {
            that = new DraftsView();
        }
        return that;
    }

    public static void addNewDraft(LogTypes type) {
        addNewDraft(type, false);
    }

    public static void addNewDraft(LogTypes type, boolean withoutShowEdit) {
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
                    EventHandler.getSelectedCache().updateBooleanStore();
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(true);
                    pop.show();
                }
            } else if (type == LogTypes.didnt_find) {
                // DidNotFound -> fremden Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    EventHandler.getSelectedCache().updateBooleanStore();
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    QuickDraftFeedbackPopUp pop2 = new QuickDraftFeedbackPopUp(false);
                    pop2.show();
                }
            }

            notifyDataSetChanged();
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

        if (withoutShowEdit) {
            // new Draft
            tmpDrafts.add(newDraft);
            newDraft.writeToDatabase();
            aktDraft = newDraft;
            if (newDraft.type == LogTypes.found || newDraft.type == LogTypes.attended || newDraft.type == LogTypes.webcam_photo_taken) {
                // Found it! -> Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    EventHandler.getSelectedCache().updateBooleanStore();
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
                    EventHandler.getSelectedCache().updateBooleanStore();
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft FoundIt löschen
                tmpDrafts.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogTypes.found);
            }

            DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());

            notifyDataSetChanged();

        } else {
            efnActivity = EditDrafts.getInstance();
            efnActivity.setDraft(newDraft, DraftsView::addOrChangeDraft, true);
            efnActivity.show();
        }
    }

    static void addOrChangeDraft(DraftEntry draftEntry, boolean isNewDraft) {

        if (draftEntry != null) {

            if (isNewDraft) {
                // nur, wenn eine Draft neu angelegt wurde
                // new Draft
                draftEntries.add(draftEntry);

                // eine evtl. vorhandene Draft /DNF löschen
                if (draftEntry.type == LogTypes.attended //
                        || draftEntry.type == LogTypes.found //
                        || draftEntry.type == LogTypes.webcam_photo_taken //
                        || draftEntry.type == LogTypes.didnt_find) {
                    draftEntries.deleteDraftByCacheId(draftEntry.CacheId, LogTypes.found);
                    draftEntries.deleteDraftByCacheId(draftEntry.CacheId, LogTypes.didnt_find);
                }
            }

            draftEntry.writeToDatabase();
            aktDraft = draftEntry;

            if (isNewDraft) {
                // nur, wenn eine Draft neu angelegt wurde
                // wenn eine Draft neu angelegt werden soll dann kann hier auf SelectedCache zugegriffen werden, da nur für den
                // SelectedCache eine fieldNote angelegt wird
                if (draftEntry.type == LogTypes.found //
                        || draftEntry.type == LogTypes.attended //
                        || draftEntry.type == LogTypes.webcam_photo_taken) {
                    // Found it! -> Cache als gefunden markieren
                    if (!EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(true);
                        EventHandler.getSelectedCache().updateBooleanStore();
                        DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                        Config.FoundOffset.setValue(aktDraft.foundNumber);
                        Config.AcceptChanges();
                    }

                } else if (draftEntry.type == LogTypes.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
                    if (EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(false);
                        EventHandler.getSelectedCache().updateBooleanStore();
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
        notifyDataSetChanged();
    }

    public static void notifyDataSetChanged() {
        if (isInstanceCreated()) {
            CB.postOnGlThread(new NamedRunnable("DraftsView") {
                @Override
                public void run() {
                    DraftsView.getInstance().loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
                }
            });
        }
    }

    @Override
    protected void create() {
        draftListItemStyle = VisUI.getSkin().get("DraftListItemStyle", DraftListItemStyle.class);

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
        that = null;
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

    void loadDrafts(DraftList.LoadingType type) {
        draftEntries.loadDrafts("", type);

        if (items == null) {
            items = new Array<>();
        }
        items.clear();

        int idx = 0;
        for (DraftEntry entry : draftEntries) {
            items.add(new DraftsViewItem(idx++, entry, draftListItemStyle));
        }
        CB.postOnNextGlThread(() -> listView.setAdapter(listViewAdapter));
    }

    private void uploadDraft() {
        upload(false);
    }

    private void uploadLog() {
        upload(true);
    }

    private void upload(boolean uploadLog) {
        new CancelProgressDialog("UploadDraftsDialog", "",
                new ProgressCancelRunnable() {

                    ICancel iCancel = this::isCanceled;

                    @Override
                    public void canceled() {
                        log.debug("cancel clicked");
                    }

                    @Override
                    public void run() {
                        DraftList drafts = new DraftList();
                        drafts.loadDrafts("(Uploaded=0 or Uploaded is null)", DraftList.LoadingType.LOAD_ALL);

                        int count = 0;
                        int anzahl = 0;
                        for (DraftEntry draft : drafts) {
                            if (!draft.uploaded)
                                anzahl++;
                        }

                        GCVote gcVote = new GCVote(Database.Data, Config.GcLogin.getValue(), Config.GcVotePassword.getValue());

                        StringBuilder UploadMeldung = new StringBuilder();
                        if (anzahl > 0) {

                            for (DraftEntry draft : drafts) {
                                if (isCanceled())
                                    break;

                                if (draft.uploaded)
                                    continue;

                                // Progress status Melden
                                setProgress((100 * count) / anzahl, draft.CacheName);

                                if (!draft.isTbDraft) {
                                    if (gcVote.isPossible()) {
                                        try {
                                            if (!gcVote.sendVote(draft.gc_Vote, draft.CacheUrl, draft.gcCode)) {
                                                UploadMeldung.append(draft.gcCode).append("\n").append("GC-Vote Error").append("\n");
                                            }
                                        } catch (Exception e) {
                                            UploadMeldung.append(draft.gcCode).append("\n").append("GC-Vote Error").append("\n");
                                        }
                                    }
                                }

                                int result;

                                if (draft.isTbDraft) {
                                    result = GroundspeakAPI.getInstance().uploadTrackableLog(draft.TravelBugCode, draft.TrackingNumber, draft.gcCode, LogTypes.CB_LogType2GC(draft.type), draft.timestamp, draft.comment);
                                } else {
                                    String logReferenceCode = GroundspeakAPI.getInstance().UploadDraftOrLog(draft.gcCode, draft.type.getGcLogTypeId(), draft.timestamp, draft.comment, uploadLog);
                                    draft.GcId = logReferenceCode;
                                    result = GroundspeakAPI.getInstance().APIError;
                                }

                                if (result == GroundspeakAPI.ERROR) {
                                    UploadMeldung.append(draft.gcCode).append("\n").append(GroundspeakAPI.getInstance().LastAPIError).append("\n");
                                } else {
                                    // set Draft as uploaded
                                    draft.uploaded = true;
                                    if (uploadLog && !draft.isTbDraft) {
                                        // LogListView.notifyDataSetChanged(); // getInstance().resetInitial(); // if own log is written !
                                    }
                                }
                                draft.writeToDatabase();
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
                        notifyDataSetChanged();
                    }
                }
        ).show();
    }

    private Menu getSecondMenu() {
        Menu sm = new Menu("OwnerLogTypesTitle");
        boolean IM_owner = EventHandler.getSelectedCache().ImTheOwner();
        sm.addMenuItem("enabled", draftListItemStyle.logTypesStyle.enabled, () -> addNewDraft(LogTypes.enabled)).setEnabled(IM_owner);
        sm.addMenuItem("temporarilyDisabled", draftListItemStyle.logTypesStyle.temporarily_disabled, () -> addNewDraft(LogTypes.temporarily_disabled)).setEnabled(IM_owner);
        sm.addMenuItem("ownerMaintenance", draftListItemStyle.logTypesStyle.owner_maintenance, () -> addNewDraft(LogTypes.owner_maintenance)).setEnabled(IM_owner);
        // todo check if needed: addNewFieldnote(LogTypes.reviewer_note)
        return sm;
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
                    cm.addMenuItem("will-attended", draftListItemStyle.logTypesStyle.will_attend, () -> addNewDraft(LogTypes.will_attend));
                    cm.addMenuItem("attended", draftListItemStyle.logTypesStyle.attended, () -> addNewDraft(LogTypes.attended));
                    break;
                case Camera:
                    cm.addMenuItem("webCamFotoTaken", draftListItemStyle.logTypesStyle.webcam_photo_taken, () -> addNewDraft(LogTypes.webcam_photo_taken));
                    break;
                default:
                    cm.addMenuItem("found", draftListItemStyle.logTypesStyle.found, () -> addNewDraft(LogTypes.found));
                    break;
            }

            cm.addMenuItem("DNF", draftListItemStyle.logTypesStyle.didnt_find, () -> addNewDraft(LogTypes.didnt_find));
        }

        // Aktueller Cache ist von geocaching.com
        if (abstractCache != null && abstractCache.getGcCode().toString().toLowerCase().startsWith("gc")) {
            cm.addMenuItem("maintenance", draftListItemStyle.logTypesStyle.needs_maintenance, () -> addNewDraft(LogTypes.needs_maintenance));
            cm.addMenuItem("writenote", draftListItemStyle.logTypesStyle.note, () -> addNewDraft(LogTypes.note));
        }

        // Owner logs
        if (abstractCache != null && !abstractCache.ImTheOwner()) {
            cm.addMenuItem("ownerLogTypes", CB.getSkin().getMenuIcon.ownerLogTypes, () -> {
            }).setMoreMenu(getSecondMenu());
        }

        cm.addDivider(-1);
        cm.addMenuItem("uploadDrafts", CB.getSkin().getMenuIcon.uploadDraft, this::uploadDraft);
        cm.addMenuItem("directLog", CB.getSkin().getMenuIcon.logViewIcon, this::uploadLog);
        cm.addMenuItem("DeleteAllDrafts", CB.getSkin().getMenuIcon.deleteAllDrafts, this::deleteAllDrafts);

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
