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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.SoundCache;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final AtomicInteger latInt = new AtomicInteger();
    private final AtomicInteger lonInt = new AtomicInteger();
    private final AtomicBoolean newCoords = new AtomicBoolean(false);
    private CLLocationManager locationManager;
    private Coordinate target;
    UIApplication application;

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        boolean ret = super.didFinishLaunching(application, launchOptions);
        this.application = application;
        return ret;
    }


    @Override
    public void didEnterBackground(final UIApplication application) {
        super.didEnterBackground(application);
//        log.debug("didEnterBackGround");
//
//        //start background Task only if needed!
//        boolean bgNeeded = false;
//        if (!Config.GlobalVolume.getValue().Mute) {
//            target = EventHandler.getSelectedCoord();
//            if (target != null) {
//                //check if approach sound finish
//                if (!CB.viewmanager.locationReceiver.isApproachCompleted()) {
//                    bgNeeded = true;
//                }
//            }
//        }
//
//        if (bgNeeded) {
//
//
//            long bgTaskId = application.beginBackgroundTask("BackgroundTask", new Runnable() {
//                @Override
//                public void run() {
//                    log.debug("End BGTask");
//                    application.endBackgroundTask(bgTask.get());
//                }
//            });
//
//            bgTask.set(bgTaskId);
//
//            isBgTaskRunning.set(true);
//            final AtomicInteger soundApproachDistance = new AtomicInteger(Config.SoundApproachDistance.getValue() * 1000000);
//            final AtomicInteger debugCount = new AtomicInteger();
//
//            DispatchQueue globalQueue = DispatchQueue.getGlobalQueue(PRIORITY_DEFAULT, 0);
//
//
//            globalQueue.async(new Runnable() {
//                @Override
//                public void run() {
//
//                    DispatchQueue.getMainQueue().sync(new Runnable() {
//                        @Override
//                        public void run() {
//                            //start background location listener
//                            locationManager = new CLLocationManager();
//                            locationManager.setDelegate(delegateAdapter);
//                            locationManager.setDesiredAccuracy(CLLocationAccuracy.NearestTenMeters);
//                            locationManager.setDistanceFilter(10.0); //3 m
//                            if (Foundation.getMajorSystemVersion() >= 8) {
//                                locationManager.requestAlwaysAuthorization();
//                                locationManager.requestWhenInUseAuthorization();
//                            }
//                            locationManager.setAllowsBackgroundLocationUpdates(true);
//
//                            // Once configured, the location manager must be "started".
//                            locationManager.startUpdatingLocation();
//                        }
//                    });
//
//                    //Run on Background
//                    log.debug("Start the long-running background task");
//                    while (isBgTaskRunning.get()) {
//                        if (newCoords.get()) {
//                            log.debug("new background coords");
//                            //calculate distance
//                            LatLong latLong = new LatLong(latInt.get() / 1000000, lonInt.get() / 1000000);
//                            double distance = target.distance(latLong, MathUtils.CalculationType.FAST);
//
//                            if (distance < soundApproachDistance.get() / 1000000) {
//                                log.debug("Near target, play approach sound");
//                                SoundCache.play(SoundCache.Sounds.Approach);
//
//                                // now we can cancel background task
//                                isBgTaskRunning.set(false);
//                            }
//                            newCoords.set(false);
//                        } else {
//                            try {
//                                Thread.sleep(10000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            log.debug("Wait on BGTask for {}sec", debugCount.decrementAndGet() * 10);
//
//                            if (debugCount.get() * 10 < -10) {
//                                //play test sound
//                                log.debug("Near target, play approach sound");
//                                FileHandle soundFileHandle = Gdx.files.absolute(CB.WorkPath + "/data/sound/Approach.mp3");
//                                IOS_BackgroundSound sound = new IOS_BackgroundSound(soundFileHandle);
//                                sound.play();
//
//                            }
//                        }
//                    }
//
//                    log.debug("Finish background task");
//                    locationManager.stopUpdatingLocation();
//                    locationManager.setDelegate(null);
//                    // finish background task
//                    application.endBackgroundTask(bgTask.get());
//                    bgTask.set(UIBackgroundTaskInvalid);
//                }
//            });
//        }
    }

    CLLocationManagerDelegateAdapter delegateAdapter = new CLLocationManagerDelegateAdapter() {

        @Override
        public void didUpdateLocations(CLLocationManager manager, NSArray<CLLocation> locations) {
            CLLocation newLocation = locations.last();
            CLLocationCoordinate2D coord = newLocation.getCoordinate();
            latInt.set((int) (coord.getLatitude() * 1000000));
            lonInt.set((int) (coord.getLongitude() * 1000000));
            newCoords.set(true);
        }

        @Override
        public void didFail(CLLocationManager manager, NSError error) {
            if (error.getErrorCode() != CLErrorCode.LocationUnknown) {
                log.error("LocationManagerDelegateAdapter didFail " + error.getErrorCode().toString());
                //close BackgroundTask
                isBgTaskRunning.set(false);
            }
        }
    };

    @Override
    public void willEnterForeground(final UIApplication application) {
        super.willEnterForeground(application);
//        log.debug("willEnterForeground");
//
//        //close BackgroundTask
//        isBgTaskRunning.set(false);

    }
}
