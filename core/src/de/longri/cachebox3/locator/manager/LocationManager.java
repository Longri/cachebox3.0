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
package de.longri.cachebox3.locator.manager;

import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.locator.Region;


/**
 * Created by Longri on 06.03.18.
 */
public abstract class LocationManager {
    public abstract void setDelegate(LocationEvents locationEvents);

    public abstract void startUpdateLocation();

    public abstract void startUpdateHeading();

    public abstract void stopUpdateLocation();

    public abstract void stopUpdateHeading();

    public abstract void setDistanceFilter(float distance);

    public abstract void dispose();

    public abstract void stopMonitoring(Region region);

    public abstract void startMonitoring(Region region);

    public abstract void setCanCalibrateCallBack(GenericHandleCallBack<Boolean> canCalibrateCallBack);
}
