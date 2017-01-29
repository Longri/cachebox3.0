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
package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.Waypoint;
import org.oscim.backend.canvas.Bitmap;

/**
 * Created by Longri on 24.01.2017.
 */
public class Validate_MapWayPointItemStyle extends ValidationTask {

    private final StringBuilder missingSyles = new StringBuilder();
    private final StringBuilder missingBitmaps = new StringBuilder();
    private final StringBuilder wrongBitmapsSize = new StringBuilder();


    public Validate_MapWayPointItemStyle(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage);
    }


    @Override
    public String getName() {
        return "MapWayPointItemStyle";
    }

    /**
     * Check if exist a MapWayPointItemStyle for all CacheTypes and WayPointTypes
     */
    @Override
    public void runValidation() {

        checkCacheTypes();


        //Check changes of MapWayPointItemStyle.class
        Field[] fields = ClassReflection.getFields(MapWayPointItemStyle.class);
        String classChanged = "";
        if (fields.length > 3) {
            classChanged = "MapWayPointItemStyle is changed, maybe you must add new Validation " +
                    "on de.longri.cachebox3.develop.tools.skin_editor.validation.Validate_MapWayPointItemStyle.java";
        }


        //set result
        if (missingBitmaps.length > 0 || missingSyles.length > 0) {
            errorMsg = "Missing Styles:\n\n" + missingSyles.toString() + "\n\nMissing Bitmaps:\n\n" + missingBitmaps.toString();
        }

        if (!classChanged.isEmpty()) {
            warnMsg = classChanged + "\n\n Wrong Sizes:\n\n" + wrongBitmapsSize.toString();
        } else if (wrongBitmapsSize.length > 0) {
            warnMsg = "Wrong Sizes:\n\n" + wrongBitmapsSize.toString();
        }


    }

    private void checkCacheTypes() {
        //Check Cache, I'm the owner
        Cache ownerCache = new Cache(0, 0, "test", CacheTypes.Cache, "GCCODE");
        checkCache(ownerCache);

        //Check Cache, found
        Cache foundCache = new Cache(0, 0, "test", CacheTypes.Cache, "GCCODE");
        foundCache.setOwner("nicht meiner");
        foundCache.setFound(true);
        checkCache(foundCache);

        //Check Cache, solved
        Cache solvedCache = new Cache(0, 0, "test", CacheTypes.Mystery, "GCCODE");
        solvedCache.setOwner("nicht meiner");
        solvedCache.setCorrectedCoordinates(true);
        checkCache(solvedCache);


        //Check Cache, multi start
        Cache multiStartCache = new Cache(0, 0, "test", CacheTypes.Multi, "GCCODE");
        multiStartCache.setOwner("nicht meiner");
        Waypoint wp = new Waypoint(0, 0, false);
        wp.IsStart = true;
        multiStartCache.waypoints.add(wp);
        checkCache(multiStartCache);

        //Check Cache, myst start
        Cache mystStartCache = new Cache(0, 0, "test", CacheTypes.Mystery, "GCCODE");
        mystStartCache.setOwner("nicht meiner");
        Waypoint wpm = new Waypoint(0, 0, false);
        wpm.IsStart = true;
        mystStartCache.waypoints.add(wpm);
        checkCache(mystStartCache);


        for (CacheTypes type : CacheTypes.values()) {

            // create a Temp Cache
            Cache cache = new Cache(0, 0, "test", type, "GCCODE");
            cache.setOwner("nicht meiner");
            checkCache(cache);
        }
    }


    private void checkCache(Cache cache) {
        MapWayPointItemStyle style = null;
        try {
            style = WaypointLayer.getClusterSymbolsByCache(cache);
        } catch (GdxRuntimeException e) {
        }

        String styleName = WaypointLayer.getMapIconName(cache);

        if (style == null) {
            missingSyles.append(styleName);
            missingSyles.append("\n");
            return;
        }

        checkBitmap(style.small, styleName, Size.small);
        checkBitmap(style.middle, styleName, Size.middle);
        checkBitmap(style.large, styleName, Size.large);
    }


    private enum Size {

        small(".small", 7f, 14f), middle(".middle", 17, 27f), large(".large", 35f, 44f);

        private final float min;
        private final float max;
        private final String fieldName;

        Size(String fieldName, float min, float max) {
            this.fieldName = fieldName;
            this.min = min;
            this.max = max;
        }
    }

    private void checkBitmap(Bitmap bitmap, String styleName, Size fieldSize) {
        // check if no bitmaps are NULL
        if (bitmap == null) {
            missingBitmaps.append(styleName);
            missingBitmaps.append(fieldSize.fieldName);
            missingBitmaps.append("\n");
        } else { // check Size
            int minWidth = (int) CB.getScaledFloat(fieldSize.min);
            int maxWidth = (int) CB.getScaledFloat(fieldSize.max);
            if (bitmap.getWidth() < minWidth || bitmap.getWidth() > maxWidth) {
                wrongBitmapsSize.append(styleName);
                wrongBitmapsSize.append(fieldSize.fieldName);
                wrongBitmapsSize.append(" width: " + bitmap.getWidth());
                wrongBitmapsSize.append("   => The width should be between " + minWidth + " and " + maxWidth + "!");
                wrongBitmapsSize.append(" \n");
            }

            if (bitmap.getHeight() < minWidth || bitmap.getHeight() > maxWidth) {
                wrongBitmapsSize.append(styleName);
                wrongBitmapsSize.append(fieldSize.fieldName);
                wrongBitmapsSize.append(" height: " + bitmap.getHeight());
                wrongBitmapsSize.append("   => The height should be between " + minWidth + " and " + maxWidth + "!");
                wrongBitmapsSize.append(", \n");
            }
        }
    }

}
