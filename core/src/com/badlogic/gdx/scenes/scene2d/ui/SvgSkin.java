/*
 * Copyright (C) 2016 team-cachebox.de
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
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.views.ListView;
import de.longri.cachebox3.gui.widgets.ColorDrawable;
import de.longri.cachebox3.utils.ScaledSizes;
import org.oscim.backend.CanvasAdapter;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Longri on 20.07.2016.
 */
public class SvgSkin extends Skin {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(SvgSkin.class);

    public final static String TMP_UI_ATLAS = "/user/temp/ui_tmp.atlas";

    /**
     * Create a Skin from given Jason-file!
     * The drawable resources are created from Svg-Folder and putted into a Atlas
     *
     * @param svgFolder
     * @param json
     */
    public SvgSkin(FileHandle svgFolder, FileHandle json) {
        //  this.addRegions(createTextureAtlasFromImages(svgFolder));
        this.load(json);
    }


    public static TextureAtlas createTextureAtlasFromImages(ArrayList<ScaledSvg> scaledSvgList) {

        FileHandle cachedTexturatlasFileHandle = Gdx.files.absolute(CB.WorkPath + TMP_UI_ATLAS);
        if (cachedTexturatlasFileHandle.exists()) {
            if (HashAtlasWriter.hashEquals(scaledSvgList)) {
                log.debug("Load cached TextureAtlas");
                return new TextureAtlas(cachedTexturatlasFileHandle);
            }
        }

        log.debug("Create new TextureAtlas");

        // max texture size are 2048x2048
        int pageWidth = 2048;
        int pageHeight = 2048;
        int padding = 2;
        boolean duplicateBorder = false;

        PixmapPacker packer = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, padding, duplicateBorder);


        final int prime = 31;
        int resultHashCode = 1;
        // resultHashCode is the hashcode.
        for (ScaledSvg scaledSvg : scaledSvgList) {

            Pixmap pixmap = null;
            String name = null;

            FileHandle fileHandle = Gdx.files.internal(scaledSvg.name);

            try {
                resultHashCode = resultHashCode * prime + Utils.getMd5(fileHandle).hashCode();
                resultHashCode = (resultHashCode * (int) (prime * scaledSvg.scale));
                pixmap = Utils.getPixmapFromBitmap(PlatformConnector.getSvg(fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale));
                name = fileHandle.nameWithoutExtension();
            } catch (IOException e) {
                e.printStackTrace();
            }

            log.debug("Pack Svg: " + name + " Size:" + pixmap.getWidth() + "/" + pixmap.getHeight());

            if (pixmap != null) packer.pack(name, pixmap);

        }

        // add one pixel color for colorDrawable
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        packer.pack("color", pixmap);

        TextureAtlas atlas = packer.generateTextureAtlas(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.MipMapNearestNearest, true);
        PixmapPackerIO pixmapPackerIO = new PixmapPackerIO();

        PixmapPackerIO.SaveParameters parameters = new PixmapPackerIO.SaveParameters();
        parameters.magFilter = Texture.TextureFilter.MipMapNearestNearest;
        parameters.minFilter = Texture.TextureFilter.MipMapNearestNearest;

        try {
            HashAtlasWriter.save(resultHashCode, cachedTexturatlasFileHandle, packer, parameters);
        } catch (IOException e) {
            e.printStackTrace();
        }


        packer.dispose();
        pixmap.dispose();
        return atlas;
    }

    @Override
    public void add(String name, Object resource, Class type) {
        if (name == null) throw new IllegalArgumentException("name cannot be null.");
        if (resource == null) throw new IllegalArgumentException("resource cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        if (typeResources == null) {
            typeResources = new ObjectMap(type == ColorDrawable.class || type == TextureRegion.class || type == Drawable.class || type == Sprite.class ? 256 : 64);
            resources.put(type, typeResources);
        }
        typeResources.put(name, resource);
    }

    /**
     * Returns a registered drawable. If no drawable is found but a region, ninepatch, or sprite exists with the name, then the
     * appropriate drawable is created and stored in the skin.
     */
    @Override
    public Drawable getDrawable(String name) {
        Drawable drawable = optional(name, Drawable.class);
        if (drawable != null) return drawable;

        // Use texture or texture region. If it has splits, use ninepatch. If it has rotation or whitespace stripping, use sprite.
        try {
            TextureRegion textureRegion = getRegion(name);
            if (textureRegion instanceof TextureAtlas.AtlasRegion) {
                TextureAtlas.AtlasRegion region = (TextureAtlas.AtlasRegion) textureRegion;
                if (region.splits != null)
                    drawable = new NinePatchDrawable(getPatch(name));
                else if (region.rotate || region.packedWidth != region.originalWidth || region.packedHeight != region.originalHeight)
                    drawable = new SpriteDrawable(getSprite(name));
            }
            if (drawable == null) drawable = new TextureRegionDrawable(textureRegion);
        } catch (GdxRuntimeException ignored) {
        }

        // Check for explicit registration of ninepatch, sprite, or tiled drawable.
        if (drawable == null) {
            NinePatch patch = optional(name, NinePatch.class);
            if (patch != null)
                drawable = new NinePatchDrawable(patch);
            else {
                Sprite sprite = optional(name, Sprite.class);
                if (sprite != null)
                    drawable = new SpriteDrawable(sprite);
                else {
                    ColorDrawable.ColorDrawableStyle colorDrawableStyle = optional(name, ColorDrawable.ColorDrawableStyle.class);
                    if (colorDrawableStyle != null) {
                        drawable = new ColorDrawable(colorDrawableStyle);
                    } else
                        throw new GdxRuntimeException(
                                "No Drawable, NinePatch, TextureRegion, Texture, or Sprite registered with name: " + name);
                }
            }
        }

        if (drawable instanceof BaseDrawable) ((BaseDrawable) drawable).setName(name);

        add(name, drawable, Drawable.class);
        return drawable;
    }

    protected Json getJsonLoader(final FileHandle skinFile) {
        final Skin skin = this;

        final Json json = new Json() {
            public <T> T readValue(Class<T> type, Class elementType, JsonValue jsonData) {
                // If the JSON is a string but the type is not, look up the actual value by name.
                if (jsonData.isString() && !ClassReflection.isAssignableFrom(CharSequence.class, type))
                    return get(jsonData.asString(), type);
                return super.readValue(type, elementType, jsonData);
            }
        };
        json.setTypeName(null);
        json.setUsePrototypes(false);

        json.setSerializer(Skin.class, new Json.ReadOnlySerializer<Skin>() {
            public Skin read(Json json, JsonValue typeToValueMap, Class ignored) {
                for (JsonValue valueMap = typeToValueMap.child; valueMap != null; valueMap = valueMap.next) {
                    try {

                        if (valueMap.name().equals(ScaledSvg.class.getName())) {
                            log.debug("read scaled SVG'S");
                            readScaledSvgs(json, ClassReflection.forName(valueMap.name()), valueMap);
                        } else {
                            readNamedObjects(json, ClassReflection.forName(valueMap.name()), valueMap);
                        }
                    } catch (ReflectionException ex) {
                        throw new SerializationException(ex);
                    }
                }
                return skin;
            }

            private void readScaledSvgs(Json json, Class type, JsonValue valueMap) {
                ArrayList<ScaledSvg> resistedSvgs = new ArrayList<ScaledSvg>();
                for (JsonValue valueEntry = valueMap.child; valueEntry != null; valueEntry = valueEntry.next) {
                    ScaledSvg object = (ScaledSvg) json.readValue(type, valueEntry);
                    resistedSvgs.add(object);
                }

                //create and register atlas
                SvgSkin.this.addRegions(createTextureAtlasFromImages(resistedSvgs));
            }


            private void readNamedObjects(Json json, Class type, JsonValue valueMap) {
                Class addType = type == TintedDrawable.class ? Drawable.class : type;
                for (JsonValue valueEntry = valueMap.child; valueEntry != null; valueEntry = valueEntry.next) {
                    Object object = json.readValue(type, valueEntry);
                    if (object == null) continue;
                    try {
                        add(valueEntry.name, object, addType);
                        if (addType != Drawable.class && ClassReflection.isAssignableFrom(Drawable.class, addType))
                            add(valueEntry.name, object, Drawable.class);
                    } catch (Exception ex) {
                        throw new SerializationException(
                                "Error reading " + ClassReflection.getSimpleName(type) + ": " + valueEntry.name, ex);
                    }
                }
            }
        });

        json.setSerializer(ColorDrawable.class, new Json.ReadOnlySerializer<ColorDrawable>() {
            public ColorDrawable read(Json json, JsonValue jsonData, Class type) {
                Color color = json.readValue("color", Color.class, jsonData);
                ColorDrawable drawable = new ColorDrawable(color);
                return drawable;
            }
        });

        json.setSerializer(ListView.ListViewStyle.class, new Json.ReadOnlySerializer<ListView.ListViewStyle>() {
            public ListView.ListViewStyle read(Json json, JsonValue jsonData, Class type) {
                ListView.ListViewStyle style = new ListView.ListViewStyle();

                String background, firstItem, secondItem, selectedItem;

                background = json.readValue("background", String.class, jsonData);
                firstItem = json.readValue("firstItem", String.class, jsonData);
                secondItem = json.readValue("secondItem", String.class, jsonData);
                selectedItem = json.readValue("selectedItem", String.class, jsonData);

                style.background = getDrawable(background);
                style.firstItem = getDrawable(firstItem);
                style.secondItem = getDrawable(secondItem);
                style.selectedItem = getDrawable(selectedItem);

                float pad, padLeft, padRight, padTop, padBottom;
                pad = json.readValue("pad", float.class, 0f, jsonData);
                padLeft = json.readValue("padLeft", float.class, 0f, jsonData);
                padRight = json.readValue("padRight", float.class, 0f, jsonData);
                padTop = json.readValue("padTop", float.class, 0f, jsonData);
                padBottom = json.readValue("padBottom", float.class, 0f, jsonData);

                //scale values
                style.pad = CB.getScaledFloat(pad);
                style.padLeft = CB.getScaledFloat(padLeft);
                style.padRight = CB.getScaledFloat(padRight);
                style.padTop = CB.getScaledFloat(padTop);
                style.padBottom = CB.getScaledFloat(padBottom);
                return style;
            }
        });

        json.setSerializer(SvgNinePatchDrawable.class, new Json.ReadOnlySerializer<SvgNinePatchDrawable>() {
            public SvgNinePatchDrawable read(Json json, JsonValue jsonData, Class type) {

                String name = json.readValue("name", String.class, jsonData);
                int left = json.readValue("left", int.class, 0, jsonData);
                int right = json.readValue("right", int.class, 0, jsonData);
                int top = json.readValue("top", int.class, 0, jsonData);
                int bottom = json.readValue("bottom", int.class, 0, jsonData);
                int leftWidth = json.readValue("leftWidth", int.class, 0, jsonData);
                int rightWidth = json.readValue("rightWidth", int.class, 0, jsonData);
                int topHeight = json.readValue("topHeight", int.class, 0, jsonData);
                int bottomHeight = json.readValue("bottomHeight", int.class, 0, jsonData);

                // get texture region
                TextureRegion textureRegion = getRegion(name);

                //scale nine patch regions
                left = CB.getScaledInt(left);
                right = CB.getScaledInt(right);
                top = CB.getScaledInt(top);
                bottom = CB.getScaledInt(bottom);
                leftWidth = leftWidth == 0 ? left : CB.getScaledInt(leftWidth);
                rightWidth = rightWidth == 0 ? right : CB.getScaledInt(rightWidth);
                topHeight = topHeight == 0 ? top : CB.getScaledInt(topHeight);
                bottomHeight = bottomHeight == 0 ? bottom : CB.getScaledInt(bottomHeight);

                return new SvgNinePatchDrawable(new NinePatch(textureRegion, left, right, top, bottom),
                        leftWidth, rightWidth, topHeight, bottomHeight);
            }
        });


        json.setSerializer(BitmapFont.class, new Json.ReadOnlySerializer<BitmapFont>() {
            public BitmapFont read(Json json, JsonValue jsonData, Class type) {
                String path = json.readValue("font", String.class, jsonData) + ".ttf";
                int scaledSize = json.readValue("size", int.class, -1, jsonData);

                FileHandle fontFile = skinFile.parent().child(path);
                if (!fontFile.exists()) fontFile = Gdx.files.internal(path);
                if (!fontFile.exists()) throw new SerializationException("Font file not found: " + fontFile);

                try {
                    SkinFont font = new SkinFont(fontFile, scaledSize);
                    return font;
                } catch (RuntimeException ex) {
                    throw new SerializationException("Error loading bitmap font: " + fontFile, ex);
                }
            }
        });

        json.setSerializer(Color.class, new Json.ReadOnlySerializer<Color>() {
            public Color read(Json json, JsonValue jsonData, Class type) {
                if (jsonData.isString()) return get(jsonData.asString(), Color.class);
                String hex = json.readValue("hex", String.class, (String) null, jsonData);
                if (hex != null) return Color.valueOf(hex);
                float r = json.readValue("r", float.class, 0f, jsonData);
                float g = json.readValue("g", float.class, 0f, jsonData);
                float b = json.readValue("b", float.class, 0f, jsonData);
                float a = json.readValue("a", float.class, 1f, jsonData);
                return new Color(r, g, b, a);
            }
        });

        json.setSerializer(TintedDrawable.class, new Json.ReadOnlySerializer() {
            public Object read(Json json, JsonValue jsonData, Class type) {
                String name = json.readValue("name", String.class, jsonData);
                Color color = json.readValue("color", Color.class, jsonData);
                Drawable drawable = newDrawable(name, color);
                if (drawable instanceof BaseDrawable) {
                    BaseDrawable named = (BaseDrawable) drawable;
                    named.setName(jsonData.name + " (" + name + ", " + color + ")");
                }
                return drawable;
            }
        });

        json.setSerializer(ColorDrawable.class, new Json.ReadOnlySerializer<ColorDrawable>() {
            public ColorDrawable read(Json json, JsonValue jsonData, Class type) {
                Color color = json.readValue("color", Color.class, jsonData);
                ColorDrawable drawable = new ColorDrawable(color);
                return drawable;
            }
        });

        json.setSerializer(ScaledSizes.class, new Json.ReadOnlySerializer<ScaledSizes>() {
            public ScaledSizes read(Json json, JsonValue jsonData, Class type) {
                float button_width = CB.getScaledFloat(json.readValue("button_width", float.class, jsonData));
                float button_height = CB.getScaledFloat(json.readValue("button_height", float.class, jsonData));
                float button_width_wide = CB.getScaledFloat(json.readValue("button_width_wide", float.class, jsonData));
                float margin = CB.getScaledFloat(json.readValue("margin", float.class, jsonData));
                float check_box_height = CB.getScaledFloat(json.readValue("check_box_height", float.class, jsonData));
                float window_margin = CB.getScaledFloat(json.readValue("check_box_height", float.class, jsonData));
                ScaledSizes scaledSizes = new ScaledSizes(button_width, button_height, button_width_wide, margin,
                        check_box_height, window_margin);

                //set also to static access on CB
                CB.scaledSizes = scaledSizes;

                return scaledSizes;
            }
        });

        return json;
    }


}
