/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.location.SpeedChangedEvent;
import de.longri.cachebox3.events.location.SpeedChangedListener;
import de.longri.cachebox3.gui.map.layer.MapOrientationMode;
import de.longri.cachebox3.gui.skin.styles.MapInfoPanelStyle;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 21.03.2017.
 */
public class MapInfoPanel extends Catch_Table implements SpeedChangedListener, Disposable {

    private final Compass compass;
    private final VisLabel distanceLabel, speedLabel, distanceUnitLabel, speedUnitLabel, coordinateLabel1, coordinateLabel2;

    private float aktDistance = -1;


    public MapInfoPanel() {
        EventHandler.add(this);
        MapInfoPanelStyle style = VisUI.getSkin().get("infoPanel", MapInfoPanelStyle.class);
        this.setBackground(style.background);
        compass = new Compass("mapCompassStyle", true);


        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = style.distanceLabel_Font;
        labelStyle.fontColor = style.distanceLabel_Color;
        distanceLabel = new VisLabel("---", labelStyle);

        Label.LabelStyle labelStyle2 = new Label.LabelStyle();
        labelStyle2.font = style.speedLabel_Font;
        labelStyle2.fontColor = style.speedLabel_Color;
        speedLabel = new VisLabel("+++", labelStyle2);

        Label.LabelStyle labelStyle3 = new Label.LabelStyle();
        labelStyle3.font = style.distanceUnitLabel_Font;
        labelStyle3.fontColor = style.distanceUnitLabel_Color;
        distanceUnitLabel = new VisLabel("km", labelStyle3);

        Label.LabelStyle labelStyle4 = new Label.LabelStyle();
        labelStyle4.font = style.speedUnitLabel_Font;
        labelStyle4.fontColor = style.speedUnitLabel_Color;
        speedUnitLabel = new VisLabel("kmh", labelStyle4);

        Label.LabelStyle labelStyle5 = new Label.LabelStyle();
        labelStyle5.font = style.coordinateLabel_Font;
        labelStyle5.fontColor = style.coordinateLabel_Color;
        coordinateLabel1 = new VisLabel("------------", labelStyle5);
        coordinateLabel2 = new VisLabel("============", labelStyle5);


        // add controls to table
        this.add(compass).left();

        Table nestedTable = new Table();
//        nestedTable.setDebug(true);
        nestedTable.add(distanceLabel).right().bottom();
        nestedTable.add(distanceUnitLabel).left().bottom().padLeft(CB.scaledSizes.MARGIN);
        nestedTable.add(coordinateLabel1).expandX().right().bottom().padLeft(CB.scaledSizes.MARGINx2);
        nestedTable.row();
        nestedTable.add(speedLabel).right().bottom();
        nestedTable.add(speedUnitLabel).left().bottom().padLeft(CB.scaledSizes.MARGIN);
        nestedTable.add(coordinateLabel2).expandX().right().bottom().padLeft(CB.scaledSizes.MARGINx2);
        this.add(nestedTable).fillX().expandX().padLeft(CB.scaledSizes.MARGINx2);

        this.pack();
    }

    public void setNewValues(final Coordinate myPosition, final float bearing) {
        if (myPosition == null) return;
        CB.postOnGlThread(new NamedRunnable("set new values") {
            @Override
            public void run() {
                compass.setBearing(bearing);
                coordinateLabel1.setText(UnitFormatter.formatLatitudeDM(myPosition.getLatitude()));
                coordinateLabel2.setText(UnitFormatter.formatLongitudeDM(myPosition.getLongitude()));

                if (EventHandler.getSelectedCoord() != null) {
                    Coordinate targetCoordinate = EventHandler.getSelectedCoord();
                    setDistance(targetCoordinate.distance(MathUtils.CalculationType.ACCURATE));
                    compass.setHeading(myPosition.bearingTo(targetCoordinate, MathUtils.CalculationType.ACCURATE) - bearing);
                }
            }
        });
    }

    private void setDistance(float distance) {
        distance = Math.round(distance);
        if (distanceLabel == null)
            return;
        if (aktDistance == distance)
            return;
        aktDistance = distance;
        try {
            if (distance == -1) {
                distanceLabel.setText("?");
                distanceUnitLabel.setText("");
            } else {
                String[] strings = UnitFormatter.distanceString(distance, true).split(" ", 2);
                distanceLabel.setText(strings[0]);
                distanceUnitLabel.setText(strings[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void speedChanged(SpeedChangedEvent event) {
        String[] strings = UnitFormatter.speedString(event.speed, true).split(" ", 2);
        speedLabel.setText(strings[0]);
        speedUnitLabel.setText(strings[1]);
    }

    @Override
    public void dispose() {
        EventHandler.remove(this);
    }

    public String toString() {
        return "MapInfoPanel";
    }

    public float getPrefHeight() {
        return compass.getPrefHeight() + CB.scaledSizes.MARGINx2;
    }

    public void setMapOrientationMode(MapOrientationMode state) {
        compass.setState(state);
    }

    public MapOrientationMode getOrientationState() {
        return compass.getState();
    }

    public void setStateChangedListener(Compass.StateChanged listener) {
        compass.setStateChangedListener(listener);
    }
}
