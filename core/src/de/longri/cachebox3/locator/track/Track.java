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
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.backend.canvas.Paint;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.theme.styles.LineStyle;

import java.util.ArrayList;
import java.util.HashMap;

public class Track extends Array<Coordinate> {
    private static final int MAXZOOM = 30;
    private static final int unreducedEntry = MAXZOOM - 1;
    private static final int deltaEntry = MAXZOOM - 2;
    private final HashMap<Integer, ArrayList<GeoPoint>> reduced;
    private CharSequence name;
    private CharSequence fileName;
    private Color color;
    private boolean isVisible;
    private double trackLength;
    private double elevationDifference, lastUsedElevation;
    private Coordinate lastCoordinate;
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

    public boolean addPoint(Coordinate coordinate) {
        return addPoint(coordinate, true);
    }

    public boolean addPoint(Coordinate coordinate, boolean force) {
        // if added return true
        if (size > 0) {
            // update tracklength and accumulated elevation
            float[] distanceToLastRecordedPosition = new float[1];
            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST,
                    lastCoordinate.getLatitude(), lastCoordinate.getLongitude(),
                    coordinate.getLatitude(), coordinate.getLongitude(),
                    distanceToLastRecordedPosition);
            if (force || distanceToLastRecordedPosition[0] > Config.TrackDistance.getValue()) {
                add(coordinate);
                trackLength = trackLength + distanceToLastRecordedPosition[0];
                double ed = Math.abs(lastUsedElevation - coordinate.getElevation());
                if (ed >= 25) {
                    elevationDifference = elevationDifference + ed;
                    lastUsedElevation = coordinate.getElevation();
                }
                lastCoordinate = coordinate;
            } else {
                return false;
            }
        } else {
            lastUsedElevation = coordinate.getElevation();
            lastCoordinate = coordinate;
            add(coordinate);
        }
        return true;
    }

    // =================================================================================================================

    public void addPointToTrackLayer(Coordinate point) {
        if (trackLayer != null)
            trackLayer.addPoint(new GeoPoint(point.getLatitude(), point.getLongitude()));
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
        LineStyle.LineBuilder<?> lb = LineStyle.builder();
        lb.color(Color.argb8888(color));
        lb.cap(Paint.Cap.BUTT);
        lb.strokeWidth(CB.getScaledFloat(3));
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
            // add the new Coordinates to GeoPoints
            ArrayList<GeoPoint> unreducedGeoPoints = reduced.get(unreducedEntry);
            int unreducedGeoPointsSize = unreducedGeoPoints.size();
            for (int i = unreducedGeoPointsSize; i < size; i++) {
                unreducedGeoPoints.add(new GeoPoint(get(i).getLatitude(), get(i).getLongitude()));
            }
            reduced.clear();
            reduced.put(unreducedEntry, unreducedGeoPoints);
        }

        if (reduced.get(zoom) == null) {
            double tolerance = 0.01 * Math.exp(-1 * (zoom - 10));
            reduced.put(zoom, PolylineReduction.polylineReduction(reduced.get(unreducedEntry), tolerance));
        }

        trackLayer.setPoints(reduced.get(zoom));
    }
}