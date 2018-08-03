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
package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import com.badlogic.gdx.scenes.scene2d.ui.ScaledSvg;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;

/**
 * Created by Longri on 24.01.17.
 */
public class Validate_UnusedSvgFiles extends ValidationTask {

    public Validate_UnusedSvgFiles(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage);
    }

    @Override
    public String getName() {
        return "Check for unused *.svg files";
    }

    @Override
    public void runValidation() {

        FileHandle svgFolder = validationSkin.skinFolder.child("svg");
        FileHandle[] files = svgFolder.list();

        ObjectMap<java.lang.String, ScaledSvg> registed = validationSkin.getAll(ScaledSvg.class);

        Array<String> neadedList = new Array<String>();
        for (ScaledSvg scaledSvg : registed.values()) {
            neadedList.add(scaledSvg.path);
        }

        Array<String> svgList = new Array<String>();
        for (FileHandle file : files) {
            svgList.add("svg/" + file.name());
        }

        for (String test : neadedList) {
            svgList.removeValue(test, true);
        }

        StringBuilder warnMassageBuilder = new StringBuilder();
        if (svgList.size > 0) {
            warnMassageBuilder.append("Unused *.svg files : \n\n");
            for (String unusedFile : svgList) {
                warnMassageBuilder.append(unusedFile);
                warnMassageBuilder.append("\n");
            }
            warnMsg = warnMassageBuilder.toString();
        }

    }
}
