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
package de.longri.cachebox3.gui.activities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.actions.show_activities.Action_Quit;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.FileList;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqlite;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.SINGLE;

/**
 * Created by Longri on 29.08.2016.
 */
public class SelectDB_Activity extends ActivityBase {
    final static Logger log = LoggerFactory.getLogger(SelectDB_Activity.class);
    private final IReturnListener returnListener;
    Timer updateTimer;
    private int autoStartTime = 10;
    private int autoStartCounter = 0;
    private String DBPath;
    private CB_Button bNew;
    private CB_Button bSelect;
    private CB_Button bCancel;
    private CB_Button bAutostart;
    private ListView lvFiles;
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (autoStartCounter == 0) {
                stopTimer();
                selectDB();
            } else {
                try {
                    autoStartCounter--;
                    bAutostart.setText(autoStartCounter + "    " + Translation.get("confirm"));
                } catch (Exception e) {
                    autoStartCounter = 0;
                    stopTimer();
                    selectDB();
                }
            }
        }
    };
    private CustomAdapter lvAdapter;
    private String[] fileInfos;
    private boolean mustSelect = false;
    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            stopTimer();
            if (mustSelect) {
                new Action_Quit().execute();
            } else {
                finish();
            }
        }
    };
    private boolean needsLayout = true;

    public SelectDB_Activity(IReturnListener returnListener, boolean mustSelect) {
        super("select DB dialog");
        this.returnListener = returnListener;
        this.mustSelect = mustSelect;
        lvFiles = new ListView(VERTICAL);
        lvFiles.setSelectable(SINGLE);
        this.setListViewAdapter();
        this.addActor(lvFiles);


        bNew = new CB_Button(Translation.get("NewDB"));
        bSelect = new CB_Button(Translation.get("confirm"));
        bCancel = new CB_Button(Translation.get("cancel"));
        bAutostart = new CB_Button("");

        this.addActor(bSelect);
        this.addActor(bNew);
        this.addActor(bCancel);
        this.addActor(bAutostart);

        // New Button
        bNew.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                stopTimer();

                NewDB_InputBox inputBox = new NewDB_InputBox(new OnMsgBoxClickListener() {
                    @Override
                    public boolean onClick(int which, Object data) {
                        switch (which) {
                            case ButtonDialog.BUTTON_POSITIVE: // ok clicked
                                Object[] dataObjects = (Object[]) data;

                                Boolean ownRepository = !(Boolean) dataObjects[0];
                                String NewDB_Name = (String) dataObjects[1];

                                if (Database.createNewDB(Database.Data, Gdx.files.absolute(CB.WorkPath), NewDB_Name, ownRepository))
                                    return true;
                                Config.AcceptChanges();
                                Config.DatabaseName.setValue(NewDB_Name + ".db3");

                                Database.Data.cacheList.clear();

                                finish();
                                break;
                            case ButtonDialog.BUTTON_NEUTRAL: // cancel clicked

                                break;
                            case ButtonDialog.BUTTON_NEGATIVE:

                                break;
                        }

                        return true;
                    }
                });
                inputBox.show();
            }
        });

        // Select Button
        bSelect.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                stopTimer();
                if (lvFiles.getSelectedItem() == null) {
                    CB.viewmanager.toast("Please select Database!", ViewManager.ToastLength.NORMAL);
                    return;
                }
                selectDB();
            }
        });

        // Cancel Button
        bCancel.addListener(cancelClickListener);
        CB.stageManager.registerForBackKey(cancelClickListener);

        // AutoStart Button
        bAutostart.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                stopTimer();
                showSelectionMenu();
            }
        });

        autoStartTime = Config.MultiDBAutoStartTime.getValue();
        if (autoStartTime > 0) {
            autoStartCounter = autoStartTime;
            bAutostart.setText(autoStartCounter + " " + Translation.get("confirm"));
            if ((autoStartTime > 0) && (lvFiles.getSelectedItem() != null)) {
                updateTimer = new Timer();
                updateTimer.scheduleAtFixedRate(timerTask, 1000, 1000);
            } else
                stopTimer();
        }
        setAutoStartText();

    }

    private void setListViewAdapter() {
        String DBFile = Config.DatabaseName.getValue();
        final FileList files = new FileList(CB.WorkPath, "DB3", true);
        fileInfos = new String[files.size];
        int index = 0;
        int selectedIndex = -1;
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(DBFile))
                selectedIndex = index;
            fileInfos[index] = "";
            index++;
        }
        lvAdapter = new CustomAdapter(files);
        lvFiles.setAdapter(lvAdapter);
        if (selectedIndex > -1) {
            final int finalIdx = selectedIndex;
            CB.postOnNextGlThread(new Runnable() {
                @Override
                public void run() {
                    lvFiles.setSelection(finalIdx);
                }
            });

        }
        readCountAtThread();
    }

    @Override
    public void layout() {
        super.layout();
        if (!needsLayout) return;
        if (getWidth() <= 0 || getHeight() <= 0) return;

        float xPos = CB.scaledSizes.MARGIN, yPos = CB.scaledSizes.MARGIN;
        float btnWidth = (getWidth() - (CB.scaledSizes.MARGIN * 4)) / 3;
        float btnHeight = CB.scaledSizes.BUTTON_HEIGHT;
        float btnAreaWidth = btnWidth * 3 + CB.scaledSizes.MARGINx2;


        bNew.setBounds(xPos, yPos, btnWidth, btnHeight);
        xPos += CB.scaledSizes.MARGIN + btnWidth;
        bSelect.setBounds(xPos, yPos, btnWidth, btnHeight);
        xPos += CB.scaledSizes.MARGIN + btnWidth;
        bCancel.setBounds(xPos, yPos, btnWidth, btnHeight);

        xPos = CB.scaledSizes.MARGIN;
        yPos += CB.scaledSizes.MARGIN + btnHeight;
        bAutostart.setBounds(xPos, yPos, btnAreaWidth, btnHeight);
        yPos += CB.scaledSizes.MARGIN + btnHeight;

//        lvFiles.setBackground(null);
        lvFiles.setBounds(CB.scaledSizes.MARGIN, yPos, getWidth() - CB.scaledSizes.MARGINx2, getHeight() - (yPos + CB.scaledSizes.MARGIN));
        needsLayout = false;
    }

    @Override
    public void sizeChanged() {
        needsLayout = true;
        layout();
    }

    private void readCountAtThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                int index = 0;
                for (File file : lvAdapter.files) {
                    String lastModified = sdf.format(file.lastModified());
                    String fileSize = String.valueOf(file.length() / (1024 * 1024)) + "MB";
                    String cacheCount = String.valueOf(Database.getCacheCountInDB(file.getAbsolutePath()));
                    fileInfos[index] = cacheCount + " Caches  " + fileSize + "#last use " + lastModified;
                    index++;
                }
                Gdx.graphics.requestRendering();
            }
        };
        thread.start();
    }

    @Override
    public void onShow() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    lvFiles.setSelectedItemVisible(false);
                } catch (Exception e) {
                    log.error("Set selected Item Visible with onShow SelectDbActivity", e);
                }
            }
        });
    }

    private void selectDB() {

        CB.postAsync(new NamedRunnable("SelectDB_Activity") {
            @Override
            public void run() {
                if (lvFiles.getSelectedItem() == null) {
                    CB.viewmanager.toast("no DB selected", ViewManager.ToastLength.SHORT);
                    return;
                }

                String name = ((SelectDBItem) lvFiles.getSelectedItem()).getFileName();


                // if Database schema version <1028 we ask the User for convert
                GdxSqlite tempDB = null;
                GdxSqliteCursor cursor = null;
                try {
                    FileHandle dbFile = Gdx.files.absolute(CB.WorkPath).child(name);
                    tempDB = new GdxSqlite(dbFile);
                    tempDB.openOrCreateDatabase();

                    //get schema version
                    cursor = tempDB.rawQuery("SELECT Value FROM Config WHERE [Key] like 'DatabaseSchemeVersionWin'");
                    cursor.moveToFirst();
                    int version = Integer.parseInt(cursor.getString(0));

                    if (version < 1028) {

                        final AtomicBoolean WAIT = new AtomicBoolean(true);
                        final AtomicBoolean CONVERT = new AtomicBoolean(false);

                        CharSequence msg = Translation.get("DB_outdated_question");
                        CharSequence title = Translation.get("DB_outdated");

                        MessageBox.show(msg, title, MessageBoxButtons.YesNo, MessageBoxIcon.Database, new OnMsgBoxClickListener() {
                            @Override
                            public boolean onClick(int which, Object data) {

                                if (which == ButtonDialog.BUTTON_POSITIVE)
                                    CONVERT.set(true);

                                WAIT.set(false);
                                return true;
                            }
                        });
                        CB.wait(WAIT);
                        if (!CONVERT.get()) return;

                        //Show BlockUiActivity
                        CB.postOnGlThread(new NamedRunnable("SelectDbActivity") {
                            @Override
                            public void run() {
                                new BlockUiProgress_Activity(Translation.get("DB_Convert")).show();
                            }
                        });

                        //copy Db to *.db3.old
                        FileHandle target = dbFile.parent().child(name + ".old");
                        dbFile.copyTo(target);
                    }
                } catch (SQLiteGdxException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    if (cursor != null) cursor.close();
                    try {
                        if (tempDB != null) tempDB.closeDatabase();
                    } catch (SQLiteGdxException e) {
                        e.printStackTrace();
                    }
                }
                finish();

                Config.MultiDBAutoStartTime.setValue(autoStartTime);
                Config.MultiDBAsk.setValue(autoStartTime >= 0);
                Config.DatabaseName.setValue(name);
                Config.AcceptChanges();

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (returnListener != null)
                            returnListener.back();
                    }
                });
            }
        });
    }

    private void setAutoStartText() {
        if (autoStartTime < 0)
            bAutostart.setText(Translation.get("StartWithoutSelection"));
        else if (autoStartTime == 0)
            bAutostart.setText(Translation.get("AutoStartDisabled"));
        else
            bAutostart.setText(Translation.get("AutoStartTime", String.valueOf(autoStartTime)));
    }

    private void stopTimer() {
        if (updateTimer != null)
            updateTimer.cancel();
    }

    private void showSelectionMenu() {
        Menu cm = new Menu("SelectDBContextMenuTitle");
        cm.addMenuItem("StartWithoutSelection", null, () -> {
            autoStartTime = -1;
            setAutoStartText();
        });
        cm.addMenuItem("AutoStartDisabled", null, () -> {
            autoStartTime = 0;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "5").toString(), null, () -> {
            autoStartTime = 5;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "10").toString(), null, () -> {
            autoStartTime = 10;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "25").toString(), null, () -> {
            autoStartTime = 25;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "60").toString(), null, () -> {
            autoStartTime = 60;
            setAutoStartText();
        });
        cm.show();
    }

    @Override
    public void dispose() {
        DBPath = null;
        bNew = null;
        bSelect = null;
        bCancel = null;
        bAutostart = null;
        lvFiles = null;
        lvAdapter = null;
        fileInfos = null;
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
        super.dispose();
    }

    private void deleteDatabase(final File dbFile) {

        OnMsgBoxClickListener dialogClickListener = new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                switch (which) {
                    case ButtonDialog.BUTTON_POSITIVE:
                        // Yes button clicked
                        // delete aktDatabase

                        //check of own repository
                        FileHandle workPathFileHandle = Gdx.files.absolute(CB.WorkPath);
                        FileHandle dbFileHandle = workPathFileHandle.child(dbFile.getName());
                        FileHandle repositoryFileHandle = workPathFileHandle.child("repositories").child(dbFileHandle.nameWithoutExtension());

                        dbFileHandle.delete();
                        if (repositoryFileHandle.exists() && repositoryFileHandle.isDirectory())
                            repositoryFileHandle.deleteDirectory();
                        setListViewAdapter();
                        break;
                    case ButtonDialog.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        break;
                }
                return true;
            }
        };
        CharSequence message = Translation.get("confirmDatabaseDeletion", dbFile.getName());
        MessageBox.show(message, Translation.get("deleteDatabase"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, dialogClickListener);
    }


    public interface IReturnListener {
        public void back();
    }

    public static class SelectDbStyle {
        public BitmapFont nameFont, infoFont;
        public Color nameColor, infoColor;
    }

    private class CustomAdapter implements ListViewAdapter {

        Array<SelectDBItem> itemArray = new Array();
        private FileList files;

        private CustomAdapter(FileList files) {
            this.files = files;
        }

        public void setFiles(FileList files) {
            this.files = files;
        }

        public FileList getFileList() {
            return files;
        }

        @Override
        public int getCount() {
            return files.size;
        }

        public File getItem(int position) {
            return files.get(position);
        }

        @Override
        public ListViewItem getView(final int listIndex) {

            if (itemArray.size - 1 < listIndex) {
                SelectDBItem item = new SelectDBItem(listIndex, files.get(listIndex), VisUI.getSkin().get("default", SelectDbStyle.class));

                item.addListener(new ClickLongClickListener() {
                    @Override
                    public boolean clicked(InputEvent event, float x, float y) {
                        return false;
                    }

                    @Override
                    public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                        if (actor instanceof SelectDBItem) {
                            log.debug("longClick on Item {}", files.get(listIndex).getName());
                            deleteDatabase(files.get(listIndex));
                            return true;
                        }
                        return false;
                    }
                });
                return item;
            }
            return itemArray.get(listIndex);
        }

        @Override
        public void update(ListViewItem view) {
            SelectDBItem dbItem = (SelectDBItem) view;
            dbItem.updateFileInfo(fileInfos[view.getListIndex()]);
        }

    }


}
