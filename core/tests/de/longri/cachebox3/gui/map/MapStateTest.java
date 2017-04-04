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
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 08.03.2017.
 */
class MapStateTest {
    @Test
    void setMapMode() {

        MapState state = new MapState();
        state.setMapMode(MapMode.CAR);
        assertThat("Must MapMode.CAR", state.getMapMode() == MapMode.CAR);

        state.setMapMode(MapMode.FREE);
        assertThat("Must MapMode.FREE", state.getMapMode() == MapMode.FREE);

        state.setMapMode(MapMode.GPS);
        assertThat("Must MapMode.GPS", state.getMapMode() == MapMode.GPS);

        state.setMapMode(MapMode.LOCK);
        assertThat("Must MapMode.LOCK", state.getMapMode() == MapMode.LOCK);

        state.setMapMode(MapMode.WP);
        assertThat("Must MapMode.WP", state.getMapMode() == MapMode.WP);

    }

    @Test
    void setMapOrientationMode() {

        MapState state = new MapState();
        state.setMapOrientationMode(MapOrientationMode.COMPASS);
        assertThat("Must MapOrientationMode.COMPASS", state.getMapOrientationMode() == MapOrientationMode.COMPASS);

        state.setMapOrientationMode(MapOrientationMode.NORTH);
        assertThat("Must MapOrientationMode.NORTH", state.getMapOrientationMode() == MapOrientationMode.NORTH);

        state.setMapOrientationMode(MapOrientationMode.USER);
        assertThat("Must MapOrientationMode.USER", state.getMapOrientationMode() == MapOrientationMode.USER);
    }

    @Test
    void setZoom() {
        MapState state = new MapState();
        state.setZoom(12);
        assertThat("Must zoom Level 12", state.getZoom() == 12);
        state.setZoom(7);
        assertThat("Must zoom Level 7", state.getZoom() == 7);
        state.setZoom(22);
        assertThat("Must zoom Level 22", state.getZoom() == 22);
        state.setZoom(3);
        assertThat("Must zoom Level 3", state.getZoom() == 3);
        state.setZoom(19);
        assertThat("Must zoom Level 19", state.getZoom() == 19);
    }


    @Test
    void setMixed() {
        MapState state = new MapState();
        state.setMapMode(MapMode.CAR);
        state.setMapOrientationMode(MapOrientationMode.COMPASS);
        state.setZoom(12);
        assertThat("Must zoom Level 12", state.getZoom() == 12);
        assertThat("Must MapOrientationMode.COMPASS", state.getMapOrientationMode() == MapOrientationMode.COMPASS);
        assertThat("Must MapMode.CAR", state.getMapMode() == MapMode.CAR);

        state.setMapMode(MapMode.FREE);
        state.setMapOrientationMode(MapOrientationMode.NORTH);
        state.setZoom(7);
        assertThat("Must zoom Level 7", state.getZoom() == 7);
        assertThat("Must MapOrientationMode.NORTH", state.getMapOrientationMode() == MapOrientationMode.NORTH);
        assertThat("Must MapMode.FREE", state.getMapMode() == MapMode.FREE);

        state.setMapMode(MapMode.GPS);
        state.setMapOrientationMode(MapOrientationMode.USER);
        assertThat("Must zoom Level 7", state.getZoom() == 7);
        state.setZoom(22);
        assertThat("Must MapOrientationMode.USER", state.getMapOrientationMode() == MapOrientationMode.USER);
        assertThat("Must MapMode.GPS", state.getMapMode() == MapMode.GPS);
    }
}