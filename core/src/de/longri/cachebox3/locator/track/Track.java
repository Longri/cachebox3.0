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
import org.oscim.backend.canvas.Paint;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.theme.styles.LineStyle;

import java.util.ArrayList;
import java.util.HashMap;

public class Track extends Array<Coordinate> {
    private static final int MAXZOOM = 30;
    private static final int unreducedEntry = MAXZOOM - 1;
    private final HashMap<Integer, ArrayList<GeoPoint>> reduced;
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
        reduced = new HashMap<>(MAXZOOM);
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

    // =================================================================================================================

    public void addPointToTrackLayer(GeoPoint geoPoint) {
        if (trackLayer != null)
            trackLayer.addPoint(geoPoint);
    }

    public void createTrackLayer() {
        CacheboxMapAdapter cacheboxMapAdapter = MapView.getCacheboxMapAdapter();
        if (cacheboxMapAdapter != null) {
            if (isVisible) {
                addTrackLayer();
            }
        }
    }

    public void updateTrackLayer() {
        CacheboxMapAdapter cacheboxMapAdapter = MapView.getCacheboxMapAdapter();
        if (cacheboxMapAdapter != null) {
            if (isVisible) {
                cacheboxMapAdapter.layers().remove(trackLayer);
                addTrackLayer();
            }
        }
    }

    public void hideTrackLayer() {
        CacheboxMapAdapter cacheboxMapAdapter = MapView.getCacheboxMapAdapter();
        if (cacheboxMapAdapter != null) {
            cacheboxMapAdapter.layers().remove(trackLayer);
        }
    }

    private void addTrackLayer() {
        CacheboxMapAdapter cacheboxMapAdapter = MapView.getCacheboxMapAdapter();
        trackLayer = new PathLayer(cacheboxMapAdapter, buildLineStyle());
        fillTrackLayer();
        cacheboxMapAdapter.layers().add(trackLayer);
    }

    private LineStyle buildLineStyle() {
        // ? to do skin's style for track
        LineStyle.LineBuilder lb = LineStyle.builder();
        lb.color(Color.argb8888(color));
        lb.cap(Paint.Cap.BUTT);
        lb.strokeWidth(CB.getScaledFloat(2));
        return lb.build();
    }

    private void fillTrackLayer() {
        int zoom = CB.lastMapState.getZoom();
        if (reduced.get(unreducedEntry) == null) {
            ArrayList<GeoPoint> trackPoints = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                trackPoints.add(new GeoPoint(get(i).getLatitude(), get(i).getLongitude()));
            }
            reduced.put(unreducedEntry, trackPoints);
        } else {
            // recalculate reduced tracks, if number of points differ more than 50 points
            ArrayList<GeoPoint> trackPoints = reduced.get(unreducedEntry);
            int reducedBaseSize = trackPoints.size();
            if (size - reducedBaseSize > 50) {
                for (int i = reducedBaseSize; i < size; i++) {
                    trackPoints.add(new GeoPoint(get(i).getLatitude(), get(i).getLongitude()));
                }
            }
            reduced.clear();
            reduced.put(unreducedEntry, trackPoints);
        }
        if (reduced.get(zoom) == null) {
            double tolerance = 0.01 * Math.exp(-1 * (zoom - 10));
            reduced.put(zoom, PolylineReduction.polylineReduction(reduced.get(unreducedEntry), tolerance));
        }
        trackLayer.setPoints(reduced.get(zoom));
    }
}