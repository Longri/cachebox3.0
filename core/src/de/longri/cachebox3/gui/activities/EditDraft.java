/*
 * Copyright (C) 2020-2018 team-cachebox.de
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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GCVote;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.skin.styles.ListViewStyle;
import de.longri.cachebox3.gui.views.DraftsView;
import de.longri.cachebox3.gui.widgets.AdjustableStarWidget;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static de.longri.cachebox3.apis.GroundspeakAPI.OK;
import static de.longri.cachebox3.gui.activities.EditDraft.SaveMode.LocalUpdate;

/**
 * Created by Longri on 02.09.2017.
 */
public class EditDraft extends Activity {
    private static final Logger log = LoggerFactory.getLogger(EditDraft.class);

    private final static DateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US);
    private final static DateFormat iso8601FormatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final static DateFormat iso8601FormatTime = new SimpleDateFormat("HH:mm", Locale.US);
    private final DraftListItemStyle draftListItemStyle;
    private static EditDraft editDraft;
    private final CB_Button btnLog;
    private final CB_Button btnDraft;
    private final VisLabel foundLabel, dateLabel, timeLabel;
    private final EditTextField dateTextArea, timeTextArea, commentTextArea;
    private boolean isNewDraft;
    private Draft currentDraft;
    private Draft originalDraft;
    private AdjustableStarWidget gcVoteWidget;

    private EditDraft() {
        super("EditDraft", CB.getSkin().menuIcon.editDraft);
        draftListItemStyle = VisUI.getSkin().get(DraftListItemStyle.class);

        btnOK.setText(Translation.get("save"));
        btnLog = new CB_Button(Translation.get("GCLog"));
        btnDraft = new CB_Button(Translation.get("GCDraft"));
        btnDraft.addListener(btnOK.getClickListener());
        btnLog.addListener(btnOK.getClickListener());

        if (!Config.GcVotePassword.getEncryptedValue().equalsIgnoreCase("")) {
            gcVoteWidget = new AdjustableStarWidget(AdjustableStarWidget.Type.STAR, Translation.get("maxRating"),
                    new IntProperty(), draftListItemStyle.starStyle, draftListItemStyle.cacheSizeStyle);
            gcVoteWidget.setBackground(CB.getSkin().get(ListViewStyle.class).firstItem);
        }

        foundLabel = new VisLabel("");

        dateLabel = new VisLabel(Translation.get("date") + ":");
        timeLabel = new VisLabel(Translation.get("time") + ":");

        dateTextArea = new EditTextField(false);
        timeTextArea = new EditTextField(false) {
            //return same width like dateTextArea
            public float getPrefWidth() {
                return dateTextArea.getPrefWidth();
            }
        };
        commentTextArea = new EditTextField(true);

    }

    public static EditDraft getInstance() {
        if (editDraft == null || editDraft.isDisposed()) {
            editDraft = new EditDraft();
        }
        return editDraft;
    }

    public void setDraft(Draft _note, boolean _isNewDraft) {
        isNewDraft = _isNewDraft;
        currentDraft = _note;
        originalDraft = _note.copy();
        invalidate();
    }

    @Override
    protected void createMainContent() {

        if (currentDraft.isTbDraft)
            foundLabel.setText("");
        else
            foundLabel.setText("Founds: #" + currentDraft.foundNumber);

        dateTextArea.setText(iso8601FormatDate.format(currentDraft.timestamp));
        timeTextArea.setText(iso8601FormatTime.format(currentDraft.timestamp));
        commentTextArea.setText(currentDraft.comment);

        Label.LabelStyle headerLabelStyle = new Label.LabelStyle();
        headerLabelStyle.font = draftListItemStyle.headerFont;
        headerLabelStyle.fontColor = draftListItemStyle.headerFontColor;

        Label.LabelStyle commentLabelStyle = new Label.LabelStyle();
        commentLabelStyle.font = draftListItemStyle.descriptionFont;
        commentLabelStyle.fontColor = draftListItemStyle.descriptionFontColor;


        VisTable cacheTable = new VisTable();

        VisTable iconTable = new VisTable();
        iconTable.add(currentDraft.cacheType.getCacheWidget(draftListItemStyle.cacheTypeStyle, null, null, null, null));
        iconTable.pack();
        iconTable.layout();

        cacheTable.add(iconTable).left().padRight(CB.scaledSizes.MARGINx2);
        VisLabel nameLabel = new VisLabel(currentDraft.CacheName, headerLabelStyle);
        nameLabel.setWrap(true);
        cacheTable.add(nameLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();
        cacheTable.row();
        cacheTable.add((Actor) null).left().padRight(CB.scaledSizes.MARGINx2);
        VisLabel gcLabel = new VisLabel(currentDraft.gcCode, headerLabelStyle);
        gcLabel.setWrap(true);
        cacheTable.add(gcLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        VisTable foundRow = new VisTable();
        Image typeIcon = new Image(currentDraft.type.getDrawable(draftListItemStyle.logTypesStyle));
        foundRow.defaults().pad(CB.scaledSizes.MARGINx2);
        foundRow.add(typeIcon);
        foundRow.add(foundLabel);
        foundRow.add((Actor) null).expandX().fillX();

        VisTable dateRow = new VisTable();
        dateRow.defaults().pad(CB.scaledSizes.MARGINx2);
        dateRow.add((Actor) null).expandX().fillX();
        dateRow.add(dateLabel).right();
        dateRow.add(dateTextArea).right();

        VisTable timeRow = new VisTable();
        timeRow.defaults().pad(CB.scaledSizes.MARGINx2);
        timeRow.add((Actor) null).expandX().fillX();
        timeRow.add(timeLabel);
        timeRow.add(timeTextArea);

        mainContent.addLast(cacheTable);
        mainContent.addLast(foundRow);
        mainContent.addLast(dateRow);
        mainContent.addLast(timeRow);
        mainContent.addLast(gcVoteWidget);
        mainContent.addLast(commentTextArea);

        mainContent.addNext(btnLog);
        mainContent.addLast(btnDraft);
    }

    @Override
    protected void runAtOk(InputEvent event, float x, float y) {
        SaveMode clickedBy = SaveMode.OnlyLocal;
        if (event.getListenerActor() == btnLog) clickedBy = SaveMode.Log;
        else if (event.getListenerActor() == btnDraft) clickedBy = SaveMode.Draft;

        currentDraft.isDirectLog = false;

        currentDraft.comment = commentTextArea.getText();
        if (gcVoteWidget != null) {
            currentDraft.gc_Vote = gcVoteWidget.getValue() * 100;
        }

        //parse Date and Time
        String date = dateTextArea.getText();
        String time = timeTextArea.getText();

        date = date.replace("-", ".");
        time = time.replace(":", ".");

        try {
            currentDraft.timestamp = dateFormatter.parse(date + "." + time + ".00");
        } catch (ParseException e) {
            MessageBox.show(Translation.get("wrongDate"), Translation.get("Error"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
            return;
        }

        // check of changes
        if (!originalDraft.equals(currentDraft)) {
            currentDraft.uploaded = false;
            currentDraft.updateDatabase();
            Drafts.createVisitsTxt(Config.DraftsGarminPath.getValue());
        }
        addOrChangeDraft(currentDraft, isNewDraft, clickedBy);
    }

    @Override
    protected void runAtCancel(InputEvent event, float x, float y) {
            addOrChangeDraft(null, false, SaveMode.Cancel);
    }

    void addOrChangeDraft(Draft draft, boolean isNewDraft, EditDraft.SaveMode saveMode) {

        if (draft != null) {

            if (isNewDraft) {
                // nur, wenn eine Draft neu angelegt wurde
                // new Draft
                DraftsView.getInstance().drafts.add(draft);

                // eine evtl. vorhandene Draft /DNF löschen
                if (draft.type == LogType.attended //
                        || draft.type == LogType.found //
                        || draft.type == LogType.webcam_photo_taken //
                        || draft.type == LogType.didnt_find) {
                    DraftsView.getInstance().drafts.deleteDraftByCacheId(draft.CacheId, LogType.found);
                    DraftsView.getInstance().drafts.deleteDraftByCacheId(draft.CacheId, LogType.didnt_find);
                }
            }

            draft.writeToDatabase();
            currentDraft = draft;

            if (isNewDraft) {
                // nur, wenn eine Draft neu angelegt wurde
                // wenn eine Draft neu angelegt werden soll dann kann hier auf SelectedCache zugegriffen werden, da nur für den
                // SelectedCache eine fieldNote angelegt wird
                if (draft.type == LogType.found //
                        || draft.type == LogType.attended //
                        || draft.type == LogType.webcam_photo_taken) {
                    // Found it! -> Cache als gefunden markieren
                    if (!EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(true);
                        EventHandler.getSelectedCache().updateBooleanStore();
                        DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                        Config.FoundOffset.setValue(currentDraft.foundNumber);
                        Config.AcceptChanges();
                    }

                } else if (draft.type == LogType.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
                    if (EventHandler.getSelectedCache().isFound()) {
                        EventHandler.getSelectedCache().setFound(false);
                        EventHandler.getSelectedCache().updateBooleanStore();
                        AbstractCache newCache = DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, EventHandler.getSelectedCache());
                        EventHandler.fire(new SelectedCacheChangedEvent(newCache));
                        Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                        Config.AcceptChanges();
                    } // und eine evtl. vorhandene Draft FoundIt löschen
                    DraftsView.getInstance().drafts.deleteDraftByCacheId(EventHandler.getSelectedCache().getId(), LogType.found);
                }
            }
            Drafts.createVisitsTxt(Config.DraftsGarminPath.getValue());

            if (saveMode == EditDraft.SaveMode.Log)
                logOnline(currentDraft, true);
            else if (saveMode == EditDraft.SaveMode.Draft)
                logOnline(currentDraft, false);

            // Reload List
            if (isNewDraft) {
                DraftsView.getInstance().drafts.loadDrafts("", Drafts.LoadingType.LOAD_NEW);
            } else {
                DraftsView.getInstance().drafts.loadDrafts("", Drafts.LoadingType.LOAD_NEW_LAST_LENGTH);
            }
        }
        DraftsView.getInstance().notifyDataSetChanged();
    }

    public void logOnline(Draft draft, final boolean directLog) {

        if (!draft.isTbDraft) {
            if (draft.gc_Vote > 0) {
                // Stimme abgeben
                try {
                    GCVote gcVote = new GCVote(Database.Data, Config.GcLogin.getValue(), Config.GcVotePassword.getValue());
                    if (gcVote.isPossible()) {
                        if (!gcVote.sendVote(draft.gc_Vote, draft.CacheUrl, draft.gcCode)) {
                            log.error(draft.gcCode + " GC-Vote");
                        }
                    }
                } catch (Exception e) {
                    log.error(draft.gcCode + " GC-Vote");
                }
            }
        }

        String logReferenceCode = GroundspeakAPI.getInstance().UploadDraftOrLog(draft.gcCode, draft.type.getGcLogTypeId(), draft.timestamp, draft.comment, directLog);
        if (GroundspeakAPI.getInstance().APIError == OK) {
            // after direct Log change state to uploaded
            draft.uploaded = true;
            if (directLog && !draft.isTbDraft) {
                draft.GcId = logReferenceCode;
                // LogListView.notifyDataSetChanged(); // getInstance().resetInitial(); // if own log is written !
            }
            addOrChangeDraft(draft, false, LocalUpdate);
        } else {
            // Error handling
            MessageBox.show(Translation.get("CreateFieldnoteInstead"), Translation.get("UploadFailed"), MessageBoxButton.YesNoRetry, MessageBoxIcon.Question, (which, data) -> {
                switch (which) {
                    case ButtonDialog.BUTTON_NEGATIVE:
                        logOnline(draft,true);// try again create log at gc
                        break;
                    case ButtonDialog.BUTTON_NEUTRAL:
                        break;
                    case ButtonDialog.BUTTON_POSITIVE:
                        logOnline(draft,false); // create draft at gc
                }
                return true;
            });
        }
        if (GroundspeakAPI.getInstance().LastAPIError.length() > 0) {
            MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get("Error"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
        }

    }

    public enum SaveMode {Cancel, OnlyLocal, Draft, Log, LocalUpdate}

}
