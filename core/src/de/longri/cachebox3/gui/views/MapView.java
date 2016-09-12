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
import org.oscim.gdx.LayerHandler;
import org.oscim.gdx.MotionHandler;

/**
 * Created by Longri on 24.07.16.
 */
public class MapView extends AbstractView {

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
        GestureDetector gestureDetectore = new GestureDetector(new LayerHandler(CacheboxMain.mMap));
        MotionHandler motionHandler = new MotionHandler(CacheboxMain.mMap);
        MapInputHandler inputHandler = new MapInputHandler(CacheboxMain.mMap);
        mapInputHandler = new InputMultiplexer();
        mapInputHandler.addProcessor(motionHandler);
        mapInputHandler.addProcessor(gestureDetectore);
        mapInputHandler.addProcessor(inputHandler);
        StageManager.addMapMultiplexer(mapInputHandler);
    }

    @Override
    public void onHide() {
        CacheboxMain.drawMap = false;
        StageManager.removeMapMultiplexer(mapInputHandler);
    }


    @Override
    public void dispose() {

    }
}
