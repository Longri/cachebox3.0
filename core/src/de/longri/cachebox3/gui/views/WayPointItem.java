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
    final CharSequence wayPointGcCode, description, wayPointTitle, coord;
    private boolean needsLayout = true;
    Image arrowImage;
    VisLabel distanceLabel;
    boolean distanceOrBearingChanged = true;

    public WayPointItem(CacheTypes type, CharSequence wayPointGcCode, CharSequence wayPointTitle,
                        CharSequence description, CharSequence coord, WayPointListItemStyle style) {
        super();
        this.style = style;
        this.type = type;
        this.wayPointGcCode = wayPointGcCode;
        this.wayPointTitle = wayPointTitle;
        this.description = description;
        this.coord = coord;
    }


    public synchronized void layout() {
//        this.setDebug(true, false);
        if (!needsLayout) {
            super.layout();
            return;
        }

        this.clear();

        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.style.nameFont;
        nameLabelStyle.fontColor = this.style.nameFontColor;


        arrowImage = new Image(this.style.arrow);

        Label.LabelStyle distanceLabelStyle = new Label.LabelStyle();
        distanceLabelStyle.font = this.style.distanceFont;
        distanceLabelStyle.fontColor = this.style.distanceFontColor;

        distanceLabel = new VisLabel("---- --", distanceLabelStyle);


        if (type != null) {
            VisTable iconTable = new VisTable();
            iconTable.add(type.getCacheWidget(style.typeStyle, null, null, null, null));
            iconTable.pack();
            iconTable.layout();
            this.add(iconTable).left().top().padRight(CB.scaledSizes.MARGINx2);
        }

        Table contentTable = new Table();
        VisLabel nameLabel = new VisLabel(wayPointGcCode + ": " + wayPointTitle, nameLabelStyle);
        VisLabel descriptionLabel = description == null ? null : new VisLabel(description, nameLabelStyle);
        VisLabel coordLabel = new VisLabel(coord, nameLabelStyle);

        if (type != null) {
            nameLabel.setWrap(true);
            coordLabel.setWrap(true);
        }
        descriptionLabel.setWrap(true);
        contentTable.add(nameLabel).left().expandX().fillX();
        contentTable.row();
        contentTable.add(descriptionLabel).left().expandX().fillX();
        contentTable.row();
        contentTable.add(coordLabel).left().expandX().fillX();

        contentTable.invalidate();
        contentTable.pack();

        this.add(contentTable).expandX().fillX();

        if (type != null) {
            VisTable arrowTable = new VisTable();
            if (this.style.arrow != null) {
                arrowImage.setOrigin(this.style.arrow.getMinWidth() / 2, this.style.arrow.getMinHeight() / 2);
                arrowTable.add(arrowImage);
                arrowTable.row();
            }

            arrowTable.add(distanceLabel).padTop(CB.scaledSizes.MARGIN);
            this.add(arrowTable).right();
        }
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
