/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.longri.cachebox3.develop.tools.skin_editor.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import de.longri.cachebox3.develop.tools.skin_editor.ColorPickerDialog;
import de.longri.cachebox3.develop.tools.skin_editor.FontPickerDialog;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.develop.tools.skin_editor.ValidateDialog;


/**
 * A table representing the menu bar at the top of the interface
 *
 * @author Yanick Bourbeau
 * @author Longri 2017
 */
public class MenuBar extends Table {

    private SkinEditorGame game;
    private Label labelProjectName;

    static private FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);

    static {
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
    }

    /**
     *
     */
    public MenuBar(final SkinEditorGame game) {

        super();

        this.game = game;

        left();
        setBackground(game.skin.getDrawable("default-pane"));

        TextButton buttonDrawables = new TextButton("Drawables", game.skin);
        add(buttonDrawables).pad(5);

        buttonDrawables.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                DrawablePickerDialog dlg = new DrawablePickerDialog(game, null,-1, false, getStage());
                dlg.show(game.screenMain.stage);

            }

        });
        add(buttonDrawables).pad(5);

        TextButton buttonFonts = new TextButton("Bitmap Fonts", game.skin);
        buttonFonts.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                FontPickerDialog dlg = new FontPickerDialog(game, null);
                dlg.show(game.screenMain.stage);
            }

        });
        add(buttonFonts).pad(5);

        TextButton buttonColors = new TextButton("Colors", game.skin);
        buttonColors.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                ColorPickerDialog dlg = new ColorPickerDialog(game, null);
                dlg.show(game.screenMain.stage);

            }

        });
        add(buttonColors).pad(5);


        TextButton buttonRefresh = new TextButton("Refresh Resources", game.skin);
        add(buttonRefresh).pad(5);

        buttonRefresh.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                game.screenMain.refreshResources();

            }

        });

        TextButton buttonExport = new TextButton("Export to Directory", game.skin);
        add(buttonExport).pad(5);

        buttonExport.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                showExportDialog();

            }

        });


        TextButton buttonClose = new TextButton("Close Project", game.skin);
        add(buttonClose).pad(5).expandX().left();

        buttonClose.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                game.screenMain.setCurrentProject("");
                game.setScreen(game.screenWelcome);

            }

        });

        CreateNightSkinButton buttonCreateNightSkin = new CreateNightSkinButton(game, game.skin);
        add(buttonCreateNightSkin).pad(5);

        TextButton buttonValidate = new TextButton("Validate Skin", game.skin);
        buttonValidate.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ValidateDialog dlg = new ValidateDialog(game, game.skinProject);
                dlg.show(game.screenMain.stage);
            }

        });
        add(buttonValidate).pad(5);


        Label labelCurrentProject = new Label("Current Project:", game.skin, "title");
        add(labelCurrentProject).pad(5).padRight(20);

        labelProjectName = new Label("---", game.skin);
        add(labelProjectName).pad(5).padRight(20);

    }


    /*
     * show export dialog
     */
    protected void showExportDialog() {

        final Preferences prefs = Gdx.app.getPreferences("skin_editor_project_" + game.screenMain.getcurrentProject());
        final TextField textDirectory = new TextField(prefs.getString("export_to_directory"), game.skin);

        Dialog dlg = new Dialog("Export to Directory", game.skin) {

            @Override
            protected void result(Object object) {

                if ((Boolean) object == true) {

                    if (textDirectory.getText().isEmpty() == true) {
                        game.showMsgDlg("Warning", "Directory field is empty!", game.screenMain.stage);
                        return;
                    }


                    FileHandle targetDirectory = new FileHandle(textDirectory.getText());
                    if (targetDirectory.exists() == false) {
                        game.showMsgDlg("Warning", "Directory not found!", game.screenMain.stage);
                        return;
                    }

                    // Copy uiskin.* and *.fnt

                    FileHandle projectFolder = Gdx.files.local("projects").child(game.screenMain.getcurrentProject());
                    for (FileHandle file : projectFolder.list()) {
                        if (file.name().startsWith("skin.")) {
                            Gdx.app.log("MenuBar", "Copying file: " + file.name() + " ...");
                            FileHandle target = targetDirectory.child(file.name());
                            file.copyTo(target);
                        } else if (file.name().startsWith("svg")) {
                            // create "svg" folder
                            FileHandle targetFolder = targetDirectory.child("svg");
                            targetFolder.mkdirs();
                            for (FileHandle svgFfile : file.list()) {
                                FileHandle target = targetFolder.child(svgFfile.name());
                                svgFfile.copyTo(target);
                                Gdx.app.log("MenuBar", "Copying file: " + svgFfile.name() + " ...");
                            }
                        } else if (file.name().startsWith("fonts")) {
                            // create "fonts" folder
                            FileHandle targetFolder = targetDirectory.child("fonts");
                            targetFolder.mkdirs();
                            for (FileHandle fontFfile : file.list()) {
                                FileHandle target = targetFolder.child(fontFfile.name());
                                fontFfile.copyTo(target);
                                Gdx.app.log("MenuBar", "Copying file: " + fontFfile.name() + " ...");
                            }
                        }
                    }
                    game.showMsgDlg("Operation Completed", "Project successfully exported!", game.screenMain.stage);
                }
            }
        };

        dlg.pad(20);

        Table table = dlg.getContentTable();
        table.padTop(20);
        table.add("Directory:");
        table.add(textDirectory).width(320);

        TextButton buttonChoose = new TextButton("...", game.skin);
        buttonChoose.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

//                // Need to steal focus first with this hack (Thanks to Z-Man)
//                Frame frame = new Frame();
//                frame.setUndecorated(true);
//                frame.setOpacity(0);
//                frame.setLocationRelativeTo(null);
//                frame.setVisible(true);
//                frame.toFront();
//                frame.setVisible(false);
//                frame.dispose();


//                JFileChooser chooser = new JFileChooser();
//                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//                int ret = chooser.showOpenDialog(null);
//                if (ret == JFileChooser.APPROVE_OPTION) {
//                    File f = chooser.getSelectedFile();
//                    textDirectory.setText(f.getAbsolutePath());
//
//                    // Store to file
//                    prefs.putString("export_to_directory", f.getAbsolutePath());
//                    prefs.flush();
//                }


                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> fileList) {
                        FileHandle selectedFolder = fileList.get(0);
                        textDirectory.setText(selectedFolder.file().getAbsolutePath());

                        // Store to file
                        prefs.putString("export_to_directory", selectedFolder.file().getAbsolutePath());
                        prefs.flush();
                    }
                });
                FileHandle directory = Gdx.files.absolute(prefs.getString("export_to_directory"));
                fileChooser.setDirectory(directory);
                //displaying chooser with fade in animation
                getStage().addActor(fileChooser.fadeIn());

            }

        });

        table.add(buttonChoose);

        table.row();
        table.padBottom(20);

        dlg.button("Export", true);
        dlg.button("Cancel", false);
        dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
        dlg.show(getStage());

    }


    /**
     * Update project name field
     */
    public void update(String projectName) {
        labelProjectName.setText(projectName);
    }

}
