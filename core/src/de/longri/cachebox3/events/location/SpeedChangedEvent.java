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
package de.longri.cachebox3.events.location;

import de.longri.cachebox3.events.AbstractEvent;

/**
 * Created by Longri on 23.03.2017.
 */
public class SpeedChangedEvent extends AbstractEvent<Float> {
    public final float speed;

    public SpeedChangedEvent(float speed) {
        super(Float.class);
        this.speed = speed;
    }

    public SpeedChangedEvent(float speed, short id) {
        super(Float.class, id);
        this.speed = speed;
    }

    @Override
    public Class getListenerClass() {
        return SpeedChangedListener.class;
    }
}
