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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.Field;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import com.mobidevelop.maps.editor.ui.utils.Tooltips;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.NinePatchEditorDialog;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.develop.tools.skin_editor.SvgFileIconProvider;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.utils.SkinColor;
import org.oscim.backend.canvas.Bitmap;

import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * Display a dialog allowing to pick a drawable resource such as a ninepatch
 * or a texture region. You can also add resource from file (PNG only for now)
 *
 * @author Yanick Bourbeau
 */
public class DrawablePickerDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(CB.class);

    private Table topMenuTable;
    private SkinEditorGame game;
    private Field field;
    private Table tableDrawables;
    private boolean zoom = false;
    private HashMap<String, InternalItem> items = new HashMap<String, InternalItem>();
    private ScrollPane scrollPane;
    static private FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
    static private SvgFileIconProvider svgFileIconProvider;

    TextButton togglShowNinePatch;
    TextButton togglShowDrawable;
    TextField filterField;
    private final boolean disableNinePatch;
    private final Stage stage;

    private static class InternalItem {
        private Drawable drawable;
        private Object skinInfo;

        public InternalItem(Object item, Drawable drawable) {
            this.drawable = drawable;
            this.skinInfo = item;
        }
    }


    static {
        FileTypeFilter typeFilter = new FileTypeFilter(true); //allow "All Types" mode where all files are shown
        typeFilter.addRule("SVG files (*.svg)", "svg");
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(typeFilter);
        svgFileIconProvider = new SvgFileIconProvider(fileChooser);
        fileChooser.setIconProvider(svgFileIconProvider);
    }

    public DrawablePickerDialog(final SkinEditorGame game, final Field field, boolean disableNinePatch, Stage stage) {

        super("Drawable Picker", game.skin);

        this.game = game;
        this.field = field;
        this.disableNinePatch = disableNinePatch;
        this.stage = stage;

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


        ObjectMap<String, ScaledSvg> svgItems = game.skinProject.getAll(ScaledSvg.class);
        ObjectMap<String, SvgNinePatchDrawable> svg9PatchItems = game.skinProject.getAll(SvgNinePatchDrawable.class);


//        ObjectMap<String, Drawable> itemsDrawables = game.skinProject.getAll(Drawable.class);
//        ObjectMap<String, TextureRegion> itemsRegions = game.skinProject.getAll(TextureRegion.class);

        items.clear();

        boolean showDrawables = togglShowDrawable.isChecked();
        boolean show9Patch = togglShowNinePatch.isChecked() && !disableNinePatch;


        if (showDrawables) {
            Iterator<String> it = svgItems.keys().iterator();
            while (it.hasNext()) {
                String key = it.next();

                // key filter
                String filter = filterField.getText();
                if (!filter.isEmpty()) {
                    if (!key.toLowerCase().contains(filter.toLowerCase())) {
                        continue;
                    }
                }

                ScaledSvg svgItem = svgItems.get(key);
                items.put(key, new InternalItem(svgItem, game.skinProject.get(key, Drawable.class)));

            }
        }


        if (show9Patch) {
            Iterator<String> it = svg9PatchItems.keys().iterator();
            while (it.hasNext()) {
                String key = it.next();

                // key filter
                String filter = filterField.getText();
                if (!filter.isEmpty()) {
                    if (!key.toLowerCase().contains(filter.toLowerCase())) {
                        continue;
                    }
                }


                SvgNinePatchDrawable ninePatchDrawable = svg9PatchItems.get(key);
                items.put(key, new InternalItem(ninePatchDrawable.values, ninePatchDrawable));
            }
        }


        updateTable();

    }

    /**
     *
     */
    public void updateTable() {

        tableDrawables.clear();

        Tooltips.TooltipStyle styleTooltip = new Tooltips.TooltipStyle(game.skin.getFont("default-font"),
                game.skin.getDrawable("default-round"),
                game.skin.get("white", SkinColor.class));


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
            InternalItem item = items.get(key);

            img = new Image((Drawable) item.drawable);


            if (zoom == true || item.drawable instanceof SvgNinePatchDrawable) {
                buttonItem.add(img).expand().fill().pad(5);
            } else {
                buttonItem.add(img).expand().pad(5);
            }

            buttonItem.addListener(new ChangeListener() {


                private Event lastHandeldEvent;

                public boolean handle(Event event) {
                    if (!(event instanceof ChangeEvent)) return false;

                    if (event == lastHandeldEvent) {
                        lastHandeldEvent = null;
                        return true;
                    }

                    changed((ChangeEvent) event, event.getTarget());
                    if (event.isHandled()) {
                        lastHandeldEvent = event;
                        return true;
                    }
                    lastHandeldEvent = null;
                    return false;
                }

                @Override
                public void changed(ChangeEvent event, final Actor actor) {

                    if (actor instanceof TextButton) {
                        //handle click on info Label

                        if (((InternalItem) actor.getUserObject()).drawable instanceof SvgNinePatchDrawable) {

                        } else {

                            String lastScaleValue = Float.toString(((ScaledSvg) ((InternalItem) actor.getUserObject()).skinInfo).scale);

                            // show change Scale Value dialog
                            final TextField scaleValueTextField = new TextField(lastScaleValue, game.skin);
                            Dialog dlg = new Dialog("Set new Scale Value", game.skin) {

                                @Override
                                protected void result(Object object) {
                                    if ((Boolean) object == false) {
                                        return;
                                    }

                                    float newScalfactor = 0;
                                    String text = scaleValueTextField.getText();
                                    if (text.isEmpty() == false) {
                                        newScalfactor = Float.valueOf(text);
                                    }


                                    // overwrite scaled svg section
                                    ScaledSvg scaledSvg = ((ScaledSvg) ((InternalItem) actor.getUserObject()).skinInfo);

                                    scaledSvg.scale = newScalfactor;
                                    scaledSvg.setRegisterName(key);
                                    game.skinProject.add(key, scaledSvg);

                                    FileHandle projectFolder = new FileHandle("projects/" + game.screenMain.getcurrentProject());
                                    FileHandle projectFile = projectFolder.child("skin.json");
                                    game.skinProject.save(projectFile);

                                    game.screenMain.refreshResources();
                                    refresh();
                                    game.showMsgDlg("Scalevalue successfully changed.", getStage());
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


                        event.cancel();  //todo break event
                        return;
                    }


                    if (field == null || event.isCancelled()) {
                        return;
                    }

                    try {
                        // Since we have reloaded everything we have to get
                        // field back
                        game.screenMain.paneOptions.refreshSelection();

                        if (field.getType() == Bitmap.class) {
                            Bitmap bmp = game.skinProject.get(key, Bitmap.class);
                            field.set(game.screenMain.paneOptions.currentStyle, bmp);
                        } else {
                            field.set(game.screenMain.paneOptions.currentStyle, items.get(key).drawable);
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


            InternalItem itemObject = items.get(key);
            boolean isNinePatch = itemObject.drawable instanceof SvgNinePatchDrawable;


            String info = " info line ";

            if (isNinePatch) {

            } else {
                info = "scale: " + Float.toString(((ScaledSvg) itemObject.skinInfo).scale) +
                        "\n  w: " + itemObject.drawable.getMinWidth() +
                        " /  h: " + itemObject.drawable.getMinHeight();
            }


            TextButton infoLabel = new TextButton(info, game.skin);
            infoLabel.setUserObject(itemObject);

            Tooltips tooltip = new Tooltips(styleTooltip, stage);
            tooltip.registerTooltip(infoLabel,
                    isNinePatch ? "Change NinePatch Values " : "Change Scale Values");


            buttonItem.setTouchable(Touchable.childrenOnly);


            String objectType = isNinePatch ? "Nine Patch" : "Drawable";

            buttonItem.row();
            buttonItem.add(new Label(key, game.skin));
            buttonItem.row();
            buttonItem.add(new Label(objectType, game.skin, "title"));
            buttonItem.row();
            buttonItem.add(infoLabel);
            buttonItem.row();
            buttonItem.setClip(true);
            tableDrawables.add(buttonItem).width(160).height(184).pad(5);

            if (count == 4) { //TODO calculate with Width
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
