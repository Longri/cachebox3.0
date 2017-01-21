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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.Field;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import de.longri.cachebox3.develop.tools.skin_editor.NinePatchEditorDialog;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.develop.tools.skin_editor.SvgFileIconProvider;
import org.oscim.backend.canvas.Bitmap;

import java.awt.*;
import java.util.*;

/**
 * Display a dialog allowing to pick a drawable resource such as a ninepatch
 * or a texture region. You can also add resource from file (PNG only for now)
 *
 * @author Yanick Bourbeau
 */
public class DrawablePickerDialog extends Dialog {

    private Table topMenuTable;
    private SkinEditorGame game;
    private Field field;
    private Table tableDrawables;
    private boolean zoom = false;
    private HashMap<String, Object> items = new HashMap<String, Object>();
    private ScrollPane scrollPane;
    static private FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
    static private SvgFileIconProvider svgFileIconProvider;

    TextButton togglShowNinePatch;
    TextButton togglShowDrawable;
    TextField filterField;
    private final boolean disableNinePatch;


    static {
        FileTypeFilter typeFilter = new FileTypeFilter(true); //allow "All Types" mode where all files are shown
        typeFilter.addRule("SVG files (*.svg)", "svg");
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(typeFilter);
        svgFileIconProvider = new SvgFileIconProvider(fileChooser);
        fileChooser.setIconProvider(svgFileIconProvider);
    }

    public DrawablePickerDialog(final SkinEditorGame game, final Field field, boolean disableNinePatch) {

        super("Drawable Picker", game.skin);

        this.game = game;
        this.field = field;
        this.disableNinePatch = disableNinePatch;

        initializeSelf();

    }

    private void initializeSelf() {
        this.clear();
        defaults().space(6);
        add(topMenuTable = new Table(game.skin)).fillX();
        row();
        add(contentTable = new Table(game.skin)).expand().fill();
        row();
        add(buttonTable = new Table(game.skin)).fillX();

        topMenuTable.defaults().space(6);
        contentTable.defaults().space(6);
        buttonTable.defaults().space(6);

        buttonTable.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
                if (!cancelHide) hide();
                cancelHide = false;
            }
        });


        tableDrawables = new Table(game.skin);
        scrollPane = new ScrollPane(tableDrawables, game.skin);

        contentTable.add(scrollPane);
        scrollPane.setFlickScroll(false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(true);

        TextButton buttonNewNinePatch = new TextButton("Create NinePatch", game.skin);
        buttonNewNinePatch.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                NinePatchEditorDialog dlg = new NinePatchEditorDialog(game) {
                    @Override
                    public void hide() {
                        super.hide();

                        updateTable();
                    }
                };

                dlg.show(game.screenMain.stage);
            }

        });

        TextButton buttonNewDrawable = new TextButton("Import Image", game.skin);
        buttonNewDrawable.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                final Preferences prefs = Gdx.app.getPreferences("skin_editor_project_" + game.screenMain.getcurrentProject());

                // Need to steal focus first with this hack (Thanks to Z-Man)
                Frame frame = new Frame();
                frame.setUndecorated(true);
                frame.setOpacity(0);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.toFront();
                frame.setVisible(false);
                frame.dispose();


                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> fileList) {
                        if (fileList.size < 1) return;
                        final FileHandle selectedFile = fileList.get(0);

                        if (selectedFile == null) {
                            return;
                        }

                        prefs.putString("last_import_directory", selectedFile.parent().path());

                        // ask for name of generated recource
                        String selectedFileName = selectedFile.name().substring(0, selectedFile.name().lastIndexOf("."));
                        final TextField nameTextField = new TextField(selectedFileName, game.skin);
                        Dialog dlg0 = new Dialog("Set resource name", game.skin) {

                            @Override
                            protected void result(Object object) {
                                if ((Boolean) object == false) {
                                    return;
                                }

                                final String finalResourceName = nameTextField.getText();
                                final TextField scaleValueTextField = new TextField(String.valueOf(1.0f), game.skin);
                                Dialog dlg = new Dialog("Set Scale Value", game.skin) {

                                    @Override
                                    protected void result(Object object) {
                                        if ((Boolean) object == false) {
                                            return;
                                        }

                                        float scalfactor = 0;
                                        String text = scaleValueTextField.getText();
                                        if (text.isEmpty() == false) {
                                            scalfactor = Float.valueOf(text);
                                        }

                                        // Copy the file
                                        FileHandle orig = selectedFile;
                                        String originalName = orig.name();
                                        FileHandle dest = new FileHandle("projects/" + game.screenMain.getcurrentProject() + "/svg/" + originalName);
                                        orig.copyTo(dest);

                                        // write scaled svg section
                                        ScaledSvg scaledSvg = new ScaledSvg();
                                        scaledSvg.path = "svg/" + originalName;
                                        scaledSvg.scale = scalfactor;
                                        scaledSvg.setRegisterName(finalResourceName);
                                        game.skinProject.add(finalResourceName, scaledSvg);

                                        FileHandle projectFolder = new FileHandle("projects/" + game.screenMain.getcurrentProject());
                                        FileHandle projectFile = projectFolder.child("skin.json");
                                        game.skinProject.save(projectFile);

                                        game.screenMain.refreshResources();
                                        refresh();
                                        game.showMsgDlg("File successfully added to your project.", getStage());
                                    }
                                };

                                dlg.pad(20);
                                dlg.getContentTable().add("Float Value:");
                                dlg.getContentTable().add(scaleValueTextField).pad(20);
                                dlg.button("OK", true);
                                dlg.button("Cancel", false);
                                dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
                                dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
                                dlg.show(getStage());
                                getStage().setKeyboardFocus(scaleValueTextField);
                            }
                        };

                        dlg0.pad(20);
                        dlg0.getContentTable().add("Resource name:");
                        dlg0.getContentTable().add(nameTextField).pad(20);
                        dlg0.button("OK", true);
                        dlg0.button("Cancel", false);
                        dlg0.key(com.badlogic.gdx.Input.Keys.ENTER, true);
                        dlg0.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
                        dlg0.show(getStage());
                        getStage().setKeyboardFocus(nameTextField);
                    }
                });

                fileChooser.setDirectory(prefs.getString("last_import_directory"));

                fileChooser.setSize(game.screenMain.stage.getWidth() * 0.9f,
                        game.screenMain.stage.getHeight() * 0.9f);

                //displaying chooser with fade in animation
                getStage().addActor(fileChooser.fadeIn());

            }
        });

        TextButton buttonZoom = new TextButton("Toggle Zoom", game.skin);
        buttonZoom.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                zoom = !zoom;
                updateTable();

            }

        });

        TextButton buttonNoDrawable = new TextButton("Empty Drawable", game.skin);

        buttonNoDrawable.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                try {
                    field.set(game.screenMain.paneOptions.currentStyle, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                game.screenMain.saveToSkin();

                hide();
                game.screenMain.panePreview.refresh();
                game.screenMain.paneOptions.updateSelectedTableFields();

            }

        });

        ChangeListener refreshListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refresh();
            }
        };

        togglShowNinePatch = new TextButton("Show NinePatch", game.skin, "toggle");
        togglShowDrawable = new TextButton("Show Drawable", game.skin, "toggle");
        filterField = new TextField("", game.skin);

        togglShowNinePatch.addListener(refreshListener);
        togglShowDrawable.addListener(refreshListener);
        filterField.addListener(refreshListener);


        togglShowNinePatch.toggle();
        togglShowDrawable.toggle();

        if (disableNinePatch) {
            togglShowNinePatch.setDisabled(true);
        }

        topMenuTable.add(togglShowNinePatch);
        topMenuTable.add(togglShowDrawable);
        topMenuTable.add(filterField);

        contentTable.add(scrollPane).width(getPrefWidth()).height(getPrefHeight() * 0.7f).pad(20);

        buttonTable.add(buttonNewNinePatch);
        buttonTable.add(buttonNewDrawable);
        buttonTable.add(buttonZoom);
        if (field != null) {
            buttonTable.add(buttonNoDrawable);
        }
        buttonTable.padBottom(15);
        button("Cancel", true);
        key(com.badlogic.gdx.Input.Keys.ESCAPE, false);

        this.layout();
    }

    @Override
    public Dialog show(Stage stage) {
        refresh();
        Dialog d = super.show(stage);
        getStage().setScrollFocus(scrollPane);
        return d;
    }

    private void refresh() {

        ObjectMap<String, Drawable> itemsDrawables = game.skinProject.getAll(Drawable.class);
        ObjectMap<String, TextureRegion> itemsRegions = game.skinProject.getAll(TextureRegion.class);

        items.clear();

        boolean showDrawables = togglShowDrawable.isChecked();
        boolean show9Patch = togglShowNinePatch.isChecked() && !disableNinePatch;

        Iterator<String> it = itemsDrawables.keys().iterator();
        while (it.hasNext()) {
            String key = it.next();

            // key filter
            String filter = filterField.getText();
            if (!filter.isEmpty()) {
                if (!key.toLowerCase().contains(filter.toLowerCase())) {
                    continue;
                }
            }


            Drawable drawable = itemsDrawables.get(key);
            if (show9Patch && drawable instanceof SvgNinePatchDrawable)
                items.put(key, drawable);
            else if (showDrawables && !(drawable instanceof SvgNinePatchDrawable)) {
                items.put(key, drawable);
            }


        }

        if(showDrawables){
            it = itemsRegions.keys().iterator();
            while (it.hasNext()) {
                String key = it.next();

                // key filter
                String filter = filterField.getText();
                if (!filter.isEmpty()) {
                    if (!key.toLowerCase().contains(filter.toLowerCase())) {
                        continue;
                    }
                }

                if (items.containsKey(key))
                    continue;

                items.put(key, itemsRegions.get(key));
            }
        }



        updateTable();

    }

    /**
     *
     */
    public void updateTable() {

        tableDrawables.clear();


        // Sorted Map......By Key
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        Map<String, Object> treeMap = new TreeMap<String, Object>(comparator);
        treeMap.putAll(items);


        Iterator<String> keys = treeMap.keySet().iterator();
        int count = 0;

        while (keys.hasNext()) {

            final String key = keys.next();
            if (key.startsWith("widgets/")) {
                continue;
            }

            Button buttonItem = new Button(game.skin);

            Image img = null;
            Object item = items.get(key);
            if (item instanceof Drawable) {
                img = new Image((Drawable) item);
            } else {
                img = new Image((TextureRegion) item);

            }

            if (zoom == true || item instanceof SvgNinePatchDrawable) {
                buttonItem.add(img).expand().fill().pad(5);
            } else {
                buttonItem.add(img).expand().pad(5);
            }

            buttonItem.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeEvent event, Actor actor) {

                    if (field == null) {
                        return;
                    }

                    try {
                        // Since we have reloaded everything we have to get
                        // field back

                        game.screenMain.paneOptions.refreshSelection();
                        if (items.get(key) instanceof Drawable) {
                            if (field.getType() == Bitmap.class) {
                                Bitmap bmp = game.skinProject.get(key, Bitmap.class);
                                field.set(game.screenMain.paneOptions.currentStyle, bmp);
                            } else {
                                field.set(game.screenMain.paneOptions.currentStyle, items.get(key));
                            }
                        } else {

                            boolean ninepatch = false;
                            FileHandle test = new FileHandle("projects/" + game.screenMain.getcurrentProject() + "/assets/" + key + ".9.png");
                            if (test.exists() == true) {
                                ninepatch = true;
                            }

                            if (ninepatch == true) {
                                game.skinProject.add(key, new NinePatchDrawable(new NinePatch((TextureRegion) items.get(key))));
                                field.set(game.screenMain.paneOptions.currentStyle, game.skinProject.getDrawable(key));

                            } else {

                                if (field.getType() == Bitmap.class) {
                                    Bitmap bmp = game.skinProject.get(key, Bitmap.class);
                                    field.set(game.screenMain.paneOptions.currentStyle, bmp);
                                } else {
                                    game.skinProject.add(key, new SpriteDrawable(new Sprite((TextureRegion) items.get(key))));
                                    field.set(game.screenMain.paneOptions.currentStyle, game.skinProject.getDrawable(key));
                                }
                            }
                        }

                        game.screenMain.saveToSkin();
                        hide();
                        game.screenMain.panePreview.refresh();
                        game.screenMain.paneOptions.updateSelectedTableFields();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });

            String objectType = items.get(key).getClass().getSimpleName();
            objectType = objectType.replace("Drawable", "");

            buttonItem.row();
            buttonItem.add(new Label(key, game.skin));
            buttonItem.row();
            buttonItem.add(new Label(objectType, game.skin, "title"));
            buttonItem.row();
            buttonItem.setClip(true);
            tableDrawables.add(buttonItem).width(160).height(184).pad(5);

            if (count == 4) {
                count = 0;
                tableDrawables.row();
                continue;
            }

            count++;
        }

    }

    public float getPrefWidth() {
        return game.screenMain.stage.getWidth() * 0.8f;
    }

    public float getPrefHeight() {
        return game.screenMain.stage.getHeight() * 0.9f;
    }


}
