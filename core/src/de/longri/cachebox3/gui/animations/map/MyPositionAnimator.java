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
package de.longri.cachebox3.gui.animations.map;

import de.longri.cachebox3.gui.map.layer.DirectLineLayer;
import de.longri.cachebox3.gui.map.layer.LocationAccuracyLayer;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import de.longri.cachebox3.locator.LatLong;

import static de.longri.cachebox3.gui.animations.map.DoubleAnimator.DEFAULT_DURATION;

/**
 * Created by Longri on 15.03.2018.
 */
public class MyPositionAnimator {

    private final DoubleAnimator posLatitude, posLongitude, heading, accuracy;
    private final DirectLineLayer directLineLayer;
    private final LocationLayer myLocationLayer;
    private final LocationAccuracyLayer myLocationAccuracy;
    private double actLatitude, actLongitude, actAccuracy;
    private float actHead;

    public MyPositionAnimator(DirectLineLayer directLineLayer, LocationLayer myLocationLayer, LocationAccuracyLayer myLocationAccuracy) {
        this.directLineLayer = directLineLayer;
        this.myLocationLayer = myLocationLayer;
        this.myLocationAccuracy = myLocationAccuracy;
        this.posLatitude = new DoubleAnimator();
        this.posLongitude = new DoubleAnimator();
        this.heading = new DoubleAnimator();
        this.accuracy = new DoubleAnimator();
    }

    public void update(float delta) {
        boolean posChanged = false;
        boolean accuracyChanged = false;

        if (posLatitude.update(delta)) {
            actLatitude = posLatitude.getAct();
            posChanged = true;
        }

        if (posLongitude.update(delta)) {
            actLongitude = posLongitude.getAct();
            posChanged = true;
        }

        if (heading.update(delta)) {
            actHead = (float) heading.getAct();
            posChanged = true;
        }

        if (accuracy.update(delta)) {
            actAccuracy = accuracy.getAct();
            accuracyChanged = true;
        }

        if (posChanged) {
            myLocationLayer.setPosition(actLatitude, actLongitude, actHead);
            directLineLayer.redrawLine(new LatLong(actLatitude, actLongitude));
            myLocationAccuracy.setPosition(actLatitude, actLongitude, actAccuracy);
        } else if (accuracyChanged) {
            myLocationAccuracy.setPosition(actLatitude, actLongitude, actAccuracy);
        }

    }

    public void setPosition(double latitude, double longitude) {
        this.posLatitude.start(DEFAULT_DURATION, actLatitude, latitude);
        this.posLongitude.start(DEFAULT_DURATION, actLongitude, longitude);
    }

    public void setArrowHeading(float heading) {
        this.heading.start(DEFAULT_DURATION, actHead, heading);
    }

    private void setAccuracy(double accuracy) {
        this.accuracy.start(DEFAULT_DURATION, actAccuracy, accuracy);
    }
}
