/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CacheListItemStyle;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.gui.widgets.CacheSizeWidget;
import de.longri.cachebox3.gui.widgets.Stars;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.LogTypes;

/**
 * Created by Longri on 05.09.2016.
 */
public class CacheListItem extends ListViewItem implements Disposable {

    public static ListViewItem getListItem(int listIndex, final Cache cache) {
        if (cache == null) return null;


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


        ListViewItem listViewItem = new CacheListItem(listIndex, cache.getType(), cache.getName(),
                (int) (cache.getDifficulty() * 2), (int) (cache.getTerrain() * 2),
                (int) Math.min(cache.getRating() * 2, 5 * 2), cache.getSize(), cache.getSize().toShortString(), left, right, isAvailable);
        return listViewItem;
    }


    private final CacheListItemStyle style;
    private final CacheTypes type;
    private final CharSequence cacheName;
    private boolean needsLayout = true;
    private Image arrowImage;
    private VisLabel distanceLabel;
    private boolean distanceOrBearingChanged = true;
    private final int difficulty;
    private final int terrain;
    private final int vote;
    private final CacheSizes size;
    private final String shortSizeString;
    private final Drawable leftInfoIcon, rightInfoIcon;
    private final boolean isAvailable;


    private CacheListItem(int listIndex, CacheTypes type, CharSequence cacheName, int difficulty, int terrain,
                          int vote, CacheSizes size, String shortSizeString, LogTypes leftLogType,
                          LogTypes rightLogType, boolean isAvailable) {
        super(listIndex);
        this.difficulty = difficulty;
        this.terrain = terrain;
        this.vote = vote;
        this.size = size;
        this.shortSizeString = shortSizeString;
        this.style = VisUI.getSkin().get("cacheListItems", CacheListItemStyle.class);
        this.type = type;
        this.cacheName = cacheName;
        this.leftInfoIcon = leftLogType == null ? null : leftLogType.getDrawable(style.logTypesStyle);
        this.rightInfoIcon = rightLogType == null ? null : rightLogType.getDrawable(style.logTypesStyle);
        this.isAvailable = isAvailable;
    }


    public synchronized void layout() {
        if (!needsLayout) {
            super.layout();
            return;
        }

        this.clear();

        VisTable iconTable = new VisTable();
        iconTable.add(type.getCacheWidget(style.typeStyle, leftInfoIcon, rightInfoIcon));
        iconTable.pack();
        iconTable.layout();

        this.add(iconTable).left().top().padRight(CB.scaledSizes.MARGIN);


        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.style.nameFont;
        nameLabelStyle.fontColor = isAvailable ? this.style.nameFontColor : this.style.notAvailableColor == null ? this.style.nameFontColor : this.style.notAvailableColor;
        VisLabel nameLabel = new VisLabel(cacheName, nameLabelStyle);
        nameLabel.setWrap(true);
        this.add(nameLabel).top().expandX().fillX();


        VisTable arrowTable = new VisTable();

        arrowImage = new Image(this.style.arrow);
        arrowImage.setOrigin(this.style.arrow.getMinWidth() / 2, this.style.arrow.getMinHeight() / 2);

        Label.LabelStyle distanceLabelStyle = new Label.LabelStyle();
        distanceLabelStyle.font = this.style.distanceFont;
        distanceLabelStyle.fontColor = this.style.distanceFontColor;
        distanceLabel = new VisLabel("---- --", distanceLabelStyle);

        arrowTable.add(arrowImage);
        arrowTable.row();
        arrowTable.add(distanceLabel).padTop(CB.scaledSizes.MARGIN);
        this.add(arrowTable).right();

        this.row();

        VisTable line1 = new VisTable();
        VisLabel dLabel = new VisLabel("D", distanceLabelStyle);
        line1.left();
        line1.add(dLabel);
        Stars difficultyStars = new Stars(this.difficulty);
        line1.add(difficultyStars);
        VisLabel sLabel = new VisLabel(shortSizeString, distanceLabelStyle);
        line1.add(sLabel).padLeft(CB.scaledSizes.MARGIN);
        CacheSizeWidget sizeWidget = new CacheSizeWidget(this.size);
        line1.add(sizeWidget).padLeft(CB.scaledSizes.MARGIN_HALF);


        this.add(line1).colspan(3).align(Align.left);
        this.row();

        VisTable line2 = new VisTable();
        VisLabel tLabel = new VisLabel("T", distanceLabelStyle);
        line2.left();
        line2.add(tLabel);
        Stars terrainStars = new Stars(this.terrain);
        line2.add(terrainStars);

        VisLabel vLabel = new VisLabel("GcV", distanceLabelStyle);
        line2.add(vLabel).padLeft(CB.scaledSizes.MARGIN);
        Stars vStars = new Stars(this.vote);
        line2.add(vStars);

        this.add(line2).colspan(3).align(Align.left);

        super.layout();
        needsLayout = false;
    }

    public boolean update(float bearing, CharSequence distance) {
        if (!distanceOrBearingChanged) return false;
        arrowImage.setRotation(bearing);
        distanceLabel.setText(distance);

        arrowImage.layout();
        distanceLabel.layout();
        distanceOrBearingChanged = false;
        return true;
    }

    public void posOrBearingChanged() {
        distanceOrBearingChanged = true;
    }

    @Override
    public synchronized void dispose() {
        if (arrowImage != null) {
            arrowImage.setDrawable(null);
            arrowImage.clear();
        }
        arrowImage = null;

        if (distanceLabel != null) {
            distanceLabel.setText(null);
            distanceLabel.clear();
        }
        distanceLabel = null;
    }
}
