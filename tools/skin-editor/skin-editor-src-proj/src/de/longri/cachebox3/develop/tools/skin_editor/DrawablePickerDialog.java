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
package de.longri.cachebox3.develop.tools.skin_editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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
import org.oscim.backend.canvas.Bitmap;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Display a dialog allowing to pick a drawable resource such as a ninepatch
 * or a texture region. You can also add resource from file (PNG only for now)
 *
 * @author Yanick Bourbeau
 */
public class DrawablePickerDialog extends Dialog {


    private SkinEditorGame game;
    private Field field;
    private Table tableDrawables;
    private boolean zoom = false;
    private HashMap<String, Object> items = new HashMap<String, Object>();
    private ScrollPane scrollPane;
    static private FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
    static private SvgFileIconProvider svgFileIconProvider;

    static {
        FileTypeFilter typeFilter = new FileTypeFilter(true); //allow "All Types" mode where all files are shown
        typeFilter.addRule("SVG files (*.svg)", "svg");
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(typeFilter);
        svgFileIconProvider = new SvgFileIconProvider(fileChooser);
        fileChooser.setIconProvider(svgFileIconProvider);
    }

    public DrawablePickerDialog(final SkinEditorGame game, final Field field) {

        super("Drawable Picker", game.skin);
        this.game = game;
        this.field = field;


        //set size

        //TODO set size


        tableDrawables = new Table(game.skin);
        scrollPane = new ScrollPane(tableDrawables, game.skin);

        getContentTable().add(scrollPane);
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

                // Need to steal focus first with this hack (Thanks to Z-Man)
                Frame frame = new Frame();
                frame.setUndecorated(true);
                frame.setOpacity(0);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.toFront();
                frame.setVisible(false);
                frame.dispose();


//                JFileChooser chooser = new JFileChooser();
//                FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "svg");
//                chooser.setFileFilter(filter);
//                int returnVal = chooser.showOpenDialog(null);
//                if (returnVal != JFileChooser.APPROVE_OPTION) {
//                    return;
//                }


                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> fileList) {
                        FileHandle selectedFile = fileList.get(0);

                        if (selectedFile == null) {
                            return;
                        }
                        // Loop until the file is not found
                        while (true) {
                            String resourceName = selectedFile.name();
                            String ext = resourceName.substring(resourceName.lastIndexOf(".") + 1);
                            resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
                            resourceName = JOptionPane.showInputDialog("Please choose the name of your resource", resourceName);
                            if (resourceName == null) {
                                return;
                            }


                            String scalfactor = JOptionPane.showInputDialog("Please choose the scale of your resource", "1.0");

                            // Copy the file
                            FileHandle orig = selectedFile;
                            String originalName = orig.name();
                            FileHandle dest = new FileHandle("/projects/" + game.screenMain.getcurrentProject() + "/svg/" + originalName);
                            orig.copyTo(dest);


                            // write scaled svg section
                            ScaledSvg scaledSvg = new ScaledSvg();
                            scaledSvg.path = "svg/" + originalName;
                            scaledSvg.scale = Float.parseFloat(scalfactor);
                            scaledSvg.setRegisterName(resourceName);
                            game.skinProject.add(resourceName, scaledSvg);



                            FileHandle projectFolder = new FileHandle("/projects/" + game.screenMain.getcurrentProject());
                            FileHandle projectFile = projectFolder.child("skin.json");
                            game.skinProject.save(projectFile);

                            game.screenMain.refreshResources();
                            refresh();
                            JOptionPane.showMessageDialog(null, "File successfully added to your project.");
                            return;

                        }
                    }
                });


                fileChooser.setSize(game.screenMain.stage.getWidth() * 0.9f,
                        game.screenMain.stage.getHeight() * 0.9f);

                //displaying chooser with fade in animation
                getStage().addActor(fileChooser.fadeIn());


//
//                File selectedFile = chooser.getSelectedFile();
//
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

        getContentTable().add(scrollPane).width(getPrefWidth()).height(getPrefHeight() * 0.9f).pad(20);
        getButtonTable().add(buttonNewNinePatch);
        getButtonTable().add(buttonNewDrawable);
        getButtonTable().add(buttonZoom);
        if (field != null) {
            getButtonTable().add(buttonNoDrawable);
        }
        getButtonTable().padBottom(15);
        button("Cancel", false);
        key(com.badlogic.gdx.Input.Keys.ESCAPE, false);

    }

    @Override
    public Dialog show(Stage stage) {

        refresh();

        Dialog d = super.show(stage);
        getStage().setScrollFocus(scrollPane);

//        d.setBounds(0, 0, 700, 700);

        return d;
    }

    private void refresh() {

        ObjectMap<String, Drawable> itemsDrawables = game.skinProject.getAll(Drawable.class);
        ObjectMap<String, TextureRegion> itemsRegions = game.skinProject.getAll(TextureRegion.class);

        items.clear();

        Iterator<String> it = itemsDrawables.keys().iterator();
        while (it.hasNext()) {
            String key = it.next();
            items.put(key, itemsDrawables.get(key));
        }

        it = itemsRegions.keys().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (itemsDrawables.containsKey(key)) {
                continue;
            }
            items.put(key, itemsRegions.get(key));
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
            if (items.get(key) instanceof Drawable) {
                img = new Image((Drawable) items.get(key));
            } else {
                img = new Image((TextureRegion) items.get(key));

            }

            if (zoom == true) {
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
