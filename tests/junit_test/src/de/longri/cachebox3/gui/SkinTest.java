/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.gui.skin.styles.AbstractIconStyle;
import de.longri.cachebox3.gui.skin.styles.EditWaypointStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;
import de.longri.cachebox3.platform_test.AfterAll;
import de.longri.cachebox3.types.CacheTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 20.08.2019.
 */
public class SkinTest {

    static SvgSkin testSkin;


    @BeforeAll
    static void initial() {
        TestUtils.initialGdx();
        final FileHandle androidAssetSkin = Gdx.files.absolute("../../launcher/android/assets/skins/day");
        final String finalSkinName = "internalDefault";
        final SvgSkin.StorageType finalType = SvgSkin.StorageType.INTERNAL;
        testSkin = new SvgSkin(true, finalSkinName, finalType, androidAssetSkin);
        CB.setActSkin(testSkin);
        CB.backgroundColor = CB.getColor("background");
    }

    @AfterAll
    static void clear() {
        FileHandle resourceFileHandle = TestUtils.getResourceFileHandle("testsResources", true);
        resourceFileHandle.child("skins").deleteDirectory();
        resourceFileHandle.parent().child("user").deleteDirectory();
    }


    @Test
    void loadSkinJsonTest() {
        assertNotNull(testSkin, "Skin must be loaded and not NULL");
    }

    @Test
    void existAllStyleClasses() throws ClassNotFoundException {
        List<Class<?>> clazzList = TestUtils.getClassesInSamePackage(AbstractIconStyle.class);
        for (Class<?> clazz : clazzList) {
            if (clazz.equals(AbstractIconStyle.class)) {
                System.out.println("ignore AbstractIconStyle.class");
                continue; // ignore abstract classes
            }
            if (clazz.equals(EditWaypointStyle.class)) {
                System.out.println("Must implement the missing EditWaypointStyle");
                continue; // TODO implement EditWaypointStyle
            }

            ObjectMap<String, ?> allStyles = testSkin.getAll(clazz);
            assertNotNull(allStyles, "Style for class:" + clazz.toString() + " must not be Null");
            assertTrue(allStyles.size > 0);
        }
    }

    @Test
    void parseUsedStylesAndCheckExist() throws IOException {

        Array<GetStyleEntry> caller = new Array<>();
        FileHandle srcCoreFolder = Gdx.files.absolute("../../core/src");

        getAllStyleCallers(srcCoreFolder, caller);

        assertTrue(caller.size > 0, "No style call found on core src! Wrong path? " + srcCoreFolder.file().getCanonicalPath());


        // check if for every used style a entry on loaded Skin
        for (GetStyleEntry styleEntry : caller) {
            Object style = null;
            try {
                style = testSkin.get(styleEntry.name, styleEntry.clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNotNull(style, "No '" + styleEntry.clazz.toString() + "'style found for name '" + styleEntry.name + "'!");
        }


    }

    private void getAllStyleCallers(FileHandle src, Array<GetStyleEntry> caller) {
        for (FileHandle fileHandle : src.list()) {
            if (fileHandle.isDirectory()) {
                getAllStyleCallers(fileHandle, caller);
            } else {
                if (fileHandle.extension().equals("java")) {
                    // read file and search for call of "VisUI.getSkin().get("
                    String fileStr = fileHandle.readString("UTF-8");
                    int pos = -1;
                    while ((pos = 20 + fileStr.indexOf("VisUI.getSkin().get(", pos)) >= 20) {
                        if (fileStr.substring(pos).startsWith("symbolStyleName, MapWayPointItemStyle.class)")) {
                            // add all used Styles for WaypointLayer and skip parsing of this file!
                            GetStyleEntry entry = new GetStyleEntry("mapStar", MapWayPointItemStyle.class);
                            if (!caller.contains(entry, false)) caller.add(entry);
                            entry = new GetStyleEntry("mapFound", MapWayPointItemStyle.class);
                            if (!caller.contains(entry, false)) caller.add(entry);
                            entry = new GetStyleEntry("mapSolved", MapWayPointItemStyle.class);
                            if (!caller.contains(entry, false)) caller.add(entry);
                            entry = new GetStyleEntry("mapMultiStartP", MapWayPointItemStyle.class);
                            if (!caller.contains(entry, false)) caller.add(entry);
                            entry = new GetStyleEntry("mapMysteryStartP", MapWayPointItemStyle.class);
                            if (!caller.contains(entry, false)) caller.add(entry);
                            entry = new GetStyleEntry("mapMultiStageStartP", MapWayPointItemStyle.class);
                            if (!caller.contains(entry, false)) caller.add(entry);

                            for (CacheTypes type : CacheTypes.values()) {
                                entry = new GetStyleEntry("map" + type.name(), MapWayPointItemStyle.class);
                                if (!caller.contains(entry, false)) caller.add(entry);
                            }
                            break;
                        }


                        String styleName = "default";

                        int classSearchPos = pos;
                        boolean defaultName = true;
                        if (fileStr.charAt(pos) == '\"' || fileStr.charAt(pos + 1) == '\"') {
                            int endNamePos = fileStr.indexOf('\"', pos + 2);
                            styleName = fileStr.substring(pos, endNamePos).replace('"', ' ').trim();
                            classSearchPos = endNamePos;
                            defaultName = false;
                        }

                        int classNamePos = defaultName ? pos : 1 + fileStr.indexOf(',', classSearchPos);
                        int classNameEndpos = fileStr.indexOf(".class", classNamePos);
                        String className = fileStr.substring(classNamePos, classNameEndpos).trim();
                        String subClassName = "";
                        int dotPos = className.indexOf('.');
                        if (dotPos >= 0) {
                            subClassName = className.substring(dotPos).replace(".", "$");
                            className = className.substring(0, dotPos);
                        }
                        Class clazz = null;

                        try {
                            clazz = Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            // if class not found

                            // try with own package
                            int packageStartPos = fileStr.indexOf("package") + 8;
                            int packageEndPos = fileStr.indexOf(";", packageStartPos);
                            String packageName = fileStr.substring(packageStartPos, packageEndPos).trim();

                            if (fileStr.contains("class " + className + " ")) {
                                try {
                                    clazz = Class.forName(packageName + "." + fileHandle.nameWithoutExtension() + "$" + className);
                                } catch (ClassNotFoundException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            if (clazz == null) {
                                try {
                                    clazz = Class.forName(packageName + "." + className);
                                } catch (ClassNotFoundException ex) {
                                    // search import for determine Class with imported package
                                    int importEnd = fileStr.indexOf(className) + className.length();
                                    int importStart = 6 + fileStr.lastIndexOf("import", importEnd);

                                    String classNameWithPackage = (fileStr.substring(importStart, importEnd).trim() + subClassName);
                                    try {
                                        clazz = Class.forName(classNameWithPackage);
                                    } catch (ClassNotFoundException exx) {
                                        exx.printStackTrace();
                                    }
                                }
                            }
                        }

                        if (clazz != null) {
                            GetStyleEntry entry = new GetStyleEntry(styleName, clazz);
                            if (!caller.contains(entry, false))
                                caller.add(entry);
                        }
                    }
                }
            }
        }
    }

    static class GetStyleEntry {
        private Class clazz;
        private String name;

        public GetStyleEntry(String styleName, Class clazz) {
            this.clazz = clazz;
            this.name = styleName;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof GetStyleEntry) {
                GetStyleEntry otherEntry = (GetStyleEntry) other;
                if (!otherEntry.name.equals(this.name)) return false;
                if (otherEntry.clazz.equals(this.clazz)) return true;
                return false;
            }
            return false;
        }
    }


    static final float ICON_MIN_WIDTH = 20.0f;
    static final float ICON_MIN_HEIGHT = 44.0f;
    static final float ICON_MAX_WIDTH = 55.0f;
    static final float ICON_MAX_HEIGHT = 47.0f;

    @Test
    void menuIconSizeCheck() {

        Field[] fields = ClassReflection.getFields(MenuIconStyle.class);
        MenuIconStyle style = testSkin.get(MenuIconStyle.class);

        for (Field field : fields) {
            try {
                Drawable drawable = (Drawable) field.get(style);
                if (drawable != null && drawable instanceof TextureRegionDrawable) {
                    TextureRegionDrawable textureRegionDrawable = (TextureRegionDrawable) drawable;
                    float width = textureRegionDrawable.getRegion().getRegionWidth();
                    float height = textureRegionDrawable.getRegion().getRegionHeight();

                    if (width < ICON_MIN_WIDTH || width > ICON_MAX_WIDTH || height < ICON_MIN_HEIGHT || height > ICON_MAX_HEIGHT)
                        Assertions.fail("Menu icon '" + field.getName() + "' has wrong size! min width:" + ICON_MIN_WIDTH
                                + "/ max width:" + ICON_MAX_WIDTH + "/ min height:" + ICON_MIN_HEIGHT + "/ max height:"
                                + ICON_MAX_HEIGHT + " || icon with:" + width + " / icon height:" + height);

                } else {
                    System.out.println("WARNING: no menu icon on Skin for '" + field.getName() + "'");
                }
            } catch (ReflectionException e) {
                e.printStackTrace();
            }
        }

    }

}
