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

    private final WayPointListItemStyle wayPointListItemStyle;
    private final CacheTypes cacheTypes;
    final CharSequence wayPointGcCode, description, wayPointTitle, coord;
    private boolean needsLayout = true;
    Image arrowImage;
    VisLabel distanceLabel;
    boolean distanceOrBearingChanged = true;

    public WayPointItem(CacheTypes _cacheTypes, CharSequence _wayPointGcCode, CharSequence _wayPointTitle,
                        CharSequence _description, CharSequence _coord, WayPointListItemStyle _wayPointListItemStyle) {
        super();
        wayPointListItemStyle = _wayPointListItemStyle;
        cacheTypes = _cacheTypes;
        wayPointGcCode = _wayPointGcCode;
        wayPointTitle = _wayPointTitle;
        description = _description;
        coord = _coord;
    }


    public synchronized void layout() {
//        setDebug(true, false);
        if (!needsLayout) {
            super.layout();
            return;
        }

        clear();

        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = wayPointListItemStyle.nameFont;
        nameLabelStyle.fontColor = wayPointListItemStyle.nameFontColor;


        arrowImage = new Image(wayPointListItemStyle.arrow);

        Label.LabelStyle distanceLabelStyle = new Label.LabelStyle();
        distanceLabelStyle.font = wayPointListItemStyle.distanceFont;
        distanceLabelStyle.fontColor = wayPointListItemStyle.distanceFontColor;

        distanceLabel = new VisLabel("---- --", distanceLabelStyle);


        if (cacheTypes != null) {
            VisTable iconTable = new VisTable();
            iconTable.add(cacheTypes.getCacheWidget(wayPointListItemStyle.cacheTypeStyle, null, null, null, null));
            iconTable.pack();
            iconTable.layout();
            add(iconTable).left().top().padRight(CB.scaledSizes.MARGINx2);
        }

        Table contentTable = new Table();
        VisLabel nameLabel = new VisLabel(wayPointGcCode + ": " + wayPointTitle, nameLabelStyle);
        VisLabel descriptionLabel = description == null ? null : new VisLabel(description, nameLabelStyle);
        VisLabel coordLabel = new VisLabel(coord, nameLabelStyle);

        if (cacheTypes != null) {
            nameLabel.setWrap(true);
            coordLabel.setWrap(true);
        }
        if (descriptionLabel != null) descriptionLabel.setWrap(true);
        contentTable.add(nameLabel).left().expandX().fillX();
        contentTable.row();
        contentTable.add(descriptionLabel).left().expandX().fillX();
        contentTable.row();
        contentTable.add(coordLabel).left().expandX().fillX();

        contentTable.invalidate();
        contentTable.pack();

        add(contentTable).expandX().fillX();

        if (cacheTypes != null) {
            VisTable arrowTable = new VisTable();
            if (wayPointListItemStyle.arrow != null) {
                arrowImage.setOrigin(wayPointListItemStyle.arrow.getMinWidth() / 2, wayPointListItemStyle.arrow.getMinHeight() / 2);
                arrowTable.add(arrowImage);
                arrowTable.row();
            }

            arrowTable.add(distanceLabel).padTop(CB.scaledSizes.MARGIN);
            add(arrowTable).right();
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
        return wayPointGcCode;
    }
}
