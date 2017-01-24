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

import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import org.oscim.backend.canvas.Bitmap;

/**
 * Created by Longri on 24.01.2017.
 */
public class Validate_MapWayPointItemStyle extends ValidationTask {

    StringBuilder missingSyles = new StringBuilder();
    StringBuilder missingBitmaps = new StringBuilder();
    StringBuilder wrongBitmapsSize = new StringBuilder();


    public Validate_MapWayPointItemStyle(SkinEditorGame game, SavableSvgSkin validationSkin) {
        super(game, validationSkin);
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

//
//        if (cache.ImTheOwner())
//            return "star";
//        else if (cache.isFound())
//            return "mapFound";
//        else if ((cache.Type == CacheTypes.Mystery) && cache.CorrectedCoordiantesOrMysterySolved())
//            return "mapSolved";
//        else if ((cache.Type == CacheTypes.Multi) && cache.HasStartWaypoint())
//            return "mapMultiStartP"; // Multi with start point
//        else if ((cache.Type == CacheTypes.Mystery) && cache.HasStartWaypoint())
//            return "mapMysteryStartP"; // Mystery without Final but with start point
//        else


        //Check Cache, I'm the owner
        Cache ownerCache = new Cache(0, 0, "test", CacheTypes.Cache, "GCCODE");
        checkCache(ownerCache);


        for (CacheTypes type : CacheTypes.values()) {

            // create a Temp Cache
            Cache cache = new Cache(0, 0, "test", type, "GCCODE");
            cache.setOwner("nicht meiner");
            checkCache(cache);
        }

        setReadyIcon();
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

        small(".small", 2.5f, 4.5f), middle(".middle", 5f, 8.5f), large(".large", 9f, 13f);

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
            missingBitmaps.append(", \n");
        } else { // check Size
            float minWidth = CB.getScaledFloat(fieldSize.min);
            float maxWidth = CB.getScaledFloat(fieldSize.max);
            if (bitmap.getWidth() <= minWidth || bitmap.getWidth() > maxWidth) {
                wrongBitmapsSize.append(styleName);
                wrongBitmapsSize.append(fieldSize.fieldName);
                wrongBitmapsSize.append(" width: " + bitmap.getWidth());
                wrongBitmapsSize.append(", \n");
            }

            if (bitmap.getHeight() <= minWidth || bitmap.getHeight() > maxWidth) {
                wrongBitmapsSize.append(styleName);
                wrongBitmapsSize.append(fieldSize.fieldName);
                wrongBitmapsSize.append(" height: " + bitmap.getHeight());
                wrongBitmapsSize.append(", \n");
            }
        }
    }

}
