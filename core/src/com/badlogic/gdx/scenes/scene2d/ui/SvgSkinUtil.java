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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.skin.styles.CacheTypeStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.utils.SkinColor;
import org.oscim.backend.canvas.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Longri on 11.01.17.
 */
public class SvgSkinUtil {

    private SvgSkinUtil() {
    }

    private final static Logger log = LoggerFactory.getLogger(SvgSkinUtil.class);
    public final static String TMP_UI_ATLAS_PATH = "/user/temp/";
    public final static String TMP_UI_ATLAS = "_ui_tmp.atlas";

    public static TextureAtlas createTextureAtlasFromImages(boolean forceNew, String skinName, ArrayList<ScaledSvg> scaledSvgList,
                                                            FileHandle skinFile) {
        FileHandle cachedTexturatlasFileHandle = null;
        if (!forceNew) {
            cachedTexturatlasFileHandle = Gdx.files.absolute(CB.WorkPath + TMP_UI_ATLAS_PATH + skinName + TMP_UI_ATLAS);
            if (cachedTexturatlasFileHandle.exists()) {
                if (HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, scaledSvgList, skinFile)) {
                    log.debug("Load cached TextureAtlas");
                    return new TextureAtlas(cachedTexturatlasFileHandle);
                }
            }
        }
        log.debug("Create new TextureAtlas");

        // max texture size are 2048x2048
        int pageWidth = 2048;
        int pageHeight = 2048;
        int padding = 4;
        boolean duplicateBorder = true;

        PixmapPacker packer = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, padding, duplicateBorder);


        for (ScaledSvg scaledSvg : scaledSvgList) {

            Pixmap pixmap = null;
            String name = null;
//            skinFile.parent().child(scaledSvg.path);
            FileHandle fileHandle = skinFile.parent().child(scaledSvg.path);

            try {
                name = scaledSvg.getRegisterName();
                pixmap = Utils.getPixmapFromBitmap(PlatformConnector.getSvg(name, fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale));

            } catch (IOException e) {
                e.printStackTrace();
            }

            log.debug("Pack Svg: " + name + " Size:" + pixmap.getWidth() + "/" + pixmap.getHeight());

            if (pixmap != null) {
                packer.pack(name, pixmap);
            }

        }

        // add one pixel color for colorDrawable
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        packer.pack("color", pixmap);

        TextureAtlas atlas = packer.generateTextureAtlas(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.MipMapNearestNearest, true);

        PixmapPackerIO.SaveParameters parameters = new PixmapPackerIO.SaveParameters();
        parameters.magFilter = Texture.TextureFilter.MipMapNearestNearest;
        parameters.minFilter = Texture.TextureFilter.MipMapNearestNearest;

        int resultHashCode = HashAtlasWriter.getResultHashCode(scaledSvgList, skinFile);

        if (cachedTexturatlasFileHandle != null) {
            try {
                HashAtlasWriter.save(resultHashCode, cachedTexturatlasFileHandle, packer, parameters);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //write hash file
        if (cachedTexturatlasFileHandle != null) {
            try {
                FileHandle hashFile = cachedTexturatlasFileHandle.sibling(cachedTexturatlasFileHandle.nameWithoutExtension() + ".hash");
                Writer hashwriter = hashFile.writer(false);
                hashwriter.write("hash: " + resultHashCode + "\n");
                hashwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        packer.dispose();
        pixmap.dispose();
        return atlas;
    }


    public static void saveSkin(SvgSkin skin, Array<Class> items, FileHandle fileHandle) {

        StringWriter jsonText = new StringWriter();
        JsonWriter writer = new JsonWriter(jsonText);

        Json json = new Json();

        json.setOutputType(JsonWriter.OutputType.json);
        json.setWriter(writer);

        json.writeObjectStart();

        for (Class<?> item : items) {
            ObjectMap<String, Object> typeResources = skin.resources.get(item);

            if (item == BitmapFont.class) {
                ObjectMap<String, Object> addTypeResources = skin.resources.get(SkinFont.class);
                if (addTypeResources != null) typeResources.putAll(addTypeResources);
            }

            if (typeResources == null) continue;

            String name = item.getName();
            json.writeObjectStart(name);

            // Build a temporary array for string keys to prevent nested
            // iterators with getObjetName function.

            Array<String> styles = new Array<String>();
            Iterator itStyles = typeResources.keys().iterator();
            while (itStyles.hasNext()) {
                String style = (String) itStyles.next();
                styles.add(style);
            }

            styles.sort();
            for (String style : styles) {
                Object object = typeResources.get(style);

                if (!item.equals(BitmapFont.class)) {
                    if (!item.equals(object.getClass())) continue;
                }

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

                    if (field.isStatic()) continue;

                    try {

                        Object valueObject = field.get(typeResources.get(style));
                        if (valueObject != null || valueObject instanceof GetName) {
                            if (valueObject instanceof BitmapFont) {

                                String value = resolveObjectName(skin, BitmapFont.class, valueObject);
                                if (value != null) {
                                    json.writeValue(field.getName(), value);
                                }

                            } else if (valueObject instanceof Boolean) {
                                if (valueObject.equals(field.get(ClassReflection.newInstance(typeResources.get(style).getClass())))) {
                                    // skip if default value
                                } else {
                                    json.writeValue(field.getName(), valueObject);
                                }
                            } else if (valueObject instanceof Float) {
                                if ((Float) valueObject != 0.0f) {
                                    json.writeValue(field.getName(), valueObject);
                                }
                            } else if (valueObject instanceof Paint.Cap) {
                                if (valueObject.equals(field.get(ClassReflection.newInstance(typeResources.get(style).getClass())))) {
                                    // skip if default value
                                } else {
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
                                if (typeResources.get(style) instanceof SvgSkin.TintedDrawable) {
                                    // Skip drawable if it is from tinted drawable
                                } else {
                                    String value = null;
                                    value = resolveObjectName(skin, Drawable.class, valueObject);
//
                                    if (value != null) {
                                        json.writeValue(field.getName(), value);
                                    }
                                }
                            } else if (valueObject instanceof List.ListStyle) {
                                String value = resolveObjectName(skin, List.ListStyle.class, valueObject);
                                if (value != null) {
                                    json.writeValue(field.getName(), value);
                                }

                            } else if (valueObject instanceof ScrollPane.ScrollPaneStyle) {
                                String value = resolveObjectName(skin, ScrollPane.ScrollPaneStyle.class, valueObject);
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
                            } else if (valueObject instanceof CacheTypeStyle) {
                                String objName = resolveObjectName(skin, CacheTypeStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
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
        settings.singleLineColumns = 1; // wrap all
        fileHandle.writeString(json.prettyPrint(jsonText.toString(), settings), false);
    }

    /**
     * Retrieve the textual name of an object
     */
    public static String resolveObjectName(Skin skin, Class<?> classType, Object object) {

        if (skin.resources.get(classType) == null) {
            return null;
        }

        Iterator<String> keys = skin.resources.get(classType).keys();
        while (keys.hasNext()) {

            String key = keys.next();
            Object obj = skin.resources.get(classType).get(key);

            if (obj.equals(object)) {
                return key;
            }

        }
        return null;
    }

}
