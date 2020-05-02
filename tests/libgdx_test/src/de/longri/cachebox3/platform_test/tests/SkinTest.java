

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

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
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;
import de.longri.cachebox3.platform_test.*;

import java.io.IOException;
import java.util.List;

import static de.longri.cachebox3.platform_test.Assert.assertNotNull;
import static de.longri.cachebox3.platform_test.Assert.assertTrue;

/**
 * Created by Longri on 20.08.2019.
 */
public class SkinTest {

    static SvgSkin testSkin;
    static float ICON_MIN_WIDTH;
    static float ICON_MIN_HEIGHT;
    static float ICON_MAX_WIDTH;
    static float ICON_MAX_HEIGHT;

    @BeforeAll
    public static void initial() {
        TestUtils.initialGdx();
        final FileHandle androidAssetSkin = TestUtils.getSkinFileHandle();
        final String finalSkinName = "internalDefault";
        final SvgSkin.StorageType finalType = SvgSkin.StorageType.INTERNAL;
        testSkin = new SvgSkin(true, finalSkinName, finalType, androidAssetSkin);

        ICON_MIN_WIDTH = CB.getScaledInt(20);
        ICON_MIN_HEIGHT = CB.getScaledInt(43);
        ICON_MAX_WIDTH = CB.getScaledInt(55);
        ICON_MAX_HEIGHT = CB.getScaledInt(48);
    }

    @AfterAll
    public static void clear() {
        FileHandle resourceFileHandle = TestUtils.getResourceFileHandle("testsResources", true);
        resourceFileHandle.child("skins").deleteDirectory();
        resourceFileHandle.parent().child("user").deleteDirectory();
    }


    @Test
    public void loadSkinJsonTest() throws PlatformAssertionError {
        assertNotNull(testSkin, "Skin must be loaded and not NULL");
    }

    @Test
    public void existAllStyleClasses() throws ClassNotFoundException, PlatformAssertionError {
        List<Class<?>> clazzList = TestUtils.getUsedStyleClasses();
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
    public void parseUsedStylesAndCheckExist() throws IOException, PlatformAssertionError {
        Array<StyleEntry> caller = TestUtils.getStyleCaller();
        // check if for every used style a entry on loaded Skin
        for (StyleEntry styleEntry : caller) {
            Object style = null;
            try {
                style = testSkin.get(styleEntry.name, styleEntry.clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNotNull(style, "No '" + styleEntry.clazz.toString() + "'style found for name '" + styleEntry.name + "'!");
        }
    }


    @Test
    public void menuIconSizeCheck() throws PlatformAssertionError {

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
                        Assert.fail("Menu icon '" + field.getName() + "' has wrong size! min width:" + ICON_MIN_WIDTH
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
