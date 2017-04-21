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


import com.badlogic.gdx.scenes.scene2d.ui.ClusteredList;
import de.longri.cachebox3.utils.lists.CancelRunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 21.12.16.
 */
public class ClusterRunnable implements CancelRunable {

    public enum Task {
        expand, reduce
    }


    private final static Logger log = LoggerFactory.getLogger(ClusterRunnable.class);

    protected final double distance;
    private final CallBack callBack;
    private final ClusteredList workList;
    private final GeoBoundingBoxInt boundingBox;
    private final boolean all;
    private final Task task;

    @Override
    public void cancel() {
        workList.cancel();
    }

    public interface CallBack {
        void callBack();
    }

    public ClusterRunnable(double distance, final ClusteredList workList, final CallBack callBack,
                           GeoBoundingBoxInt boundingBox, Task task, boolean all) {
        this.callBack = callBack;
        this.distance = distance;
        this.workList = workList;
        this.boundingBox = boundingBox;
        this.task = task;
        this.all = all;
    }


    @Override
    public void run() {
        log.debug("Runnable started with " + task);
        int lastSize = workList.size;
        try {
            workList.clusterByDistance(distance, boundingBox, task, all);
            log.debug(task + "from " + lastSize + " to " + workList.size + "[" + workList.getAllSize() + "]");
            callBack.callBack();
        } catch (Exception e) {
            log.error("Runnable exception", e);
        }
    }
}
