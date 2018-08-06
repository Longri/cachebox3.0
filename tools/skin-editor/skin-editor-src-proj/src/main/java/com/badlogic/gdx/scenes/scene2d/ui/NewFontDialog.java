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
import com.badlogic.gdx.graphics.g2d.freetype.SkinFont;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.FontFileIconProvider;
import de.longri.cachebox3.develop.tools.skin_editor.HoverListener;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import org.oscim.backend.CanvasAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * Create new font using Hiero classes
 *
 * @author Yanick Bourbeau
 */
public class NewFontDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(NewFontDialog.class);
    static private FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);

    static {
        FileTypeFilter typeFilter = new FileTypeFilter(true); //allow "All Types" mode where all files are shown
        typeFilter.addRule("TTF Font files (*.ttf)", "ttf");
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(typeFilter);
        FontFileIconProvider fontFileIconProvider = new FontFileIconProvider(fileChooser);
        fileChooser.setIconProvider(fontFileIconProvider);
    }

    private SkinEditorGame game;
    private TextField textFontName;
    private TextField textFontPreview;
    private SelectBox<String> selectFonts;
    private SelectBox<String> selectSize;

    /**
     *
     */
    public NewFontDialog(final SkinEditorGame game) {

        super("New Font", game.skin);
        this.game = game;
        loadSkinFonts();
        Table table = new Table(game.skin);
        table.debug();
        table.defaults().pad(10);

        table.add("Bitmap font name:");

        textFontName = new TextField("", game.skin);
        table.add(textFontName).width(300).left().colspan(4);
        table.row();


        table.add("Source font (TTF):").padRight(10);

        selectFonts = new SelectBox<String>(game.skin);
        table.add(selectFonts).left().colspan(3);

        Map<String, File> mapFonts = new TreeMap<String, File>(game.fm.fonts);
        Array<String> items = new Array<String>();
        Iterator<String> it = mapFonts.keySet().iterator();

        while (it.hasNext()) {
            items.add(it.next());
        }
        selectFonts.setItems(items);

        Button fontAddButton = new TextButton("+", game.skin);
        table.add(fontAddButton).left();

        fontAddButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                //open select file dialog
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> fileList) {
                        if (fileList.size < 1) return;
                        final FileHandle selectedFile = fileList.get(0);

                        if (selectedFile == null) {
                            return;
                        }

                        // copy to project font folder
                        FileHandle target = game.skinProject.skinFolder.child("fonts").child(selectedFile.name());
                        selectedFile.copyTo(target);

                        // refresh font list
                        loadSkinFonts();

                        // refresh list items
                        Map<String, File> mapFonts = new TreeMap<String, File>(game.fm.fonts);
                        Array<String> items = new Array<String>();
                        Iterator<String> it = mapFonts.keySet().iterator();

                        while (it.hasNext()) {
                            items.add(it.next());
                        }
                        selectFonts.setItems(items);

                    }
                });


                String fontDir = "";

                switch (CanvasAdapter.platform) {
                    case LINUX:
                        fontDir = "~/.fonts/";
                        break;
                    case MACOS:
                        fontDir = "~/Library/Fonts/";
                        break;
                    case WINDOWS:
                        fontDir = "c:\\Windows\\Fonts\\";
                        break;
                }

                fileChooser.setDirectory(fontDir);

                fileChooser.setSize(game.screenMain.stage.getWidth() * 0.9f,
                        game.screenMain.stage.getHeight() * 0.9f);

                //displaying chooser with fade in animation
                getStage().addActor(fileChooser.fadeIn());

            }
        });

        table.row();
        table.add("Font size:");
        selectSize = new SelectBox<String>(game.skin);
        selectSize.setItems("6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32",
                "34", "36", "38", "40", "42", "44", "46", "48", "50", "52", "54", "56", "58", "60", "62", "63");
        selectSize.setSelected("16");
        table.add(selectSize).left().width(100);


        table.row();


        TextField.TextFieldStyle textStyle = new TextField.TextFieldStyle();
        textStyle.cursor = game.skin.getDrawable("cursor");
        textStyle.selection = game.skin.getDrawable("selection");
        textStyle.background = game.skin.getDrawable("textfield");
        textStyle.fontColor = Color.YELLOW;
        textStyle.font = game.skin.getFont("default-font");

        textFontPreview = new TextField("This is a preview text", textStyle);
        table.add(textFontPreview).pad(20).colspan(5).expand().fill().left();

        selectFonts.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String fontName = selectFonts.getSelected();
                int size = Integer.valueOf(selectSize.getSelected());
                refreshFontPreview(size, fontName);
            }
        });

        selectFonts.selectBoxList.list.addListener(new HoverListener() {
            private String lastValue = "";

            public boolean mouseMoved(InputEvent event, float x, float y) {
                super.mouseMoved(event, x, y);
                int idx = selectFonts.selectBoxList.list.getSelectedIndex();
                final String name = selectFonts.selectBoxList.list.items.get(idx);
                if (!name.equals(lastValue)) {
                    final int size = Integer.valueOf(selectSize.getSelected());
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            refreshFontPreview(size, name);
                        }
                    });
                    lastValue = name;
                }
                return true;
            }

        });


        selectSize.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String fontName = selectFonts.getSelected();
                int size = Integer.valueOf(selectSize.getSelected());
                refreshFontPreview(size, fontName);
            }
        });

        selectSize.selectBoxList.list.addListener(new HoverListener() {
            private int lastValue = -1;

            public boolean mouseMoved(InputEvent event, float x, float y) {
                super.mouseMoved(event, x, y);
                int idx = selectSize.selectBoxList.list.getSelectedIndex();
                String strValue = selectSize.selectBoxList.list.items.get(idx);
                final int value = Integer.valueOf(strValue);
                if (value != lastValue) {
                    final String fontName = selectFonts.getSelected();

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            refreshFontPreview(value, fontName);
                        }
                    });
                    lastValue = value;
                }
                return true;
            }

        });


        String fontName = selectFonts.getSelected();
        int size = Integer.valueOf(selectSize.getSelected());
        refreshFontPreview(size, fontName);

        getContentTable().add(table).width(520).height(320).pad(20);
        getButtonTable().padBottom(15);

        TextButton buttonCreate = new TextButton("Create Font", game.skin);
        buttonCreate.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                // First check if the name is already in use
                if (game.skinProject.has(textFontName.getText(), BitmapFont.class)) {
                    game.showMsgDlg("Error", "A font with the same name already exists!", getStage());
                    return;
                }
                int size = Integer.valueOf(selectSize.getSelected());
                String fontName = selectFonts.getSelected();
                File fontPath = game.fm.fonts.get(fontName);

                String path = "fonts/" + fontPath.getName();

                BitmapFont font = new SkinFont(path, new FileHandle(fontPath), size, null);

                game.skinProject.add(textFontName.getText(), font);
                game.screenMain.saveToSkin();
                game.screenMain.refreshResources();


                hide();

            }

        });

        getButtonTable().add(buttonCreate);
        button("Cancel", false);
        key(com.badlogic.gdx.Input.Keys.ESCAPE, false);

    }


    /**
     *
     */
    @Override
    protected void result(Object object) {
        if ((Boolean) object == false) {
            return;
        }

    }

    private void loadSkinFonts() {

        // clear last
        game.fm.fontPaths.clear();
        game.fm.fontPaths.add(game.skinProject.skinFolder.child("fonts").file().getAbsolutePath());
        game.fm.refreshFonts();
    }


    FreeTypeFontGenerator generator;
    File lastFontPath;

    public void refreshFontPreview(int size, String fontName) {

        try {
            File fontPath = game.fm.fonts.get(fontName);
            log.debug("Refreshing preview for font: " + fontName + "  size: " + size);
            log.debug("Loading font from file:" + fontPath);


            if (generator == null || !fontPath.equals(lastFontPath)) {
                lastFontPath = fontPath;
                generator = new FreeTypeFontGenerator(new FileHandle(fontPath));
            }

            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = CB.getScaledInt(size);
            parameter.characters = SkinFont.DEFAULT_CHARACTER;
            parameter.genMipMaps = true;
            parameter.minFilter = Texture.TextureFilter.MipMapNearestNearest;

            BitmapFont bitmapFont = generator.generateFont(parameter);

            // update preview
            TextField.TextFieldStyle textStyle = new TextField.TextFieldStyle();
            textStyle.cursor = game.skin.getDrawable("cursor");
            textStyle.selection = game.skin.getDrawable("selection");
            textStyle.background = game.skin.getDrawable("textfield");
            textStyle.fontColor = Color.YELLOW;
            textStyle.font = bitmapFont;

            textFontPreview.setStyle(textStyle);
        } catch (Exception e) {
            e.printStackTrace();
        }


//		try {
//			String fontName = selectFonts.getSelected();
//			Gdx.app.log("FontPickerDialog", "Refreshing preview for font: " + fontName);
//
//			File fontPath = game.fm.fonts.get(selectFonts.getSelected());
//			Gdx.app.log("FontPickerDialog","Loading font from file:" + fontPath);
//
//			Font font = Font.createFont(Font.TRUETYPE_FONT, fontPath);
//			UnicodeFont unicodeFont = new UnicodeFont(font, Integer.valueOf(selectSize.getSelected()), checkBold.isChecked(), checkItalic.isChecked());
//
//			if (checkShadow.isChecked() == true) {
//
//				ColorEffect colorEffect = new ColorEffect();
//				colorEffect.setColor(java.awt.Color.BLACK);
//				unicodeFont.getEffects().add(colorEffect);
//
//				ShadowEffect shadow = new ShadowEffect();
//				shadow.setOpacity(1.0f);
//				shadow.setXDistance(1);
//				shadow.setYDistance(1);
//				shadow.setColor(java.awt.Color.WHITE);
//				unicodeFont.getEffects().add(shadow);
//
//			} else {
//				ColorEffect colorEffect = new ColorEffect();
//				colorEffect.setColor(java.awt.Color.WHITE);
//				unicodeFont.getEffects().add(colorEffect);
//
//			}
//
//			unicodeFont.addAsciiGlyphs();
//
//			String newFontName = generateProperFontName(fontName);
//
//			textFontName.setText(newFontName);
//
//			// Create bitmap font
//			BMFontUtil bfu = new BMFontUtil(unicodeFont);
//
//
//			FileHandle handle = new FileHandle(System.getProperty("java.io.tmpdir")).child(newFontName);
//			FileHandle handleFont = new FileHandle(handle.file().getAbsolutePath() + ".fnt");
//			bfu.save(handle.file());
//
//			FileHandle handleImage = new FileHandle(System.getProperty("java.io.tmpdir")).child(newFontName + ".png");
//
//			TextField.TextFieldStyle textStyle = new TextField.TextFieldStyle();
//			textStyle.cursor = game.skin.getDrawable("cursor");
//	    	textStyle.selection = game.skin.getDrawable("selection");
//			textStyle.background = game.skin.getDrawable("textfield");
//			textStyle.fontColor = Color.YELLOW;
//			textStyle.font = new BitmapFont(handleFont, handleImage, false);
//
//			textFontPreview.setStyle(textStyle);
//
//			// Have to do this to force clipping of font
//			textFontPreview.setText(textFontPreview.getText());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			textFontPreview.getStyle().font = game.skin.getFont("default-font");
//			// Have to do this to force clipping of font
//			textFontPreview.setText(textFontPreview.getText());
//		}
    }

}
