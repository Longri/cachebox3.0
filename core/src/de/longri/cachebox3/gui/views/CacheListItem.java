/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.skin.styles.CacheListItemStyle;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.LogTypes;

/**
 * Created by Longri on 05.09.2016.
 */
public class CacheListItem extends ListViewItem implements Disposable {


    public static ListViewItem getListItem(int listIndex, final AbstractCache abstractCache, final float targetWidth) {
        if (abstractCache == null) return null;

        LogTypes left = null;
        LogTypes right = null;
        boolean isAvailable = true;
        if (abstractCache.isFound()) {
            left = LogTypes.found;
        }

        if (!abstractCache.isAvailable()) {
            right = LogTypes.temporarily_disabled;
            isAvailable = false;
        }

        if (abstractCache.isArchived()) {
            right = LogTypes.archived;
            isAvailable = false;
        }

        CacheListItem cacheListItem = new CacheListItem(listIndex, abstractCache.getId(), abstractCache.getType(), abstractCache.getName(),
                (int) (abstractCache.getDifficulty() * 2), (int) (abstractCache.getTerrain() * 2),
                (int) Math.min(abstractCache.getRating() * 2, 5 * 2), abstractCache.getSize(),
                abstractCache.getSize().toShortString(), left, right, isAvailable, abstractCache.isFavorite(),
                abstractCache.getFavoritePoints(), abstractCache.getNumTravelbugs());
        cacheListItem.setWidth(targetWidth);
        cacheListItem.invalidate();
        cacheListItem.pack();
        return cacheListItem;
    }

    private final CacheItem cacheItem;
    private final long cacheId;

    private CacheListItem(int listIndex, long cacheId, CacheTypes type, CharSequence cacheName, int difficulty, int terrain,
                          int vote, CacheSizes size, String shortSizeString, LogTypes leftLogType,
                          LogTypes rightLogType, boolean isAvailable, boolean isFavorite, int favPoints, int numOfTb) {
        super(listIndex);
        CacheListItemStyle style = VisUI.getSkin().get("cacheListItems", CacheListItemStyle.class);
        cacheItem = new CacheItem(type, cacheName, difficulty, terrain, vote, size, shortSizeString,
                leftLogType, rightLogType, isAvailable, isFavorite, favPoints, numOfTb, style);
        this.add(cacheItem).expand().fill();
        this.cacheId = cacheId;
    }

    public boolean update(float bearing, CharSequence distance) {
        if (!cacheItem.distanceOrBearingChanged) return false;
        cacheItem.arrowImage.setRotation(bearing);
        cacheItem.distanceLabel.setText(distance);
        cacheItem.arrowImage.layout();
        cacheItem.distanceLabel.layout();
        cacheItem.distanceOrBearingChanged = false;
        return true;
    }

    public void posOrBearingChanged() {
        cacheItem.distanceOrBearingChanged = true;
    }

    @Override
    public synchronized void dispose() {
        cacheItem.dispose();
    }

    public long getId() {
        return this.cacheId;
    }
}
