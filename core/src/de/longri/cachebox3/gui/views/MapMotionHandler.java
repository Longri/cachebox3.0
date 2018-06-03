/*
 * Copyright (C) 2017 team-cachebox.de
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

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import org.oscim.gdx.MotionHandler;
import org.oscim.map.Map;

/**
 * Created by Longri on 09.03.2017.
 */
public class MapMotionHandler extends MotionHandler {

    private final MapStateButton mapStateButton;

    public MapMotionHandler(Map map, MapStateButton mapStateButton) {
        super(map);
        this.mapStateButton = mapStateButton;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (CB.lastMapState.getMapMode() == MapMode.CAR || CB.lastMapState.getMapMode() == MapMode.LOCK) return true;
        return super.touchDragged(screenX, screenY, pointer);
    }
}
