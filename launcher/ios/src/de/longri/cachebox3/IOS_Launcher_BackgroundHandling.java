/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.uikit.UIApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.robovm.apple.dispatch.DispatchQueue.PRIORITY_DEFAULT;

/**
 * Created by Longri on 01.03.2018.
 */
public abstract class IOS_Launcher_BackgroundHandling extends IOSApplication.Delegate {

    private final static Logger log = LoggerFactory.getLogger(IOS_Launcher_BackgroundHandling.class);

    private long UIBackgroundTaskInvalid = UIApplication.getInvalidBackgroundTask();
    private final AtomicLong bgTask = new AtomicLong(UIBackgroundTaskInvalid);
    private final AtomicBoolean isBgTaskRunning = new AtomicBoolean(false);
   

    @Override
    public void didEnterBackground(final UIApplication application) {
        super.didEnterBackground(application);
        log.debug("didEnterBackGround");

        //start background Task only if needed!
        boolean bgNeeded = false;
        if (!Config.GlobalVolume.getValue().Mute) {
            Coordinate target = EventHandler.getSelectedCoord();
            if (target != null) {
                //check if approach sound finish
                if (!CB.viewmanager.locationReceiver.isApproachCompleted()) {
                    bgNeeded = true;
                }
            }
        }

        if (bgNeeded) {
            bgTask.set(application.beginBackgroundTask("BackgroundTask", new Runnable() {
                @Override
                public void run() {
                    application.endBackgroundTask(bgTask.get());
                }
            }));

            isBgTaskRunning.set(true);
            DispatchQueue.getGlobalQueue(bgTask.get(), PRIORITY_DEFAULT).async(new Runnable() {
                @Override
                public void run() {

                    //Run on Background
                    log.debug("Start the long-running background task");
                    while (isBgTaskRunning.get()) {
                        log.debug("Run in background mode");
                        //.... .....
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    log.debug("Finish background task");

                    // finish background task
                    application.endBackgroundTask(bgTask.get());
                    bgTask.set(UIBackgroundTaskInvalid);
                }
            });
        }
    }

    @Override
    public void willEnterForeground(final UIApplication application) {
        super.willEnterForeground(application);
        log.debug("willEnterForeground");

        //close BackgroundTask
        isBgTaskRunning.set(false);

    }
}
