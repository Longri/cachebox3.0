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
package de.longri.cachebox3.types;

import de.longri.cachebox3.sqlite.Database;

/**
 * A special class for Import, don't use that for hold and dispose if not used anymore!
 *
 * Created by Longri on 19.10.2017.
 */
public class WaypointImport extends AbstractWaypoint {

    public WaypointImport(double latitude, double longitude) {
        super(latitude, longitude);
    }

    @Override
    public float distance() {
        return 0;
    }

    @Override
    public CharSequence getGcCode() {
        return null;
    }

    @Override
    public void setGcCode(String gcCode) {

    }

    @Override
    public CharSequence getTitle() {
        return null;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public CharSequence getDescription(Database database) {
        return null;
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public CharSequence getClue(Database cb3Database) {
        return null;
    }

    @Override
    public void setClue(String clue) {

    }

    @Override
    public void setCheckSum(int i) {

    }

    @Override
    public int getCheckSum() {
        return 0;
    }

    @Override
    public long getCacheId() {
        return 0;
    }

    @Override
    public void setCacheId(long cacheId) {

    }

    @Override
    public CacheTypes getType() {
        return null;
    }

    @Override
    public void setType(CacheTypes type) {

    }

    @Override
    public boolean isUserWaypoint() {
        return false;
    }

    @Override
    public void setUserWaypoint(boolean userWaypoint) {

    }

    @Override
    public boolean isSyncExcluded() {
        return false;
    }

    @Override
    public void setSyncExcluded(boolean syncExcluded) {

    }

    @Override
    public boolean isStart() {
        return false;
    }

    @Override
    public void setStart(boolean start) {

    }

    public void reset(){

    }

    public void dispose(){

    }
}
