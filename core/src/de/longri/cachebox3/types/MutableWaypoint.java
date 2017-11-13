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
 * <p>
 * Created by Longri on 19.10.2017.
 */
public class MutableWaypoint extends AbstractWaypoint {

    private double latitude = 0;
    private double longitude = 0;
    private long cacheId = 0L;
    private String gcCode = "";
    private String title = "";
    private CacheTypes type = CacheTypes.Undefined;
    private boolean isStart = false;
    private boolean syncExclude = false;
    private boolean userWaypoint = false;
    private String description = "";
    private String clue = "";


    public MutableWaypoint(double latitude, double longitude, long cacheId) {
        super(0, 0);
        this.latitude = latitude;
        this.longitude = longitude;
        this.cacheId = cacheId;
    }

    public MutableWaypoint(Database database, AbstractWaypoint waypoint) {
        super(0, 0);
        this.latitude = waypoint.getLatitude();
        this.longitude = waypoint.getLongitude();
        this.cacheId = waypoint.getCacheId();
        this.gcCode = waypoint.getGcCode().toString();
        this.title = waypoint.getTitle().toString();
        this.type = waypoint.getType();
        this.isStart = waypoint.isStart();
        this.syncExclude = waypoint.isSyncExcluded();
        this.userWaypoint = waypoint.isUserWaypoint();
        this.description = waypoint.getDescription(database).toString();
        this.clue = waypoint.getClue(database).toString();
    }

    public MutableWaypoint(String title, CacheTypes type, double latitude, double longitude, long cacheId, String s1, String gcCode) {
        super(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.cacheId = cacheId;
        this.gcCode = gcCode;
        this.title = title;
        this.isStart = false;
        this.syncExclude = false;
        this.userWaypoint = false;
        this.description = "";
        this.clue = "";
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public float distance() {
        return 0;
    }

    @Override
    public CharSequence getGcCode() {
        return this.gcCode;
    }

    @Override
    public void setGcCode(String gcCode) {
        this.gcCode = gcCode;
    }

    @Override
    public CharSequence getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public CharSequence getDescription(Database database) {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public CharSequence getClue(Database cb3Database) {
        return this.clue;
    }

    @Override
    public void setClue(String clue) {
        this.clue = clue;
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
        return this.cacheId;
    }

    @Override
    public void setCacheId(long cacheId) {
        this.cacheId = cacheId;
    }

    @Override
    public CacheTypes getType() {
        return this.type;
    }

    @Override
    public void setType(CacheTypes type) {
        this.type = type;
    }

    @Override
    public boolean isUserWaypoint() {
        return this.userWaypoint;
    }

    @Override
    public void setUserWaypoint(boolean userWaypoint) {
        this.userWaypoint = userWaypoint;
    }

    @Override
    public boolean isSyncExcluded() {
        return this.syncExclude;
    }

    @Override
    public void setSyncExcluded(boolean syncExcluded) {
        this.syncExclude = syncExcluded;
    }

    @Override
    public boolean isStart() {
        return this.isStart;
    }

    @Override
    public void setStart(boolean start) {
        this.isStart = start;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public AbstractWaypoint getMutable(Database database) {
        return this;
    }

    @Override
    public void setLatLon(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void reset() {
        latitude = 0;
        longitude = 0;
        cacheId = 0L;
        gcCode = "";
        title = "";
        type = CacheTypes.Undefined;
        isStart = false;
        syncExclude = false;
        userWaypoint = false;
        description = "";
        clue = "";
    }

    public void dispose() {
        gcCode = null;
        title = null;
        type = null;
        description = null;
        clue = null;
    }
}
