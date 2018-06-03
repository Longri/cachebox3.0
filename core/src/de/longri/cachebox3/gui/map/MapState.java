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
package de.longri.cachebox3.gui.map;

import de.longri.cachebox3.gui.map.layer.MapOrientationMode;
import de.longri.cachebox3.locator.LatLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store for map state like MapMode, zoom , orientation
 * Store 3 Bit's for MapMode FREE 000, GPS 001, WP 010, LOCK 011, CAR 100;
 * Store 2 Bit's for MapOrientationMode NORTH 00, COMPASS 01, USER 10
 * Store 5 Bit's for ZoomLevel 0-28
 * Created by Longri on 08.03.2017.
 */
public class MapState {
    private final static Logger log = LoggerFactory.getLogger(MapState.class);

    private final int MAP_MODE_MASK = 7;
    private final int MAP_ORIENTATION_MODE_MASK = 24;
    private final int MAP_ZOOM_MASK = 992;

    private int value = 0;
    private LatLong freePosition;
    private float orientation;
    private float tilt;

    public void setPosition(LatLong latLong) {
        this.freePosition = latLong;
    }

    public LatLong getFreePosition() {
        return freePosition;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }

    public float getTilt() {
        return tilt;
    }

    public void setMapMode(MapMode mode) {
        log.debug("set MapMode to: {}", mode);
        int shift = mode.ordinal() & MAP_MODE_MASK;
        value = (~MAP_MODE_MASK & value) | shift;
    }

    public MapMode getMapMode() {
        int ordinal = value & MAP_MODE_MASK;
        return MapMode.fromOrdinal(ordinal);
    }

    public void setMapOrientationMode(MapOrientationMode mode) {
        log.debug("set MapOrientationMode to: {}", mode);
        int shift = (mode.ordinal() << 3 & MAP_ORIENTATION_MODE_MASK);
        value = (~MAP_ORIENTATION_MODE_MASK & value) | shift;
    }

    public MapOrientationMode getMapOrientationMode() {
        int ordinal = (value & MAP_ORIENTATION_MODE_MASK) >> 3;
        return MapOrientationMode.fromOrdinal(ordinal);
    }

    public void setZoom(int zoom) {
        log.debug("set Zoom to: {}", zoom);
        int shift = (zoom << 5 & MAP_ZOOM_MASK);
        value = (~MAP_ZOOM_MASK & value) | shift;
    }

    public int getZoom() {
        return (value & MAP_ZOOM_MASK) >> 5;
    }

    public void setValues(int values) {
        this.value = values;
    }

    public int getValues() {
        return this.value;
    }

    public String toString() {
        return getMapMode() + "/ " + getMapOrientationMode() + " / Z:" + getZoom();
    }

    public void set(MapState mapState) {
        this.value = mapState.value;
        this.tilt = mapState.tilt;
        this.orientation = mapState.orientation;
        this.freePosition = mapState.freePosition.copy();
    }
}
