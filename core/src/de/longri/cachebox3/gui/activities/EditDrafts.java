/*
 * Copyright (C) 2017-2018 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.skin.styles.ListViewStyle;
import de.longri.cachebox3.gui.widgets.AdjustableStarWidget;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.EditTextBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.DraftEntry;
import de.longri.cachebox3.types.DraftList;
import de.longri.cachebox3.types.IntProperty;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Longri on 02.09.2017.
 */
public class EditDrafts extends ActivityBase {

    private final static DateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault());
    private final static DateFormat iso8601FormatDate = new SimpleDateFormat("yyyy-MM-dd");
    private final static DateFormat iso8601FormatTime = new SimpleDateFormat("HH:mm");


    private final VisTable contentTable;
    private final CB_Button btnOk, btnCancel;
    private final DraftListItemStyle itemStyle;
    private final VisScrollPane scrollPane;
    private final VisLabel foundLabel, dateLabel, timeLabel;
    private final EditTextBox dateTextArea, timeTextArea, commentTextArea;

    private boolean isNewDraft;
    private ReturnListener returnListener;
    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (returnListener != null)
                returnListener.returnedDraft(null, false, false);
            finish();
        }
    };
    private DraftEntry actDraft;
    private DraftEntry altDraft;
    private boolean needsLayout = true;
    private AdjustableStarWidget gcVoteWidget;
    private Button onlineOption, fieldNoteOption;
    private final ClickListener saveClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (returnListener != null) {

                if (actDraft.type.isDirectLogType()) {
                    actDraft.isDirectLog = onlineOption.isChecked();
                } else {
                    actDraft.isDirectLog = false;
                }

                actDraft.comment = commentTextArea.getText();
                if (gcVoteWidget != null) {
                    actDraft.gc_Vote = gcVoteWidget.getValue() * 100;
                }

                //parse Date and Time
                String date = dateTextArea.getText();
                String time = timeTextArea.getText();

                date = date.replace("-", ".");
                time = time.replace(":", ".");

                try {
                    Date timestamp = dateFormatter.parse(date + "." + time + ".00");
                    actDraft.timestamp = timestamp;
                } catch (ParseException e) {
                    MessageBox.show(Translation.get("wrongDate"), Translation.get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
                    return;
                }

                // check of changes
                if (!altDraft.equals(actDraft)) {
                    actDraft.uploaded = false;
                    actDraft.updateDatabase();
                    DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());
                }
                returnListener.returnedDraft(actDraft, isNewDraft, actDraft.isDirectLog);
            }
            finish();
        }
    };

    public EditDrafts(DraftEntry note, ReturnListener returnListener, boolean isNewDraft) {
        super("EditDraft");
        itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", DraftListItemStyle.class);

        btnOk = new CB_Button(Translation.get("save"));
        btnOk.addListener(saveClickListener);
        btnCancel = new CB_Button(Translation.get("cancel"));
        btnCancel.addListener(cancelClickListener);
        contentTable = new VisTable();
        setDraft(note, returnListener, isNewDraft);
        if (!Config.GcVotePassword.getEncryptedValue().equalsIgnoreCase("")) {
            gcVoteWidget = new AdjustableStarWidget(AdjustableStarWidget.Type.STAR, Translation.get("maxRating"),
                    new IntProperty(), itemStyle.starStyle, itemStyle.cacheSizeStyle);
            gcVoteWidget.setBackground(CB.getSkin().get(ListViewStyle.class).firstItem);
        }

        scrollPane = new VisScrollPane(contentTable);

        if (note.isTbDraft)
            foundLabel = new VisLabel("");
        else
            foundLabel = new VisLabel("Founds: #" + note.foundNumber);

        dateLabel = new VisLabel(Translation.get("wptDate") + ":");
        timeLabel = new VisLabel(Translation.get("time") + ":");

        dateTextArea = new EditTextBox(false);
        timeTextArea = new EditTextBox(false) {
            //return same width like dateTextArea
            public float getPrefWidth() {
                return dateTextArea.getPrefWidth();
            }
        };
        commentTextArea = new EditTextBox(true);

        dateTextArea.setText(iso8601FormatDate.format(note.timestamp));
        timeTextArea.setText(iso8601FormatTime.format(note.timestamp));
        commentTextArea.setText(note.comment);

    }

    @Override
    public void onShow() {
        super.onShow();
        CB.stageManager.registerForBackKey(cancelClickListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
    }

    public void setDraft(DraftEntry note, ReturnListener listener, boolean isNewDraft) {
        this.isNewDraft = isNewDraft;
        this.returnListener = listener;
        this.actDraft = note;
        this.altDraft = note.copy();

        needsLayout = true;
        this.invalidate();
    }

    @Override
    public void layout() {
//        this.setDebug(true);

        if (!needsLayout) return;

        this.clear();
        contentTable.clear();
        this.addActor(btnOk);
        this.addActor(btnCancel);
        this.addActor(scrollPane);

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);
        x -= CB.scaledSizes.MARGIN + btnOk.getWidth();
        btnOk.setPosition(x, y);

        x = CB.scaledSizes.MARGIN;
        y += CB.scaledSizes.MARGIN + btnCancel.getHeight();

        Label.LabelStyle headerLabelStyle = new Label.LabelStyle();
        headerLabelStyle.font = this.itemStyle.headerFont;
        headerLabelStyle.fontColor = this.itemStyle.headerFontColor;

        Label.LabelStyle commentLabelStyle = new Label.LabelStyle();
        commentLabelStyle.font = this.itemStyle.descriptionFont;
        commentLabelStyle.fontColor = this.itemStyle.descriptionFontColor;


        VisTable cacheTable = new VisTable();

        VisTable iconTable = new VisTable();
        iconTable.add(actDraft.cacheType.getCacheWidget(itemStyle.cacheTypeStyle, null, null, null, null));
        iconTable.pack();
        iconTable.layout();

        cacheTable.add(iconTable).left().padRight(CB.scaledSizes.MARGINx2);

        VisLabel nameLabel = new VisLabel(actDraft.CacheName, headerLabelStyle);
        nameLabel.setWrap(true);
        cacheTable.add(nameLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        cacheTable.row();

        cacheTable.add((Actor) null).left().padRight(CB.scaledSizes.MARGINx2);

        VisLabel gcLabel = new VisLabel(actDraft.gcCode, headerLabelStyle);
        gcLabel.setWrap(true);
        cacheTable.add(gcLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        VisTable foundRow = new VisTable();
        Image typeIcon = new Image(actDraft.type.getDrawable(itemStyle.typeStyle));
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


        VisTable optionTable = new VisTable();
        optionTable.defaults().pad(CB.scaledSizes.MARGINx2);
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.checked = CB.getSkin().getDrawable("option_1");
        buttonStyle.up = CB.getSkin().getDrawable("option_0");
        onlineOption = new Button(buttonStyle);
        fieldNoteOption = new Button(buttonStyle);
        ButtonGroup<Button> optionGroup = new ButtonGroup<>();
        optionGroup.add(onlineOption, fieldNoteOption);
        fieldNoteOption.setChecked(true);
        Label onlineOptionLabel = new Label(Translation.get("directLog"), commentLabelStyle);
        Label fieldNoteOptionLabel = new Label(Translation.get("onlyDraft"), commentLabelStyle);

        onlineOptionLabel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                onlineOption.setChecked(true);
            }
        });
        fieldNoteOptionLabel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                fieldNoteOption.setChecked(true);
            }
        });

        optionTable.add(onlineOption).left();
        optionTable.add(onlineOptionLabel).left().expandX().fillX();
        optionTable.row();
        optionTable.add(fieldNoteOption).left();
        optionTable.add(fieldNoteOptionLabel).left().expandX().fillX();

//        contentTable.setDebug(true);
//        timeRow.setDebug(true);
        contentTable.defaults().pad(CB.scaledSizes.MARGIN);

        contentTable.add(cacheTable).expandX().fillX();
        contentTable.row().padBottom(CB.scaledSizes.MARGINx2);
        contentTable.add(foundRow).expandX().fillX();
        contentTable.row();
        contentTable.add(dateRow).right().expandX().fillX();
        contentTable.row();
        contentTable.add(timeRow).expandX().fillX();
        contentTable.row();
        contentTable.add(gcVoteWidget).expandX().fillX();
        contentTable.row();
        contentTable.add(commentTextArea).expandX().fillX();
        contentTable.row();
        contentTable.add(optionTable).expandX().fillX();
        contentTable.row();


        contentTable.add((Actor) null).expand().fill();//Fill
        contentTable.layout();

        scrollPane.setBounds(x, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGINx2));
        scrollPane.layout();

        super.layout();
    }

    public interface ReturnListener {
        void returnedDraft(DraftEntry fn, boolean isNewDraft, boolean directlog);
    }


}
