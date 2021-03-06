/*
 * Copyright (C) 2016 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 14.09.2016.
 */
public class NotesView extends AbstractTableView implements SelectedCacheChangedListener {
    private final static Logger log = LoggerFactory.getLogger(NotesView.class);
    private CB_Button getSolverButton;
    private EditTextField notes;
    private CB_Button btnUpload;
    private AbstractCache currentCache;
    private String notesText;

    public NotesView(BitStore reader) {
        super(reader);
    }

    public NotesView() {
        super("NotesView");

        btnUpload = new CB_Button(Translation.get("Upload"));
        btnUpload.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (notes.getText().length() > 0) {
                    btnUpload.setText("Cancel");
                    String UploadText = notes.getText().replace("<Import from Geocaching.com>", "").replace("</Import from Geocaching.com>", "").trim();
                    int result = GroundspeakAPI.getInstance().uploadCacheNote(currentCache.getGeoCacheCode().toString(), UploadText);
                    btnUpload.disable();
                    if (result == 0) {
                        btnUpload.setText(Translation.get("successful"));
                    } else {
                        btnUpload.setText(Translation.get("Error"));
                    }
                }
            }
        });

        getSolverButton = new CB_Button(Translation.get("getSolver"));
        getSolverButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String solver;
                if (currentCache != null) {
                    solver = Database.getSolver(currentCache.getId());
                } else solver = null;
                solver = solver != null ? "<Solver>\r\n" + solver + "\r\n</Solver>" : "";
                String text = notes.getText();
                int i1 = text.indexOf("<Solver>");
                if (i1 > -1) {
                    int i2 = text.indexOf("</Solver>");
                    String t1 = text.substring(0, i1);
                    String t2;
                    if (i2 > -1) {
                        t2 = text.substring(i2 + 9);
                    } else {
                        t2 = text.substring(i1);
                    }
                    text = t1 + t2 + solver;
                } else {
                    text = text + solver;
                }
                notes.setText(text);
            }
        });

        notes = new EditTextField(true);
        notes.setMinLineCount(5);
        notes.setMaxLineCount(25);
        notes.setTextChangedCallBack(value -> {
            btnUpload.setText(Translation.get("Upload"));
            if (notes.getText().trim().length() == 0)
                btnUpload.disable();
            else
                btnUpload.enable();
        });

        contentTable.setTableAndCellDefaults();

    }

    @Override
    public void onShow() {
        loadNotes(EventHandler.getSelectedCache());
        if (notes.getText().trim().length() == 0) {
            btnUpload.disable();
        }
        create();
    }

    @Override
    public void onHide() {
        saveNotes();
    }

    private void loadNotes(AbstractCache newCache) {
        if (currentCache != newCache) {
            currentCache = newCache;
            notesText = currentCache != null ? Database.getNote(currentCache.getId()) : "";
            if (notesText == null)
                notesText = "";
            notes.setText(notesText);
            btnUpload.setText(Translation.get("Upload"));
            btnUpload.enable();
        }
    }

    private void saveNotes() {
        // Save changed Note text to Database
        String text = notes.getText();
        if (!notesText.equals(text)) {
            if (text != null) {
                try {
                    if (currentCache != null)
                        Database.setNote(currentCache, text);
                } catch (Exception e) {
                    log.error("Write note to database", e);
                }
            }
            else {
                log.error("NotesView: null text can not be written to database");
            }
        }
    }

    @Override
    public void dispose() {
    }

    protected void create() {
        contentTable.addLast(notes);
        contentTable.addNext(getSolverButton).padTop(0);
        contentTable.addLast(btnUpload).padTop(0);
    }

    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        saveNotes();
        loadNotes(event.cache);
    }
}
