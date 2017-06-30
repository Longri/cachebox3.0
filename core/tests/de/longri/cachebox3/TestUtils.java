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
package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationLogger;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.utils.BuildInfo;
import org.apache.commons.codec.Charsets;
import org.slf4j.impl.DummyLogApplication;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by longri on 14.04.17.
 */
public class TestUtils {

    public static void initialGdx() {
        if (Gdx.net != null) return;
        BuildInfo.setTestBuildInfo("JUnitTest");
        Gdx.net = new LwjglNet();
        Gdx.files = new LwjglFiles();
        Gdx.app = new DummyLogApplication() {
            @Override
            public ApplicationType getType() {
                return ApplicationType.HeadlessDesktop;
            }
        };
        Gdx.app.setApplicationLogger(new LwjglApplicationLogger());
    }


    public static String getResourceRequestString(String path, String apiKey) throws IOException {
        File file = new File(path);
        InputStream stream = getResourceRequestStream(path);

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
        String expected = new String(b, Charsets.UTF_8);
        if (apiKey != null && !apiKey.isEmpty()) {
            expected = expected.replace("\"AccessToken\":\"+DummyKEY\"",
                    "\"AccessToken\":\"" + apiKey + "\"");
        }
        return expected;
    }

    public static InputStream getResourceRequestStream(String path) throws FileNotFoundException {
        File file = new File(path);
        FileInputStream stream = new FileInputStream(file);

        return stream;
    }

    public static void assetCacheAttributes(Cache cache, ArrayList<Attributes> positiveList, ArrayList<Attributes> negativeList) {
        Iterator<Attributes> positiveIterator = positiveList.iterator();
        Iterator<Attributes> negativeIterator = negativeList.iterator();

        while (positiveIterator.hasNext()) {
            assertThat("Attribute wrong", cache.isAttributePositiveSet((Attributes) positiveIterator.next()));
        }

        while (negativeIterator.hasNext()) {
            Attributes tmp = negativeIterator.next();
            assertThat(tmp.name() + " negative Attribute wrong", cache.isAttributeNegativeSet((tmp)));
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
            assertThat(attr.name() + "Attribute wrong", !cache.isAttributePositiveSet(attr));
            assertThat(attr.name() + "Attribute wrong", !cache.isAttributeNegativeSet(attr));
        }
    }
}
