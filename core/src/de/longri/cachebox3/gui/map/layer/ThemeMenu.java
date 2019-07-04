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
import de.longri.cachebox3.translation.Translation;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import de.longri.serializable.BitStore;
import org.oscim.theme.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Longri on 22.03.2018.
 */
public class ThemeMenu implements XmlRenderThemeMenuCallback {
    private final Logger log = LoggerFactory.getLogger(ThemeMenu.class);
    private IRenderTheme thisRenderTheme;
    private ObjectMap<String, String> styles; // <title, Id>
    private String defaultStyle;
    private ObjectMap<String, ObjectMap<String, String>> allOverlays; // <styleId, <title, Id>>
    private Set<String> configOverlays;
    private String themePath;
    private String mapStyleId;
    private Mode mode;

    public ThemeMenu(String selectedTheme) {
        themePath = selectedTheme;
    }

    public void readTheme() {
        mode = Mode.get;
        try {
            // parse RenderTheme "ThemeLoader.load" to get XmlRenderThemeMenuCallback getCategories called
            thisRenderTheme = ThemeLoader.load(themePath, this);
            // for internal theme we must use "ThemeLoader.load(themeFile);" there is no themepath (and no config),
            // so handling internal themes is done at CB.setCurrentTheme
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    public void applyConfig(String mapStyleId) {
        mode = Mode.set;
        this.mapStyleId = mapStyleId;
        configOverlays = new HashSet<>();
        readConfig(mapStyleId); // into configOverlays
        try {
            // parse RenderTheme "ThemeLoader.load" to get XmlRenderThemeMenuCallback getCategories called
            thisRenderTheme = ThemeLoader.load(themePath, this);
            // for internal theme we must use "ThemeLoader.load(themeFile);" there is no themepath (and no config),
            // so handling internal themes is done at CB.setCurrentTheme
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Override
    public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
        styles = new ObjectMap<>();
        allOverlays = new ObjectMap<>();

        if (mode == Mode.get) {
            for (XmlRenderThemeStyleLayer styleLayer : style.getLayers().values()) {
                if (styleLayer.isVisible()) {
                    styles.put(styleLayer.getTitle(Translation.get("Language2Chars").toString().toLowerCase()), styleLayer.getId());
                    // could now get the overlays of this styleLayer
                    XmlRenderThemeStyleLayer selected_Layer = style.getLayer(styleLayer.getId());
                    ObjectMap<String, String> overlays = new ObjectMap<>();
                    for (XmlRenderThemeStyleLayer overlay : selected_Layer.getOverlays()) {
                        if (overlay.isEnabled()) {
                            overlays.put(overlay.getTitle(Translation.get("Language2Chars").toString()), "+" + overlay.getId());
                        } else {
                            overlays.put(overlay.getTitle(Translation.get("Language2Chars").toString()), "-" + overlay.getId());
                        }
                    }
                    allOverlays.put(styleLayer.getId(), overlays);
                }
            }

            defaultStyle = style.getDefaultValue();
            return null;
        } else {
            XmlRenderThemeStyleLayer selectedLayer = style.getLayer(mapStyleId);
            if (selectedLayer == null) {
                return null;
            }
            Set<String> categories = selectedLayer.getCategories();
            // add the categories from overlays that are enabled
            for (XmlRenderThemeStyleLayer overlay : selectedLayer.getOverlays()) {
                if (configOverlays.contains(overlay.getId())) {
                    categories.addAll(overlay.getCategories());
                }
            }
            return categories;
        }
    }

    public IRenderTheme getRenderTheme() {
        return thisRenderTheme;
    }

    public ObjectMap<String, String> getStyles() {
        if (styles == null) {
            styles = new ObjectMap<>();
        }
        return styles;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public ObjectMap<String, String> getOverlays(String mapStyleId) {
        return allOverlays.get(mapStyleId);
    }

    public void readConfig(String mapStyleId) {
        // try to get all categories from Config
        GdxSqliteCursor cursor = Database.Settings.rawQuery("SELECT blob FROM Config WHERE Key=\"" + themePath + "!" + mapStyleId + "\"");
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                BitStore store = new BitStore(cursor.getBlob(0));
                int count = store.readInt();
                for (int i = 0; i < count; i++)
                    configOverlays.add(store.readString());
            } catch (Exception e) {
                log.error("Can't read Theme Menu from Settings", e);
            }
        }
    }

    private void writeConfig(String mapStyleId) {
        // getCategories must have been called in advance (read allOverlays)
        try {
            BitStore store = new BitStore();
            int count = allOverlays.get(mapStyleId).size;
            store.write(count);
            for (String overlay : allOverlays.get(mapStyleId).values()) {
                if (overlay.startsWith("+")) {
                    store.write(overlay.substring(1));
                }
            }

            GdxSqlitePreparedStatement statement = Database.Settings.myDB.prepare("INSERT OR REPLACE into Config VALUES(?,?,?,?,?)");
            statement.bind(themePath + "!" + mapStyleId, null, null, null, store.getArray());

            statement.commit();
            statement.close();

        } catch (Exception e) {
            log.error("Can't write Theme Menu to Settings", e);
        }
    }

    private enum Mode {
        get, set
    }
}

