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
import de.longri.cachebox3.gui.activities.EditDraft;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.skin.styles.LogTypesStyle;
import de.longri.cachebox3.gui.utils.TemplateFormatter;
import de.longri.cachebox3.gui.widgets.QuickDraftFeedbackPopUp;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Draft;
import de.longri.cachebox3.types.Drafts;
import de.longri.cachebox3.types.LogType;
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
    public Drafts drafts;
    private static DraftsView draftsView;
    private static Draft currentDraft;
    private ListView listView = new ListView(VERTICAL);
    private DraftListItemStyle draftListItemStyle;
    private LogTypesStyle logTypesStyleForMenu;
    private Array<ListViewItem> items;
    private final ListViewAdapter listViewAdapter = new ListViewAdapter() {

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

    public static DraftsView getInstance() {
        if (draftsView == null) {
            draftsView = new DraftsView();
        }
        return draftsView;
    }

    public void addNewDraft(LogType type) {
        addNewDraft(type, false);
    }

    public void addNewDraft(LogType type, boolean withoutShowEdit) {
        AbstractCache abstractCache = EventHandler.getSelectedCache();

        if (abstractCache == null) {
            MessageBox.show(Translation.get("NoCacheSelect"), Translation.get("thisNotWork"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
            return;
        }

        // chk car found?
        if (abstractCache.getGeoCacheCode().toString().equalsIgnoreCase("CBPark")) {
            if (type == LogType.found) {
                MessageBox.show(Translation.get("My_Parking_Area_Found"), Translation.get("thisNotWork"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            } else if (type == LogType.didnt_find) {
                MessageBox.show(Translation.get("My_Parking_Area_DNF"), Translation.get("thisNotWork"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
            }
            return;
        }

        // kein GC Cache
        if (!abstractCache.getGeoCacheCode().toString().toLowerCase().startsWith("gc")) {

            if (type == LogType.found || type == LogType.attended || type == LogType.webcam_photo_taken) {
                // Found it! -> fremden Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    EventHandler.getSelectedCache().updateBooleanStore();
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    new QuickDraftFeedbackPopUp(true).show();
                }
            } else if (type == LogType.didnt_find) {
                // DidNotFound -> fremden Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    EventHandler.getSelectedCache().updateBooleanStore();
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    new QuickDraftFeedbackPopUp(false).show();
                }
            }

            notifyDataSetChanged();
            return;
        }

        Drafts tmpDrafts = new Drafts();
        tmpDrafts.loadDrafts("", Drafts.LoadingType.LOAD_ALL);

        Draft newDraft = null;
        if ((type == LogType.found) //
                || (type == LogType.attended) //
                || (type == LogType.webcam_photo_taken) //
                || (type == LogType.didnt_find)) {
            // nachsehen, ob für diesen Cache bereits eine Draft des Types angelegt wurde
            // und gegebenenfalls diese ändern und keine neue anlegen
            // gilt nur für Found It! und DNF.
            // needMaintance oder Note können zusätzlich angelegt werden

            for (Draft nfne : tmpDrafts) {
                if ((nfne.CacheId == abstractCache.getId()) && (nfne.type == type)) {
                    newDraft = nfne;
                    newDraft.deleteFromDatabase();
                    newDraft.timestamp = new Date();
                    currentDraft = newDraft;
                }
            }
        }

        if (newDraft == null) {
            newDraft = new Draft(type);
            newDraft.CacheName = abstractCache.getGeoCacheName();
            newDraft.gcCode = abstractCache.getGeoCacheCode().toString();
            newDraft.foundNumber = Config.FoundOffset.getValue();
            newDraft.timestamp = new Date();
            newDraft.CacheId = abstractCache.getId();
            newDraft.comment = "";
            CharSequence url = abstractCache.getUrl();
            newDraft.CacheUrl = url == null ? null : url.toString();
            newDraft.cacheType = abstractCache.getType();
            // aktDraftIndex = -1;
            currentDraft = newDraft;
        } else {
            tmpDrafts.removeValue(newDraft, false);
        }

        switch (type) {
            case found:
                if (!abstractCache.isFound())
                    newDraft.foundNumber++; //
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case attended:
                if (!abstractCache.isFound())
                    newDraft.foundNumber++; //
                if (newDraft.comment.equals(""))
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.AttendedTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case webcam_photo_taken:
                if (!abstractCache.isFound())
                    newDraft.foundNumber++; //
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
            currentDraft = newDraft;
            if (newDraft.type == LogType.found || newDraft.type == LogType.attended || newDraft.type == LogType.webcam_photo_taken) {
                // Found it! -> Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    EventHandler.getSelectedCache().updateBooleanStore();
                    AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                    EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                    Config.FoundOffset.setValue(currentDraft.foundNumber);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft DNF löschen
                tmpDrafts.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogType.didnt_find);
            } else if (newDraft.type == LogType.didnt_find) {
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
                tmpDrafts.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogType.found);
            }

            Drafts.createVisitsTxt(Config.DraftsGarminPath.getValue());

            notifyDataSetChanged();

        } else {
            EditDraft editDraft = EditDraft.getInstance();
            editDraft.setDraft(newDraft, true);
            editDraft.show();
        }
    }

    public void notifyDataSetChanged() {
        CB.postOnGlThread(new NamedRunnable("DraftsView") {
            @Override
            public void run() {
                loadDrafts(Drafts.LoadingType.LOAD_NEW_LAST_LENGTH);
            }
        });
    }

    @Override
    protected void create() {
        draftListItemStyle = VisUI.getSkin().get(DraftListItemStyle.class);
        logTypesStyleForMenu = VisUI.getSkin().get("LogTypesSize48", LogTypesStyle.class);

        drafts = new Drafts();
        loadDrafts(Drafts.LoadingType.LOAD_NEW_LAST_LENGTH);

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
        if (drafts != null) drafts.clear();
        drafts = null;
        currentDraft = null;
        draftsView = null;
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

    void loadDrafts(Drafts.LoadingType type) {
        drafts.loadDrafts("", type);

        if (items == null) {
            items = new Array<>();
        }
        items.clear();

        int idx = 0;
        for (Draft entry : drafts) {
            items.add(new DraftsViewItem(idx++, entry, draftListItemStyle));
        }
        CB.postOnNextGlThread(() -> listView.setAdapter(listViewAdapter));
    }

    public void uploadDraft() {
        upload(false);
    }

    public void uploadLog() {
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
                        Drafts drafts = new Drafts();
                        drafts.loadDrafts("(Uploaded=0 or Uploaded is null)", Drafts.LoadingType.LOAD_ALL);

                        int count = 0;
                        int anzahl = 0;
                        for (Draft draft : drafts) {
                            if (!draft.uploaded)
                                anzahl++;
                        }

                        GCVote gcVote = new GCVote(Database.Data, Config.GcLogin.getValue(), Config.GcVotePassword.getValue());

                        StringBuilder UploadMeldung = new StringBuilder();
                        if (anzahl > 0) {

                            for (Draft draft : drafts) {
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
                                    result = GroundspeakAPI.getInstance().uploadTrackableLog(draft.TravelBugCode, draft.TrackingNumber, draft.gcCode, LogType.CB_LogType2GC(draft.type), draft.timestamp, draft.comment);
                                } else {
                                    String logReferenceCode = GroundspeakAPI.getInstance().uploadDraftOrLog(draft.gcCode, draft.type.getGcLogTypeId(), draft.timestamp, draft.comment, uploadLog);
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
                                    MessageBox.show(finalUploadMeldung, Translation.get("Error"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
                                    log.debug("Show MessageBox for ERROR on upload Draft");
                                }
                            }, 300);
                        } else {
                            CB.scheduleOnGlThread(new NamedRunnable("DraftsView") {
                                @Override
                                public void run() {
                                    MessageBox.show(Translation.get("uploadFinished"), Translation.get("uploadDrafts"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
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
        boolean IM_owner = EventHandler.getSelectedCache().iAmTheOwner();
        sm.addMenuItem("enabled", logTypesStyleForMenu.enabled, () -> addNewDraft(LogType.enabled)).setEnabled(IM_owner);
        sm.addMenuItem("temporarilyDisabled", logTypesStyleForMenu.temporarily_disabled, () -> addNewDraft(LogType.temporarily_disabled)).setEnabled(IM_owner);
        sm.addMenuItem("ownerMaintenance", logTypesStyleForMenu.owner_maintenance, () -> addNewDraft(LogType.owner_maintenance)).setEnabled(IM_owner);
        // todo check if needed: addNewFieldnote(LogType.reviewer_note)
        return sm;
    }

    private void deleteAllDrafts() {
        final OnMsgBoxClickListener dialogClickListener = (which, data) -> {
            switch (which) {
                case ButtonDialog.BUTTON_POSITIVE:
                    // Yes button clicked
                    // delete all Drafts
                    // reload all Drafts!
                    drafts.loadDrafts("", Drafts.LoadingType.LOAD_ALL);

                    for (Draft entry : drafts) {
                        entry.deleteFromDatabase();

                    }

                    drafts.clear();
                    currentDraft = null;

                    loadDrafts(Drafts.LoadingType.LOAD_NEW_LAST_LENGTH);
                    break;

                case ButtonDialog.BUTTON_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;

        };
        final CharSequence message = Translation.get("DelDrafts?");
        Gdx.app.postRunnable(() -> MessageBox.show(message, Translation.get("DeleteAllDrafts"), MessageBoxButton.YesNo, MessageBoxIcon.Warning, dialogClickListener));
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
                    cm.addMenuItem("will-attended", logTypesStyleForMenu.will_attend, () -> addNewDraft(LogType.will_attend));
                    cm.addMenuItem("attended", logTypesStyleForMenu.attended, () -> addNewDraft(LogType.attended));
                    break;
                case Camera:
                    cm.addMenuItem("webCamFotoTaken", logTypesStyleForMenu.webcam_photo_taken, () -> addNewDraft(LogType.webcam_photo_taken));
                    break;
                default:
                    cm.addMenuItem("found", logTypesStyleForMenu.found, () -> addNewDraft(LogType.found));
                    break;
            }

            cm.addMenuItem("DNF", logTypesStyleForMenu.didnt_find, () -> addNewDraft(LogType.didnt_find));
        }

        // Aktueller Cache ist von geocaching.com
        if (abstractCache != null && abstractCache.getGeoCacheCode().toString().toLowerCase().startsWith("gc")) {
            cm.addMenuItem("maintenance", logTypesStyleForMenu.needs_maintenance, () -> addNewDraft(LogType.needs_maintenance));
            cm.addMenuItem("writenote", logTypesStyleForMenu.note, () -> addNewDraft(LogType.note));
        }

        // Owner logs
        if (abstractCache != null && !abstractCache.iAmTheOwner()) {
            cm.addMenuItem("ownerLogTypes", CB.getSkin().menuIcon.ownerLogTypes, () -> {
            }).setMoreMenu(getSecondMenu());
        }

        cm.addDivider(-1);
        cm.addMenuItem("uploadDrafts", CB.getSkin().menuIcon.uploadDraft, this::uploadDraft);
        cm.addMenuItem("directLog", CB.getSkin().menuIcon.me2Logbook, this::uploadLog);
        cm.addMenuItem("DeleteAllDrafts", CB.getSkin().menuIcon.delete, this::deleteAllDrafts);

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
