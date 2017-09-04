/*
 * Copyright (C) 2017 team-cachebox.de
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
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.skin.styles.FieldNoteListItemStyle;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.widgets.AdjustableStarWidget;
import de.longri.cachebox3.gui.widgets.EditTextBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.FieldNoteEntry;
import de.longri.cachebox3.types.FieldNoteList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Longri on 02.09.2017.
 */
public class EditFieldNotes extends ActivityBase {

    private final static DateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault());
    private final static DateFormat iso8601FormatDate = new SimpleDateFormat("yyyy-MM-dd");
    private final static DateFormat iso8601FormatTime = new SimpleDateFormat("HH:mm");


    private final VisTable contentTable;
    private final VisTextButton btnOk, btnCancel;
    private final FieldNoteListItemStyle itemStyle;
    private final VisScrollPane scrollPane;
    private final VisLabel foundLabel, dateLabel, timeLabel;
    private final EditTextBox dateTextArea, timeTextArea, commentTextArea;

    private boolean isNewFieldNote;
    private ReturnListener returnListener;
    private FieldNoteEntry actFieldNote;
    private FieldNoteEntry altFieldNote;
    private boolean needsLayout = true;
    private AdjustableStarWidget gcVoteWidget;
    private Button onlineOption, fieldNoteOption;

    public interface ReturnListener {
        void returnedFieldNote(FieldNoteEntry fn, boolean isNewFieldNote, boolean directlog);
    }


    public EditFieldNotes(FieldNoteEntry note, ReturnListener returnListener, boolean isNewFieldNote) {
        super("EditFieldNote");
        itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", FieldNoteListItemStyle.class);

        btnOk = new VisTextButton(Translation.Get("save"));
        btnOk.addListener(saveClickListener);
        btnCancel = new VisTextButton(Translation.Get("cancel"));
        btnCancel.addListener(cancelClickListener);
        contentTable = new VisTable();
        setFieldNote(note, returnListener, isNewFieldNote);
        if (!Config.GcVotePassword.getEncryptedValue().equalsIgnoreCase("")) {
            gcVoteWidget = new AdjustableStarWidget(Translation.Get("maxRating"));
            gcVoteWidget.setBackground(CB.getSkin().get(ListView.ListViewStyle.class).firstItem);
        }

        scrollPane = new VisScrollPane(contentTable);

        if (note.isTbFieldNote)
            foundLabel = new VisLabel("");
        else
            foundLabel = new VisLabel("Founds: #" + note.foundNumber);

        dateLabel = new VisLabel(Translation.Get("date") + ":");
        timeLabel = new VisLabel(Translation.Get("time") + ":");

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

    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (returnListener != null)
                returnListener.returnedFieldNote(null, false, false);
            finish();
        }
    };

    private final ClickListener saveClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (returnListener != null) {

                if (actFieldNote.type.isDirectLogType()) {
                    actFieldNote.isDirectLog = onlineOption.isChecked();
                } else {
                    actFieldNote.isDirectLog = false;
                }

                actFieldNote.comment = commentTextArea.getText();
                if (gcVoteWidget != null) {
                    actFieldNote.gc_Vote = gcVoteWidget.getValue() * 100;
                }

                //parse Date and Time
                String date = dateTextArea.getText();
                String time = timeTextArea.getText();

                date = date.replace("-", ".");
                time = time.replace(":", ".");

                try {
                    Date timestamp = dateFormatter.parse(date + "." + time + ".00");
                    actFieldNote.timestamp = timestamp;
                } catch (ParseException e) {
                    MessageBox.Show(Translation.Get("wrongDate"), Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
                    return;
                }

                // check of changes
                if (!altFieldNote.equals(actFieldNote)) {
                    actFieldNote.uploaded = false;
                    actFieldNote.updateDatabase();
                    FieldNoteList.createVisitsTxt(Config.FieldNotesGarminPath.getValue());
                }
                returnListener.returnedFieldNote(actFieldNote, isNewFieldNote, actFieldNote.isDirectLog);
            }
            finish();
        }
    };

    @Override
    public void onShow() {
        super.onShow();
        StageManager.registerForBackKey(cancelClickListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        StageManager.unRegisterForBackKey(cancelClickListener);
    }

    public void setFieldNote(FieldNoteEntry note, ReturnListener listener, boolean isNewFieldNote) {
        this.isNewFieldNote = isNewFieldNote;
        this.returnListener = listener;
        this.actFieldNote = note;
        this.altFieldNote = note.copy();

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
        iconTable.add(actFieldNote.cacheType.getCacheWidget(itemStyle.cacheTypeStyle, null, null));
        iconTable.pack();
        iconTable.layout();

        cacheTable.add(iconTable).left().padRight(CB.scaledSizes.MARGINx4);

        VisLabel nameLabel = new VisLabel(actFieldNote.CacheName, headerLabelStyle);
        nameLabel.setWrap(true);
        cacheTable.add(nameLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        cacheTable.row();

        cacheTable.add((Actor) null).left().padRight(CB.scaledSizes.MARGINx4);

        VisLabel gcLabel = new VisLabel(actFieldNote.gcCode, headerLabelStyle);
        gcLabel.setWrap(true);
        cacheTable.add(gcLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        VisTable foundRow = new VisTable();
        Image typeIcon = new Image(actFieldNote.type.getDrawable(itemStyle.typeStyle));
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
        optionTable.defaults().pad(CB.scaledSizes.MARGINx4);
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.checked = CB.getSkin().getDrawable("option_1");
        buttonStyle.up = CB.getSkin().getDrawable("option_0");
        onlineOption = new Button(buttonStyle);
        fieldNoteOption = new Button(buttonStyle);
        ButtonGroup<Button> optionGroup = new ButtonGroup<>();
        optionGroup.add(onlineOption, fieldNoteOption);
        fieldNoteOption.setChecked(true);
        Label onlineOptionLabel = new Label(Translation.Get("directLog"), commentLabelStyle);
        Label fieldNoteOptionLabel = new Label(Translation.Get("onlyFieldNote"), commentLabelStyle);

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
        contentTable.row().padBottom(CB.scaledSizes.MARGINx4);
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


}
