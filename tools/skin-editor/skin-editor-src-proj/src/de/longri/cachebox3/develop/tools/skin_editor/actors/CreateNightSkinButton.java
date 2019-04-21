/*
 * Copyright (C) 2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.develop.tools.skin_editor.actors;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.utils.HSV_Color;
import de.longri.cachebox3.utils.SkinColor;

/**
 * Created by Longri on 28.02.2017.
 */
public class CreateNightSkinButton extends TextButton {

    private final SkinEditorGame game;
    private FileHandle projectFolder;
    private SavableSvgSkin newSkin;
    private String projectName;

    public CreateNightSkinButton(final SkinEditorGame game, Skin skin) {
        super("Create Night Skin", skin);
        this.game = game;

        this.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                askForName();
            }

        });
    }

    private void askForName() {
        final TextField nameTextField = new TextField("???", game.skin);
        Dialog dlg0 = new Dialog("name your night skin project", game.skin) {

            @Override
            protected void result(Object object) {
                if ((Boolean) object == false) {
                    return;
                }
                projectName = nameTextField.getText();
                if (projectName.isEmpty() || projectName.equals("???")) return;
                create(projectName);
            }
        };
        dlg0.pad(20);
        dlg0.getContentTable().add("name:");
        dlg0.getContentTable().add(nameTextField).pad(20);
        dlg0.button("OK", true);
        dlg0.button("Cancel", false);
        dlg0.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dlg0.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
        dlg0.show(getStage());
    }

    private void create(String name) {
        //create Folder
        projectFolder = game.skinProject.skinFolder.parent().child(name);
        if (projectFolder.exists()) {
            projectFolder.deleteDirectory();
        }
        projectFolder.mkdirs();
        this.newSkin = game.skinProject.clone(name);

        //copy font folder
        FileHandle fontFolder = game.skinProject.skinFolder.child("fonts");
        FileHandle newFontFolder = projectFolder.child("fonts");
        fontFolder.copyTo(newFontFolder);

        convertAllColorResources();
        convertAllSvgResources();

        //save skin
        FileHandle skinFile = projectFolder.child(SvgSkin.SKIN_JSON_NAME);
        this.newSkin.save(skinFile);

        game.screenMain.setCurrentProject(this.projectName);
        game.setScreen(game.screenMain);
    }

    private void convertAllColorResources() {
        ObjectMap<String, SkinColor> resourceColors = this.newSkin.getAll(SkinColor.class);

        ObjectMap<String, SkinColor> newColors = new ObjectMap<String, SkinColor>();

        for (ObjectMap.Entry<String, SkinColor> entry : resourceColors) {
            String name = entry.key;
            SkinColor color = entry.value;
            SkinColor nightColor = convertColor(color);
            newColors.put(name, nightColor);
        }

        for (ObjectMap.Entry<String, SkinColor> entry : newColors) {
            String name = entry.key;
            SkinColor color = entry.value;
            this.newSkin.add(name, color, SkinColor.class);
        }
    }

    private void convertAllSvgResources() {
        FileHandle sourceSvgFolder = game.skinProject.skinFolder.child("svg");
        FileHandle targetSvgFolder = projectFolder.child("svg");

        FileHandle[] sourceSvgFiles = sourceSvgFolder.list();

        for (FileHandle svgFile : sourceSvgFiles) {
            String svgSourceString = svgFile.readString();
            String convertedSvgString = convertSvg(svgSourceString);
            targetSvgFolder.child(svgFile.name()).writeString(convertedSvgString, false);
        }

    }

    private SkinColor convertColor(Color color) {
        SkinColor cc = new SkinColor(SkinColor.colorMatrixManipulation(color, SkinColor.GRAYSCALE_COLOR_MATRIX));
        cc.setHue(1f);
        cc.setSat(1f);
        cc.setVal(cc.getVal() * 0.5f);
        return cc;
    }

    private String convertSvg(String svgSourceString) {

        int pos = 0;
        int length = svgSourceString.length();

        while (pos < length) {
            pos = svgSourceString.indexOf("\"#", pos) + 2;
            if (pos == 1) break;
            int endPos = svgSourceString.indexOf("\"", pos);
            String colorString = svgSourceString.substring(pos, endPos);
            try {
                HSV_Color hsv_color = new HSV_Color(colorString);
                SkinColor convertedColor = convertColor(hsv_color);
                String newColorhexString = convertedColor.toString();
                if (colorString.length() == 6) {
                    // without alpha
                    newColorhexString = newColorhexString.substring(0, 6);
                }
                svgSourceString = svgSourceString.substring(0, pos) + newColorhexString + svgSourceString.substring(endPos);
            } catch (Exception e) {
                // no hex color value
            }
            pos = endPos;
        }


        return svgSourceString;
    }
}
