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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.gui.skin.styles.MapInfoPanelStyle;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.locator.events.newT.EventHandler;
import de.longri.cachebox3.locator.events.newT.SpeedChangedEvent;
import de.longri.cachebox3.locator.events.newT.SpeedChangedListener;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 21.03.2017.
 */
public class MapInfoPanel extends Table implements SpeedChangedListener, Disposable {

    final MapInfoPanelStyle style;
    final Compass compass;
    final VisLabel distanceLabel, speedLabel, coordinateLabel1, coordinateLabel2;

    public MapInfoPanel() {
        EventHandler.add(this);
        style = VisUI.getSkin().get("infoPanel", MapInfoPanelStyle.class);
        this.setBackground(style.background);
        this.setDebug(true);
        compass = new Compass("mapCompassStyle");

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = style.distanceLabel_Font;
        labelStyle.fontColor = style.distanceLabel_Color;
        distanceLabel = new VisLabel("---", labelStyle);

        Label.LabelStyle labelStyle2 = new Label.LabelStyle();
        labelStyle2.font = style.speedLabel_Font;
        labelStyle2.fontColor = style.speedLabel_Color;
        speedLabel = new VisLabel("+++", labelStyle2);

        Label.LabelStyle labelStyle3 = new Label.LabelStyle();
        labelStyle3.font = style.coordinateLabel_Font;
        labelStyle3.fontColor = style.coordinateLabel_Color;
        coordinateLabel1 = new VisLabel("------------", labelStyle3);
        coordinateLabel2 = new VisLabel("============", labelStyle3);


        // add controls to table
        this.add(compass).center();

        Table nestedTable = new Table();

        nestedTable.add(distanceLabel).center();
        nestedTable.add(coordinateLabel1).center();
        nestedTable.row();
        nestedTable.add(speedLabel).center();
        nestedTable.add(coordinateLabel2).center();
        this.add(nestedTable).expandX();

        this.pack();
    }


    public void setNewValues(CoordinateGPS myPosition, float heading) {
        compass.setHeading(heading);
        coordinateLabel1.setText(UnitFormatter.FormatLatitudeDM(myPosition.getLatitude()));
        coordinateLabel2.setText(UnitFormatter.FormatLongitudeDM(myPosition.getLongitude()));

        if (EventHandler.getSelectedCoord() != null)
            setDistance(EventHandler.getSelectedCoord().distance(MathUtils.CalculationType.ACCURATE));
    }

    private float aktDistance = -1;

    private void setDistance(float distance) {
        if (distanceLabel == null)
            return;
        if (aktDistance == distance)
            return;
        aktDistance = distance;
        try {
            if (distance == -1)
                distanceLabel.setText("?");
            else
                distanceLabel.setText(UnitFormatter.DistanceString(distance));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void speedChanged(SpeedChangedEvent event) {
        speedLabel.setText(UnitFormatter.SpeedString(event.speed));
    }

    @Override
    public void dispose() {
        EventHandler.remove(this);
    }

    public String toString() {
        return "MapInfoPanel";
    }
}
