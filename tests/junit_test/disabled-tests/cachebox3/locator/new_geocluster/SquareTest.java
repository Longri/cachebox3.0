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
package de.longri.cachebox3.locator.new_geocluster;

import com.badlogic.gdx.scenes.scene2d.ui.ClusteredList;
import com.badlogic.gdx.scenes.scene2d.ui.MapWayPointItem;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.geocluster.ClusterRunnable;
import de.longri.cachebox3.utils.lists.CB_List;
import org.junit.jupiter.api.Test;
import org.oscim.core.GeoPoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Longri on 25.12.16.
 */
public class SquareTest {

    static {
        TestUtils.initialGdx();
    }

    static final int arraySize = 100;//5;
    static final double distance = 10;
    static final double tolerance = 0.1;


//    static final int arraySize = 1000;
//    static final double distance = 10;
//    static final double tolerance = 0.1;

    static GeoPoint[][] points;
    static CB_List<GeoPoint> allPoints = new CB_List<GeoPoint>();


    public static void init(int arraySize) {
        points = new GeoPoint[arraySize][arraySize];
        allPoints.clear();
        GeoPoint leftTop = new GeoPoint(60.0, 13.0);

        GeoPoint pX, pY;
        pX = leftTop;
        pY = leftTop;

        for (int x = 0; x < arraySize; x++) {
            for (int y = 0; y < arraySize; y++) {
                allPoints.add(pX);
                points[x][y] = new GeoPoint(pX.latitudeE6, pX.longitudeE6);
                GeoPoint projectedPoint = pX.destinationPoint(distance, 90);
                assertThat("distance", pX.sphericalDistance(projectedPoint), closeTo(distance, tolerance));
                pX = projectedPoint;
            }
            pX = pY = pY.destinationPoint(distance, 180);
        }
    }


    public void chkInit(int arraySize) {
        for (int x = 0; x < arraySize - 1; x++) {
            for (int y = 0; y < arraySize - 1; y++) {
                GeoPoint a = points[x][y];
                GeoPoint b = points[x + 1][y];
                assertThat("wrong distance at x/y :" + x + "/" + y,
                        a.sphericalDistance(b), closeTo(distance, tolerance));
                GeoPoint c = points[x][y + 1];
                assertThat("wrong distance at x/y :" + x + "/" + y,
                        a.sphericalDistance(c), closeTo(distance, tolerance));
            }
        }
        assertThat("all List length", allPoints.size, equalTo(arraySize * arraySize));
    }

    @Test
    public void clustering3() {

        init(3);
        chkInit(3);

        ClusteredList allCluster = new ClusteredList();

        int index = 0;
        for (GeoPoint point : allPoints) {
            allCluster.add(new MapWayPointItem(
                    new Coordinate(point.getLatitude(), point.getLongitude()), Integer.toString(index++), null,false));
        }

        assertThat("cluster size", allCluster.size, equalTo(allPoints.size));


        allCluster.clusterByDistance(distance - 2, null, ClusterRunnable.Task.reduce, false);
        assertThat("cluster size", allCluster.size, equalTo(allPoints.size));


        allCluster.clusterByDistance(distance * 2, null, ClusterRunnable.Task.reduce, false);
        assertThat("cluster size", allCluster.size, equalTo(4));

        allCluster.clusterByDistance(distance * 4, null, ClusterRunnable.Task.reduce, false);
        assertThat("cluster size", allCluster.size, equalTo(2));


        allCluster.clusterByDistance(distance * 2, null, ClusterRunnable.Task.expand, false);
        assertThat("cluster size", allCluster.size, equalTo(4));


        allCluster.clusterByDistance(distance - 2, null, ClusterRunnable.Task.expand, false);
        assertThat("cluster size", allCluster.size, equalTo(allPoints.size));


    }

    @Test
    public void clustering100() {

        init(100);
        chkInit(100);

        ClusteredList allCluster = new ClusteredList();

        int index = 0;
        for (GeoPoint point : allPoints) {
            allCluster.add(new MapWayPointItem(
                    new Coordinate(point.getLatitude(), point.getLongitude()), Integer.toString(index++), null,false));
        }

        assertThat("cluster size", allCluster.size, equalTo(allPoints.size));


        allCluster.clusterByDistance(distance - 2, null, ClusterRunnable.Task.reduce, false);
        assertThat("cluster size", allCluster.size, equalTo(allPoints.size));


        allCluster.clusterByDistance(distance * 2, null, ClusterRunnable.Task.reduce, false);
        assertThat("cluster size", allCluster.size, equalTo(2550));

        allCluster.clusterByDistance(distance * 4, null, ClusterRunnable.Task.reduce, false);
        assertThat("cluster size", allCluster.size, equalTo(650));


        allCluster.clusterByDistance(distance * 2, null, ClusterRunnable.Task.expand, false);
        assertThat("cluster size", allCluster.size, equalTo(2550));


        allCluster.clusterByDistance(distance - 2, null, ClusterRunnable.Task.expand, false);
        assertThat("cluster size", allCluster.size, equalTo(allPoints.size));


    }

}
