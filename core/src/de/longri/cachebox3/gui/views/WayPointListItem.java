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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.WayPointListItemStyle;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.Waypoint;

/**
 * Created by Longri on 03.04.2017.
 */
public class WayPointListItem extends ListViewItem implements Disposable {

    public static WayPointListItem getListItem(int listIndex, final Waypoint waypoint) {
        WayPointListItem listViewItem = new WayPointListItem(listIndex, waypoint.Type, waypoint.getGcCode());
        return listViewItem;
    }


    private final WayPointListItemStyle style;
    private final CacheTypes type;
    private final CharSequence wayPointName;
    private boolean needsLayout = true;
    private Image arrowImage;
    private VisLabel distanceLabel;
    private boolean distanceOrBearingChanged = true;

    public WayPointListItem(int listIndex, CacheTypes type, CharSequence wayPointName) {
        super(listIndex);
        this.style = VisUI.getSkin().get("WayPointListItems", WayPointListItemStyle.class);
        this.type = type;
        this.wayPointName = wayPointName;
    }


    public synchronized void layout() {
//        this.setDebug(true, false);
        if (!needsLayout) {
            super.layout();
            return;
        }

        this.clear();

        VisTable iconTable = new VisTable();
        iconTable.add(type.getCacheWidget(style.typeStyle));

        iconTable.pack();
        iconTable.layout();

        this.add(iconTable).left().top().padRight(CB.scaledSizes.MARGIN);


        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.style.nameFont;
        nameLabelStyle.fontColor = this.style.nameFontColor;
        VisLabel nameLabel = new VisLabel(wayPointName, nameLabelStyle);
//        VisLabel nameLabel = new VisLabel("ITEM: " + this.listIndex, nameLabelStyle);
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

    public String getWaypointName() {
        return wayPointName.toString();
    }
}
