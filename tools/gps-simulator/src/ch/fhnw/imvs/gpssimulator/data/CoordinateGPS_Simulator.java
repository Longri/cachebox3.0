/*
 * Copyright (C) 2019 team-cachebox.de
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
package ch.fhnw.imvs.gpssimulator.data;

import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.locator.CoordinateGPS;

/**
 * Created by Longri on 2019-03-09.
 */
public class CoordinateGPS_Simulator extends CoordinateGPS {


    static double round(double value) {
        int intValue = (int) (value * 1000000.0);
        return intValue / 1000000.0;
    }


    public final double speed;
    public final double altitude;
    public final double course;
    public final double accuracy;
    public final double tilt;

    public CoordinateGPS_Simulator(double latitude, double longitude, double speed, double altitude, double course, double accuracy, double tilt) {
        super(round(latitude), round(longitude));
        this.speed = speed;
        this.altitude = altitude;
        this.course = course;
        this.accuracy = accuracy;
        this.tilt = tilt;
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        if (other instanceof CoordinateGPS_Simulator) {
            CoordinateGPS_Simulator o = (CoordinateGPS_Simulator) other;
            if (this.speed != o.speed) return false;
            if (this.altitude != o.altitude) return false;
            if (this.course != o.course) return false;
            if (this.accuracy != o.accuracy) return false;
            if (this.tilt != o.tilt) return false;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());

        sb.append(" c:").append(course);
        sb.append(" alt:").append(altitude);
        sb.append(" s:").append(speed);
        sb.append(" acc:").append(accuracy);
        sb.append(" tilt:").append(tilt);

        return sb.toString();
    }
}
