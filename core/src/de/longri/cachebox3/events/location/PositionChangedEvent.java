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
package de.longri.cachebox3.events.location;

import com.badlogic.gdx.utils.Pool;
import de.longri.cachebox3.events.AbstractPoolableEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;

import java.util.Date;

/**
 * Created by Longri on 23.03.2017.
 */
public class PositionChangedEvent extends AbstractPoolableEvent implements Pool.Poolable {
    private final Coordinate pos = new Coordinate();

    public PositionChangedEvent() {
        super(Coordinate.class);
    }


    @Override
    public Class getListenerClass() {
        return PositionChangedListener.class;
    }

    public void reset() {
        // reset id
        this.ID = EventHandler.getId();
        pos.reset();
    }

    public void setPos(double latitude, double longitude) {
        pos.setLatLon(latitude, longitude);
    }

    public void setElevation(double elevation) {
        pos.setElevation(elevation);
    }

    public void setAccuracy(float accuracy) {
        pos.setAccuracy(accuracy);
    }

    public void setSpeed(double speed) {
        pos.setSpeed(speed);
    }

    public void setHeading(double heading) {
        pos.setHeading(heading);
    }

    public void setIsGpsProvided(boolean isGpsProvided) {
        pos.setIsGpsProvided(isGpsProvided);
    }

    public boolean isGpsProvided() {
        return pos.isGPSprovided();
    }

    public double getElevation() {
        return pos.getElevation();
    }

    public double getLatitude() {
        return pos.getLatitude();
    }

    public double getLongitude() {
        return pos.getLongitude();
    }

    public int getCoordHash() {
        return pos.hashCode();
    }

    public double getHeading() {
        return pos.getHeading();
    }

    public int getAccuracy() {
        return pos.getAccuracy();
    }

    public double getSpeed() {
        return this.pos.getSpeed();
    }

    public void setDate(Date date) {
        this.pos.setDate(date);
    }

    public Date getDate() {
        return this.pos.getDate();
    }
}
