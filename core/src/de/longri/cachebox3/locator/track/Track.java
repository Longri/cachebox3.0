/*
 * Copyright (C) 2014-2020 team-cachebox.de
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
package de.longri.cachebox3.locator.track;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.locator.Coordinate;

public class Track {
    private Array<Coordinate> trackPoints;
    private CharSequence name;
    private CharSequence fileName;
    private Color color;
    private boolean isVisible;
    private boolean isActualTrack;
    private double trackLength;
    private double altitudeDifference;


    public Track(CharSequence _name) {
        trackPoints = new Array<>();
        name = _name;
        color = Color.MAGENTA; // or do config?
        fileName = "";
        trackLength = 0;
        isVisible = false;
        isActualTrack = false;
        altitudeDifference = 0;
    }

    public String getName() {
        return name.toString();
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color _color) {
        color = _color;
    }

    public String getFileName() {
        return fileName.toString();
    }

    public void setFileName(String _fileName) {
        fileName = _fileName;
    }

    public Array<Coordinate> getTrackPoints() {
        return trackPoints;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isActualTrack() {
        return isActualTrack;
    }

    public void setActualTrack(boolean actualTrack) {
        isActualTrack = actualTrack;
    }

    public double getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(double trackLength) {
        this.trackLength = trackLength;
    }

    public double getAltitudeDifference() {
        return altitudeDifference;
    }

    public void setAltitudeDifference(double altitudeDifference) {
        this.altitudeDifference = altitudeDifference;
    }

}