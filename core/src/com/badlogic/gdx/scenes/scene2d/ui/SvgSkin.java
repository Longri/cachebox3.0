/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.skin.styles.IconsStyle;
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.widgets.ColorDrawable;
import de.longri.cachebox3.utils.SkinColor;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by Longri on 20.07.2016.
 */
public class SvgSkin extends Skin {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(SvgSkin.class);
    public static final String SKIN_JSON_NAME = "skin.json";
    private boolean forceCreateNewAtlas = false;
    public IconsStyle getIcon;
    public MenuIconStyle getMenuIcon;


    public enum StorageType {
        LOCAL, INTERNAL
    }

    public final StorageType storageType;
    public final String name;
    public final FileHandle skinFolder;

    public SvgSkin() {
        super();
        this.storageType = null;
        this.name = null;
        this.skinFolder = null;
    }

    public SvgSkin(String name) {
        super();
        this.storageType = null;
        this.name = name;
        this.skinFolder = null;
    }


    public SvgSkin(FileHandle jsonFile) {
        super(jsonFile);
        this.storageType = null;
        this.name = null;
        this.skinFolder = null;
    }


    /**
     * Create a Skin from given Jason-file!
     * The drawable resources are created from Svg-Folder and putted into a Atlas
     *
     * @param name        Name of this skin, will be used for create tmp cache folder!
     * @param storageType LOCAL or INTERNAL
     * @param skinFolder  {@link FileHandle} to the folder of this skin
     */
    public SvgSkin(boolean forceCreateNewAtlas, String name, StorageType storageType, FileHandle skinFolder) {
        super();
        this.storageType = storageType;
        this.name = name;
        this.forceCreateNewAtlas = forceCreateNewAtlas;

        FileHandle skinFile = null;
        if (skinFolder.extension().equals("json")) {
            skinFile = skinFolder;
            this.skinFolder = skinFile.parent();
        } else {
            skinFile = skinFolder.child(SKIN_JSON_NAME);
            this.skinFolder = skinFolder;
        }
        load(skinFile);
    }

    public void load(FileHandle skinFile) {
        super.load(skinFile);

        //after load set iconStyle
        try {
            getIcon = get(IconsStyle.class);
        } catch (Exception e) {
        }

        try {
            getMenuIcon = get(MenuIconStyle.class);
        } catch (Exception e) {
        }

    }


    @Override
    public Color getColor(String name) {
        try {
            return get(name, SkinColor.class);
        } catch (Exception e) {

        }
        return super.getColor(name);
    }


    @Override
    public void add(String name, Object resource, Class type) {
        if (name == null) throw new IllegalArgumentException("path cannot be null.");
        if (resource == null) throw new IllegalArgumentException("resource cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        if (typeResources == null) {
            typeResources = new ObjectMap(type == ColorDrawable.class || type == TextureRegion.class || type == Drawable.class || type == Sprite.class ? 256 : 64);
            resources.put(type, typeResources);
        }
        typeResources.put(name, resource);
    }

    /**
     * Returns a registered drawable. If no drawable is found but a region, ninepatch, or sprite exists with the path, then the
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
                                "No Drawable, NinePatch, TextureRegion, Texture, or Sprite registered with path: " + name);
                }
            }
        }

        if (drawable instanceof BaseDrawable) ((BaseDrawable) drawable).setName(name);

        add(name, drawable, Drawable.class);
        return drawable;
    }

    @Override
    protected Json getJsonLoader(final FileHandle skinFile) {

        if (name == null) {
            //return default skin
            return super.getJsonLoader(skinFile);
        }


        final Skin skin = this;

        final Json json = new Json() {
            public <T> T readValue(Class<T> type, Class elementType, JsonValue jsonData) {

                if (type.getName().equals("com.badlogic.gdx.graphics.Color")) {
                    //change type to de.longri.cachebox3.utils.SkinColor
                    type = (Class<T>) SkinColor.class;
                }


                // If the JSON is a string but the type is not, look up the actual value by path.
                if (jsonData.isString() && !ClassReflection.isAssignableFrom(CharSequence.class, type)) {
                    if (ClassReflection.isEnum(type)) {
                        try {
                            return (T) type.getDeclaredMethod("valueOf", String.class).invoke(null, jsonData.asString());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    return get(jsonData.asString(), type);
                }
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
                ArrayList<ScaledSvg> registerdSvgs = new ArrayList<ScaledSvg>();
                for (JsonValue valueEntry = valueMap.child; valueEntry != null; valueEntry = valueEntry.next) {
                    ScaledSvg object = (ScaledSvg) json.readValue(type, valueEntry);
                    object.setRegisterName(valueEntry.name);
                    registerdSvgs.add(object);

                    //  register as Resource
                    SvgSkin.this.add(object.getRegisterName(), object, ScaledSvg.class);
                }

                //create and register atlas
                SvgSkin.this.addRegions(SvgSkinUtil.createTextureAtlasFromImages(forceCreateNewAtlas, SvgSkin.this.name, registerdSvgs, skinFile));
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

        json.setSerializer(Bitmap.class, new Json.ReadOnlySerializer<Bitmap>() {
            public Bitmap read(Json json, JsonValue jsonData, Class type) {
//                Color color = json.readValue("color", Color.class, jsonData);
//                ColorDrawable drawable = new ColorDrawable(color);
                return null;
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

                String background, firstItem, secondItem, selectedItem, vScroll = null, vScrollKnob = null, hScroll = null, hScrollKnob = null;

                background = json.readValue("background", String.class, jsonData);
                firstItem = json.readValue("firstItem", String.class, jsonData);
                secondItem = json.readValue("secondItem", String.class, jsonData);
                selectedItem = json.readValue("selectedItem", String.class, jsonData);

                style.background = getDrawable(background);
                style.firstItem = getDrawable(firstItem);
                style.secondItem = getDrawable(secondItem);
                style.selectedItem = getDrawable(selectedItem);


                style.pad = json.readValue("pad", float.class, 0f, jsonData);
                style.padLeft = json.readValue("padLeft", float.class, 0f, jsonData);
                style.padRight = json.readValue("padRight", float.class, 0f, jsonData);
                style.padTop = json.readValue("padTop", float.class, 0f, jsonData);
                style.padBottom = json.readValue("padBottom", float.class, 0f, jsonData);

                try {
                    vScroll = json.readValue("vScroll", String.class, jsonData);
                } catch (Exception e) {
                }
                try {
                    vScrollKnob = json.readValue("vScrollKnob", String.class, jsonData);
                } catch (Exception e) {
                }

                if (vScroll != null) style.vScroll = getDrawable(vScroll);
                if (vScrollKnob != null) style.vScrollKnob = getDrawable(vScrollKnob);


                try {
                    hScroll = json.readValue("hScroll", String.class, jsonData);
                } catch (Exception e) {
                }
                try {
                    hScrollKnob = json.readValue("hScrollKnob", String.class, jsonData);
                } catch (Exception e) {
                }

                if (hScroll != null) style.hScroll = getDrawable(hScroll);
                if (hScrollKnob != null) style.hScrollKnob = getDrawable(hScrollKnob);

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

                SvgNinePatchDrawable.SvgNinePatchDrawableUnScaledValues values = new SvgNinePatchDrawable.SvgNinePatchDrawableUnScaledValues();
                values.left = left;
                values.right = right;
                values.top = top;
                values.bottom = bottom;
                values.leftWidth = leftWidth;
                values.rightWidth = rightWidth;
                values.topHeight = topHeight;
                values.bottomHeight = bottomHeight;

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


                // if any value < 0 set to half width or height!
                if (left < 0) left = textureRegion.getRegionWidth() / 2;
                if (right < 0) right = textureRegion.getRegionWidth() / 2;
                if (top < 0) top = textureRegion.getRegionHeight() / 2;
                if (bottom < 0) bottom = textureRegion.getRegionHeight() / 2;
                if (leftWidth < 0) leftWidth = textureRegion.getRegionWidth() / 2;
                if (rightWidth < 0) rightWidth = textureRegion.getRegionWidth() / 2;
                if (topHeight < 0) topHeight = textureRegion.getRegionHeight() / 2;
                if (bottomHeight < 0) bottomHeight = textureRegion.getRegionHeight() / 2;

                SvgNinePatchDrawable svgNinePatchDrawable = new SvgNinePatchDrawable(new NinePatch(textureRegion, left, right, top, bottom),
                        leftWidth, rightWidth, topHeight, bottomHeight);

                svgNinePatchDrawable.name = name;
                svgNinePatchDrawable.values = values;
                return svgNinePatchDrawable;
            }
        });


        json.setSerializer(BitmapFont.class, new Json.ReadOnlySerializer<BitmapFont>() {
            public BitmapFont read(Json json, JsonValue jsonData, Class type) {
                String path = json.readValue("font", String.class, jsonData);
                int scaledSize = json.readValue("size", int.class, -1, jsonData);

                FileHandle fontFile = skinFile.parent().child(path);
//                if (!fontFile.exists()) fontFile = Gdx.files.internal(path);
                if (!fontFile.exists()) throw new SerializationException("Font file not found: " + fontFile);

                try {
                    SkinFont font = new SkinFont(path, fontFile, scaledSize);
                    return font;
                } catch (RuntimeException ex) {
                    throw new SerializationException("Error loading bitmap font: " + fontFile, ex);
                }
            }
        });

        json.setSerializer(SkinColor.class, new Json.ReadOnlySerializer<SkinColor>() {
            public SkinColor read(Json json, JsonValue jsonData, Class type) {
                if (jsonData.isString()) return get(jsonData.asString(), SkinColor.class);
                String hex = json.readValue("hex", String.class, (String) null, jsonData);
                if (hex != null) {
                    SkinColor c = new SkinColor(Color.valueOf(hex));
                    c.skinName = jsonData.name;
                    return c;
                }
                float r = json.readValue("r", float.class, 0f, jsonData);
                float g = json.readValue("g", float.class, 0f, jsonData);
                float b = json.readValue("b", float.class, 0f, jsonData);
                float a = json.readValue("a", float.class, 1f, jsonData);
                SkinColor c = new SkinColor(r, g, b, a);
                c.skinName = jsonData.name;
                return c;
            }
        });

        json.setSerializer(TintedDrawable.class, new Json.ReadOnlySerializer() {
            public Object read(Json json, JsonValue jsonData, Class type) {
                String name = json.readValue("path", String.class, jsonData);
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

        return json;
    }

    @Override
    public <T> T get(String name, Class<T> type) {

        if (type.getName().equals("org.oscim.backend.canvas.Bitmap")) {
            ObjectMap<String, Object> typeResources = resources.get(type);
            if (typeResources != null) {
                Object resource = typeResources.get(name);
                if (resource != null)
                    return (T) resource;
            }

            // get ScaledSvg
            ScaledSvg scaledSvg = get(name, ScaledSvg.class);
            FileHandle fileHandle = this.skinFolder.child(scaledSvg.path);
            Bitmap bitmap = null;
            try {
                bitmap = PlatformConnector.getSvg(name, fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale);
            } catch (IOException e) {
                e.printStackTrace();
            }
            add(name, bitmap, Bitmap.class);
            return (T) bitmap;
        }

        return super.get(name, type);
    }

}
