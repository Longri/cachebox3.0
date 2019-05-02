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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.develop.tools.skin_editor.ColorPickerDialog;
import de.longri.cachebox3.develop.tools.skin_editor.FontPickerDialog;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.develop.tools.skin_editor.StyleTypes;
import de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable;
import de.longri.cachebox3.gui.skin.styles.*;
import de.longri.cachebox3.utils.SkinColor;
import org.oscim.backend.canvas.Bitmap;

import java.util.Iterator;

/**
 * A table representing the left portion of the interface
 *
 * @author Yanick Bourbeau
 * @author Longri 2017
 */
public class OptionsPane extends Table {

    private SkinEditorGame game;
    public List<String> listStyles;
    private Array<String> listItems = new Array<String>();
    private Table tableFields;
    public Object currentStyle;
    private ObjectMap<String, Object> styles;
    final private PreviewPane previewPane;
    final private Cell styleLabelCell, styleCell, styleButtonCell;
    final private float styleButtonCellPrefHeight, styleLabelCellPrefHeight;

    /**
     *
     */
    public OptionsPane(final SkinEditorGame game, PreviewPane previewPane) {
        super();

        //this.setDebug(true);

        this.game = game;
        this.previewPane = previewPane;

        left();
        top();
        setBackground(game.skin.getDrawable("default-pane"));

        styleLabelCell = add(new Label("Styles", game.skin, "title")).pad(5);
        styleLabelCell.row();
        styleLabelCellPrefHeight = styleLabelCell.getPrefHeight();
        listStyles = new List<String>(game.skin, "dimmed");
        listStyles.setItems(listItems);
        ScrollPane styleScrollPane = new ScrollPane(listStyles, game.skin);
        styleScrollPane.setFlickScroll(false);
        styleScrollPane.setFadeScrollBars(false);
        styleScrollPane.setScrollbarsOnTop(true);
        styleScrollPane.setScrollBarPositions(false, true);
        styleScrollPane.setScrollingDisabled(true, false);
        styleCell = add(styleScrollPane).height(200).expandX().fillX().pad(5);
        styleCell.row();


        // add buttons
        Table tableStylesButtons = new Table();
        TextButton buttonNewStyle = new TextButton("New Style", game.skin);
        TextButton buttonDeleteStyle = new TextButton("Delete Style", game.skin);
        tableStylesButtons.add(buttonNewStyle).pad(5);
        tableStylesButtons.add(buttonDeleteStyle).pad(5);
        styleButtonCell = add(tableStylesButtons);
        styleButtonCell.row();
        styleButtonCellPrefHeight = styleButtonCell.getPrefHeight();


        // Callbacks
        listStyles.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                String key = (String) listStyles.getSelected();
                if (key != null) {
                    Gdx.app.log("OptionsPane", "Selected style: " + key);
                    currentStyle = styles.get(key);
                    updateTableFields(key);
                }
            }

        });

        buttonNewStyle.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                createNewStyle();
            }

        });

        buttonDeleteStyle.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showDeleteDialog();
            }

        });

        // Initialize table

        add(new Label("Fields", game.skin, "title")).pad(5).padTop(10).row();

        tableFields = new Table(game.skin);
        tableFields.setBackground(game.skin.getDrawable("dialogDim"));
        tableFields.left().top();

        ScrollPane scroll2 = new ScrollPane(tableFields, game.skin);
        scroll2.setFlickScroll(false);
        scroll2.setFadeScrollBars(false);
        scroll2.setScrollbarsOnTop(true);
        scroll2.setScrollBarPositions(false, true);
        scroll2.setScrollingDisabled(true, false);
        add(scroll2).pad(5).expand().fill();

        this.layout();
    }


    private void setStylePaneVisible(boolean visible) {

        if (!visible) {
            styleButtonCell.getActor().setVisible(false);
            styleButtonCell.height(0);
            styleLabelCell.getActor().setVisible(false);
            styleLabelCell.height(0);
            styleCell.getActor().setVisible(false);
            styleCell.height(0);
        } else {
            styleButtonCell.getActor().setVisible(true);
            styleButtonCell.height(styleButtonCellPrefHeight);
            styleLabelCell.getActor().setVisible(true);
            styleLabelCell.height(styleLabelCellPrefHeight);
            styleCell.getActor().setVisible(true);
            styleCell.height(200);
        }
        this.invalidateHierarchy();
    }


    /**
     *
     */
    protected void showDeleteDialog() {

        // FIXME: Check if it used by other style prior to delete it

        Dialog dlgStyle = new Dialog("Delete Style", game.skin) {

            @Override
            protected void result(Object object) {
                if ((Boolean) object == false) {
                    return;
                }

                // Now we really add it!
                game.skinProject.remove((String) listStyles.getSelected(), currentStyle.getClass());
//                refresh(true);
                game.screenMain.saveToSkin();
                game.screenMain.panePreview.refresh();

            }

        };

        dlgStyle.pad(20);
        dlgStyle.getContentTable().add("You are sure you want to delete this style?");
        dlgStyle.button("OK", true);
        dlgStyle.button("Cancel", false);
        dlgStyle.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dlgStyle.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
        dlgStyle.show(getStage());

    }

    /**
     *
     */
    protected void createNewStyle() {

        final TextField textStyleName = new TextField("", game.skin);
        Dialog dlgStyle = new Dialog("New Style", game.skin) {

            @Override
            protected void result(Object object) {
                if ((Boolean) object == false) {
                    return;
                }

                String styleName = textStyleName.getText();
                if (styleName.length() == 0) {

                    game.showMsgDlg("Warning", "No style name entered!", game.screenMain.stage);
                    return;
                }

                // Check if the style name is already in use
                if (listItems.contains(styleName, false)) {
                    game.showMsgDlg("Warning", "Style name already in use!", game.screenMain.stage);
                    return;
                }


                try {
                    if (currentStyle instanceof MapArrowStyle) {
                        // switch current style to MapWayPointItemStyle
                        // we have only one MapArrowStyle
                        currentStyle = MapWayPointItemStyle.class.newInstance();
                    }
                    game.skinProject.add(styleName, currentStyle.getClass().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //game.skinProject.add(text, game.skin.get("default", currentStyle.getClass()), currentStyle.getClass());
                game.screenMain.saveToSkin();
                // refresh(true);

                game.screenMain.panePreview.refresh();

            }

        };

        dlgStyle.pad(20);
        dlgStyle.getContentTable().add("Style name:");
        dlgStyle.getContentTable().add(textStyleName).pad(20);
        dlgStyle.button("OK", true);
        dlgStyle.button("Cancel", false);
        dlgStyle.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dlgStyle.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
        dlgStyle.show(getStage());
        getStage().setKeyboardFocus(textStyleName);

    }

    /**
     *
     */
    public void refreshSelection(String widgetStyle) {
        String key = listStyles.getSelected();
        Gdx.app.log("OptionsPane", "Fetching style:" + widgetStyle);

        listItems.clear();

        try {
            Class<?> style = Class.forName(widgetStyle);

            styles = (ObjectMap<String, Object>) game.skinProject.getAll(style);
            if (styles == null) {
                Gdx.app.error("OptionsPane", "No styles defined for this widget type");

                tableFields.clear();
            } else {
                Keys<String> keys = styles.keys();

                for (String k : keys) {
                    listItems.add(k);

                }

            }
            listItems.sort();
            listStyles.setItems(listItems);

        } catch (Exception e) {
            e.printStackTrace();
        }


        currentStyle = styles.get(key);
        updateTableFields(key);

    }

    /**
     *
     */
    public void refresh(final boolean stylePane, final String style) {
        final String widgetStyle = game.resolveWidgetPackageName(style);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                setStylePaneVisible(stylePane);

                Gdx.app.log("OptionsPane", "Refresh");
                Gdx.app.log("OptionsPane", "Fetching style:" + widgetStyle);

                listItems.clear();
                int selection = -1;

                try {

                    Class<?> style = Class.forName(widgetStyle);

                    currentStyle = style.newInstance();

                    styles = (ObjectMap<String, Object>) game.skinProject.getAll(style);

                    if (widgetStyle.equals("de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle")) {
                        MapArrowStyle mapArrowStyle = game.skinProject.get("myLocation", MapArrowStyle.class);
                        styles.put("myLocation", mapArrowStyle);

                        MapCenterCrossStyle mapCenterCrossStyle = game.skinProject.get("centerCross", MapCenterCrossStyle.class);
                        styles.put("centerCross", mapCenterCrossStyle);

                        MapInfoPanelStyle mapInfoPanelStyle = game.skinProject.get("infoPanel", MapInfoPanelStyle.class);
                        styles.put("infoPanel", mapInfoPanelStyle);

                        DirectLineRendererStyle directLineRendererStyle = game.skinProject.get("directLine", DirectLineRendererStyle.class);
                        styles.put("directline", directLineRendererStyle);

                        MapBubbleStyle mapBubbleStyle = game.skinProject.get("bubble", MapBubbleStyle.class);
                        styles.put("bubble", mapBubbleStyle);
                    }

                    if (widgetStyle.equals("de.longri.cachebox3.gui.views.listview.ListView$ListViewStyle")) {

                        CacheListItemStyle cacheListItemStyle = game.skinProject.get("cacheListItems", CacheListItemStyle.class);
                        styles.put("cacheListItems", cacheListItemStyle);

                        WayPointListItemStyle wayPointListItemStyle = game.skinProject.get("WayPointListItems", WayPointListItemStyle.class);
                        styles.put("WayPointListItems", wayPointListItemStyle);

                        LogListItemStyle logListItemStyle = game.skinProject.get("logListItems", LogListItemStyle.class);
                        styles.put("logListItems", logListItemStyle);

                        DraftListItemStyle fieldNoteListItemStyle = game.skinProject.get("fieldNoteListItemStyle", DraftListItemStyle.class);
                        styles.put("fieldNoteListItemStyle", fieldNoteListItemStyle);

                    }

                    if (widgetStyle.equals("com.kotcrab.vis.ui.widget.VisTextButton$VisTextButtonStyle")) {
                        ApiButtonStyle apiButtonStyle = game.skinProject.get("ApiButton", ApiButtonStyle.class);
                        styles.put("ApiButton", apiButtonStyle);
                    }

                    if (widgetStyle.equals("de.longri.cachebox3.gui.skin.styles.CompassStyle")) {
                        CompassViewStyle compassViewStyle = game.skinProject.get("compassViewStyle", CompassViewStyle.class);
                        styles.put("compassViewStyle", compassViewStyle);
                    }

                    if (widgetStyle.equals("com.badlogic.gdx.scenes.scene2d.ui.CB_ProgressBar$ProgressBarStyle")) {
                        CircularProgressStyle circularProgressStyle = game.skinProject.get("circularProgressStyle", CircularProgressStyle.class);
                        styles.put("circularProgressStyle", circularProgressStyle);
                    }

                    if (styles == null || styles.size == 0) {
                        Gdx.app.error("OptionsPane", "No styles defined for this widget type");

                        tableFields.clear();
                    } else {
                        Array<String> keys = styles.keys().toArray();
                        boolean first = true;

                        for (int i = 0, n = keys.size; i < n; i++) {
                            String key = keys.get(i);
                            listItems.add(key);

                            if (first == true) {

                                currentStyle = styles.get(key);
                                updateTableFields(key);
                                selection = listItems.size - 1;
                                first = false;
                            }

                        }

                    }
                    listItems.sort();
                    listStyles.setItems(listItems);

                    if (selection != -1) {
                        listStyles.setSelectedIndex(selection);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    /**
     *
     */
    public void updateSelectedTableFields() {

        String key = (String) listStyles.getSelected();
        if (key != null) {
            Gdx.app.log("OptionsPane", "Selected style: " + key);
            currentStyle = styles.get(key);
            updateTableFields(key);
        }
    }

    /**
     *
     */
    private void updateTableFields(final String style) {

        String widget = game.screenMain.barWidgets.selectedStyle;
        String widgetStyle = game.resolveWidgetPackageName(widget);

        Gdx.app.log("OptionsPane", "Update fields table for widget: " + widget + ", style: " + style);
        tableFields.clear();
        tableFields.add(new Label("name", game.skin, "title")).left().width(170);
        tableFields.add(new Label("Value", game.skin, "title")).left().width(60).padRight(50);
        tableFields.row();

        Field[] fields = ClassReflection.getFields(currentStyle.getClass());
        for (final Field field : fields) {
            try {


                Actor actor = null;

                // field type
                String name = field.getType().getSimpleName();
                Object obj = field.get(currentStyle);

                if (name.equals("Array")) {
                    actor = getArrayActor(field, obj);
                } else if (name.equals("Drawable")) {
                    actor = getDrawableActor(field, -1, null);
                } else if (name.equals("Bitmap")) {
                    actor = getBitmapActor(field);
                } else if (name.equals("Color")) {
                    actor = getColorActor(field);
                } else if (name.equals("BitmapFont")) {
                    actor = getBitmapFontActor(field);
                } else if (name.equals("float")) {
                    actor = getFloatActor(field);
                } else if (name.equals("ListStyle")) {
                    actor = getListStyleActor(field);
                } else if (name.equals("ScrollPaneStyle")) {
                    actor = getScrollPaneStyleActor(field);
                } else if (name.equals("boolean")) {
                    actor = getBooleanActor(field);
                } else if (field.getType().isEnum()) {
                    actor = getEnumActor(field, obj);
                } else {

                    //if Type any Style
                    String fullName = field.getType().getName();
                    for (final Class clazz : StyleTypes.items) {
                        if (clazz.getName().equals(fullName)) {
                            //get all Styles
                            final ObjectMap allStyles = game.skinProject.getAll(clazz);

                            Array<String> itemList = new Array<String>();
                            itemList.add(""); //empty entry, for not set
                            for (Object styleName : allStyles.keys())
                                itemList.add((String) styleName);
                            final VisSelectBox<String> selectBox = new VisSelectBox();
                            selectBox.setItems(itemList);

                            String selectedName = SvgSkinUtil.resolveObjectName(game.skinProject, clazz, field.get(currentStyle));
                            if (selectedName == null) selectedName = "";
                            selectBox.setSelected(selectedName);

                            selectBox.addListener(new ChangeListener() {

                                @Override
                                public void changed(ChangeEvent event, Actor actor) {

                                    String selection = (String) ((SelectBox) actor).getSelected();
                                    Object selectionObject = null;
                                    try {
                                        for (Object object : allStyles.values()) {
                                            String selectedName = SvgSkinUtil.resolveObjectName(game.skinProject, clazz, object);
                                            if (selection.equals(selectedName)) {
                                                selectionObject = object;
                                                break;
                                            }
                                        }
                                        field.set(currentStyle, selectionObject);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    game.screenMain.saveToSkin();
                                    refresh(true, currentStyle.getClass().getSimpleName());
                                    game.screenMain.paneOptions.updateSelectedTableFields();
                                    game.screenMain.panePreview.refresh();
                                }

                            });
                            actor = selectBox;
                        }
                    }
                }

                if (actor != null) {

                    // field name

                    // White required
                    // Grey optional
                    if (game.opt.isFieldOptional(currentStyle.getClass(), field.getName())) {

                        tableFields.add(new Label(field.getName(), game.skin, "optional")).left();

                    } else {
                        tableFields.add(new Label(field.getName(), game.skin, "default")).left();

                    }
//                    tableFields.add(actor).left().height(64).padRight(24).expandX().fillX();
                    tableFields.add(actor).left().padBottom(12).padRight(24).expandX().fillX();
                    tableFields.row();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            //send changes to PreviewPane
            previewPane.selectedStyleChanged();
        }

    }

    private Actor getArrayActor(final Field field, Object obj) throws ReflectionException {
        Table table = new Table();
        Object value = field.get(currentStyle);

        final Array<TextureAtlas.AtlasRegion> array = (Array<TextureAtlas.AtlasRegion>) value;

        VisTextButton minus = new VisTextButton("-");
        VisTextButton plus = new VisTextButton("+");
        plus.addListener(new ClickListener() {

            public void clicked(InputEvent event, float x, float y) {
                int newSize = (array != null ? array.size : 0) + 1;
                Array<TextureAtlas.AtlasRegion> newArray = new Array<TextureAtlas.AtlasRegion>(newSize);
                for (int i = 0, n = array.size; i < n; i++) {
                    newArray.add(array.get(i));
                }
                newArray.add(null);
                try {
                    field.set(game.screenMain.paneOptions.currentStyle, newArray);
                } catch (ReflectionException e) {
                    e.printStackTrace();
                }
                game.screenMain.saveToSkin();
                game.screenMain.panePreview.refresh();
                game.screenMain.paneOptions.updateSelectedTableFields();
            }
        });

        minus.addListener(new ClickListener() {

            public void clicked(InputEvent event, float x, float y) {
                int newSize = (array != null ? array.size : 0) - 1;
                Array<TextureAtlas.AtlasRegion> newArray = new Array<TextureAtlas.AtlasRegion>(newSize);
                for (int i = 0, n = newSize; i < n; i++) {
                    newArray.add(array.get(i));
                }
                try {
                    field.set(game.screenMain.paneOptions.currentStyle, newArray);
                } catch (ReflectionException e) {
                    e.printStackTrace();
                }
                game.screenMain.saveToSkin();
                game.screenMain.panePreview.refresh();
                game.screenMain.paneOptions.updateSelectedTableFields();
            }
        });


        VisLabel sizeLabel = new VisLabel(Integer.toString(array != null ? array.size : 0));
        table.add(minus).pad(5);
        table.add(sizeLabel).pad(5);
        table.add(plus).pad(5);
        table.row();

        if (array != null) {
            for (int i = 0, n = array.size; i < n; i++) {
                table.add(getDrawableActor(field, i, array.get(i))).colspan(3);
                table.row();
            }
        }
        return table;
    }

    private Actor getEnumActor(final Field field, Object obj) throws ReflectionException {
        Actor actor;
        String resourceName = "";
        final Object[] enumValues = field.getType().getEnumConstants();

        //Enum's should be not NULL, so set to first
        if (obj == null) {
            field.set(currentStyle, enumValues[0]);
            game.screenMain.saveToSkin();
            resourceName = enumValues[0].toString();
        } else {
            resourceName = obj.toString();
        }

        actor = new SelectBox<String>(game.skin, "default");
        final Array<String> items = new Array<String>();

        for (Object object : enumValues) {
            items.add(object.toString());
        }

        ((SelectBox) actor).setItems(items);


        ((SelectBox) actor).setSelected(resourceName);

        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                String selection = (String) ((SelectBox) actor).getSelected();
                try {

                    Object selectionObject = null;
                    for (Object object : enumValues) {
                        if (selection.equals(object.toString())) {
                            selectionObject = object;
                            break;
                        }
                    }
                    field.set(currentStyle, selectionObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                game.screenMain.saveToSkin();
                refresh(true, selection);
                game.screenMain.paneOptions.updateSelectedTableFields();
                game.screenMain.panePreview.refresh();
            }

        });
        return actor;
    }

    private Actor getBooleanActor(final Field field) throws ReflectionException {
        Actor actor;
        final Boolean[] value = new Boolean[]{(Boolean) field.get(currentStyle)};
        String resourceName = "";

        ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
                game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

        if ((value != null)) {
            resourceName = String.valueOf(value[0]);
        } else {
            buttonStyle.up = game.skin.getDrawable("default-rect");
            buttonStyle.checked = game.skin.getDrawable("default-rect");
        }

        actor = new ImageTextButton(resourceName, buttonStyle);
        ((ImageTextButton) actor).setClip(true);
        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                value[0] = !value[0];
                try {
                    field.set(currentStyle, value[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                game.screenMain.saveToSkin();
                ((ImageTextButton) actor).setText(String.valueOf(value[0]));
            }

        });
        return actor;
    }

    private Actor getScrollPaneStyleActor(final Field field) throws ReflectionException {
        Actor actor;
        ScrollPaneStyle scrollStyle = (ScrollPaneStyle) field.get(currentStyle);

        actor = new SelectBox<String>(game.skin, "default");
        Array<String> items = new Array<String>();

        final ObjectMap<String, ScrollPaneStyle> values = game.skinProject.getAll(ScrollPaneStyle.class);
        Iterator<String> it = values.keys().iterator();
        String selection = null;

        while (it.hasNext()) {
            String key = it.next();
            items.add(key);

            if (scrollStyle == values.get(key)) {
                selection = key;
            }
        }

        ((SelectBox) actor).setItems(items);

        if (selection != null) {
            ((SelectBox) actor).setSelected(selection);
        }

        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                String selection = (String) ((SelectBox) actor).getSelected();
                try {
                    field.set(currentStyle, values.get(selection));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                game.screenMain.saveToSkin();
                refresh(true, selection);
                game.screenMain.paneOptions.updateSelectedTableFields();
                game.screenMain.panePreview.refresh();
            }

        });
        return actor;
    }

    private Actor getListStyleActor(final Field field) throws ReflectionException {
        Actor actor;
        ListStyle listStyle = (ListStyle) field.get(currentStyle);

        actor = new SelectBox<String>(game.skin, "default");
        Array<String> items = new Array<String>();

        final ObjectMap<String, ListStyle> values = game.skinProject.getAll(ListStyle.class);
        Iterator<String> it = values.keys().iterator();
        String selection = null;

        while (it.hasNext()) {
            String key = it.next();
            items.add(key);

            if (listStyle == values.get(key)) {
                selection = key;
            }
        }

        ((SelectBox) actor).setItems(items);

        if (selection != null) {
            ((SelectBox) actor).setSelected(selection);
        }

        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                String selection = (String) ((SelectBox) actor).getSelected();
                try {
                    field.set(currentStyle, values.get(selection));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                game.screenMain.saveToSkin();
                refresh(true, selection);
                game.screenMain.paneOptions.updateSelectedTableFields();
                game.screenMain.panePreview.refresh();
            }

        });
        return actor;
    }

    private Actor getFloatActor(final Field field) throws ReflectionException {
        Actor actor;
        Float value = (Float) field.get(currentStyle);
        String resourceName = "";

        ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
                game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

        if ((value != null) && (value != 0)) {
            resourceName = String.valueOf(value);
        } else {
            buttonStyle.up = game.skin.getDrawable("default-rect");
            buttonStyle.checked = game.skin.getDrawable("default-rect");
        }

        actor = new ImageTextButton(resourceName, buttonStyle);
        ((ImageTextButton) actor).setClip(true);
        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showFloatInputDialog(field);

            }

        });
        return actor;
    }

    private Actor getBitmapFontActor(final Field field) throws ReflectionException {
        Actor actor;
        BitmapFont font = (BitmapFont) field.get(currentStyle);
        String resourceName = "";
        ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
                game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

        if (font != null) {
            resourceName = SvgSkinUtil.resolveObjectName(game.skinProject, BitmapFont.class, font);
            buttonStyle.font = font;
        } else {
            buttonStyle.up = game.skin.getDrawable("default-rect");
            buttonStyle.checked = game.skin.getDrawable("default-rect");
        }

        actor = new ImageTextButton(resourceName, buttonStyle);
        ((ImageTextButton) actor).setClip(true);

        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showFontPickerDialog(field);

            }

        });
        return actor;
    }

    private Actor getColorActor(final Field field) throws ReflectionException {
        Actor actor;
        Color color = (Color) field.get(currentStyle);
        ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
                game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

        String resourceName = "";
        if (color != null) {

            if (color instanceof SkinColor) {
                resourceName = ((SkinColor) color).skinName;
            } else {
                resourceName = SvgSkinUtil.resolveObjectName(game.skinProject, SkinColor.class, color);
            }

            resourceName += " (" + color.toString() + ")";

            // Create drawable on the fly
            Pixmap pixmap = new Pixmap(18, 18, Pixmap.Format.RGBA8888);
            pixmap.setColor(color);
            pixmap.fill();
            pixmap.setColor(Color.BLACK);
            pixmap.drawRectangle(0, 0, 18, 18);
            Texture texture = new Texture(pixmap);
            buttonStyle.imageUp = new SpriteDrawable(new Sprite(texture));
            pixmap.dispose();
        } else {
            buttonStyle.up = game.skin.getDrawable("default-rect");
            buttonStyle.checked = game.skin.getDrawable("default-rect");
        }

        actor = new ImageTextButton(resourceName, buttonStyle);
        ((ImageTextButton) actor).setClip(true);
        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showColorPickerDialog(field);

            }

        });
        return actor;
    }

    private Actor getBitmapActor(final Field field) throws ReflectionException {
        Actor actor;
        Bitmap bitmap = (Bitmap) field.get(currentStyle);
        byte[] bytes = null;
        if (bitmap != null) bytes = bitmap.getPngEncodedData();
        Drawable drawable = bitmap != null ?
                new TextureRegionDrawable(new TextureRegion(new Texture(new Pixmap(bytes, 0, bytes.length))))
                : null;

        String resourceName = "";
        ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
                game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

        if (drawable != null) {
            resourceName = ((GetName) bitmap).getName();
            buttonStyle.imageUp = drawable;
        } else {
            buttonStyle.up = game.skin.getDrawable("default-rect");
            buttonStyle.checked = game.skin.getDrawable("default-rect");
        }

        actor = new ImageTextButton(resourceName, buttonStyle);
        ((ImageTextButton) actor).setClip(true);
        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showDrawableDialog(field, true, -1);

            }

        });
        return actor;
    }

    private Actor getDrawableActor(final Field field, final int arrayIndex, TextureAtlas.AtlasRegion region) throws ReflectionException {
        Actor actor;

        Drawable drawable = null;
        String resourceName = null;
        if (field.get(currentStyle) instanceof Array) {
            if (region != null) {
                drawable = new TextureRegionDrawable(region);
                resourceName = region.name;
            }
        } else {
            drawable = (Drawable) field.get(currentStyle);
        }

        ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
                game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

        if (drawable != null) {
            if (resourceName == null) {
                resourceName = SvgSkinUtil.resolveObjectName(game.skinProject, Drawable.class, drawable);
                if (resourceName == null) {
                    resourceName = SvgSkinUtil.resolveObjectName(game.skinProject, TextureRegion.class, drawable);
                }
            }

            if (drawable instanceof SvgNinePatchDrawable) {
                //TODO override pref width and height
//                ((SvgNinePatchDrawable) drawable).setAdditionalPrefWidth(50);
//                ((SvgNinePatchDrawable) drawable).setAdditionalPrefHeight(50);
            }

            buttonStyle.imageUp = drawable;
        } else {
            buttonStyle.up = game.skin.getDrawable("default-rect");
            buttonStyle.checked = game.skin.getDrawable("default-rect");
        }

        actor = new ImageTextButton(resourceName, buttonStyle);
        ((ImageTextButton) actor).setClip(false);
        actor.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showDrawableDialog(field, false, arrayIndex);

            }

        });
        return actor;
    }

    /**
     * Display a input dialog asking for a float value
     */
    public void showFloatInputDialog(final Field field) {

        try {
            final TextField textValue = new TextField(String.valueOf((Float) field.get(currentStyle)), game.skin);
            Dialog dlg = new Dialog("Change Value", game.skin) {

                @Override
                protected void result(Object object) {
                    if ((Boolean) object == false) {
                        return;
                    }

                    float value = 0;
                    String text = textValue.getText();
                    if (text.isEmpty() == false) {
                        value = Float.valueOf(text);
                    }

                    try {
                        field.set(currentStyle, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    game.screenMain.saveToSkin();
//                    refresh(true);
                    game.screenMain.paneOptions.updateSelectedTableFields();
                    game.screenMain.panePreview.refresh();
                }
            };

            dlg.pad(20);
            dlg.getContentTable().add("Float Value:");
            dlg.getContentTable().add(textValue).pad(20);
            dlg.button("OK", true);
            dlg.button("Cancel", false);
            dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
            dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
            dlg.show(getStage());
            getStage().setKeyboardFocus(textValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show color picker dialog
     */
    public void showColorPickerDialog(final Field field) {

        ColorPickerDialog dlg = new ColorPickerDialog(game, field);
        dlg.show(getStage());
    }

    /**
     * show font picker dialog
     */
    public void showFontPickerDialog(final Field field) {

        FontPickerDialog dlg = new FontPickerDialog(game, field);
        dlg.show(getStage());
    }


    /**
     * show drawable picker dialog
     *
     * @param field
     */
    public void showDrawableDialog(final Field field, boolean disableNinePatch, int arrayIndex) {

        DrawablePickerDialog dlg = new DrawablePickerDialog(game, field, arrayIndex, disableNinePatch, getStage());
        dlg.show(getStage());
    }

    public Object getSelectedStyle() {
        return currentStyle;
    }
}
