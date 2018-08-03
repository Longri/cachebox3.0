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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.Field;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import de.longri.cachebox3.utils.SkinColor;


import java.util.Iterator;

/**
 * A color picker class that allow to create and re-use colors across
 * the skin. It uses Swing color picker.
 *
 * @author Yanick Bourbeau
 */
public class ColorPickerDialog extends Dialog {

    private SkinEditorGame game;
    private Table tableColors;
    ObjectMap<String, SkinColor> colors;
    private Field field;

    ColorPickerAdapter newColorPickerAdapter = new ColorPickerAdapter() {
        @Override
        public void finished(final Color color) {
            if (color != null) {
                final TextField nameTextField = new TextField("???", game.skin);
                Dialog dlg0 = new Dialog("name your color", game.skin) {

                    @Override
                    protected void result(Object object) {
                        if ((Boolean) object == false) {
                            return;
                        }

                        String colorName = nameTextField.getText();

                        if ((colorName != null) && (colorName.isEmpty() == false)) {
                            // Verify if the color name is already in use
                            if (colors.containsKey(colorName) == true) {
                                game.showMsgDlg("Error", "Color name already in use!", game.screenMain.stage);
                            } else {
                                // add the color (asuming RGBA)

                                SkinColor newColor = new SkinColor(color);
                                newColor.skinName = colorName;
                                colors.put(colorName, newColor);
                                game.screenMain.saveToSkin();

                                // update table
                                updateTable();
                            }
                        }
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

            }
        }
    };

    ColorPicker colorPicker = new ColorPicker(newColorPickerAdapter);

    /**
     *
     */
    public ColorPickerDialog(final SkinEditorGame game, final Field field) {

        super("Color Picker", game.skin);

        this.game = game;
        this.field = field;

        tableColors = new Table(game.skin);
        tableColors.left().top().pad(5);
        tableColors.defaults().pad(5);
        colors = game.skinProject.getAll(SkinColor.class);

        updateTable();


        TextButton buttonNewColor = new TextButton("New Color", game.skin);
        buttonNewColor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Call color picker
                getStage().addActor(colorPicker.fadeIn());
            }

        });

        TextButton buttonNoColor = new TextButton("Empty Color", game.skin);
        buttonNoColor.addListener(new ChangeListener() {

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

        ScrollPane scrollPane = new ScrollPane(tableColors, game.skin);
        scrollPane.setFlickScroll(false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(true);

        getContentTable().add(scrollPane).width(540).height(320).pad(20);
        getButtonTable().add(buttonNewColor);
        if (field != null) {
            getButtonTable().add(buttonNoColor);
        }
        getButtonTable().padBottom(15);
        button("Cancel", false);
        key(com.badlogic.gdx.Input.Keys.ESCAPE, false);


    }


    /**
     * Refresh table content with colors from the skin
     */
    public void updateTable() {

        tableColors.clear();
        tableColors.add(new Label("Color name", game.skin, "title")).left().width(170);
        tableColors.add(new Label("Value", game.skin, "title")).colspan(2).left().width(60).padRight(50);

        tableColors.row();

        Iterator<String> it = colors.keys().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            final Color color = colors.get(key);

            tableColors.add(key).left();

            // Create drawable on the fly
            Pixmap pixmap = new Pixmap(18, 18, Pixmap.Format.RGBA8888);
            pixmap.setColor(color);
            pixmap.fill();
            pixmap.setColor(Color.BLACK);
            pixmap.drawRectangle(0, 0, 18, 18);
            Texture texture = new Texture(pixmap);
            pixmap.dispose();


            Image colorImage = new Image(texture);
            colorImage.addListener(new ClickListener() {

                public void clicked(InputEvent event, float x, float y) {
                    // change color
                    colorPicker.setColor(color);

                    colorPicker.setListener(new ColorPickerAdapter() {
                        @Override
                        public void finished(final Color newColor) {
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    SkinColor changedColor = new SkinColor(newColor);
                                    changedColor.skinName = key;
                                    colors.put(key, changedColor);
                                    game.screenMain.saveToSkin();
                                    // update table
                                    updateTable();
                                }
                            });
                        }
                    });
                    // Call color picker
                    ColorPickerDialog.this.getStage().addActor(colorPicker.fadeIn());
                }

            });

            tableColors.add(colorImage);
            tableColors.add(color.toString()).left();

            TextButton buttonSelect = new TextButton("Select", game.skin);
            buttonSelect.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        field.set(game.screenMain.paneOptions.currentStyle, color);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    hide();
                    game.screenMain.panePreview.refresh();
                    game.screenMain.paneOptions.updateSelectedTableFields();
                    game.screenMain.saveToSkin();


                }

            });

            TextButton buttonRemove = new TextButton("remove", game.skin);
            buttonRemove.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeEvent event, Actor actor) {

                    Dialog dlg = new Dialog("Delete Style", game.skin) {

                        @Override
                        protected void result(Object object) {
                            if ((Boolean) object == false) {
                                return;
                            }

                            colors.remove(key);
                            // update table
                            updateTable();
                            game.screenMain.saveToSkin();

                        }

                    };

                    dlg.pad(20);
                    dlg.getContentTable().add("You are sure you want to delete this color?");
                    dlg.button("OK", true);
                    dlg.button("Cancel", false);
                    dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
                    dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
                    dlg.show(game.screenMain.stage);


                }

            });

            if (field != null) {
                tableColors.add(buttonSelect).padRight(5);
            }
            tableColors.add(buttonRemove);
            tableColors.row();
        }

    }
}
