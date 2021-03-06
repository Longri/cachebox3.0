/*
 * Copyright (C) 2020 team-cachebox.de
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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;

/**
 * Created by Longri on 28.01.17.
 */
public abstract class Validate_Abstract_Icons<T extends ValidationTask> extends ValidationTask {

    protected final int TOLERANCE = 1;

    private final Array<String> isNullList = new Array<>();
    private final Array<String> isMissingIconList = new Array<>();
    private final StringBuilder wrongBitmapsSize = new StringBuilder();
    private final Class<T> tClass;

    public Validate_Abstract_Icons(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage, Class<T> tClass) {
        super(game, validationSkin, stage);
        this.tClass = tClass;
    }

    @Override
    public String getName() {
        return "Are all icons available";
    }

    @Override
    public void runValidation() {

        errorMsg = "";
        warnMsg = "";


        try {
            Field[] fields = ClassReflection.getFields(tClass);

            ObjectMap<String, T> allStyles =  validationSkin.getAll(tClass);

            for (Object instance : allStyles.values()) {

                String styleName = allStyles.findKey(instance, false);

                for (Field field : fields) {
                    Object object = field.get(instance);

                    if (object instanceof Integer) {
                        continue;
                    }

                    if (object != null) {
                        TextureRegionDrawable trd = (TextureRegionDrawable) object;
                        String svgName = trd.getName();

                        ScaledSvg scaledSvg = validationSkin.get(svgName, ScaledSvg.class);
                        if (scaledSvg.path.toLowerCase().contains("missingicon")) {
                            isMissingIconList.add(styleName + field.getName());
                        }

                        checkSize(object, styleName, field.getName());


                    } else {
                        isNullList.add(styleName + field.getName());
                    }

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

        if (wrongBitmapsSize.length > 0) {
            warnMsg += "\n\nWrong Sizes:\n\n" + wrongBitmapsSize.toString();
        }
    }

    private void checkSize(Object object, String styleName, String fieldName) {
        int width = 0, height = 0;

        if (object instanceof TextureRegionDrawable) {
            TextureRegionDrawable trd = (TextureRegionDrawable) object;
            width = (int) trd.getMinWidth();
            height = (int) trd.getMinHeight();
        }

        if (width < getMinWidth(styleName) || width > getMaxWidth(styleName)) {
            wrongBitmapsSize.append(styleName)
                    .append(" / ")
                    .append(fieldName)
                    .append(" width: ")
                    .append(String.valueOf(width))
                    .append("   => The width should be between ")
                    .append(String.valueOf(getMinWidth(styleName)))
                    .append(" and ")
                    .append(String.valueOf(getMaxWidth(styleName)))
                    .append("  !\n");
        }

        if (height < getMinHeight(styleName) || height > getMaxHeight(styleName)) {
            wrongBitmapsSize.append(styleName)
                    .append(" / ")
                    .append(fieldName)
                    .append(" height: ")
                    .append(String.valueOf(height))
                    .append("   => The height should be between ")
                    .append(String.valueOf(getMinHeight(styleName)))
                    .append(" and ")
                    .append(String.valueOf(getMaxHeight(styleName)))
                    .append("  !, \n");
        }
    }

    protected abstract int getMinWidth(String styleName);

    protected abstract int getMaxWidth(String styleName);

    protected abstract int getMinHeight(String styleName);

    protected abstract int getMaxHeight(String styleName);
}
