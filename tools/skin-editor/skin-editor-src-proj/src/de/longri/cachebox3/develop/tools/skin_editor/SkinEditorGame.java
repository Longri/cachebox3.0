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
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import de.longri.cachebox3.develop.tools.skin_editor.screens.MainScreen;
import de.longri.cachebox3.develop.tools.skin_editor.screens.WelcomeScreen;
import de.longri.cachebox3.logging.Logger;


/**
 * Main game class I re-use everywhere in this program.
 *
 * @author Yanick Bourbeau
 */
public class SkinEditorGame extends Game {

    static {
        FileChooser.setDefaultPrefsName("SkinEditor");
    }

    public final static String[] widgets = {"MapWayPointItem", "Sizes", "Label", "Button", "TextButton", "ImageButton", "CheckBox", "TextField", "List", "SelectBox", "ProgressBar", "Slider", "ScrollPane", "SplitPane", "Window", "Tree"};

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

    @Override
    public void create() {

        Logger.setCurrentLogLevel(Logger.LOG_LEVEL_TRACE);


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
//            TexturePacker.Settings settings = new TexturePacker.Settings();
//            settings.maxHeight = 2048;
//            settings.maxWidth = 2048;
//            settings.combineSubdirectories = true;
//            TexturePacker.process(settings, "skin-editor-src-proj/assets/resources/raw/", ".",
//                    "skin-editor-src-proj/assets/resources/uiskin");
        }

        batch = new SpriteBatch();

//        skin = new SavableSvgSkin(null);
        skin = new SavableSvgSkin("UiSkin");

        // add skin editor Texture pack
        skin.addRegions(new TextureAtlas(Gdx.files.classpath("resources/uiskin.atlas")));

        // add VisUi Texture pack
        skin.addRegions(new TextureAtlas(Gdx.files.classpath("resources/visuiskin.atlas")));


        skin.load(Gdx.files.classpath("resources/visuiskin.json"));

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


    public String resolveWidgetPackageName(String widget) {
        if (widget.equals("MapWayPointItem")) {
            return "de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle";
        } else if (widget.equals("Sizes")) {
            return "de.longri.cachebox3.gui.skin.styles.ScaledSize";
        } else {
            return "com.badlogic.gdx.scenes.scene2d.ui." + widget + "$" + widget + "Style";
        }
    }


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
