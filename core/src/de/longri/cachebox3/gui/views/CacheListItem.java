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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.skin.styles.CacheListItemStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.LogTypes;

/**
 * Created by Longri on 05.09.2016.
 */
public class CacheListItem extends ListViewItem implements Disposable {

    private final CacheItem cacheItem;
    public AbstractCache geoCache;

    CacheListItem(int listIndex, final AbstractCache _geoCache, final float targetWidth) {
        super(listIndex);
        geoCache = _geoCache;
        LogTypes leftLogType = null;
        LogTypes rightLogType = null;
        boolean isAvailable = true;
        if (_geoCache.isFound()) {
            leftLogType = LogTypes.found;
        }

        if (!_geoCache.isAvailable()) {
            rightLogType = LogTypes.temporarily_disabled;
            isAvailable = false;
        }

        if (_geoCache.isArchived()) {
            rightLogType = LogTypes.archived;
            isAvailable = false;
        }
        CacheListItemStyle style = VisUI.getSkin().get(CacheListItemStyle.class);
        cacheItem = new CacheItem(_geoCache.getType(), _geoCache.getGeoCacheName(), (int) (_geoCache.getDifficulty() * 2), (int) (_geoCache.getTerrain() * 2),
                (int) Math.min(_geoCache.getRating() * 2, 5 * 2), _geoCache.getSize(), _geoCache.getSize().toShortString(),
                leftLogType, rightLogType, isAvailable, _geoCache.isFavorite(), _geoCache.getFavoritePoints(), _geoCache.getNumTravelbugs(), style);
        add(cacheItem).expand().fill();
        setWidth(targetWidth);
        invalidate();
        pack();
        addListener(new ClickLongClickListener() {
            // DescriptionView().getContextMenu( needs the correct EventHandler.getSelectedCache()
            @Override
            public boolean clicked(InputEvent event, float x, float y) {
                if (Config.CacheContextMenuShortClickToggle.getValue()) {
                    if (geoCache != EventHandler.getSelectedCache()) {
                        EventHandler.fire(new SelectedCacheChangedEvent(geoCache));
                        EventHandler.fire(new CacheListChangedEvent()); // now its yellow
                    }
                    new DescriptionView().getContextMenu(false).show();
                }
                return false; // if true selection changed won't be recognized
            }

            @Override
            public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                if (geoCache != EventHandler.getSelectedCache()) {
                    EventHandler.fire(new SelectedCacheChangedEvent(geoCache));
                    EventHandler.fire(new CacheListChangedEvent()); // now its yellow
                }
                new DescriptionView().getContextMenu(false).show();
                return false; // if true selection changed won't be recognized
            }
        });
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
        return geoCache.getId();
    }
}
