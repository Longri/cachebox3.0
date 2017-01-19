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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import de.longri.cachebox3.utils.SkinColor;

import java.io.StringWriter;
import java.util.Iterator;

/**
 * Created by Longri on 12.01.2017.
 */
public class SaveableSvgSkin extends SvgSkin {

    public SaveableSvgSkin(String name) {
        super(name);
    }

    public SaveableSvgSkin(String name, StorageType storageType, FileHandle skinFolder) {
        super(name, storageType, skinFolder);
    }

    /**
     * Retrieve the textual name of an object
     */
    public String resolveObjectName(Class<?> classType, Object object) {

        if (resources.get(classType) == null) {
            return null;
        }

        Iterator<String> keys = resources.get(classType).keys();
        while (keys.hasNext()) {

            String key = keys.next();
            Object obj = resources.get(classType).get(key);

            if (obj.equals(object)) {
                return key;
            }

        }
        return null;
    }

    /**
     * Store all resources in the specified skin JSON file.
     */
    public boolean save(FileHandle skinFile) {

        StringWriter jsonText = new StringWriter();
        JsonWriter writer = new JsonWriter(jsonText);

        Json json = new Json();

        json.setOutputType(JsonWriter.OutputType.json);
        json.setWriter(writer);

        json.writeObjectStart();


        // Sort items
        Array<Class> items = new Array<Class>();


        //items for cachebox 3.0 skin
        items.add(com.badlogic.gdx.scenes.scene2d.ui.ScaledSvg.class);
        items.add(de.longri.cachebox3.utils.ScaledSizes.class);
        items.add(de.longri.cachebox3.utils.SkinColor.class);
        items.add(de.longri.cachebox3.gui.widgets.ColorDrawable.ColorDrawableStyle.class);
        items.add(com.badlogic.gdx.graphics.g2d.BitmapFont.class);
        items.add(com.badlogic.gdx.scenes.scene2d.ui.SvgNinePatchDrawable.class);
        items.add(com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle.class);
        items.add(com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle.class);
        items.add(de.longri.cachebox3.gui.widgets.ButtonBar.ButtonBarStyle.class);
        items.add(de.longri.cachebox3.gui.widgets.GestureButton.GestureButtonStyle.class);
        items.add(com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle.class);
        items.add(com.kotcrab.vis.ui.widget.VisTextButton.VisTextButtonStyle.class);
        items.add(de.longri.cachebox3.gui.dialogs.ButtonDialog.ButtonDialogStyle.class);
        items.add(com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle.class);
        items.add(de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle.class);
        items.add(de.longri.cachebox3.gui.menu.Menu.MenuStyle.class);
        items.add(de.longri.cachebox3.gui.menu.MenuItem.MenuItemStyle.class);
        items.add(de.longri.cachebox3.gui.help.HelpWindow.HelpWindowStyle.class);
        items.add(de.longri.cachebox3.gui.help.GestureHelp.GestureHelpStyle.class);
        items.add(de.longri.cachebox3.gui.ActivityBase.ActivityBaseStyle.class);
        items.add(de.longri.cachebox3.gui.activities.Settings_Activity.SettingsActivityStyle.class);
        items.add(com.kotcrab.vis.ui.widget.VisTextField.VisTextFieldStyle.class);
        items.add(de.longri.cachebox3.gui.activities.SelectDB_Activity.SelectDbStyle.class);
        items.add(com.kotcrab.vis.ui.widget.VisCheckBox.VisCheckBoxStyle.class);
        items.add(de.longri.cachebox3.gui.views.CacheListItem.CacheListItemStyle.class);
        items.add(de.longri.cachebox3.gui.widgets.Slider.SliderStyle.class);
        items.add(de.longri.cachebox3.gui.widgets.QuickButtonList.QuickButtonListStyle.class);
        items.add(de.longri.cachebox3.gui.widgets.MapStateButton.MapStateButtonStyle.class);
        items.add(de.longri.cachebox3.gui.widgets.ZoomButton.ZoomButtonStyle.class);
        items.add(de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle.class);


        for (Class<?> item : items) {

            String name = item.getName();
            json.writeObjectStart(name);
            ObjectMap<String, Object> typeResources = resources.get(item);


            // Build a temporary array for string keys to prevent nested
            // iterators with getObjetName function.

            Array<String> styles = new Array<String>();
            Iterator itStyles = typeResources.keys().iterator();
            while (itStyles.hasNext()) {
                String style = (String) itStyles.next();
                styles.add(style);
            }

            for (String style : styles) {
                Object object = typeResources.get(style);

                json.writeObjectStart(style);
                Field[] fields = ClassReflection.getFields(object.getClass());


                // Handle SvgNinePatchDrawable
                if (object instanceof SvgNinePatchDrawable) {
                    SvgNinePatchDrawable svgNinePatchDrawable = (SvgNinePatchDrawable) object;
                    SvgNinePatchDrawable.SvgNinePatchDrawableUnScaledValues values = svgNinePatchDrawable.values;
                    json.writeValue("name", svgNinePatchDrawable.name);
                    json.writeValue("left", values.left);
                    json.writeValue("right", values.right);
                    json.writeValue("top", values.top);
                    json.writeValue("bottom", values.bottom);

                    if (values.leftWidth != 0) json.writeValue("leftWidth", values.leftWidth);
                    if (values.rightWidth != 0) json.writeValue("rightWidth", values.rightWidth);
                    if (values.topHeight != 0) json.writeValue("topHeight", values.topHeight);
                    if (values.bottomHeight != 0) json.writeValue("bottomHeight", values.bottomHeight);
                    json.writeObjectEnd();
                    continue;

                }

                for (Field field : fields) {

                    if (object instanceof SkinColor) {
                        if (field.getName().equals("skinName")) {
                            continue;
                        }
                    }


                    try {

                        Object valueObject = field.get(typeResources.get(style));
                        if (valueObject != null|| valueObject instanceof GetName) {
                            if (valueObject instanceof BitmapFont) {

                                String value = resolveObjectName(BitmapFont.class, valueObject);
                                if (value != null) {
                                    json.writeValue(field.getName(), value);
                                }

                            } else if (valueObject instanceof Float) {
                                if ((Float) valueObject != 0.0f) {
                                    json.writeValue(field.getName(), valueObject);
                                }
                            } else if (valueObject instanceof Integer) {
                                json.writeValue(field.getName(), valueObject);
                            } else if (valueObject instanceof SkinColor) {
                                json.writeValue(field.getName(), ((SkinColor) valueObject).skinName);
                            } else if (valueObject instanceof Color) {
                                if (typeResources.get(style) instanceof Color) {
                                    // Skip sub-color
                                } else {
                                    if (valueObject.equals(field.get(ClassReflection.newInstance(typeResources.get(style).getClass())))) {
                                        // skip if default value
                                    } else {
                                        json.writeValue(field.getName(), valueObject);
                                    }
                                }
                            } else if (valueObject instanceof Drawable) {
                                if (typeResources.get(style) instanceof SaveableSvgSkin.TintedDrawable) {
                                    // Skip drawable if it is from tinted drawable
                                } else {
                                    String value = null;
                                    value = resolveObjectName(Drawable.class, valueObject);
//
                                    if (value != null) {
                                        json.writeValue(field.getName(), value);
                                    }
                                }
                            } else if (valueObject instanceof List.ListStyle) {
                                String value = resolveObjectName(List.ListStyle.class, valueObject);
                                if (value != null) {
                                    json.writeValue(field.getName(), value);
                                }

                            } else if (valueObject instanceof ScrollPane.ScrollPaneStyle) {
                                String value = resolveObjectName(ScrollPane.ScrollPaneStyle.class, valueObject);
                                if (value != null) {
                                    json.writeValue(field.getName(), value);
                                }

                            } else if (valueObject instanceof String) {
                                // only used to get original drawable for tinted drawable
                                json.writeValue(field.getName(), valueObject);

                            } else if (valueObject instanceof FileHandle) {
                                json.writeValue(field.getName(), valueObject.toString());

                            } else if (valueObject instanceof char[]) {
                                // Don't store.
                            } else if (valueObject instanceof float[]) {
                                // Don't store.
                            } else if (valueObject instanceof GetName) {
                                json.writeValue(field.getName(), ((GetName) valueObject).getName());
                            } else {
                                throw new IllegalArgumentException("resource object type is unknown: " + valueObject.getClass().getCanonicalName());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                json.writeObjectEnd();

            }

            json.writeObjectEnd();
        }
        json.writeObjectEnd();

        JsonValue.PrettyPrintSettings settings = new JsonValue.PrettyPrintSettings();
        settings.outputType = JsonWriter.OutputType.json;
        settings.singleLineColumns = 100;
        skinFile.writeString(json.prettyPrint(jsonText.toString(), settings), false);

        return true;
    }
}
