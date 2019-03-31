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

import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.gui.map.layer.MapOrientationMode;
import de.longri.cachebox3.locator.LatLong;
import de.longri.serializable.BitStore;
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

    private final float CONVERSION = 1000000;
    private final int MAP_MODE_MASK = 7;
    private final int MAP_ORIENTATION_MODE_MASK = 24;
    private final int MAP_ZOOM_MASK = 992;

    private int value = 0;
    private LatLong freePosition;
    private float orientation;
    private float tilt;
    private boolean changed = false;

    public MapState() {
    }

    public MapState(byte[] serialize) {
        deserialize(serialize);
    }

    public void deserialize(byte[] bytes) {
        if (bytes == null) return;
        BitStore store = new BitStore(bytes);

        value = store.readInt();
        if (store.readBool()) {
            double lat = (float) store.readInt() / CONVERSION;
            double lon = (float) store.readInt() / CONVERSION;
            freePosition = new LatLong(lat, lon);
        }
        orientation = (float) store.readInt() / CONVERSION;
        tilt = (float) store.readInt() / CONVERSION;
    }

    public byte[] serialize() {
        BitStore store = new BitStore();
        store.write(value);
        if (freePosition != null) {
            store.write(true);
            store.write((int) (freePosition.getLatitude() * CONVERSION));
            store.write((int) (freePosition.getLongitude() * CONVERSION));
        } else {
            store.write(false);
        }
        store.write((int) (orientation * CONVERSION));
        store.write((int) (tilt * CONVERSION));
        return store.getArray();
    }

    //setter

    public void setPosition(LatLong latLong) {
        this.freePosition = latLong;
        changed = true;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
        changed = true;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
        changed = true;
    }

    public void setMapMode(MapMode mode) {
        log.debug("set MapMode to: {}", mode);
        int shift = mode.ordinal() & MAP_MODE_MASK;
        value = (~MAP_MODE_MASK & value) | shift;
        changed = true;
    }

    public void setMapOrientationMode(MapOrientationMode mode) {
        log.debug("set MapOrientationMode to: {}", mode);
        int shift = (mode.ordinal() << 3 & MAP_ORIENTATION_MODE_MASK);
        value = (~MAP_ORIENTATION_MODE_MASK & value) | shift;
        changed = true;
    }

    public void setZoom(int zoom) {
        log.debug("set Zoom to: {}", zoom);
        int shift = (zoom << 5 & MAP_ZOOM_MASK);
        value = (~MAP_ZOOM_MASK & value) | shift;
        changed = true;
    }

    public void setValues(int values) {
        this.value = values;
        changed = true;
    }

    public void set(MapState mapState) {
        this.value = mapState.value;
        this.tilt = mapState.tilt;
        this.orientation = mapState.orientation;
        this.freePosition = mapState.freePosition.copy();
        changed = true;
    }

    //getter

    public LatLong getFreePosition() {
        return freePosition;
    }

    public float getOrientation() {
        return orientation;
    }

    public float getTilt() {
        return tilt;
    }

    public MapMode getMapMode() {
        int ordinal = value & MAP_MODE_MASK;
        return MapMode.fromOrdinal(ordinal);
    }

    public MapOrientationMode getMapOrientationMode() {
        int ordinal = (value & MAP_ORIENTATION_MODE_MASK) >> 3;
        return MapOrientationMode.fromOrdinal(ordinal);
    }

    public int getZoom() {
        return (value & MAP_ZOOM_MASK) >> 5;
    }

    public int getValues() {
        return this.value;
    }


    /**
     * Returns TRUE if no value changed
     *
     * @return
     */
    public boolean isEmpty() {
        return !changed;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("M:").append(getMapMode()).append(" ");
        sb.append("O:").append(getMapOrientationMode()).append(" ");
        sb.append("Z:").append(getZoom()).append(" ");
        sb.append("GPS:").append(getFreePosition()).append(" ");
        sb.append("T:").append(getTilt()).append(" ");
        sb.append("H:").append(getOrientation()).append(" ");
        return sb.toString();
    }
}
