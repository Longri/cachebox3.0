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
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.utils.GeoUtils;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import static de.longri.cachebox3.platform_test.Assert.*;

public class TestUtils {

    public static final Coordinate LONGRI_HOME_COORDS = new Coordinate(52.581892, 13.398128);

    public static boolean isPlatformTest() {
        return true;
    }

    public static void initialGdx() {
        // we don't need to initial GDX on PlatformTest Gdx are initial!
    }

    public static void initialVisUI() {
        // we don't need to initial VisUI on PlatformTest VisUI are loaded!
    }

    public static FileHandle getResourceFileHandle(String path, boolean mustexist) {
        boolean isDevice = mustexist && (CanvasAdapter.platform == Platform.IOS || CanvasAdapter.platform == Platform.ANDROID);
        FileHandle fileHandle = isDevice ? Gdx.files.internal(path) : Gdx.files.absolute(path);

        if (mustexist && !fileHandle.exists()) {
            fileHandle = isDevice ? Gdx.files.internal("platform_test/" + path) : Gdx.files.absolute("platform_test/" + path);
        }

        return fileHandle;
    }

    public static AbstractView assertAbstractViewSerialation(AbstractView abstractView, Class<?> expectedClazz) throws PlatformAssertionError {
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

    public static String getResourceRequestString(String path, String apiKey) throws IOException {
        boolean isDevice = CanvasAdapter.platform == Platform.IOS || CanvasAdapter.platform == Platform.ANDROID;

        FileHandle file = isDevice ? Gdx.files.internal(path) : Gdx.files.absolute(path);

        if (!file.exists()) {
            file = isDevice ? Gdx.files.internal("platform_test/" + path) : Gdx.files.absolute("platform_test/" + path);
        }
        if (!file.exists()) throw new FileNotFoundException("can't find file: " + path);

        InputStream stream = file.read();

        byte[] b = new byte[(int) file.length()];
        int len = b.length;
        int total = 0;

        while (total < len) {
            int result = stream.read(b, total, len - total);
            if (result == -1) {
                break;
            }
            total += result;
        }
        String expected = new String(b);
        if (apiKey != null && !apiKey.isEmpty()) {
            expected = expected.replace("\"AccessToken\":\"+DummyKEY\"",
                    "\"AccessToken\":\"" + apiKey + "\"");
        }
        return expected;
    }

    public static InputStream getResourceRequestStream(String path) throws FileNotFoundException {
        boolean isDevice = CanvasAdapter.platform == Platform.IOS || CanvasAdapter.platform == Platform.ANDROID;

        FileHandle file = isDevice ? Gdx.files.internal(path) : Gdx.files.absolute(path);

        if (!file.exists()) {
            file = isDevice ? Gdx.files.internal("platform_test/" + path) : Gdx.files.absolute("platform_test/" + path);
        }
        if (!file.exists()) throw new FileNotFoundException("can't find file: " + path);
        return file.read();
    }

    static int dbCount = 0;

    public static Database getTestDB(boolean inMemory) {
        if (inMemory) {
            Database database = new Database(Database.DatabaseType.CacheBox3);
            Database.createNewInMemoryDB(database, "createNewDB" + Integer.toString(dbCount++));
            return database;
        } else {
            FileHandle dbFiileHandle = Gdx.files.local("testDBfile" + Integer.toString(dbCount++) + ".db3");

            //delete if exist
            dbFiileHandle.delete();

            Database database = new Database(Database.DatabaseType.CacheBox3);
            database.startUp(dbFiileHandle);

            return database;
        }
    }

    public static double roundDoubleCoordinate(double value) {
        value = Math.round(GeoUtils.degreesToMicrodegrees(value));
        value = GeoUtils.microdegreesToDegrees((int) value);
        return value;
    }

    public static void assetCacheAttributes(Database database, AbstractCache abstractCache, ArrayList<Attributes> positiveList, ArrayList<Attributes> negativeList) throws PlatformAssertionError {
        Iterator<Attributes> positiveIterator = positiveList.iterator();
        Iterator<Attributes> negativeIterator = negativeList.iterator();

        while (positiveIterator.hasNext()) {
            assertThat("Attribute wrong", abstractCache.isAttributePositiveSet((Attributes) positiveIterator.next()));
        }

        while (negativeIterator.hasNext()) {
            Attributes tmp = negativeIterator.next();
            assertThat(tmp.name() + " negative Attribute wrong", abstractCache.isAttributeNegativeSet((tmp)));
        }

        // f�lle eine Liste mit allen Attributen
        ArrayList<Attributes> attributes = new ArrayList<Attributes>();
        Attributes[] tmp = Attributes.values();
        for (Attributes item : tmp) {
            attributes.add(item);
        }

        // L�sche die vergebenen Atribute aus der Kommplett Liste
        positiveIterator = positiveList.iterator();
        negativeIterator = negativeList.iterator();

        while (positiveIterator.hasNext()) {
            attributes.remove(positiveIterator.next());
        }

        while (negativeIterator.hasNext()) {
            attributes.remove(negativeIterator.next());
        }

        attributes.remove(Attributes.getAttributeEnumByGcComId(64));
        attributes.remove(Attributes.getAttributeEnumByGcComId(65));
        attributes.remove(Attributes.getAttributeEnumByGcComId(66));

        // Teste ob die �brig gebliebenen Atributte auch nicht vergeben wurden.
        Iterator<Attributes> RestInterator = attributes.iterator();

        while (RestInterator.hasNext()) {
            Attributes attr = (Attributes) RestInterator.next();
            assertThat(attr.name() + "Attribute wrong", !abstractCache.isAttributePositiveSet(attr));
            assertThat(attr.name() + "Attribute wrong", !abstractCache.isAttributeNegativeSet(attr));
        }
    }

}