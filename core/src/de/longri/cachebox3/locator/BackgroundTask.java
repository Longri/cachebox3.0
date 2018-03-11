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
    private int waitTime = 0;

    private LocationManager locationManager;
    private Coordinate target;
    private int approachDistance;

    public BackgroundTask() {

    }

    private boolean create() {

        if (CB.viewmanager.locationReceiver.isApproachCompleted()) {
            return false;
        }

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

                        float distance = target.distance(myPosition, MathUtils.CalculationType.FAST);
                        // set locationManager distance filter to half distance
                        locationManager.setDistanceFilter(distance / 2);

                        locationManager.setDelegate(new LocationEvents() {
                            @Override
                            public void newGpsPos(double latitude, double longitude, float accuracy) {
                                float distance = target.distance(new LatLong(latitude, longitude), MathUtils.CalculationType.FAST);

                                log.debug("New Background location! distance: {}", distance);

                                if (distance <= approachDistance) {
                                    playApproach();

                                    // cancel this background task
                                    cancel();
                                } else {
                                    if (distance / 2 > approachDistance) {
                                        locationManager.setDistanceFilter(distance / 2);
                                        log.debug("Set location distance filter to {}m", distance / 2);
                                    } else {
                                        locationManager.setDistanceFilter(0);
                                        log.debug("Set location distance filter to 0m");
                                    }
                                }
                            }

                            @Override
                            public void newNetworkPos(double latitude, double longitude, float accuracy) {

                            }

                            @Override
                            public void newAltitude(double altitude) {

                            }

                            @Override
                            public void newBearing(float bearing, boolean gps) {

                            }

                            @Override
                            public void newSpeed(double speed) {

                            }

                            @Override
                            public void newPitch(float pitch) {

                            }

                            @Override
                            public void newRoll(float roll) {

                            }
                        });
                        locationManager.startUpdateLocation();
                    }
                }
            });
        }
        return true;
    }

    private void workCycle() {
        // do nothing!
        // all work are on created locationManager!
    }

    private void playApproach() {
        FileHandle soundFileHandle;
        if (CanvasAdapter.platform != Platform.IOS) {
            soundFileHandle = Gdx.files.internal("sound/Approach.mp3");
        } else {
            soundFileHandle = Gdx.files.absolute(CB.WorkPath + "/data/sound/Approach.mp3");
        }
        PlatformConnector.playNotifySound(soundFileHandle);
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
        CB.postOnMainThread(new NamedRunnable("dispose background location listener") {
            @Override
            public void run() {
                log.debug("Cancel background task, stop location updates");
                cancel.set(true);
                locationManager.stopUpdateLocation();
                locationManager.dispose();
                locationManager = null;
            }
        });
    }
}
