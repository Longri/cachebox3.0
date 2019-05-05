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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CacheListItemStyle;
import de.longri.cachebox3.gui.widgets.CacheSizeWidget;
import de.longri.cachebox3.gui.widgets.Stars;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.LogTypes;
import de.longri.cachebox3.utils.NamedRunnable;

/**
 * Created by Longri on 31.01.2018.
 */
public class CacheItem extends VisTable implements Disposable {

    final CacheListItemStyle style;
    private final CacheTypes type;
    private final CharSequence cacheName;
    private final Drawable rightTopIcon;
    private boolean needsLayout = true;
    Image arrowImage;
    VisLabel distanceLabel;
    boolean distanceOrBearingChanged = true;
    private final int difficulty;
    private final int terrain;
    private final int vote;
    private final CacheSizes size;
    private final String shortSizeString;
    private final Drawable leftInfoIcon, rightInfoIcon;
    private final boolean isAvailable;
    private final int favPoints;
    private final int tbCount;


    public CacheItem(CacheTypes type, CharSequence cacheName, int difficulty, int terrain,
                     int vote, CacheSizes size, String shortSizeString, LogTypes leftLogType,
                     LogTypes rightLogType, boolean isAvailable, boolean isFavorite, int favPoints,
                     int tbCount, CacheListItemStyle style) {

        this.difficulty = difficulty;
        this.terrain = terrain;
        this.vote = vote;
        this.size = size;
        this.shortSizeString = shortSizeString;
        this.style = style;
        this.type = type;
        this.cacheName = cacheName;
        this.leftInfoIcon = leftLogType == null ? null : leftLogType.getDrawable(style == null ? null : style.logTypesStyle);
        this.rightInfoIcon = rightLogType == null ? null : rightLogType.getDrawable(style == null ? null : style.logTypesStyle);
        this.rightTopIcon = isFavorite ? LogTypes.ownFavorite.getDrawable(style == null ? null : style.logTypesStyle) : null;
        this.isAvailable = isAvailable;
        this.favPoints = favPoints;
        this.tbCount = tbCount;
    }


    public synchronized void layout() {
        if (!needsLayout) {
            super.layout();
            return;
        }

        this.clear();


        if (this.style == null) return;
        if (this.type != null) {
            VisTable iconTable = new VisTable();
            iconTable.add(type.getCacheWidget(style.typeStyle, leftInfoIcon, rightInfoIcon, null, rightTopIcon));
            iconTable.pack();
            iconTable.layout();
            this.add(iconTable).left().top().padRight(CB.scaledSizes.MARGIN);
        }

        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.style.nameFont;
        nameLabelStyle.fontColor = isAvailable ? this.style.nameFontColor : this.style.notAvailableColor == null ? this.style.nameFontColor : this.style.notAvailableColor;
        VisLabel nameLabel = new VisLabel(cacheName, nameLabelStyle);
        nameLabel.setWrap(true);
        this.add(nameLabel).top().expandX().fillX();


        VisTable arrowTable = new VisTable();

        if (this.style.arrow != null) {
            arrowImage = new Image(this.style.arrow, Scaling.none);
            arrowImage.pack();
            arrowImage.setOrigin(arrowImage.getWidth() / 2.0f, arrowImage.getHeight() / 2.0f);
            arrowTable.add(arrowImage).expandY();
            arrowTable.row();
        }

        Label.LabelStyle distanceLabelStyle = new Label.LabelStyle();
        distanceLabelStyle.font = this.style.distanceFont;
        distanceLabelStyle.fontColor = this.style.distanceFontColor;
        distanceLabel = new VisLabel(arrowImage != null ? "---- --" : "   ", distanceLabelStyle);
        arrowTable.add(distanceLabel).padTop(CB.scaledSizes.MARGIN);

        this.add(arrowTable).right();
        this.row();

        VisTable line1 = new VisTable();
        VisLabel dLabel = new VisLabel("D", distanceLabelStyle);
        line1.left();
        line1.add(dLabel);
        Stars difficultyStars = new Stars(this.difficulty, style.starStyle);
        line1.add(difficultyStars);
        VisLabel sLabel = new VisLabel(shortSizeString, distanceLabelStyle);
        line1.add(sLabel).padLeft(CB.scaledSizes.MARGIN);
        CacheSizeWidget sizeWidget = new CacheSizeWidget(this.size, style.cacheSizeStyle);
        line1.add(sizeWidget).padLeft(CB.scaledSizes.MARGIN_HALF);

        if (this.tbCount > 0) {
            // don't show we have no TB's
            Image favpointIcon = new Image(style.trackable);
            line1.add(favpointIcon).padLeft(CB.scaledSizes.MARGIN).align(Align.top);
            VisLabel fLabel = new VisLabel("x" + Integer.toString(this.tbCount), distanceLabelStyle);
            line1.add(fLabel);
        }


        this.add(line1).colspan(3).align(Align.left);
        this.row();

        VisTable line2 = new VisTable();
        VisLabel tLabel = new VisLabel("T", distanceLabelStyle);
        line2.left();
        line2.add(tLabel);
        Stars terrainStars = new Stars(this.terrain, style.starStyle);
        line2.add(terrainStars);

        VisLabel vLabel = new VisLabel("GcV", distanceLabelStyle);
        line2.add(vLabel).padLeft(CB.scaledSizes.MARGIN);
        Stars vStars = new Stars(this.vote, style.starStyle);
        line2.add(vStars);

        if (this.favPoints > 0) {
            // don't show we have no favpoint info's
            Image favpointIcon = new Image(style.favoritPoints);
            line2.add(favpointIcon).padLeft(CB.scaledSizes.MARGIN).align(Align.top);
            VisLabel fLabel = new VisLabel("x" + Integer.toString(this.favPoints), distanceLabelStyle);
            line2.add(fLabel);
        }


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
        CB.postOnGlThread(new NamedRunnable("Post on GlThread") {
            @Override
            public void run() {
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
        });
    }
}
