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
package de.longri.cachebox3.events;

import de.longri.cachebox3.locator.CoordinateGPS;

/**
 * Created by Longri on 23.03.2017.
 */
public class PositionChangedEvent extends AbstractEvent<CoordinateGPS> {
    public final CoordinateGPS pos;

    public PositionChangedEvent(CoordinateGPS pos) {
        super(CoordinateGPS.class);
        this.pos = pos;
    }

    public PositionChangedEvent(CoordinateGPS pos, short id) {
        super(CoordinateGPS.class, id);
        this.pos = pos;
    }

    @Override
    Class getListenerClass() {
        return PositionChangedListener.class;
    }
}
