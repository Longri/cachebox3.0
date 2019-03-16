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
package de.longri.cachebox3.locator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.locator.manager.LocationManager;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 09.03.18.
 */
public class BackgroundTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BackgroundTask.class);


    private AtomicBoolean cancel = new AtomicBoolean(false);
    private AtomicBoolean playSound = new AtomicBoolean(false);
    private AtomicBoolean testDistance = new AtomicBoolean(false);
    private int waitTime = 0;

    private LocationManager locationManager;
    private Coordinate target, lastBackgroundLocation;
    private int approachDistance;
    private Region targetRegion;

    public BackgroundTask() {

    }

    private boolean create() {

        if (CB.viewmanager.locationReceiver.isApproachCompleted()) {
            return false;
        }

        log.debug("create background task");

        approachDistance = Config.SoundApproachDistance.getValue();
        target = EventHandler.getSelectedCoord();

        if (target != null) {

            CB.postOnMainThread(new NamedRunnable("Initial background location listener") {
                @Override
                public void run() {
                    Coordinate myPosition = EventHandler.getMyPosition();
                    if (myPosition != null) {
                        //start background location listener
                        locationManager = CB.locationHandler.getBackgroundLocationManager();
                        locationManager.setDistanceFilter(0);
                        locationManager.setCanCalibrateCallBack(new GenericHandleCallBack<Boolean>() {
                            @Override
                            public boolean callBack(Boolean value) {
                                return false;
                            }
                        });

                        locationManager.setDelegate(new LocationEvents() {
                            @Override
                            public void newGpsPos(double latitude, double longitude, float accuracy) {
                                lastBackgroundLocation = new Coordinate(latitude, longitude);
                                if (testDistance.get()) {
                                    float distance = target.distance(new LatLong(latitude, longitude), MathUtils.CalculationType.FAST);
                                    log.debug("New Background location! distance: {}", distance);
                                    if (distance <= approachDistance) {
                                        playSound.set(true);
                                        CB.postOnMainThread(new NamedRunnable("enter region") {
                                            @Override
                                            public void run() {
                                                locationManager.stopUpdateLocation();
                                                locationManager.stopMonitoring(targetRegion);
                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void newNetworkPos(double latitude, double longitude, float accuracy) {
//                                log.debug("New Background Network location!");
                            }

                            @Override
                            public void newAltitude(double altitude) {
//                                log.debug("New Background Altitude!");
                            }

                            @Override
                            public void newTilt(double tilt) {

                            }

                            @Override
                            public void newBearing(float bearing, boolean gps) {
//                                log.debug("New Background Bearing!");
                            }

                            @Override
                            public void newSpeed(double speed) {
//                                log.debug("New Background speed");
                            }

                            @Override
                            public void newPitch(float pitch) {
//                                log.debug("New Background Pitch!");
                            }

                            @Override
                            public void newRoll(float roll) {
//                                log.debug("New Background Roll!");
                            }

                            @Override
                            public void didEnterRegion(Region region) {
                                log.debug("Did enter region {}", region);
                                testDistance.set(true);
                            }

                            @Override
                            public void didExitRegion(Region region) {
                                log.debug("Did exit region {}", region);
                                testDistance.set(false);
                            }
                        });
                        targetRegion = new CircularRegion(target, approachDistance);
                        locationManager.startUpdateLocation();
                        locationManager.startMonitoring(targetRegion);
                    }
                }
            });
        }
        return true;
    }


    private int count = 0;
    private int backgroundMinutes = 0;

    private void workCycle() {

        if (++count >= 60) {
            log.debug("Background work Cycle for {}minutes", ++backgroundMinutes);
            count = 0;
        }


        if (playSound.get()) {
            playSound.set(false);
            playApproach();
            CB.postAsyncDelayd(1000, new NamedRunnable("cancel background") {
                @Override
                public void run() {
                    log.debug("Approach sound complete, cancel background task");
                    cancel();
                }
            });
        }
    }

    private void playApproach() {
        try {
            FileHandle soundFileHandle;
            if (CanvasAdapter.platform != Platform.IOS) {
                soundFileHandle = Gdx.files.internal("sound/Approach.mp3");
            } else {
                soundFileHandle = Gdx.files.absolute(CB.WorkPath + "/data/sound/Approach.mp3");
            }
            PlatformConnector.playNotifySound(soundFileHandle);
            CB.viewmanager.locationReceiver.setApproachCompleted();
        } catch (Exception e) {
            log.error("Play Approach", e);
        }
    }


    @Override
    public void run() {
        if (create()) {
            while (!cancel.get()) {
                if (waitTime >= 1000) {
                    workCycle();
                    waitTime = 0;
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitTime += 250;
            }
        }
    }

    public void cancel() {
        cancel.set(true);
        CB.postOnMainThread(new NamedRunnable("dispose background location listener") {
            @Override
            public void run() {
                log.debug("Cancel background task, stop location updates");
                try {
                    if (locationManager != null) locationManager.stopUpdateLocation();
                    if (locationManager != null) locationManager.stopMonitoring(targetRegion);
                    if (locationManager != null) locationManager.dispose();
                    if (locationManager != null) locationManager = null;
                } catch (Exception e) {
                    log.error("Cancel Background task", e);
                }
            }
        });
    }

    public Coordinate getLastCoord() {
        return lastBackgroundLocation;
    }
}
