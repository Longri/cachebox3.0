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
import de.longri.cachebox3.types.*;
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
        AbstractCache ownerAbstractCache = new MutableCache(0, 0, "test", CacheTypes.Cache, "GCCODE");
        checkCache(ownerAbstractCache);

        //Check Cache, found
        AbstractCache foundAbstractCache = new MutableCache(0, 0, "test", CacheTypes.Cache, "GCCODE");
        foundAbstractCache.setOwner("nicht meiner");
        foundAbstractCache.setFound(true);
        checkCache(foundAbstractCache);

        //Check Cache, solved
        AbstractCache solvedAbstractCache = new MutableCache(0, 0, "test", CacheTypes.Mystery, "GCCODE");
        solvedAbstractCache.setOwner("nicht meiner");
        solvedAbstractCache.setHasCorrectedCoordinates(true);
        checkCache(solvedAbstractCache);


        //Check Cache, multi start
        AbstractCache multiStartAbstractCache = new MutableCache(0, 0, "test", CacheTypes.Multi, "GCCODE");
        multiStartAbstractCache.setOwner("nicht meiner");
        AbstractWaypoint wp = new MutableWaypoint("wp", CacheTypes.MultiStage, 0, 0, 100, "");
        wp.setStart(true);
        multiStartAbstractCache.getWaypoints().add(wp);
        checkCache(multiStartAbstractCache);

        //Check Cache, myst start
        AbstractCache mystStartAbstractCache = new MutableCache(0, 0, "test", CacheTypes.Mystery, "GCCODE");
        mystStartAbstractCache.setOwner("nicht meiner");
        MutableWaypoint wpm = new MutableWaypoint("wp", CacheTypes.MultiStage, 0, 0, 100, "");
        wpm.setStart(true);
        mystStartAbstractCache.getWaypoints().add(wpm);
        checkCache(mystStartAbstractCache);


        {// check multiStageStart
            AbstractWaypoint wpMS = new MutableWaypoint("wp", CacheTypes.MultiStage, 0, 0, 100, "");
            wpMS.setStart(true);

            MapWayPointItemStyle style = null;
            try {
                style = WaypointLayer.getClusterSymbolsByWaypoint(wpMS);
            } catch (GdxRuntimeException e) {
            }
            String styleName = WaypointLayer.getMapIconName(wpMS);

            if (style == null) {
                missingSyles.append(styleName);
                missingSyles.append("\n");
            } else {
                checkBitmap(style.small, styleName, Size.small);
                checkBitmap(style.middle, styleName, Size.middle);
                checkBitmap(style.large, styleName, Size.large);
            }
        }

        for (CacheTypes type : CacheTypes.values()) {
            // create a Temp Cache
            AbstractCache abstractCache = new MutableCache(0, 0, "test", type, "GCCODE");
            abstractCache.setOwner("nicht meiner");
            checkCache(abstractCache);
        }
    }


    private void checkCache(AbstractCache abstractCache) {
        MapWayPointItemStyle style = null;
        try {
            style = WaypointLayer.getClusterSymbolsByCache(abstractCache);
        } catch (GdxRuntimeException e) {
        }

        String styleName = WaypointLayer.getMapIconName(abstractCache);

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
