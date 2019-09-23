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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.skin.styles.FileChooserStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.IconButton;
import de.longri.cachebox3.gui.widgets.ScrollLabel;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.regex.Matcher;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.SINGLE;

/**
 * Created by Longri on 20.02.2017.
 */
public class FileChooser extends ActivityBase {

    private final static Logger log = LoggerFactory.getLogger(FileChooser.class);
    private boolean actDirIsRoot;

    public enum Mode {
        OPEN, SAVE, BROWSE
    }

    public enum SelectionMode {
        FILES, DIRECTORIES, ALL
    }

    public interface SelectionReturnListner {
        void selected(FileHandle fileHandle);
    }

    FileFilter directoryFileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };

    FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };

    FileFilter extentionFileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) return true;

            String name = pathname.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex == -1) return false;
            String ext = name.substring(dotIndex + 1);

            for (String ex : fileExtentions) {
                if (ext.equals(ex)) return true;
            }
            return false;
        }
    };

    FileFilter browseFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };


    private CB_Button btnCancel;
    private IconButton btnAction;
    private FileHandle rootDir;
    private FileHandle actDir;
    private FileChooserStyle fileChooserStyle;
    private FileFilter actFilter = browseFilter;
    private final Array<FileHandle> actFileList = new Array<>();
    private String[] fileExtentions;
    private FileHandle selectedFile;
    private final SelectionMode selectionMode;
    private final Mode mode;
    private SelectionReturnListner selectionReturnListner;

    public FileChooser(CharSequence title, Mode mode, SelectionMode selectMode) {
        this(title, mode, selectMode, (String) null);
    }

    public FileChooser(CharSequence title, Mode mode, SelectionMode selectMode, String... extentions) {
        super("FileChooser", VisUI.getSkin().get("default", FileChooserStyle.class));
        this.mode = mode;
        fileChooserStyle = (FileChooserStyle) this.style;
        this.setStageBackground(style.background);
        createButtons();

        switch (selectMode) {
            case FILES:
                if (extentions == null || extentions.length == 0 || (extentions.length == 1 && extentions[0] == null)) {
                    this.actFilter = fileFilter;
                } else {
                    this.fileExtentions = extentions;
                    this.actFilter = extentionFileFilter;
                }
                break;
            case DIRECTORIES:
                this.actFilter = directoryFileFilter;
                break;
        }
        this.selectionMode = selectMode;

    }


    public void setDirectory(FileHandle directory) {
        this.setDirectory(directory, false);
    }

    public void setDirectory(FileHandle directory, boolean isRoot) {

        if (isRoot)
            rootDir = directory;

        // list all parents
        String absolutPath = directory.file().getAbsolutePath();
        String split = CB.fs;

        if (split.equals("\\")) {
            split = "/";
            absolutPath = absolutPath.replaceAll(Matcher.quoteReplacement("\\"), Matcher.quoteReplacement("/"));
        }
        String[] folder = absolutPath.split(split);

        String path = "";
        for (int i = 0, n = folder.length; i < n; i++) {
            if (folder[i] == null || folder[i].equals(".")) continue;
            path += folder[i] + "/";
        }
        setInternDirectory(Gdx.files.absolute(path), isRoot);
    }

    private void setInternDirectory(FileHandle directory, boolean isRoot) {
        this.selectedFile = null;
        this.actDir = directory;
        this.actDirIsRoot = isRoot;
        fillFileList(FileChooser.this.actDirIsRoot, false);
    }

    private void fillFileList(boolean actDirIsRoot, boolean reload) {
        actFileList.clear();
        for (FileHandle fileHandle : this.actDir.list(this.actFilter))
            actFileList.add(fileHandle);
        fillContent(actDirIsRoot, reload);
        checkButton(null);
    }

    private void checkButton(ListView listView) {
        if (selectionMode != SelectionMode.DIRECTORIES) {
            if (listView == null || listView.getSelectedItem() == null) {
                log.debug("chekButton switch off");
                btnAction.setDisabled(true);
            } else {
                log.debug("chekButton switch on");
                btnAction.setDisabled(false);
            }
        }
        CB.requestRendering();
    }

    private void createButtons() {

        if (this.mode == Mode.BROWSE) {
            btnAction = new IconButton(Translation.get("delete"), fileChooserStyle.deleteBtnIcon);
            btnAction.addListener(deleteClickListener);
        } else {
            btnAction = new IconButton(Translation.get("select"));
            btnAction.addListener(selectClickListener);
        }

        btnCancel = new CB_Button(Translation.get(this.mode == Mode.BROWSE ? "close" : "cancel"));

        this.addActor(btnAction);
        this.addActor(btnCancel);

        btnCancel.addListener(cancelClickListener);
        CB.stageManager.registerForBackKey(cancelClickListener);
    }

    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (selectionReturnListner != null) selectionReturnListner.selected(null);
            finish();
        }
    };

    private final ClickListener deleteClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            // ask for deletion selected files

            ListView actListView = (ListView) listViews.peek().getChildren().peek();

            Array<ListViewItemInterface> selectedItems = actListView.getSelectedItems();
            Array<FileHandle> delFiles = new Array<>();
            int delDirCount = 0;
            int delFileCount = 0;
            for (ListViewItemInterface itemInterface : selectedItems) {
                FileChooserItem item = (FileChooserItem) itemInterface;
                if (item.fileHandle.isDirectory()) delDirCount++;
                else delFileCount++;
                delFiles.add(item.fileHandle);
            }
            CharSequence msg;

            if ((delDirCount > 0 && delFileCount > 0) || delDirCount > 1 || delFileCount > 1) {
                msg = Translation.get("delObj", Integer.toString(delDirCount + delFileCount));
            } else if (delDirCount > 0) {
                msg = Translation.get("delDir");
            } else {
                msg = Translation.get("delFile");
            }
            MessageBox.show(msg, null, MessageBoxButtons.YesNo, MessageBoxIcon.Warning, (which, data) -> {
                if (which == ButtonDialog.BUTTON_POSITIVE)
                    CB.postAsync(new NamedRunnable("delete files") {
                        @Override
                        public void run() {
                            //delete Files
                            for (FileHandle delFile : delFiles) {
                                if (delFile.isDirectory())
                                    delFile.deleteDirectory();
                                else
                                    delFile.delete();
                            }
                            //after delete reload file list
                            fillFileList(FileChooser.this.actDirIsRoot, true);
                        }
                    });
                return true;
            });
        }
    };

    private final ClickListener selectClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (btnAction.isDisabled()) return;
            if (selectionReturnListner != null) {
                if (selectionMode == SelectionMode.FILES) {
                    selectionReturnListner.selected(selectedFile);
                } else {
                    selectionReturnListner.selected(actDir);
                }
            }
            finish();
        }
    };

    public void setSelectionReturnListener(SelectionReturnListner listener) {
        this.selectionReturnListner = listener;
    }

    @Override
    public void layout() {
        super.layout();

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);

        x -= CB.scaledSizes.MARGIN + btnAction.getWidth();

        btnAction.setPosition(x, y);
    }

    private Array<WidgetGroup> listViews = new Array<WidgetGroup>();
    private Array<String> listViewsNames = new Array<String>();
    Label.LabelStyle nameStyle;


    private void fillContent(boolean isRoot, boolean reload) {
        //set LabelStyles
        nameStyle = new Label.LabelStyle();
        nameStyle.font = fileChooserStyle.itemNameFont;
        nameStyle.fontColor = fileChooserStyle.itemNameFontColor;

        actFileList.sort(new Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle o1, FileHandle o2) {
                // directories first
                if (o1.isDirectory() && !o2.isDirectory()) return -1;
                if (!o1.isDirectory() && o2.isDirectory()) return 1;

                // in alphabetical order
                return o1.name().compareToIgnoreCase(o2.name());
            }
        });


        final FileListAdapter listViewAdapter = new FileListAdapter(actFileList) {

            IntMap<FileChooserItem> items = new IntMap<>();

            @Override
            public int getCount() {
                return fileList.size;
            }

            @Override
            public ListViewItem getView(int index) {
                if (items.containsKey(index))
                    return items.get(index);
                FileChooserItem item = getEntryItem(index);
                items.put(index, item);
                return item;
            }

            @Override
            public void update(ListViewItem view) {

            }

            private FileChooserItem getEntryItem(final int index) {

                final FileHandle file = fileList.get(index);
                FileChooserItem item = new FileChooserItem(index, file) {
                    @Override
                    public void dispose() {
                    }
                };

                ScrollLabel label = new ScrollLabel(file.name(), nameStyle);
                label.setAlignment(Align.left);

                float width = FileChooser.this.getWidth() - (fileChooserStyle.folderIcon.getMinWidth()
                        + CB.scaledSizes.MARGINx4 + CB.scaledSizes.MARGINx4);
                item.add(label).pad(CB.scaledSizes.MARGIN).width(new Value.Fixed(width));

                if (file.isDirectory()) {
                    // add folder icon
                    Image next = new Image(fileChooserStyle.folderIcon);
                    item.add(next).width(next.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

                    // add clicklistener
                    item.addListener(new ClickLongClickListener() {
                        public boolean clicked(InputEvent event, float x, float y) {
                            if (event.getType() == InputEvent.Type.touchUp) {
                                if (mode == Mode.BROWSE) {
                                    log.debug("click");
                                    //remove selection before switch to dir
                                    item.setSelected(false);
                                    Array<ListViewItemInterface> selectedItems = listView.getSelectedItems();
                                    if (selectedItems != null) {
                                        for (ListViewItemInterface item : selectedItems) {
                                            item.setSelected(false);
                                        }
                                        selectedItems.clear();
                                    }
                                }
                                setInternDirectory(file, false);
                            }
                            return true;
                        }

                        @Override
                        public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                            log.debug("longClick on Actor: {}", actor.toString());
                            //select and not browse
                            if (mode == Mode.BROWSE) {
                                if (item.isSelected()) {
                                    item.setSelected(false);
                                    Array<ListViewItemInterface> selectedItems = listView.getSelectedItems();
                                    if (selectedItems != null) {
                                        selectedItems.removeValue(item, true);
                                    }
                                } else {
                                    item.setSelected(true);
                                    Array<ListViewItemInterface> selectedItems = listView.getSelectedItems();
                                    if (selectedItems != null) {
                                        selectedItems.add(item);
                                    } else {
                                        listView.setSelection(item.getListIndex());
                                    }
                                }
                            }
                            return true;
                        }

                        public boolean handle(Event e) {
                            boolean ret = super.handle(e);
                            if (!(e instanceof InputEvent)) return ret;
                            InputEvent event = (InputEvent) e;
                            if (event.getType() == InputEvent.Type.touchUp) {
                                log.debug("touchUp");
                                CB.postAsyncDelayd(200, new NamedRunnable("") {
                                    @Override
                                    public void run() {
                                        checkButton(listView);
                                    }
                                });
                            }
                            return ret;
                        }


                    });
                    return item;
                } else if (file.extension().equals("map")) {


                    // add map file icon
                    Image next = new Image(fileChooserStyle.mapFileIcon);
                    item.add(next).width(next.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

                    // add clicklistener
                    item.addListener(new ClickListener() {
                        public void clicked(InputEvent event, float x, float y) {
                            if (event.getType() == InputEvent.Type.touchUp) {
                                selectedFile = file;
                                select(listView, index);
                            }
                        }
                    });
                    return item;
                } else {


                    // add file icon
                    Image next = new Image(fileChooserStyle.fileIcon);
                    item.add(next).width(next.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

                    // add clicklistener
                    item.addListener(new ClickListener() {
                        public void clicked(InputEvent event, float x, float y) {
                            if (event.getType() == InputEvent.Type.touchUp & !event.isCancelled()) {
                                selectedFile = file;
                                select(listView, index);
                                event.cancel();
                            }
                        }
                    });
                    return item;
                }
            }

        };
        final ListView listView = new ListView(VERTICAL);

        listViewAdapter.listView = listView;

        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(listViewAdapter);
                showListView(listView, FileChooser.this.actDir.name(), true, isRoot, reload);
            }
        });


    }

    private void select(ListView listView, int index) {
        if (selectionMode == SelectionMode.ALL) {
            ListViewItemInterface item = listView.getListItem(index);
            if (item.isSelected()) {
                listView.getSelectedItems().removeValue(item, true);
                item.setSelected(false);
                CB.requestRendering();
            } else {
                item.setSelected(true);
                Array<ListViewItemInterface> selectedItems = listView.getSelectedItems();
                if (selectedItems == null || selectedItems.size == 0) {
                    listView.setSelection(index);
                } else {
                    selectedItems.add(item);
                }
            }
        } else {
            listView.setSelectable(SINGLE);
            listView.setSelection(index);
        }
        this.checkButton(listView);
    }

    private void showListView(ListView listView, String name, boolean animate, boolean isRoot, boolean reload) {

        float y = btnAction.getY() + btnAction.getHeight() + CB.scaledSizes.MARGIN;

        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.setBounds(CB.scaledSizes.MARGIN, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGIN));

        // title
        WidgetGroup titleGroup = new WidgetGroup();

        float topY = widgetGroup.getHeight() - CB.scaledSizes.MARGIN_HALF;
        float xPos = 0;

        ClickListener backClickListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                backClick();
            }
        };

        // add the titleLabel on top
        if (fileChooserStyle.backIcon != null && listViewsNames.size > 0) {
            Image backImage = new Image(fileChooserStyle.backIcon);
            backImage.setPosition(xPos, 0);
            xPos += backImage.getWidth() + CB.scaledSizes.MARGIN;
            titleGroup.addActor(backImage);
        }


        Label.LabelStyle nameStyle = new Label.LabelStyle();
        nameStyle.font = fileChooserStyle.itemNameFont;
        nameStyle.fontColor = fileChooserStyle.itemNameFontColor;

        VisLabel titleLabel = new VisLabel(name, nameStyle);

        if (listViewsNames.size > 0) {
            Label.LabelStyle parentStyle = new Label.LabelStyle();
            parentStyle.font = fileChooserStyle.backItemNameFont;
            parentStyle.fontColor = fileChooserStyle.backItemNameFontColor;

            VisLabel parentTitleLabel = new VisLabel(listViewsNames.get(listViewsNames.size - 1), parentStyle);
            parentTitleLabel.setPosition(xPos, 0);

            if (parentTitleLabel.getWidth() + CB.scaledSizes.MARGINx2 > (Gdx.graphics.getWidth() - titleLabel.getWidth()) / 2) {
                //center titleLabel
                xPos = parentTitleLabel.getWidth() + CB.scaledSizes.MARGINx2 * 2;
            } else {
                //center titleLabel
                xPos = (Gdx.graphics.getWidth() - titleLabel.getWidth()) / 2;
            }
            titleGroup.addActor(parentTitleLabel);
        } else {
            //center titleLabel
            xPos = (Gdx.graphics.getWidth() - titleLabel.getWidth()) / 2;
        }

        titleLabel.setPosition(xPos, 0);
        titleGroup.addActor(titleLabel);

        float titleHeight = titleLabel.getHeight() + CB.scaledSizes.MARGIN;
        titleGroup.setBounds(0, Gdx.graphics.getHeight() - (y + titleHeight), Gdx.graphics.getWidth(), titleHeight);
        if (!isRoot) titleGroup.addListener(backClickListener);
        widgetGroup.addActor(titleGroup);

        listView.setBounds(0, 0, widgetGroup.getWidth(), titleGroup.getY() - CB.scaledSizes.MARGIN);
        listView.layout();
        listView.setBackground(null); // remove default background

        //set selection mode
        if (this.selectionMode == SelectionMode.ALL)
            listView.setSelectable(SelectableType.MULTI);


        widgetGroup.addActor(listView);

        if (listViews.size > 0) {
            // animate
            float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;
            if (animate && !reload) {
                listViews.get(listViews.size - 1).addAction(Actions.moveTo(0 - nextXPos, y, Menu.MORE_MENU_ANIMATION_TIME));
                widgetGroup.setPosition(nextXPos, y);
                widgetGroup.addAction(Actions.moveTo(CB.scaledSizes.MARGIN, y, Menu.MORE_MENU_ANIMATION_TIME));
            } else {
                widgetGroup.setPosition(CB.scaledSizes.MARGIN, y);
            }
        }
        widgetGroup.addActor(listView);

        if (listViews.size > 0) {
            // animate
            float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;
            if (animate) {
                listViews.get(listViews.size - 1).addAction(Actions.moveTo(0 - nextXPos, y, Menu.MORE_MENU_ANIMATION_TIME));
                widgetGroup.setPosition(nextXPos, y);
                widgetGroup.addAction(Actions.moveTo(CB.scaledSizes.MARGIN, y, Menu.MORE_MENU_ANIMATION_TIME));
            } else {
                widgetGroup.setPosition(CB.scaledSizes.MARGIN, y);
            }
        }
        listViews.add(widgetGroup);
        listViewsNames.add(name);
        this.addActor(widgetGroup);
    }

    private void backClick() {
        float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;

        if (listViews.size == 0) return;

        listViewsNames.pop();
        WidgetGroup actWidgetGroup = listViews.pop();
        WidgetGroup showingWidgetGroup = listViews.get(listViews.size - 1);

        float y = actWidgetGroup.getY();
        actWidgetGroup.addAction(Actions.sequence(Actions.moveTo(nextXPos, y, Menu.MORE_MENU_ANIMATION_TIME), Actions.removeActor()));
        showingWidgetGroup.addAction(Actions.moveTo(CB.scaledSizes.MARGIN, y, Menu.MORE_MENU_ANIMATION_TIME));
    }

    private abstract class FileListAdapter implements ListViewAdapter {
        protected final Array<FileHandle> fileList;

        protected FileListAdapter(Array<FileHandle> fileList) {
            this.fileList = fileList;
        }

        ListView listView;

    }

    @Override
    public void dispose() {
        super.dispose();
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
    }

    static class FileChooserItem extends ListViewItem {

        final FileHandle fileHandle;

        public FileChooserItem(int index, FileHandle fileHandle) {
            super(index);
            this.fileHandle = fileHandle;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
        }
    }
}


