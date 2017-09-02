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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.stages.StageManager;
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


    private final VisTable contentTable;
    private final VisTextButton btnOk, btnCancel;

    private boolean isNewFieldNote;
    private ReturnListener returnListener;
    private FieldNoteEntry actFieldNote;
    private FieldNoteEntry altFieldNote;
    private boolean needsLayout = true;

    public interface ReturnListener {
        void returnedFieldNote(FieldNoteEntry fn, boolean isNewFieldNote, boolean directlog);
    }


    public EditFieldNotes(FieldNoteEntry note, ReturnListener returnListener, boolean isNewFieldNote) {
        super("EditFieldNote");
        btnOk = new VisTextButton(Translation.Get("save"));
        btnOk.addListener(saveClickListener);
        btnCancel = new VisTextButton(Translation.Get("cancel"));
        btnCancel.addListener(cancelClickListener);
        contentTable = new VisTable();
        setFieldNote(note, returnListener, isNewFieldNote);
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
//            if (returnListener != null) {
//
//                if (actFieldNote.type.isDirectLogType()) {
//                    actFieldNote.isDirectLog = rbDirectLog.isChecked();
//                } else {
//                    actFieldNote.isDirectLog = false;
//                }
//
//                actFieldNote.comment = etComment.getText();
//                if (GcVote != null) {
//                    actFieldNote.gc_Vote = (int) (GcVote.getValue() * 100);
//                }
//
//                // parse Date and Time
//                String date = tvDate.getText();
//                String time = tvTime.getText();
//
//                date = date.replace("-", ".");
//                time = time.replace(":", ".");
//
//                try {
//                    Date timestamp;
//                    DateFormat formatter;
//
//                    formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault());
//                    timestamp = formatter.parse(date + "." + time + ".00");
//
//                    actFieldNote.timestamp = timestamp;
//                } catch (ParseException e) {
//                    MessageBox.Show(Translation.Get("wrongDate"), Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
//                    return;
//                }
//
//                // check of changes
//                if (!altFieldNote.equals(actFieldNote)) {
//                    actFieldNote.uploaded = false;
//                    actFieldNote.updateDatabase();
//                    FieldNoteList.createVisitsTxt(Config.FieldNotesGarminPath.getValue());
//                }
//
//                returnListener.returnedFieldNote(actFieldNote, isNewFieldNote, actFieldNote.isDirectLog);
//            }
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
        super.layout();

        if (!needsLayout) return;

        this.clear();
        contentTable.clear();
        this.addActor(btnOk);
        this.addActor(btnCancel);
        this.addActor(contentTable);

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);
        x -= CB.scaledSizes.MARGIN + btnOk.getWidth();
        btnOk.setPosition(x, y);

        x = CB.scaledSizes.MARGIN;
        y += CB.scaledSizes.MARGIN + btnCancel.getHeight();

        float maxWidth = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx4;
//        titleTextArea.setMaxWidth(maxWidth);
//        descriptionTextArea.setMaxWidth(maxWidth);
//        clueTextArea.setMaxWidth(maxWidth);

        contentTable.setBounds(x, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGINx2));
        contentTable.layout();
    }


}
