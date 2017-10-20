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
import de.longri.cachebox3.apis.gcvote_api.GCVote;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.activities.EditDrafts;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.popUps.QuickDraftFeedbackPopUp;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.utils.TemplateFormatter;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheDAO;
import de.longri.cachebox3.sqlite.dao.CacheListDAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.ICancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by Longri on 14.09.2016.
 */
public class DraftsView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(DraftsView.class);

    private static DraftsView THAT;
    private static DraftEntry aktDraft;
    private static DraftList fieldNoteEntries;


    private final ListView listView = new ListView();
    private final DraftListItemStyle itemStyle;

    private Array<ListViewItem> items;

    public DraftsView() {
        super("DraftsView");
        THAT = this;
        itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", DraftListItemStyle.class);

        fieldNoteEntries = new DraftList();
        loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);

        listView.setEmptyString(Translation.Get("EmptyDrafts"));
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
            aktDraft = fieldNoteEntries.get(listIndex);

            Menu cm = new Menu("DraftItem-Menu");

            cm.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public boolean onItemClick(MenuItem item) {
                    switch (item.getMenuItemId()) {
                        case MenuID.MI_SELECT_CACHE:
                            selectCacheFromDraft();
                            return true;
                        case MenuID.MI_EDIT_FIELDNOTE:
                            editDraft();
                            return true;
                        case MenuID.MI_DELETE_FIELDNOTE:
                            deleteDraft();
                            return true;

                    }
                    return false;
                }
            });

            cm.addItem(MenuID.MI_SELECT_CACHE, "SelectCache", ":\n" + aktDraft.CacheName, aktDraft.cacheType.getDrawable());
            cm.addItem(MenuID.MI_EDIT_FIELDNOTE, "edit", CB.getSkin().getMenuIcon.edit);
            cm.addItem(MenuID.MI_DELETE_FIELDNOTE, "delete", CB.getSkin().getMenuIcon.deleteAllDrafts);

            cm.show();
            return true;
        }
    };

    @Override
    public void dispose() {
        EventHandler.remove(this);
        fieldNoteEntries.clear();
        fieldNoteEntries = null;
        aktDraft = null;
        THAT = null;
        listView.dispose();
        for (ListViewItem item : items) {
            item.dispose();
        }
        items.clear();
        items = null;
    }

    private void loadDrafts(DraftList.LoadingType type) {
        fieldNoteEntries.loadDrafts("", type);

        if (items == null) {
            items = new Array<>();
        }
        items.clear();

        int idx = 0;
        for (DraftEntry entry : fieldNoteEntries) {
            items.add(new DraftsViewItem(idx++, entry, itemStyle));
        }

        listView.setAdapter(listViewAdapter);

    }

    private void uploadDrafts() {
        new CancelProgressDialog("UploadDraftsDialog", "",
                new ProgressCancelRunnable() {

                    ICancel iCancel = new ICancel() {
                        @Override
                        public boolean cancel() {
                            return isCanceled();
                        }
                    };

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

                        String UploadMeldung = "";
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
                                        if (!GCVote.sendVotes(Config.GcLogin.getValue(), Config.GcVotePassword.getValue()
                                                , fieldNote.gc_Vote, fieldNote.CacheUrl, fieldNote.gcCode, iCancel)) {
                                            UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
                                        }
                                    } catch (Exception e) {
                                        UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
                                    }
                                }

                                ApiResultState result = ApiResultState.UNKNOWN;

                                if (fieldNote.isTbDraft) {
                                    result = GroundspeakAPI.createTrackableLog(fieldNote.TravelBugCode, fieldNote.TrackingNumber, fieldNote.gcCode, LogTypes.CB_LogType2GC(fieldNote.type), fieldNote.timestamp, fieldNote.comment, iCancel);
                                } else {
                                    boolean dl = fieldNote.isDirectLog;
                                    result = GroundspeakAPI.createDraftAndPublish(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, dl, iCancel);
                                }

                                if (CB.checkApiResultState(result)) {
                                    cancel();
                                    return;
                                }

                                if (result.isErrorState()) {
                                    UploadMeldung += fieldNote.gcCode + "\n" + GroundspeakAPI.LastAPIError + "\n";
                                } else {
                                    // set Draft as uploaded
                                    fieldNote.uploaded = true;
                                }
                                fieldNote.writeToDatabase();
                                count++;
                            }
                        }

                        if (!UploadMeldung.equals("")) {
                            final String finalUploadMeldung = UploadMeldung;
                            CB.scheduleOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    MessageBox.show(finalUploadMeldung, Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
                                    log.debug("Show MessageBox for ERROR on upload Draft");
                                }
                            }, 300);
                        } else {
                            CB.scheduleOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    MessageBox.show(Translation.Get("uploadFinished"), Translation.Get("uploadDrafts"), MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);
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
        Menu sm = new Menu("DraftContextMenu/2");
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
        AbstractCache abstractCache = EventHandler.getSelectedCache();

        if (abstractCache == null) {
            MessageBox.show(Translation.Get("NoCacheSelect"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            return;
        }

        // chk car found?
        if (abstractCache.getGcCode().toString().equalsIgnoreCase("CBPark")) {
            if (type == LogTypes.found) {
                MessageBox.show(Translation.Get("My_Parking_Area_Found"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            } else if (type == LogTypes.didnt_find) {
                MessageBox.show(Translation.Get("My_Parking_Area_DNF"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            }
            return;
        }

        // kein GC Cache
        if (!abstractCache.getGcCode().toString().toLowerCase().startsWith("gc")) {

            if (type == LogTypes.found || type == LogTypes.attended || type == LogTypes.webcam_photo_taken) {
                // Found it! -> fremden Cache als gefunden markieren
                if (!EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(true);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                    QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(true);
                    pop.show();
                }
            } else if (type == LogTypes.didnt_find) {
                // DidNotFound -> fremden Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
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
            newDraft.CacheUrl = abstractCache.getUrl();
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
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                    Config.FoundOffset.setValue(aktDraft.foundNumber);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft DNF löschen
                tmpDrafts.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogTypes.didnt_find);
            } else if (newDraft.type == LogTypes.didnt_find) {
                // DidNotFound -> Cache als nicht gefunden markieren
                if (EventHandler.getSelectedCache().isFound()) {
                    EventHandler.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
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

    private static EditDrafts.ReturnListener returnListener = new EditDrafts.ReturnListener() {

        @Override
        public void returnedDraft(DraftEntry fieldNote, boolean isNewDraft, boolean directlog) {
            addOrChangeDraft(fieldNote, isNewDraft, directlog);
        }

    };

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
                fieldNoteEntries.add(fieldNote);

                // eine evtl. vorhandene Draft /DNF löschen
                if (fieldNote.type == LogTypes.attended //
                        || fieldNote.type == LogTypes.found //
                        || fieldNote.type == LogTypes.webcam_photo_taken //
                        || fieldNote.type == LogTypes.didnt_find) {
                    fieldNoteEntries.deleteDraftByCacheId(fieldNote.CacheId, LogTypes.found);
                    fieldNoteEntries.deleteDraftByCacheId(fieldNote.CacheId, LogTypes.didnt_find);
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
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                        Config.FoundOffset.setValue(aktDraft.foundNumber);
                        Config.AcceptChanges();
                    }

                } else if (fieldNote.type == LogTypes.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
                    if (EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(false);
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(EventHandler.getSelectedCache());
                        Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                        Config.AcceptChanges();
                    } // und eine evtl. vorhandene Draft FoundIt löschen
                    fieldNoteEntries.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogTypes.found);
                }
            }
            DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());

            // Reload List
            if (isNewDraft) {
                fieldNoteEntries.loadDrafts("", DraftList.LoadingType.LOAD_NEW);
            } else {
                fieldNoteEntries.loadDrafts("", DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
            }
        }
        THAT.notifyDataSetChanged();
    }

    private static void logOnline(final DraftEntry fieldNote, final boolean isNewDraft) {

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
//                int result = GroundspeakAPI.createDraftAndPublish(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, dl, this);
//
//                if (result == GroundspeakAPI.IO) {
//                    fieldNote.uploaded = true;
//
//                    // after direct Log create a fieldNote with uploded state
//                    addOrChangeDraft(fieldNote, isNewDraft, false);
//                }
//
//                if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
//                    GL.that.Toast(ConnectionError.INSTANCE);
//                    if (wd != null)
//                        wd.close();
//                    MessageBox.show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
//
//                        @Override
//                        public boolean onClick(int which, Object data) {
//                            switch (which) {
//                                case ButtonDialog.BUTTON_NEGATIVE:
//                                    addOrChangeDraft(fieldNote, isNewDraft, true);// try again
//                                    return true;
//
//                                case ButtonDialog.BUTTON_NEUTRAL:
//                                    return true;
//
//                                case ButtonDialog.BUTTON_POSITIVE:
//                                    addOrChangeDraft(fieldNote, isNewDraft, false);// create Fieldnote
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
//                    MessageBox.show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
//
//                        @Override
//                        public boolean onClick(int which, Object data) {
//                            switch (which) {
//                                case ButtonDialog.BUTTON_NEGATIVE:
//                                    addOrChangeDraft(fieldNote, isNewDraft, true);// try again
//                                    return true;
//
//                                case ButtonDialog.BUTTON_NEUTRAL:
//                                    return true;
//
//                                case ButtonDialog.BUTTON_POSITIVE:
//                                    addOrChangeDraft(fieldNote, isNewDraft, false);// create Fieldnote
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
//                            MessageBox.show(GroundspeakAPI.LastAPIError, Translation.Get("Error"), MessageBoxIcon.Error);
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

    private static EditDrafts efnActivity;

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
        // Database.Data.Query.GetCacheByGcCode(aktDraft.gcCode);

        AbstractCache tmpAbstractCache = null;
        // suche den Cache aus der DB.
        // Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(lCaches, "Id = " + aktDraft.CacheId, false, false);
        if (lCaches.size > 0)
            tmpAbstractCache = lCaches.get(0);
        final AbstractCache abstractCache = tmpAbstractCache;

        if (abstractCache == null && !aktDraft.isTbDraft) {
            String message = Translation.Get("cacheOtherDb",aktDraft.CacheName.toString());
            message += "\n" + Translation.Get("fieldNoteNoDelete");
            MessageBox.show(message);
            return;
        }

        OnMsgBoxClickListener dialogClickListener = new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                switch (which) {
                    case ButtonDialog.BUTTON_POSITIVE:
                        // Yes button clicked
                        // delete aktDraft
                        if (abstractCache != null) {
                            if (abstractCache.isFound()) {
                                abstractCache.setFound(false);
                                CacheDAO cacheDAO = new CacheDAO();
                                cacheDAO.WriteToDatabase_Found(abstractCache);
                                Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                                Config.AcceptChanges();
                                // jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
                                // damit das Smiley Symbol aus der Map und der CacheList verschwindet
                                synchronized (Database.Data.Query) {
                                    AbstractCache tc = Database.Data.Query.GetCacheById(abstractCache.getId());
                                    if (tc != null) {
                                        tc.setFound(false);
                                    }
                                }
                            }
                        }
                        fieldNoteEntries.deleteDraft(aktDraft.Id, aktDraft.type);

                        aktDraft = null;

                        fieldNoteEntries.loadDrafts("", DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);

                        loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);

                        DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());

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
        if (aktDraft.isTbDraft) {
            message = Translation.Get("confirmFieldnoteDeletionTB", aktDraft.typeString, aktDraft.TbName);
        } else {
            message = Translation.Get("confirmFieldnoteDeletion", aktDraft.typeString, aktDraft.CacheName.toString());
            if (aktDraft.type == LogTypes.found || aktDraft.type == LogTypes.attended || aktDraft.type == LogTypes.webcam_photo_taken)
                message += Translation.Get("confirmFieldnoteDeletionRst");
        }

        MessageBox.show(message, Translation.Get("deleteFieldnote"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, dialogClickListener);

    }

    private void deleteAllDrafts() {
        final OnMsgBoxClickListener dialogClickListener = new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                switch (which) {
                    case ButtonDialog.BUTTON_POSITIVE:
                        // Yes button clicked
                        // delete all Drafts
                        // reload all Fieldnotes!
                        fieldNoteEntries.loadDrafts("", DraftList.LoadingType.LOAD_ALL);

                        for (DraftEntry entry : fieldNoteEntries) {
                            entry.deleteFromDatabase();

                        }

                        fieldNoteEntries.clear();
                        aktDraft = null;

                        loadDrafts(DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
                        break;

                    case ButtonDialog.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        break;
                }
                return true;

            }
        };
        final String message = Translation.Get("DeleteAllDraftsQuestion");
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                MessageBox.show(message, Translation.Get("DeleteAllNotes"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning, dialogClickListener);

            }
        });
    }

    private void selectCacheFromDraft() {
        if (aktDraft == null)
            return;

        // suche den Cache aus der DB.
        // Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(lCaches, "Id = " + aktDraft.CacheId, false, false);
        AbstractCache tmpCache = null;
        if (lCaches.size > 0)
            tmpCache = lCaches.get(0);
        AbstractCache cache = tmpCache;

        if (cache == null) {
            String message = Translation.Get("cacheOtherDb", aktDraft.CacheName.toString());
            message += "\n" + Translation.Get("fieldNoteNoSelect");
            MessageBox.show(message, Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            return;
        }

        synchronized (Database.Data.Query) {
            cache = Database.Data.Query.GetCacheByGcCode(aktDraft.gcCode);
        }

        if (cache == null) {
            Database.Data.Query.add(tmpCache);
            cache = Database.Data.Query.GetCacheByGcCode(aktDraft.gcCode);
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

    private final OnItemClickListener itemLogClickListener = new OnItemClickListener() {


        @Override
        public boolean onItemClick(MenuItem item) {

            int index = item.getListIndex();

            aktDraft = fieldNoteEntries.get(index);

            Menu cm = new Menu("CacheListContextMenu");

            cm.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public boolean onItemClick(MenuItem item) {
                    switch (item.getMenuItemId()) {
                        case MenuID.MI_SELECT_CACHE:
                            selectCacheFromDraft();
                            return true;
                        case MenuID.MI_EDIT_FIELDNOTE:
                            editDraft();
                            return true;
                        case MenuID.MI_DELETE_FIELDNOTE:
                            deleteDraft();
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

        CB.postOnMainThread(new Runnable() {
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

        AbstractCache abstractCache = EventHandler.getSelectedCache();

        final Menu cm = new Menu("DraftContextMenu");

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
                        uploadDrafts();
                        return true;
                    case MenuID.MI_DELETE_ALL_FIELDNOTES:
                        deleteAllDrafts();
                        return true;
                }
                return false;
            }
        });

        if (abstractCache != null) {

            // Found je nach CacheType
            if (abstractCache.getType() == null)
                return null;
            switch (abstractCache.getType()) {
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
        if (abstractCache != null && abstractCache.getGcCode().toString().toLowerCase().startsWith("gc")) {
            cm.addItem(MenuID.MI_MAINTANCE, "maintenance", itemStyle.typeStyle.needs_maintenance);
            cm.addItem(MenuID.MI_NOTE, "writenote", itemStyle.typeStyle.note);
        }

        cm.addItem(MenuID.MI_UPLOAD_FIELDNOTE, "uploadDrafts", CB.getSkin().getMenuIcon.uploadDraft);
        cm.addItem(MenuID.MI_DELETE_ALL_FIELDNOTES, "DeleteAllNotes", CB.getSkin().getMenuIcon.deleteAllDrafts);

        if (abstractCache != null) {
            MenuItem mi = cm.addItem(MenuID.MI_IMPORT, "ownerLogTypes", CB.getSkin().getMenuIcon.ownerLogTypes);
            mi.setMoreMenu(getSecondMenu());

            if (!abstractCache.ImTheOwner()) {
                //disable owner log types
                mi.setEnabled(false);
            }
        }

        return cm;
    }

}
