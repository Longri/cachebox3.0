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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.actions.Action_Show_Quit;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.FileList;
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

/**
 * Created by Longri on 29.08.2016.
 */
public class SelectDB_Activity extends ActivityBase {
    final static Logger log = LoggerFactory.getLogger(SelectDB_Activity.class);

    public interface IReturnListener {
        public void back();
    }

    private final IReturnListener returnListener;
    private int autoStartTime = 10;
    private int autoStartCounter = 0;
    private String DBPath;
    private CharSequenceButton bNew;
    private CharSequenceButton bSelect;
    private CharSequenceButton bCancel;
    private CharSequenceButton bAutostart;
    private ListView lvFiles;
    private CustomAdapter lvAdapter;
    private String[] fileInfos;
    private boolean mustSelect = false;

    public SelectDB_Activity(IReturnListener returnListener, boolean mustSelect) {
        super("select DB dialog");
        this.returnListener = returnListener;

        this.mustSelect = mustSelect;

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

        lvFiles = new ListView();
        lvFiles.setSelectable(ListView.SelectableType.SINGLE);
        lvAdapter = new CustomAdapter(files);
        lvFiles.setAdapter(lvAdapter);
        this.addActor(lvFiles);

        if (selectedIndex > -1) lvFiles.setSelection(selectedIndex);

        bNew = new CharSequenceButton(Translation.get("NewDB"));
        bSelect = new CharSequenceButton(Translation.get("confirm"));
        bCancel = new CharSequenceButton(Translation.get("cancel"));
        bAutostart = new CharSequenceButton("");

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
        StageManager.registerForBackKey(cancelClickListener);

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
        readCountAtThread();
    }


    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            stopTimer();
            if (mustSelect) {
                new Action_Show_Quit().execute();
            } else {
                finish();
            }
        }
    };

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


    private boolean needsLayout = true;

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

        lvFiles.setBackground(null);
        lvFiles.setBounds(CB.scaledSizes.MARGIN, yPos, getWidth() - CB.scaledSizes.MARGINx2, getHeight() - (yPos + CB.scaledSizes.MARGIN));
        needsLayout = false;
    }

    @Override
    public void sizeChanged() {
        needsLayout = true;
        layout();
        lvFiles.dataSetChanged();
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
                    fileInfos[index] = cacheCount + " Caches  " + fileSize + "    last use " + lastModified;
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

        CB.postAsync(new Runnable() {
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
                        CB.postOnMainThread(new Runnable() {
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

    private class CustomAdapter implements Adapter {

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
        public ListViewItem getView(int listIndex) {
            SelectDBItem v = new SelectDBItem(listIndex, files.get(listIndex), fileInfos[listIndex], VisUI.getSkin().get("default", SelectDbStyle.class));
            v.pack();
            v.layout();
            return v;
        }

        @Override
        public void update(ListViewItem view) {
            SelectDBItem dbItem = (SelectDBItem) view;
            dbItem.updateFileInfo(fileInfos[view.getListIndex()]);
        }

        @Override
        public float getItemSize(int position) {
            return 0;
        }

    }

    private void stopTimer() {
        if (updateTimer != null)
            updateTimer.cancel();
    }


    Timer updateTimer;

    private void showSelectionMenu() {
        final CharSequence[] cs = new String[6];
        cs[0] = Translation.get("StartWithoutSelection");
        cs[1] = Translation.get("AutoStartDisabled");
        cs[2] = Translation.get("AutoStartTime", "5");
        cs[3] = Translation.get("AutoStartTime", "10");
        cs[4] = Translation.get("AutoStartTime", "25");
        cs[5] = Translation.get("AutoStartTime", "60");

        Menu cm = new Menu("MiscContextMenu");

        cm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {

                switch (item.getMenuItemId()) {
                    case MenuID.MI_START_WITHOUT_SELECTION:
                        autoStartTime = -1;
                        setAutoStartText();
                        break;
                    case MenuID.MI_AUTO_START_DISABLED:
                        autoStartTime = 0;
                        setAutoStartText();
                        break;
                    case MenuID.MI_5:
                        autoStartTime = 5;
                        setAutoStartText();
                        break;
                    case MenuID.MI_10:
                        autoStartTime = 10;
                        setAutoStartText();
                        break;
                    case MenuID.MI_25:
                        autoStartTime = 25;
                        setAutoStartText();
                        break;
                    case MenuID.MI_60:
                        autoStartTime = 60;
                        setAutoStartText();
                        break;
                }
                return true;
            }
        });

        cm.addItem(MenuID.MI_START_WITHOUT_SELECTION, cs[0], true);
        cm.addItem(MenuID.MI_AUTO_START_DISABLED, cs[1], true);
        cm.addItem(MenuID.MI_5, cs[2], true);
        cm.addItem(MenuID.MI_10, cs[3], true);
        cm.addItem(MenuID.MI_25, cs[4], true);
        cm.addItem(MenuID.MI_60, cs[5], true);

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
        StageManager.unRegisterForBackKey(cancelClickListener);
        super.dispose();
    }


    public static class SelectDbStyle {
        public BitmapFont nameFont, infoFont;
        public Color nameColor, infoColor;
    }


}
