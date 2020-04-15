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

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.screens.MainScreen;
import de.longri.cachebox3.develop.tools.skin_editor.screens.WelcomeScreen;


/**
 * Main game class I re-use everywhere in this program.
 *
 * @author Yanick Bourbeau
 */
public class SkinEditorGame extends Game {

    public final static String[] widgets = {"MapWayPointItem", "Sizes", "Icons", "MenuIcons", "Filter", "Label", "EditText", "Button",
            "GestureButton", "TextButton", "FileChooser", "Compass", "CacheTypes", "AttributeTypes", "LogTypes", "CheckBox", "TextField", "ListView",
            "SelectBox", "CB_ProgressBar", "Slider", "ScrollPane", "SplitPane", "Window", "Tree", "Animation", "Language"
            , "Setting", "CacheListItem", "StarStyle", "CacheSizeStyle"};

    static {
        FileChooser.setDefaultPrefsName("SkinEditor");
    }

    public SpriteBatch batch;
    public SavableSvgSkin skin;


    public MainScreen screenMain;
    public WelcomeScreen screenWelcome;

    // Project related
    public SavableSvgSkin skinProject;

    // System fonts
    public SystemFonts fm;

    // Optional check
    public OptionalChecker opt;

    public static String resolveWidgetPackageName(String widget) {
        switch (widget) {
            case "MapWayPointItem":
                return "de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle";
            case "GestureButton":
                return "de.longri.cachebox3.gui.skin.styles.GestureButtonStyle";
            case "Sizes":
                return "de.longri.cachebox3.gui.skin.styles.ScaledSize";
            case "Icons":
                return "de.longri.cachebox3.gui.skin.styles.IconsStyle";
            case "MenuIcons":
                return "de.longri.cachebox3.gui.skin.styles.MenuIconStyle";
            case "TextButton":
                return "com.kotcrab.vis.ui.widget.VisTextButton$VisTextButtonStyle";
            case "ListView":
                return "de.longri.cachebox3.gui.views.listview.ListView$ListViewStyle";
            case "FileChooser":
                return "de.longri.cachebox3.gui.skin.styles.FileChooserStyle";
            case "Compass":
                return "de.longri.cachebox3.gui.skin.styles.CompassStyle";
            case "CacheTypes":
                return "de.longri.cachebox3.gui.skin.styles.CacheTypeStyle";
            case "Animation":
                return "de.longri.cachebox3.gui.skin.styles.FrameAnimationStyle";
            case "SelectBox":
                return "de.longri.cachebox3.gui.skin.styles.SelectBoxStyle";
            case "EditText":
                return "de.longri.cachebox3.gui.skin.styles.EditTextStyle";
            case "AttributeTypes":
                return "de.longri.cachebox3.gui.skin.styles.AttributesStyle";
            case "Language":
                return "de.longri.cachebox3.gui.skin.styles.LanguageStyle";
            case "LogTypes":
                return "de.longri.cachebox3.gui.skin.styles.LogTypesStyle";
            case "Filter":
                return "de.longri.cachebox3.gui.skin.styles.FilterStyle";
            case "Setting":
                return "de.longri.cachebox3.gui.activities.Settings_Activity$SettingsActivityStyle";
            case "CacheListItem":
                return "de.longri.cachebox3.gui.skin.styles.CacheListItemStyle";
            case "StarStyle":
                return "de.longri.cachebox3.gui.skin.styles.StarsStyle";
            case "CacheSizeStyle":
                return "de.longri.cachebox3.gui.skin.styles.CacheSizeStyle";
            default:
                for (Class clazz : StyleTypes.items) {
                    if (clazz.getSimpleName().equals(widget)) {
                        return clazz.getName();
                    }
                }
                return "com.badlogic.gdx.scenes.scene2d.ui." + widget + "$" + widget + "Style";
        }


    }

    @Override
    public void create() {

        CB.setGlThread(Thread.currentThread());

        opt = new OptionalChecker();

        fm = new SystemFonts();
        fm.refreshFonts();

        // Create projects folder if not already here
        FileHandle dirProjects = new FileHandle("projects");

        if (dirProjects.isDirectory() == false) {
            dirProjects.mkdirs();
        }

        // Rebuild from raw resources, kind of overkill, might disable it for production
        {//TODO enable ones for create a new uiskin.atlas
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.maxHeight = 2048;
            settings.maxWidth = 2048;
            settings.combineSubdirectories = true;
            TexturePacker.process(settings, "skin-editor-src-proj/assets/resources/raw/", ".", "skin-editor-src-proj/assets/resources/uiskin");
        }

        batch = new SpriteBatch();

//        skin = new SavableSvgSkin(null);
        skin = new SavableSvgSkin("UiSkin");

        // add skin editor Texture pack
        skin.addRegions(new TextureAtlas(Gdx.files.internal("skin-editor-src-proj/assets/resources/uiskin.atlas")));

        // add VisUi Texture pack
        skin.addRegions(new TextureAtlas(Gdx.files.internal("skin-editor-src-proj/assets/resources/visuiskin.atlas")));

        skin.load(Gdx.files.internal("skin-editor-src-proj/assets/resources/visuiskin.json"));

        VisUI.load(skin);

        screenMain = new MainScreen(this);
        screenWelcome = new WelcomeScreen(this);
        setScreen(screenWelcome);

    }

    @Override
    public void dispose() {
        super.dispose();

        //delete temp folder

        FileHandle tmp = Gdx.files.local("null");
        if (tmp.exists()) {
            Gdx.app.log("FINISH", "Delete tmp folder");
            tmp.deleteDirectory();
        }


    }


//Language

    /**
     * Display a dialog with a notice
     */
    public void showMsgDlg(String title, String message, Stage stage) {
        Dialog dlg = new Dialog(title, skin);
        dlg.pad(20);
        dlg.getContentTable().add(message).pad(20);
        dlg.button("OK", true);
        dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, true);
        dlg.show(stage);
    }

    public void showMsgDlg(String msg, Stage stage) {
        Dialog dlg = new Dialog(msg, skin);
        dlg.pad(20);
        dlg.button("OK", true);
        dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, true);
        dlg.show(stage);
    }
}
