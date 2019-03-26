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
package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.platform_test.PlatformAssertionError;

import java.io.File;

import static de.longri.cachebox3.platform_test.Assert.assertNotNull;
import static de.longri.cachebox3.platform_test.Assert.assertTrue;

public class TestUtils {

    public static void initialGdx() {
        // we don't need to initial GDX on PlatformTest Gdx are initial!
    }

    public static void initialVisUI() {
        // we don't need to initial VisUI on PlatformTest VisUI are loaded!
    }

    public static FileHandle getResourceFileHandle(String path, boolean mustexist) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        return fileHandle;
    }

    public static AbstractView assertAbstractViewSerialation(AbstractView abstractView, Class<?> expectedClazz) throws de.longri.serializable.NotImplementedException, PlatformAssertionError {
        de.longri.serializable.BitStore store = abstractView.saveInstanceState();
        byte[] bytes = store.getArray();

        de.longri.serializable.BitStore reader = new de.longri.serializable.BitStore(bytes);

        String className = reader.readString();

        AbstractView newInstanceAbstractView = null;
        Object obj = null;

        try {
            Class clazz = ClassReflection.forName(className);
            Constructor constructor = ClassReflection.getConstructor(clazz, de.longri.serializable.BitStore.class);

            obj = constructor.newInstance(reader);
            newInstanceAbstractView = (AbstractView) obj;

        } catch (ReflectionException e) {
            e.printStackTrace(

            );
        }
        assertNotNull(obj);
        assertNotNull(newInstanceAbstractView);
        assertTrue(expectedClazz.isInstance(obj));
        return newInstanceAbstractView;
    }
}