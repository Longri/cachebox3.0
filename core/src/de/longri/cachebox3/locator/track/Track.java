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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.locator.Coordinate;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;

import java.util.ArrayList;

public class Track extends Array<Coordinate> {
    private CharSequence name;
    private CharSequence fileName;
    private Color color;
    private boolean isVisible;
    private double trackLength;
    private double elevationDifference;
    private PathLayer trackLayer;

    public Track(CharSequence _name) {
        name = _name;
        color = Color.MAGENTA; // or do config?
        fileName = "";
        trackLength = 0;
        isVisible = false;
        elevationDifference = 0;
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

    public int getLineColor() {
        return 0xFF000000 | ((int)(255 * color.r) << 16) | ((int)(255 * color.g) << 8) | ((int)(255 * color.b));
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

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public double getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(double trackLength) {
        this.trackLength = trackLength;
    }

    public double getElevationDifference() {
        return elevationDifference;
    }

    public void setElevationDifference(double altitudeDifference) {
        this.elevationDifference = altitudeDifference;
    }

    public PathLayer getTrackLayer() {
        return trackLayer;
    }

    public void showTrack() {
        // ? to do style for track (line width, ...)
        CacheboxMapAdapter cacheboxMapAdapter = MapView.getCacheboxMapAdapter();
        if (cacheboxMapAdapter != null) {
            if (isVisible) {
                PathLayer trackLayer = new PathLayer(cacheboxMapAdapter, getLineColor(), 5);
                fillTrackLayer(CB.lastMapState.getZoom());
                cacheboxMapAdapter.layers().add(trackLayer);
            }
        }
    }

    public void fillTrackLayer(int zoom) {
        // ? to do reduce no of points depending on zoom (Reduktion of Ploylines with Douglas-Peucker-Algorithmus)
        // to do remember trackPoints
        ArrayList<GeoPoint> trackPoints = new ArrayList<>(size);
        for (int i = 0; i < size ; i++) {
            trackPoints.add(new GeoPoint(get(i).getLatitude(), get(i).getLongitude()));
        }
        trackLayer.setPoints(trackPoints);
    }

    public void hideTrack() {
        CacheboxMapAdapter cacheboxMapAdapter = MapView.getCacheboxMapAdapter();
        if (cacheboxMapAdapter != null) {
            cacheboxMapAdapter.layers().remove(trackLayer);
        }
    }
}