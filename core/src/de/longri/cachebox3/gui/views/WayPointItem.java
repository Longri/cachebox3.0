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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.WayPointListItemStyle;
import de.longri.cachebox3.types.CacheTypes;

/**
 * Created by Longri on 31.01.2018.
 */
public class WayPointItem extends VisTable implements Disposable {

    private final WayPointListItemStyle style;
    private final CacheTypes type;
    final CharSequence wayPointGcCode;
    private boolean needsLayout = true;
    final Image arrowImage;
    final VisLabel distanceLabel;
    final private VisLabel descriptionLabel;
    final private VisLabel coordLabel;
    final private VisLabel nameLabel;
    boolean distanceOrBearingChanged = true;

    public WayPointItem(CacheTypes type, CharSequence wayPointGcCode, CharSequence wayPointTitle,
                        CharSequence description, CharSequence coord, WayPointListItemStyle style) {
        super();
        this.style = style;
        this.type = type;
        this.wayPointGcCode = wayPointGcCode;

        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.style.nameFont;
        nameLabelStyle.fontColor = this.style.nameFontColor;

        nameLabel = new VisLabel(wayPointGcCode + ": " + wayPointTitle, nameLabelStyle);
        descriptionLabel = description == null ? null : new VisLabel(description, nameLabelStyle);
        coordLabel = new VisLabel(coord, nameLabelStyle);

        nameLabel.setWrap(true);
        descriptionLabel.setWrap(true);
        coordLabel.setWrap(true);

        arrowImage = new Image(this.style.arrow);

        Label.LabelStyle distanceLabelStyle = new Label.LabelStyle();
        distanceLabelStyle.font = this.style.distanceFont;
        distanceLabelStyle.fontColor = this.style.distanceFontColor;

        distanceLabel = new VisLabel("---- --", distanceLabelStyle);
    }


    public synchronized void layout() {
//        this.setDebug(true, false);
        if (!needsLayout) {
            super.layout();
            return;
        }

        this.clear();

        VisTable iconTable = new VisTable();
        iconTable.add(type.getCacheWidget(style.typeStyle, null, null));

        iconTable.pack();
        iconTable.layout();

        this.add(iconTable).left().top().padRight(CB.scaledSizes.MARGINx4);

        Table contentTable = new Table();
        contentTable.add(nameLabel).left().expandX().fillX();
        contentTable.row();
        contentTable.add(descriptionLabel).left().expandX().fillX();
        contentTable.row();
        contentTable.add(coordLabel).left().expandX().fillX();

        this.add(contentTable).top().expandX().fillX();

        VisTable arrowTable = new VisTable();
        if (this.style.arrow != null) {
            arrowImage.setOrigin(this.style.arrow.getMinWidth() / 2, this.style.arrow.getMinHeight() / 2);
            arrowTable.add(arrowImage);
            arrowTable.row();
        }

        arrowTable.add(distanceLabel).padTop(CB.scaledSizes.MARGIN);
        this.add(arrowTable).right();
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

        if (distanceLabel != null) {
            distanceLabel.setText(null);
            distanceLabel.clear();
        }
    }

    public CharSequence getWaypointGcCode() {
        return this.wayPointGcCode;
    }
}