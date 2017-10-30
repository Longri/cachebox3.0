/*
 * Copyright (C) 2014-2017 team-cachebox.de
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

import java.util.ArrayList;

public class Track {
    public ArrayList<TrackPoint> Points;
    public CharSequence Name;
    public String FileName;
    public boolean ShowRoute = false;
    public boolean IsActualTrack = false;
    public Color mColor;
    public double TrackLength;
    public double AltitudeDifference;

    public Track(CharSequence name, Color color) {
        Points = new ArrayList<TrackPoint>();
        Name = name;
        mColor = color;
    }

    public Color getColor() {
        return mColor;
    }

    public void setColor(Color color) {
        mColor = color;
    }

}