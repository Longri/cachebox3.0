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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.develop.tools.skin_editor.screens.MainScreen;
import de.longri.cachebox3.gui.drawables.FrameAnimationDrawable;
import de.longri.cachebox3.gui.skin.styles.CircularProgressStyle;
import de.longri.cachebox3.gui.skin.styles.FrameAnimationStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.skin.styles.ScaledSize;
import de.longri.cachebox3.gui.widgets.CircularProgressWidget;
import de.longri.cachebox3.utils.ScaledSizes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oscim.backend.canvas.Bitmap;

import java.io.IOException;


/**
 * A table representing the right part of the interface
 *
 * @author Yanick Bourbeau
 */
public class PreviewPane extends Table {

    private final static Logger log = LoggerFactory.getLogger(PreviewPane.class);

    final private SkinEditorGame game;
    final private MainScreen mainScreen;


    // An input listener to use on items inside a scroll pane, thanks to Tomski for the hint.
    private InputListener stopTouchDown = new InputListener() {

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.stop();
            return false;
        }
    };

    /**
     *
     */
    public PreviewPane(SkinEditorGame game, MainScreen mainScreen) {

        super(game.skin);
        this.game = game;
        this.mainScreen = mainScreen;
        top();
        left();

    }


    public void refresh() {

        log.debug("Refresh pane!");
        clear();

        String widget = game.screenMain.barWidgets.selectedStyle;
        String widgetStyle = game.resolveWidgetPackageName(widget);

        try {
            Class<?> style = Class.forName(widgetStyle);


            if (style == MapWayPointItemStyle.class) {
                addPreviewFor_MapWayPointItemStyle();
            } else {
                addDefaultPreview();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addPreviewFor_MapWayPointItemStyle() throws IOException {

        //get map preview image
        Image previewImage = new Image(game.skin.getRegion("device-preview-map"));
        add(previewImage);

        // create three images from icons of selected style and scale to corresponding size of preview image
        if (!(mainScreen.getSelectedStyle() instanceof MapWayPointItemStyle)) return;
        MapWayPointItemStyle selectedStyle = (MapWayPointItemStyle) mainScreen.getSelectedStyle();

        if (selectedStyle == null) return;

        float previewScaleFactor = previewImage.getHeight() / 1440;

        if (selectedStyle.large != null) {
            Image largeScaledImage = getImageFromBitmapName(((GetName) selectedStyle.large).getName(), previewScaleFactor);
            largeScaledImage.setPosition(300, 820);
            addActor(largeScaledImage);
        }

        if (selectedStyle.middle != null) {
            Image middleScaledImage = getImageFromBitmapName(((GetName) selectedStyle.middle).getName(), previewScaleFactor);
            middleScaledImage.setPosition(350, 870);
            addActor(middleScaledImage);
        }

        if (selectedStyle.small != null) {
            Image smallScaledImage = getImageFromBitmapName(((GetName) selectedStyle.small).getName(), previewScaleFactor);
            smallScaledImage.setPosition(380, 900);
            addActor(smallScaledImage);
        }

    }

    private void addDefaultPreview() throws ClassNotFoundException {

        String widget = game.screenMain.barWidgets.selectedStyle;
        String widgetStyle = game.resolveWidgetPackageName(widget);
        Class<?> style = Class.forName(widgetStyle);

        ObjectMap<String, ?> styles = game.skinProject.getAll(style);
        if (styles == null) {
            Label label = new Label("No styles defined for this widget type", game.skin, "error");
            add(label).pad(10).row();

        } else {

            Keys<String> keys = styles.keys();
            Array<String> sortedKeys = new Array<String>();
            for (String key : keys) {
                sortedKeys.add(key);
            }
            sortedKeys.sort();

            for (String key : sortedKeys) {

                // We render one per key
                add(new Label(key, game.skin, "title")).left().pad(10).expandX().row();

                try {
                    if (widget.equals("Label")) {

                        Label w = new Label("This is a Label widget", game.skinProject, key);
                        add(w).pad(10).padBottom(20).row();

                    } else if (widget.equals("Button")) { // Button

                        Button w = new Button(game.skinProject, key);
                        add(w).width(120).height(32).pad(10).padBottom(20).row();

                    } else if (widget.equals("TextButton")) { // TextButton

                        TextButton w = new TextButton("This is a TextButton widget", game.skinProject, key);

                        add(w).pad(10).padBottom(20).row();

                    } else if (widget.equals("ImageButton")) { // ImageButton

                        ImageButton w = new ImageButton(game.skinProject, key);
                        add(w).pad(10).padBottom(20).row();

                    } else if (widget.equals("CheckBox")) { // CheckBox

                        CheckBox w = new CheckBox("This is a CheckBox widget", game.skinProject, key);
                        w.setChecked(true);
                        add(w).pad(10).padBottom(20).row();


                    } else if (widget.equals("TextField")) { // TextField

                        TextField w = new TextField("This is a TextField widget", game.skinProject, key);
                        if (w.getStyle().fontColor == null) {
                            throw new Exception("Textfield style requires a font color!");
                        }

                        w.addListener(stopTouchDown);

                        add(w).pad(10).width(220).padBottom(20).row();

                    } else if (widget.equals("List")) { // List

                        List w = new List(game.skinProject, key);
                        Array<String> items = new Array<String>();
                        items.add("This is");
                        items.add("a");
                        items.add("List widget!");
                        w.setItems(items);

                        add(w).pad(10).width(220).height(120).padBottom(20).expandX().fillX().row();

                    } else if (widget.equals("SelectBox")) { // SelectBox
                        SelectBox<String> w = new SelectBox<String>(game.skinProject, key);
                        Array<String> items = new Array<String>();
                        items.add("This is");
                        items.add("a");
                        items.add("SelectBox widget!");
                        w.setItems(items);

                        add(w).pad(10).width(220).padBottom(20).expandX().fillX().row();

                    } else if (widget.equals("CB_ProgressBar")) { // CB_ProgressBar

                        ProgressBarStyle progressStyle = null;
                        CircularProgressStyle circProgressStyle = null;
                        try {
                            progressStyle = game.skinProject.get(key, ProgressBarStyle.class);
                        } catch (Exception e) {
                            circProgressStyle = game.skinProject.get(key, CircularProgressStyle.class);
                        }

                        Widget w = null;

                        if (progressStyle != null) {
                            // Check for edge-case: fields knob and knobBefore are optional but at least one should be specified
                            if (progressStyle.knob == null && progressStyle.knobBefore == null) {
                                throw new IllegalArgumentException("Fields 'knob' and 'knobBefore' in ProgressBarStyle are both optional but at least one should be specified");
                            }

                            w = new ProgressBar(0, 100, 5, false, progressStyle);
                            ((ProgressBar) w).setValue(50);
                            w.addListener(stopTouchDown);
                            add(w).pad(10).width(220).padBottom(20).expandX().fillX().row();

                        } else {

                            // must set Scaled sizes
                            // calculate scaled sizes
                            float button_width = CB.getScaledFloat(game.skinProject.get("button_width", ScaledSize.class).value);
                            float button_height = CB.getScaledFloat(game.skinProject.get("button_height", ScaledSize.class).value);
                            float button_width_wide = CB.getScaledFloat(game.skinProject.get("button_width_wide", ScaledSize.class).value);
                            float margin = CB.getScaledFloat(game.skinProject.get("margin", ScaledSize.class).value);
                            float check_box_height = CB.getScaledFloat(game.skinProject.get("check_box_height", ScaledSize.class).value);
                            float window_margin = CB.getScaledFloat(game.skinProject.get("check_box_height", ScaledSize.class).value);
                            CB.scaledSizes = new ScaledSizes(button_width, button_height, button_width_wide, margin,
                                    check_box_height, window_margin);


                            CircularProgressWidget c1 = new CircularProgressWidget(circProgressStyle);
                            CircularProgressWidget c2 = new CircularProgressWidget(circProgressStyle);
                            CircularProgressWidget c3 = new CircularProgressWidget(circProgressStyle);

                            c1.setProgressMax(-1);

                            c2.setProgressMax(100);
                            c2.setProgress(50);

                            c3.setProgressMax(100);
                            c3.setProgress(100);

                            Table line = new VisTable();

                            line.defaults().pad(CB.scaledSizes.MARGIN);

                            line.add(c1);
                            line.add(c2);
                            line.add(c3);


                            add(line).row();
                        }


                    } else if (widget.equals("Slider")) { // Slider

                        Slider w = new Slider(0, 100, 5, false, game.skinProject, key);
                        add(w).pad(10).width(220).padBottom(20).expandX().fillX().row();
                        w.addListener(stopTouchDown);

                        Slider w2 = new Slider(0, 100, 5, true, game.skinProject, key);
                        add(w2).pad(10).padBottom(20).expandX().fillX().row();
                        w2.addListener(stopTouchDown);


                    } else if (widget.equals("ScrollPane")) { // ScrollPane

                        Table t = new Table(game.skin);
                        for (int i = 0; i < 20; i++) {
                            t.add("This is a ScrollPane Widget").padRight(10);
                            t.add("This is a ScrollPane Widget").padRight(10);
                            t.add("This is a ScrollPane Widget").row();
                        }
                        ScrollPane w = new ScrollPane(t, game.skinProject, key);
                        w.addListener(stopTouchDown);
                        w.setFlickScroll(true);
                        w.setScrollbarsOnTop(true);
                        w.setScrollBarPositions(true, true);
                        w.setFadeScrollBars(false);
                        add(w).pad(10).width(420).height(240).padBottom(20).expandX().fillX().row();

                    } else if (widget.equals("SplitPane")) { // SplitPane

                        for (int j = 0; j < 2; j++) {
                            Table t = new Table(game.skin);
                            t.setBackground(game.skin.getDrawable("default-rect"));
                            Table t2 = new Table(game.skin);
                            t2.setBackground(game.skin.getDrawable("default-rect"));
                            for (int i = 0; i < 20; i++) {
                                t.add("This is a SplitPane Widget").pad(10).row();
                                t2.add("This is a SplitPane Widget").pad(10).row();
                            }

                            SplitPane w = new SplitPane(t, t2, (j % 2 == 0), game.skinProject, key);
                            w.addListener(stopTouchDown);
                            add(w).pad(10).width(220).height(160).padBottom(20).expandX().fillX();
                        }
                        row();

                    } else if (widget.equals("Window")) { // Window

                        Table t = new Table(game.skin);
                        for (int i = 0; i < 5; i++) {
                            t.add("This is a Window Widget").row();
                        }
                        Window w = new Window("This is a Window Widget", game.skinProject, key);
                        w.addListener(stopTouchDown);
                        w.add(t);
                        add(w).pad(10).width(420).height(240).padBottom(20).expandX().fillX().row();

                    } else if (widget.equals("Touchpad")) { // Touchpad

                        Touchpad w = new Touchpad(0, game.skinProject, key);
                        w.addListener(stopTouchDown);

                        add(w).pad(10).width(200).height(200).padBottom(20).expandX().fillX().row();

                    } else if (widget.equals("Tree")) { // Tree

                        Tree w = new Tree(game.skinProject, key);
                        Tree.Node node = new Tree.Node(new Label("This", game.skin));
                        Tree.Node node1 = new Tree.Node(new Label("is", game.skin));
                        Tree.Node node2 = new Tree.Node(new Label("a", game.skin));
                        Tree.Node node3 = new Tree.Node(new Label("Tree", game.skin));
                        Tree.Node node4 = new Tree.Node(new Label("Widget", game.skin));
                        node3.add(node4);
                        node2.add(node3);
                        node1.add(node2);
                        node.add(node1);
                        w.add(node);

                        w.expandAll();
                        add(w).pad(10).width(200).height(200).padBottom(20).expandX().fillX().row();
                    } else if (widget.equals("Animation")) { // Animation
                        FrameAnimationStyle frameAnimationStyle = game.skinProject.get(key, FrameAnimationStyle.class);
                        FrameAnimationDrawable drawable = new FrameAnimationDrawable(frameAnimationStyle);
                        Image image = new Image(drawable);
                        float width = frameAnimationStyle.frames.first().getRegionWidth();
                        float height = frameAnimationStyle.frames.first().getRegionHeight();
                        add(image).pad(10).padBottom(20).width(width).height(height).row();

                    } else {
                        add(new Label("Unknown widget type!", game.skin, "error")).pad(10).padBottom(20).row();
                    }
                } catch (Exception e) {
                    add(new Label("Please fill all required fields", game.skin, "error")).pad(10).padBottom(20).row();
                }
            }

        }
    }

    public void selectedStyleChanged() {
        refresh();
    }

    private Image getImageFromBitmapName(String bitmapName, float previewScaleFactor) throws IOException {

        ScaledSvg scaledSvg = game.skinProject.get(bitmapName, ScaledSvg.class);
        FileHandle fileHandle = game.skinProject.skinFolder.child(scaledSvg.path);
        Bitmap loadedBitmap = PlatformConnector.getSvg(bitmapName, fileHandle.read(),
                PlatformConnector.SvgScaleType.DPI_SCALED,
                scaledSvg.scale / previewScaleFactor / CB.getScaledFloat(1));

        byte[] data = loadedBitmap.getPngEncodedData();
        Pixmap pixmap = new Pixmap(data, 0, data.length);
        Texture texture = new Texture(pixmap);
        TextureRegion textureRegion = new TextureRegion(texture);
        Image image = new Image(textureRegion);
        return image;
    }
}
