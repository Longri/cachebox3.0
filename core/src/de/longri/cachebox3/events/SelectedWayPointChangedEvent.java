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

import de.longri.cachebox3.types.AbstractWaypoint;

/**
 * Created by Longri on 23.03.2017.
 */
public class SelectedWayPointChangedEvent extends AbstractEvent<AbstractWaypoint> {
    public final AbstractWaypoint wayPoint;

    public SelectedWayPointChangedEvent(AbstractWaypoint wayPoint) {
        super(AbstractWaypoint.class);
        this.wayPoint = wayPoint;
    }

    public SelectedWayPointChangedEvent(AbstractWaypoint wayPoint, short id) {
        super(AbstractWaypoint.class, id);
        this.wayPoint = wayPoint;
    }

    @Override
    public Class getListenerClass() {
        return SelectedWayPointChangedListener.class;
    }
}
