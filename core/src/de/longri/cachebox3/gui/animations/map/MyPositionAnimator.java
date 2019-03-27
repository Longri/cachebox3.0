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
import de.longri.cachebox3.locator.LatLong;
import org.oscim.layers.LocationTextureLayer;

import static de.longri.cachebox3.gui.animations.map.DoubleAnimator.DEFAULT_DURATION;

/**
 * Created by Longri on 15.03.2018.
 */
public class MyPositionAnimator {

    private final DoubleAnimator posLatitude, posLongitude, heading, accuracy;
    private final DirectLineLayer directLineLayer;
    private final LocationTextureLayer myLocationLayer;
    private double actLatitude, actLongitude;
    private float actHead, actAccuracy;

    public MyPositionAnimator(DirectLineLayer directLineLayer, LocationTextureLayer myLocationLayer) {
        this.directLineLayer = directLineLayer;
        this.myLocationLayer = myLocationLayer;
        this.posLatitude = new DoubleAnimator();
        this.posLongitude = new DoubleAnimator();
        this.heading = new DoubleAnimator();
        this.accuracy = new DoubleAnimator();
    }

    public void update(float delta) {
        boolean posChanged = false;
        boolean accuracyChanged = false;
        boolean headingChanged = false;

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
            headingChanged = true;
        }

        if (accuracy.update(delta)) {
            actAccuracy = (float) accuracy.getAct();
            accuracyChanged = true;
        }

        if (posChanged) {
            myLocationLayer.setPosition(actLatitude, actLongitude, actHead, actAccuracy);
            directLineLayer.redrawLine(new LatLong(actLatitude, actLongitude));
        } else if (accuracyChanged || headingChanged) {
            myLocationLayer.setPosition(actLatitude, actLongitude, actHead, actAccuracy);
        }

    }

    public void setPosition(double latitude, double longitude) {
        setPosition(DEFAULT_DURATION, latitude, longitude);
    }

    public void setPosition(float duration, double latitude, double longitude) {
        this.posLatitude.start(duration, actLatitude, latitude);
        this.posLongitude.start(duration, actLongitude, longitude);
    }

    public void setArrowHeading(float heading) {
        this.heading.start(DEFAULT_DURATION, actHead, heading);
    }

    public void setAccuracy(double accuracy) {
        this.accuracy.start(DEFAULT_DURATION, actAccuracy, accuracy);
    }
}
