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

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import com.badlogic.gdx.scenes.scene2d.ui.ScaledSvg;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.skin.styles.IconsStyle;
import de.longri.cachebox3.utils.exceptions.NullArgumentException;

/**
 * Created by Longri on 28.01.17.
 */
public class Validate_IconsStyle extends ValidationTask {

    private final Array<String> isNullList = new Array<String>();
    private final Array<String> isMissingIconList = new Array<String>();

    public Validate_IconsStyle(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage);
    }

    @Override
    public String getName() {
        return "Are all icons available";
    }

    @Override
    public void runValidation() {

        errorMsg = "";


        try {
            Field[] fields = ClassReflection.getFields(IconsStyle.class);

            for (Field field : fields) {
                Object object = field.get(validationSkin.getIcon);

                if (object != null) {
                    TextureRegionDrawable trd = (TextureRegionDrawable) object;
                    String svgName = trd.getName();

                    ScaledSvg scaledSvg = validationSkin.get(svgName, ScaledSvg.class);
                    if (scaledSvg.path.toLowerCase().contains("missingicon")) {
                        isMissingIconList.add(field.getName());
                    }
                } else {
                    isNullList.add(field.getName());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "Error with validation task";
        }

        if (isNullList.size > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Some Icons are NULL:\n\n");
            for (String name : isNullList) {
                sb.append(name);
                sb.append("\n");
            }

            errorMsg += sb.toString();
        }

        if (isMissingIconList.size > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Some Icons are dummy Icon (\"missingIcon\") :\n\n");
            for (String name : isMissingIconList) {
                sb.append(name);
                sb.append("\n");
            }

            warnMsg = sb.toString();
        }

    }
}
