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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.gui.skin.styles.AbstractIconStyle;
import de.longri.cachebox3.gui.skin.styles.EditWaypointStyle;
import de.longri.cachebox3.platform_test.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 20.08.2019.
 */
public class SkinTest {

    static SvgSkin testSkin;


    @BeforeAll
    static void initial() {
        TestUtils.initialGdx();

        ///we want to test the current skin,
        // so we have to copy it to the Resources first!

        FileHandle androidAssetSkin = Gdx.files.absolute("../../launcher/android/assets/skins");
        FileHandle resourceFileHandle = TestUtils.getResourceFileHandle("testsResources", true);

        // delete exist skin for clean testing
        resourceFileHandle.child("skins").deleteDirectory();
        androidAssetSkin.copyTo(resourceFileHandle);


        //Load the Skin for all tests
        final String finalSkinName = "internalDefault";
        final SvgSkin.StorageType finalType = SvgSkin.StorageType.INTERNAL;
        final FileHandle finalSkinFileHandle = TestUtils.getResourceFileHandle("testsResources", true).child("skins/day");
        testSkin = new SvgSkin(true, finalSkinName, finalType, finalSkinFileHandle);
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

            if (clazz.equals(EditWaypointStyle.class)) {
                System.out.println("Must implement the missing EditWaypointStyle");
                continue; // TODO implement EditWaypointStyle
            }

            ObjectMap<String, ?> allStyles = testSkin.getAll(clazz);
            assertNotNull(allStyles, "Style for class:" + clazz.toString() + " must not be Null");
            assertTrue(allStyles.size > 0);
        }
    }
}
