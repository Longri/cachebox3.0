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
import de.longri.cachebox3.utils.lists.CB_List;

/**
 * Created by Longri on 21.12.16.
 */
public class ClusterRunnable implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ClusterRunnable.class);

    protected final double distance;
    private final CallBack callBack;
    private final ClusteredList workList;
    private volatile boolean running = true; //TODO implement cancel thread

    public interface CallBack {
        void callBack(CB_List<ClusterablePoint> reduced);
    }

    public ClusterRunnable(double distance, final ClusteredList workList, final CallBack callBack) {
        this.callBack = callBack;
        this.distance = distance;
        this.workList = workList;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {

        log.debug("Runnable started");

        try {
            workList.clusterByDistance(distance);
            log.debug("callback with reduced to " + workList.size());
            callBack.callBack(workList);
        } catch (Exception e) {
            running = false;
            log.error("Runnable exception", e);
        }
    }
}
