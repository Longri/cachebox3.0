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
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.gui.drawables.*;
import de.longri.cachebox3.gui.skin.styles.*;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.SkinColor;
import de.longri.libPP.PixmapPacker;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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

        log.debug("Used memory before create Atlas: {}", CB.getMemoryUsage());


        FileHandle cachedTexturatlasFileHandle = null;
        if (!forceNew) {
            cachedTexturatlasFileHandle = Gdx.files.absolute(CB.WorkPath + TMP_UI_ATLAS_PATH + skinName + TMP_UI_ATLAS);
            if (cachedTexturatlasFileHandle.exists()) {
                if (HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, scaledSvgList, skinFile)) {
                    log.debug("load Skin | Load cached TextureAtlas");
                    EventHandler.fire(new IncrementProgressEvent(1, "load Skin | Load cached TextureAtlas"));

                    final TextureAtlas[] atlas = new TextureAtlas[1];
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final FileHandle finalCachedTexturatlasFileHandle = cachedTexturatlasFileHandle;
                    CB.postOnGlThread(new NamedRunnable("SvgSkinUtil") {
                        @Override
                        public void run() {
                            try {
                                atlas[0] = new TextureAtlas(finalCachedTexturatlasFileHandle);
                            } catch (Exception e) {
                                log.error("", e);
                            }
                        }
                    }, true);
                    log.debug("Used memory after load Atlas: {}", CB.getMemoryUsage());
                    return atlas[0];
                }
            }
        }
        log.debug("Create new TextureAtlas");
        final int padding = 4;
        final PixmapPacker[] packer = new PixmapPacker[1];
        CB.postOnGlThread(new NamedRunnable("SvgSkinUtil") {
            @Override
            public void run() {
                boolean forcePot = CanvasAdapter.platform == Platform.IOS;
                packer[0] = new PixmapPacker(forcePot, PixmapPacker.getDeviceMaxGlTextureSize(), padding);
            }
        }, true);

        EventHandler.fire(new IncrementProgressEvent(0, "load Skin | Create new TextureAtlas", scaledSvgList.size()));

        FileHandle fileHandle;
        for (ScaledSvg scaledSvg : scaledSvgList) {

            Pixmap pixmap = null;
            String name = null;
            fileHandle = skinFile.parent().child(scaledSvg.path);

            if (!fileHandle.exists()) continue;
            EventHandler.fire(new IncrementProgressEvent(1, "load Skin | Create new TextureAtlas \npack:" + scaledSvg.path));
            InputStream stream = null;
            try {
                name = scaledSvg.getRegisterName();
                stream = fileHandle.read();
                Bitmap svgBitmap = PlatformConnector.getSvg(name, stream, PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale);
                pixmap = Utils.getPixmapFromBitmap(svgBitmap);
                svgBitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                        stream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            log.debug("Pack Svg: " + name + " Size:" + pixmap.getWidth() + "/" + pixmap.getHeight());

            if (pixmap != null) {
                packer[0].pack(name, pixmap);
            }
        }
        fileHandle = null;
        System.gc();

        // add one pixel color for colorDrawable
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        packer[0].pack("color", pixmap);
        EventHandler.fire(new IncrementProgressEvent(20, "load Skin | Create new TextureAtlas \nGenerate Texture Atlas"));

        final TextureAtlas[] atlas = new TextureAtlas[1];
        System.gc();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CB.postOnGlThread(new NamedRunnable("SvgSkinUtil") {
            @Override
            public void run() {
                atlas[0] = packer[0].generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, true);
            }
        }, true);

        System.gc();
        int resultHashCode = HashAtlasWriter.getResultHashCode(scaledSvgList, skinFile);

        if (cachedTexturatlasFileHandle != null) {
            try {
                packer[0].save(cachedTexturatlasFileHandle);
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

        packer[0].dispose();
        packer[0] = null;

        System.gc();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("Used memory after create Atlas: {}", CB.getMemoryUsage());

        return atlas[0];
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
                ObjectMap<String, Object> addTypeResources = skin.resources.get(com.badlogic.gdx.graphics.g2d.freetype.SkinFont.class);
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
                if (object instanceof de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable) {
                    de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable svgNinePatchDrawable = (de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable) object;
                    de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable.SvgNinePatchDrawableUnScaledValues values = svgNinePatchDrawable.values;
                    json.writeValue("name", svgNinePatchDrawable.name);
                    json.writeValue("left", values.left);
                    json.writeValue("right", values.right);
                    json.writeValue("top", values.top);
                    json.writeValue("bottom", values.bottom);

                    if (values.leftWidth >= 0) json.writeValue("leftWidth", values.leftWidth);
                    if (values.rightWidth >= 0) json.writeValue("rightWidth", values.rightWidth);
                    if (values.topHeight >= 0) json.writeValue("topHeight", values.topHeight);
                    if (values.bottomHeight >= 0) json.writeValue("bottomHeight", values.bottomHeight);
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
                            } else if (valueObject.getClass().isEnum()) {
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

                                    if (valueObject instanceof FrameAnimationDrawable) {
                                        FrameAnimationDrawable fad = (FrameAnimationDrawable) valueObject;
                                        FrameAnimationStyle st = fad.getStyle();
                                        value = resolveObjectName(skin, FrameAnimationStyle.class, st);
                                    } else {
                                        value = resolveObjectName(skin, Drawable.class, valueObject);
                                    }

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
                            } else if (valueObject instanceof LogTypesStyle) {
                                String objName = resolveObjectName(skin, LogTypesStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
                            } else if (valueObject instanceof ProgressBar.ProgressBarStyle) {
                                String objName = resolveObjectName(skin, ProgressBar.ProgressBarStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
                            } else if (valueObject instanceof Button.ButtonStyle) {
                                String objName = resolveObjectName(skin, Button.ButtonStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
                            } else if (valueObject instanceof CacheSizeStyle) {
                                String objName = resolveObjectName(skin, CacheSizeStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
                            } else if (valueObject instanceof StarsStyle) {
                                String objName = resolveObjectName(skin, StarsStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
                            } else if (valueObject instanceof CacheListItemStyle) {
                                String objName = resolveObjectName(skin, CacheListItemStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
                            } else if (valueObject instanceof WayPointListItemStyle) {
                                String objName = resolveObjectName(skin, WayPointListItemStyle.class, valueObject);
                                json.writeValue(field.getName(), objName);
                            } else if (valueObject instanceof Array) {
                                String arrayName = field.getName();
                                json.writeArrayStart(arrayName);
                                Array<?> array = (Array<?>) valueObject;
                                for (int i = 0, n = array.size; i < n; i++) {
                                    Object obj = array.get(i);
                                    String objName = resolveObjectName(skin, TextureRegion.class, obj);
                                    json.writeValue(objName);
                                }
                                json.writeArrayEnd();
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

    public static SvgNinePatchDrawable getSvgNinePatchDrawable(int left, int right, int top, int bottom, int leftWidth, int rightWidth, int topHeight, int bottomHeight, TextureRegion textureRegion) {
        //scale nine patch regions
        if (left > 0) left = CB.getScaledInt(left);
        if (right > 0) right = CB.getScaledInt(right);
        if (top > 0) top = CB.getScaledInt(top);
        if (bottom > 0) bottom = CB.getScaledInt(bottom);
        if (leftWidth > 0) leftWidth = CB.getScaledInt(leftWidth);
        if (rightWidth > 0) rightWidth = CB.getScaledInt(rightWidth);
        if (topHeight > 0) topHeight = CB.getScaledInt(topHeight);
        if (bottomHeight > 0) bottomHeight = CB.getScaledInt(bottomHeight);


        // if any value < 0 set to half width or height!
        if (left < 0) left = (textureRegion.getRegionWidth() / 2) - 1;
        if (right < 0) right = (textureRegion.getRegionWidth() / 2) - 1;
        if (top < 0) top = (textureRegion.getRegionHeight() / 2) - 1;
        if (bottom < 0) bottom = (textureRegion.getRegionHeight() / 2) - 1;


        return new SvgNinePatchDrawable(new NinePatch(textureRegion, left, right, top, bottom),
                leftWidth, rightWidth, topHeight, bottomHeight);
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
