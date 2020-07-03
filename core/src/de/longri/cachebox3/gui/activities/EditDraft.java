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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.skin.styles.ListViewStyle;
import de.longri.cachebox3.gui.widgets.AdjustableStarWidget;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.Draft;
import de.longri.cachebox3.types.Drafts;
import de.longri.cachebox3.types.IntProperty;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Longri on 02.09.2017.
 */
public class EditDraft extends Activity {

    private final static DateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US);
    private final static DateFormat iso8601FormatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final static DateFormat iso8601FormatTime = new SimpleDateFormat("HH:mm", Locale.US);
    private static EditDraft editDraft;
    private final CB_Button btnLog;
    private final CB_Button btnDraft;
    private final DraftListItemStyle draftListItemStyle;
    private final VisLabel foundLabel, dateLabel, timeLabel;
    private final EditTextField dateTextArea, timeTextArea, commentTextArea;
    private boolean isNewDraft;
    private IDraftsView draftsView;
    private Draft currentDraft;
    private Draft originalDraft;
    private AdjustableStarWidget gcVoteWidget;

    private EditDraft() {
        super("EditDraft", CB.getSkin().menuIcon.editDraft);
        draftListItemStyle = CB.getSkin().get(DraftListItemStyle.class);

        setOKText(Translation.get("save"));
        btnLog = new CB_Button(Translation.get("GCLog"));
        btnDraft = new CB_Button(Translation.get("GCDraft"));
        ClickListener saveClickListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (draftsView != null) {
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
                    draftsView.addOrChangeDraft(currentDraft, isNewDraft, clickedBy);
                }
                finish();
            }
        };
        setOkListener(saveClickListener);
        btnDraft.addListener(saveClickListener);
        btnLog.addListener(saveClickListener);

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

    public void setDraft(Draft _note, IDraftsView _listener, boolean _isNewDraft) {
        isNewDraft = _isNewDraft;
        draftsView = _listener;
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

        /*
        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);
        x -= CB.scaledSizes.MARGIN + btnOk.getWidth();
        btnOk.setPosition(x, y);

        x = CB.scaledSizes.MARGIN;
        y += CB.scaledSizes.MARGIN + btnCancel.getHeight();

         */

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
    }

    @Override
    protected void runAtCancel(InputEvent event, float x, float y) {
        if (draftsView != null)
            draftsView.addOrChangeDraft(null, false, SaveMode.Cancel);
    }

    public enum SaveMode {Cancel, OnlyLocal, Draft, Log, LocalUpdate}

    public interface IDraftsView {
        void addOrChangeDraft(Draft fn, boolean isNewDraft, SaveMode saveMode);
    }


}
