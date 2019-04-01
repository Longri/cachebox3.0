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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.skin.styles.MapBubbleStyle;
import de.longri.cachebox3.gui.views.CacheItem;
import de.longri.cachebox3.gui.views.WayPointItem;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.LogTypes;
import org.oscim.core.MercatorProjection;

/**
 * Created by Longri on 31.01.2018.
 */
public class MapBubble extends Catch_Table {

    private final MapBubbleStyle style;
    private final AbstractCache cache;
    private final AbstractWaypoint waypoint;
    private final Drawable background;
    VisTable content;

    public MapBubble(Object dataObject) {
        this(dataObject instanceof AbstractCache ? (AbstractCache) dataObject : null, dataObject instanceof AbstractWaypoint ? (AbstractWaypoint) dataObject : null);
    }

    private MapBubble(AbstractCache cache, AbstractWaypoint waypoint) {
        this.cache = cache;
        this.waypoint = waypoint;
        style = VisUI.getSkin().get("bubble", MapBubbleStyle.class);

        boolean isSelected = false;

        if (cache != null) {

            LogTypes left = null;
            LogTypes right = null;
            boolean isAvailable = true;
            if (cache.isFound()) {
                left = LogTypes.found;
            }

            if (!cache.isAvailable()) {
                right = LogTypes.temporarily_disabled;
                isAvailable = false;
            }

            if (cache.isArchived()) {
                right = LogTypes.archived;
                isAvailable = false;
            }

            content = new CacheItem(null, cache.getName(),
                    (int) (cache.getDifficulty() * 2), (int) (cache.getTerrain() * 2),
                    (int) Math.min(cache.getRating() * 2, 5 * 2), cache.getSize(),
                    cache.getSize().toShortString(), left, right, isAvailable, cache.isFavorite(),
                    cache.getFavoritePoints(), cache.getNumTravelbugs(), style.cacheListItemStyle);
            isSelected = (EventHandler.getSelectedWaypoint() == null && cache == EventHandler.getSelectedCache());
        } else if (waypoint != null) {

            content = new WayPointItem(null,
                    waypoint.getGcCode(), waypoint.getTitle(),
                    waypoint.getDescription(), waypoint.FormatCoordinate(), style.wayPointListItemStyle);
            isSelected = waypoint == EventHandler.getSelectedWaypoint();

        } else {
            content = null;
        }

        this.background = (isSelected ? style.selectedBackground : style.background);
        content.pack();
        this.addActor(content);
    }

    public void layout() {
        if (content == null) return;
        super.layout();
        content.invalidate();
        content.pack();
        content.layout();
        float defaultPad = CB.getScaledFloat(10);
        float leftPad = defaultPad;
        float rightPad = defaultPad;
        float topPad = defaultPad;
        float bottomPad = defaultPad;
        float width;
        float height;

        if (background != null) {
            if (background instanceof NinePatchDrawable) {
                leftPad = background.getLeftWidth();
                rightPad = background.getRightWidth();
                topPad = background.getTopHeight();
                bottomPad = background.getBottomHeight();
                width = leftPad + content.getWidth() + rightPad;
                height = topPad + content.getHeight() + bottomPad;
            } else {
                width = Math.max(background.getMinWidth() + (2 * defaultPad), content.getWidth() + (2 * defaultPad));
                height = Math.max(background.getMinHeight() + (2 * defaultPad), content.getHeight() + (2 * defaultPad));
            }
        } else {
            width = content.getWidth();
            height = content.getHeight();
        }

        setSize(width, height);
        content.setPosition(leftPad, bottomPad);
    }

    public void draw(Batch batch, float parentAlpha) {
        validate();
        if (isTransform()) {
            applyTransform(batch, computeTransform());
            drawBackground(batch, parentAlpha, 0, 0);
            drawChildren(batch, parentAlpha);
            resetTransform(batch);
        } else {
            drawBackground(batch, parentAlpha, getX(), getY());
            super.draw(batch, parentAlpha);
        }
    }

    /**
     * Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
     * drawable.
     */
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        if (background == null) return;
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        background.draw(batch, x, y, getWidth(), getHeight());
    }

    public float getOffsetX() {
        return CB.getScaledFloat(style.offsetX);
    }

    public float getOffsetY() {
        return CB.getScaledFloat(style.offsetY);
    }


    public float getMinWidth() {
        if (style.minWidth <= 0) return super.getWidth();
        return CB.getScaledFloat(style.minWidth);
    }

    public double getCoordX() {
        if (cache != null) {
            return MercatorProjection.longitudeToX(cache.getLongitude());
        }
        if (waypoint != null) {
            return MercatorProjection.longitudeToX(waypoint.getLongitude());
        }
        return 0;
    }

    public double getCoordY() {
        if (cache != null) {
            return MercatorProjection.latitudeToY(cache.getLatitude());
        }
        if (waypoint != null) {
            return MercatorProjection.latitudeToY(waypoint.getLatitude());
        }
        return 0;
    }
}
