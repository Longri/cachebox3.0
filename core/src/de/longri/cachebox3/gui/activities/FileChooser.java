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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.skin.styles.FileChooserStyle;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.lists.CB_List;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;

/**
 * Created by Longri on 20.02.2017.
 */
public class FileChooser extends ActivityBase {

    public enum Mode {
        OPEN, SAVE
    }

    public enum SelectionMode {
        FILES, DIRECTORIES, FILES_AND_DIRECTORIES
    }

    private VisTextButton btnOk, btnCancel;
    private FileHandle actDir;
    private FileChooserStyle fileChooserStyle;
    private FileFilter actFilter;
    private FileHandle[] actFileList;


    public FileChooser(String title, Mode mode, SelectionMode selectMode) {
        super("FileChooser", VisUI.getSkin().get("default", FileChooserStyle.class));
        fileChooserStyle = (FileChooserStyle) this.style;
        this.setStageBackground(style.background);
        createButtons();

        this.actFilter = directoryFileFilter;
    }

    FileFilter directoryFileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };


    public void setDirectory(FileHandle directory) {
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
            if (folder[i].equals(".")) continue;
            path += folder[i] + "/";
            setInternDirectory(Gdx.files.absolute(path));
        }
    }

    private void setInternDirectory(FileHandle directory) {
        this.actDir = directory;
        actFileList = this.actDir.list(this.actFilter);
        fillContent();
    }

    private void createButtons() {

        btnOk = new VisTextButton(Translation.Get("select"));
        btnCancel = new VisTextButton(Translation.Get("cancel"));

        this.addActor(btnOk);
        this.addActor(btnCancel);

        btnOk.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                finish();
            }
        });

        btnCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                finish();
            }
        });
    }

    @Override
    public void layout() {
        super.layout();

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);

        x -= CB.scaledSizes.MARGIN + btnOk.getWidth();

        btnOk.setPosition(x, y);
    }

    private Array<WidgetGroup> listViews = new Array<WidgetGroup>();
    private Array<String> listViewsNames = new Array<String>();
    Label.LabelStyle nameStyle;


    private void fillContent() {
        //set LabelStyles
        nameStyle = new Label.LabelStyle();
        nameStyle.font = fileChooserStyle.itemNameFont;
        nameStyle.fontColor = fileChooserStyle.itemNameFontColor;

        FileListAdapter listViewAdapter = new FileListAdapter(actFileList) {

            @Override
            public int getCount() {
                return fileList.length;
            }

            @Override
            public ListViewItem getView(int index) {
                return getEntryItem(index);
            }

            @Override
            public void update(ListViewItem view) {

            }

            @Override
            public float getItemSize(int position) {
                return 0;
            }

            private ListViewItem getEntryItem(int index) {

                final FileHandle file = fileList[index];
                if (file.isDirectory()) {
                    ListViewItem table = new ListViewItem(index) {
                        @Override
                        public void dispose() {
                        }
                    };

                    // add label with category name, align left
                    table.left();
                    VisLabel label = new VisLabel(file.name());
                    label.setAlignment(Align.left);
                    table.add(label).pad(CB.scaledSizes.MARGIN).expandX().fillX();

                    // add next icon
                    Image next = new Image(fileChooserStyle.folderIcon);
                    table.add(next).width(next.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

                    // add clicklistener
                    table.addListener(new ClickListener() {
                        public void clicked(InputEvent event, float x, float y) {
                            if (event.getType() == InputEvent.Type.touchUp) {
                                setInternDirectory(file);
                            }
                        }
                    });
                    return table;
                }
                return null;
            }
        };
        showListView(new ListView(listViewAdapter, true), this.actDir.name(), true);
    }


    private void showListView(ListView listView, String name, boolean animate) {

        float y = btnOk.getY() + btnOk.getHeight() + CB.scaledSizes.MARGIN;

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
            xPos += parentTitleLabel.getWidth() + CB.scaledSizes.MARGINx2 * 4;
            titleGroup.addActor(parentTitleLabel);
        } else {
            //center titleLabel
            xPos = (Gdx.graphics.getWidth() - titleLabel.getWidth()) / 2;
        }

        titleLabel.setPosition(xPos, 0);
        titleGroup.addActor(titleLabel);

        float titleHeight = titleLabel.getHeight() + CB.scaledSizes.MARGIN;
        titleGroup.setBounds(0, Gdx.graphics.getHeight() - (y + titleHeight), Gdx.graphics.getWidth(), titleHeight);
        titleGroup.addListener(backClickListener);
        widgetGroup.addActor(titleGroup);

        listView.setBounds(0, 0, widgetGroup.getWidth(), titleGroup.getY() - CB.scaledSizes.MARGIN);
        listView.layout();
        listView.setBackground(null); // remove default background
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

        if (listViews.size == 1) return;

        listViewsNames.pop();
        WidgetGroup actWidgetGroup = listViews.pop();
        WidgetGroup showingWidgetGroup = listViews.get(listViews.size - 1);

        float y = actWidgetGroup.getY();
        actWidgetGroup.addAction(Actions.sequence(Actions.moveTo(nextXPos, y, Menu.MORE_MENU_ANIMATION_TIME), Actions.removeActor()));
        showingWidgetGroup.addAction(Actions.moveTo(CB.scaledSizes.MARGIN, y, Menu.MORE_MENU_ANIMATION_TIME));
    }

    private abstract class FileListAdapter implements Adapter {
        protected final FileHandle[] fileList;

        protected FileListAdapter(FileHandle[] fileList) {
            this.fileList = fileList;
        }
    }
}
