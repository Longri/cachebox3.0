/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.map.layer;

import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import org.oscim.theme.XmlRenderThemeMenuCallback;
import org.oscim.theme.XmlRenderThemeStyleLayer;
import org.oscim.theme.XmlRenderThemeStyleMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Longri on 22.03.2018.
 */
public class ThemeMenuCallback implements XmlRenderThemeMenuCallback {

    private final Logger log = LoggerFactory.getLogger(ThemeMenuCallback.class);

    private final String themePath;
    private final ObjectMap<String, Boolean> allCategories = new ObjectMap<>();

    public ThemeMenuCallback(String path) {
        this.themePath = path;

        // try to get all categories from Config
        GdxSqliteCursor cursor = Database.Settings.rawQuery("SELECT blob FROM Config WHERE Key=\"" + themePath + "\"");
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                byte[] serialize = cursor.getBlob(0);
                BitStore store = new BitStore(serialize);
                int count = store.readInt();
                for (int i = 0; i < count; i++) {
                    String categorieName = store.readString();
                    boolean categorieEnabled = store.readBool();
                    allCategories.put(categorieName, categorieEnabled);
                }
            } catch (Exception e) {
                log.error("Can't read Theme Menu from Settings", e);
            }
        }
    }

    @Override
    public Set<String> getCategories(XmlRenderThemeStyleMenu renderThemeStyleMenu) {
        String style = renderThemeStyleMenu.getDefaultValue();
        // First get the selected layer's categories that are enabled together
        Set<String> categories = new HashSet<>();

        if (allCategories.size > 0) {
            for (ObjectMap.Entry<String, Boolean> entry : allCategories.entries()) {
                if (entry.value) {
                    categories.add(entry.key);
                }
            }
        } else {
            // Retrieve the layer from the style
            XmlRenderThemeStyleLayer renderThemeStyleLayer = renderThemeStyleMenu.getLayer(renderThemeStyleMenu.getDefaultValue());
            if (renderThemeStyleLayer == null) {
                System.err.println("Invalid style " + style);
                return null;
            }

            // Then add the selected layer's overlays that are enabled individually
            // Here we use the style menu, but users can use their own preferences
            for (XmlRenderThemeStyleLayer overlay : renderThemeStyleLayer.getOverlays()) {
                if (overlay.isEnabled()) {
                    for (String overlayCat : overlay.getCategories()) {
                        allCategories.put(overlayCat, true);
                        categories.add(overlayCat);
                    }
                } else {
                    for (String overlayCat : overlay.getCategories()) {
                        allCategories.put(overlayCat, false);
                    }
                }
            }
            storeCategorieSettings();
        }

        // This is the whole categories set to be enabled
        return categories;
    }

    private void storeCategorieSettings() {
        try {
            BitStore store = new BitStore();
            int count = allCategories.size;
            store.write(count);
            for (ObjectMap.Entry<String, Boolean> entry : allCategories.entries()) {
                store.write(entry.key);
                store.write(entry.value);
            }

            byte[] bytes = store.getArray();
            GdxSqlitePreparedStatement statement = Database.Settings.myDB.prepare("INSERT OR REPLACE into Config VALUES(?,?,?,?,?)");
            statement.bind(themePath, null, null, null, bytes);

            statement.commit();
            statement.close();

        } catch (Exception e) {
            log.error("Can't write Theme Menu to Settings", e);
        }
    }
}
