/*
 * Copyright (C) 2016 team-cachebox.de
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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.locator.Location;
import de.longri.cachebox3.locator.Locator;
import de.longri.cachebox3.locator.events.PositionChangedEvent;
import de.longri.cachebox3.locator.events.PositionChangedEventList;
import org.oscim.core.MapPosition;
import org.oscim.gdx.LayerHandler;
import org.oscim.gdx.MotionHandler;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.CacheboxMain.mMap;

/**
 * The MapView has transparent background. The Map render runs at CacheboxMain.
 * This View has only the controls for the Map!
 * Created by Longri on 24.07.16.
 */
public class MapView extends AbstractView implements PositionChangedEvent {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(MapView.class);

    InputMultiplexer mapInputHandler;

    public MapView() {
        super("MapView");
        this.setTouchable(Touchable.disabled);
    }

    @Override
    protected void create() {

    }

    @Override
    public void onShow() {
        CacheboxMain.drawMap = true;
        // map input handler
        GestureDetector gestureDetectore = new GestureDetector(new LayerHandler(mMap));
        MotionHandler motionHandler = new MotionHandler(mMap);
        MapInputHandler inputHandler = new MapInputHandler(mMap);
        mapInputHandler = new InputMultiplexer();
        mapInputHandler.addProcessor(motionHandler);
        mapInputHandler.addProcessor(gestureDetectore);
        mapInputHandler.addProcessor(inputHandler);
        StageManager.addMapMultiplexer(mapInputHandler);
        PositionChangedEventList.Add(this);
        testSetLocation();
    }

    @Override
    public void onHide() {
        CacheboxMain.drawMap = false;
        StageManager.removeMapMultiplexer(mapInputHandler);
        PositionChangedEventList.Remove(this);
    }

    private void testSetLocation() {


        de.longri.cachebox3.locator.Location cbLocation =
                new de.longri.cachebox3.locator.Location(52.580400947530364,
                        13.385594096047232, 10);

        cbLocation.setHasBearing(true);
//        cbLocation.setBearing(0);
//        cbLocation.setBearing(90);
//        cbLocation.setBearing(180);
        cbLocation.setBearing(360);

        cbLocation.setProvider(Location.ProviderType.GPS);
        log.trace("Update location:" + cbLocation.toString());
        de.longri.cachebox3.locator.Locator.setNewLocation(cbLocation);

    }

    @Override
    public void dispose() {

    }

    @Override
    public void PositionChanged() {
        MapPosition curentMapPosition = mMap.getMapPosition();
        Location curentLocation = Locator.getLocation();
        curentMapPosition.setPosition(curentLocation.latitude, curentLocation.longitude);
        mMap.setMapPosition(curentMapPosition);
    }

    @Override
    public void OrientationChanged() {
        MapPosition curentMapPosition = mMap.getMapPosition();
        float bearing = -Locator.getHeading() ;

        // heading must between -180 and 180
        if (bearing < -180) bearing += 360;
        log.trace("Update Map Heading:" + bearing);
        curentMapPosition.setBearing(bearing);
        mMap.setMapPosition(curentMapPosition);
    }

    @Override
    public void SpeedChanged() {

    }

    @Override
    public String getReceiverName() {
        return "MapView";
    }

    @Override
    public Priority getPriority() {
        return Priority.High;
    }
}
