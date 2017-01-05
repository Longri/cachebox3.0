/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
package de.longri.cachebox3.locator.geocluster;


import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;

/**
 * Created by Longri on 21.12.16.
 */
public class ClusterRunnable implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ClusterRunnable.class);

    protected final double distance;
    private final CallBack callBack;
    private final ClusteredList workList;
    private final GeoBoundingBox boundingBox;
    private volatile boolean running = true; //TODO implement cancel thread

    public interface CallBack {
        void callBack();
    }

    public ClusterRunnable(double distance, final ClusteredList workList, final CallBack callBack, GeoBoundingBox boundingBox) {
        this.callBack = callBack;
        this.distance = distance;
        this.workList = workList;
        this.boundingBox = boundingBox;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {

        log.debug("Runnable started");

        try {
            workList.clusterByDistance(distance, this.boundingBox);
            log.debug("callback with reduced to " + workList.size());
            callBack.callBack();
        } catch (Exception e) {
            running = false;
            log.error("Runnable exception", e);
        }
    }
}
