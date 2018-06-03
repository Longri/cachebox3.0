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

import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.events.location.GpsEventHelper;
import de.longri.cachebox3.events.location.PositionChangedEvent;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.CompassView;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.locator.manager.LocationManager;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.SoundCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Receives all position changes and sorts list or plays sounds.
 * <p>
 * Created by Longri on 30.01.2018.
 */
public class GlobalLocationReceiver implements PositionChangedListener, SelectedWayPointChangedListener, SelectedCacheChangedListener {
    final static private Logger log = LoggerFactory.getLogger(GlobalLocationReceiver.class);

    final private AtomicBoolean asyncWork = new AtomicBoolean(false);
    final private AtomicBoolean pendingWork = new AtomicBoolean(false);
    final private AtomicBoolean playSounds = new AtomicBoolean(false);
    final private AtomicBoolean approachSoundCompleted = new AtomicBoolean();
    final private AtomicMutableLatLong pendingLatLong = new AtomicMutableLatLong();

    public GlobalLocationReceiver() {
        EventHandler.add(this);
        playSounds.set(!Config.GlobalVolume.getValue().Mute);

        Config.GlobalVolume.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                playSounds.set(!Config.GlobalVolume.getValue().Mute);
            }
        });

        initialForegroundLocationListener();
    }

    @Override
    public void positionChanged(de.longri.cachebox3.events.location.PositionChangedEvent event) {
        pendingLatLong.set(event.pos);
        runAsync();
    }

    private void runAsync() {
        if (asyncWork.get()) {
            // try later
            pendingWork.set(true);
        } else {
            asyncWork.set(true);
            CB.postAsync(new NamedRunnable("GlobalLocationReceiver") {
                @Override
                public void run() {

                    playSound();

                    //ready
                    asyncWork.set(false);
                    if (pendingWork.get()) {
                        pendingWork.set(false);
                        runAsync();
                    }
                }
            });
        }
    }

    private void playSound() {
        try {
            if (playSounds.get() && !approachSoundCompleted.get()) {

                AbstractCache selectedCache = EventHandler.getSelectedCache();
                AbstractWaypoint selectedWaypoint = EventHandler.getSelectedWaypoint();

                if (selectedCache != null) {
                    LatLong pos = pendingLatLong.get();
                    float distance = selectedCache.distance(pos, MathUtils.CalculationType.FAST);
                    if (selectedWaypoint != null) {
                        distance = selectedWaypoint.distance(pos, MathUtils.CalculationType.FAST);
                    }

                    if (distance < 0) return;

                    if (!approachSoundCompleted.get() && (distance < Config.SoundApproachDistance.getValue())) {
                        CB.postOnGlThread(new NamedRunnable("Play Sound") {
                            @Override
                            public void run() {
                                SoundCache.play(SoundCache.Sounds.Approach);
                            }
                        });
                        approachSoundCompleted.set(true);
                    }
                }
            }
        } catch (Exception e) {
            log.error("GlobalLocationReceiver:Global.PlaySound(Approach.ogg)", e);
            e.printStackTrace();
        }

    }

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        resetApproach();
    }

    @Override
    public void selectedWayPointChanged(SelectedWayPointChangedEvent event) {
        resetApproach();
    }

    private void resetApproach() {

        // set approach sound if the distance low
        AbstractCache selectedCache = EventHandler.getSelectedCache();
        AbstractWaypoint selectedWaypoint = EventHandler.getSelectedWaypoint();
        LatLong pos = EventHandler.getMyPosition();
        if (selectedCache != null && pos != null) {
            float distance = selectedCache.distance(pos, MathUtils.CalculationType.FAST);
            if (selectedWaypoint != null) {
                distance = selectedWaypoint.distance(pos, MathUtils.CalculationType.FAST);
            }
            boolean value = distance < Config.SoundApproachDistance.getValue();
            approachSoundCompleted.set(value);
//            GlobalCore.switchToCompassCompleted = value;
        } else {
            approachSoundCompleted.set(true);
//            GlobalCore.switchToCompassCompleted = true;
        }

    }

    boolean isApproachCompleted() {
        return approachSoundCompleted.get();
    }

    void setApproachCompleted() {
        approachSoundCompleted.set(true);
    }

    //#######################################################################################################
    // Location manager

    private LocationManager locationManagerForeGround;

    private GpsEventHelper foreGroundHelper = new GpsEventHelper();

    private void initialForegroundLocationListener() {

        CB.postOnMainThread(new NamedRunnable("initial LocationListener") {
            @Override
            public void run() {
                if (locationManagerForeGround == null) {
                    foreGroundHelper.init();
                    locationManagerForeGround = CB.locationHandler.getNewLocationManager();
                    locationManagerForeGround.setDistanceFilter(0);
                    locationManagerForeGround.setCanCalibrateCallBack(new GenericHandleCallBack<Boolean>() {
                        @Override
                        public boolean callBack(Boolean value) {
                            // we set True only if View== Compass or Map
                            // and if MapView then not on CarMode
                            if (CB.viewmanager == null) return false;
                            AbstractView actView = CB.viewmanager.getActView();
                            if (actView == null) return false;
                            if (actView instanceof MapView) {
                                return !CB.isCarMode();
                            } else if (actView instanceof CompassView) {
                                return true;
                            }
                            return false;
                        }
                    });
                }

                locationManagerForeGround.setDelegate(foreGroundHelper);
                locationManagerForeGround.startUpdateLocation();
                locationManagerForeGround.startUpdateHeading();
            }
        });
    }

    private void removeForegroundLocationListener() {
        locationManagerForeGround.stopUpdateLocation();
        locationManagerForeGround.stopUpdateHeading();
        locationManagerForeGround.setDelegate(null);
    }

    private BackgroundTask backgroundTask;

    private void initialBackGroundLocationListener() {
        backgroundTask = new BackgroundTask();
        log.debug("start long time Background task");
        PlatformConnector.runOnBackGround(backgroundTask);
    }

    private void removeBackGroundLocationListener() {
        if (backgroundTask == null) return;
        log.debug("stop long time Background task");

        //get last background location and fire to foreground
        final Coordinate lastBackgroundCoord = backgroundTask.getLastCoord();
        backgroundTask.cancel();
        backgroundTask = null;
        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                EventHandler.fire(new PositionChangedEvent(lastBackgroundCoord, true));
            }
        });
    }

    public void pause() {
        log.debug("onPause");
        removeForegroundLocationListener();
        initialBackGroundLocationListener();
    }

    public void resume() {
        log.debug("onResume");
        CB.postAsyncDelayd(500, new NamedRunnable("Delayed start foreground location listener") {
            @Override
            public void run() {
                removeBackGroundLocationListener();
                initialForegroundLocationListener();
            }
        });
    }

    public void stopForgroundUpdates() {
        log.debug("Stop foreground location listener");
        removeForegroundLocationListener();
    }
}
