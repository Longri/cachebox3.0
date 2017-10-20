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

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.sqlite.Database;

/**
 * Created by Longri on 19.10.2017.
 */
public class Waypoint3 extends AbstractWaypoint {

    private final long cacheId;
    private final CharSequence gcCode, title;
    private final CacheTypes type;
    private final boolean isStart, syncExclude, userWaypoint;


    public Waypoint3(SQLiteGdxDatabaseCursor cursor) {
        super(cursor.getDouble(2), cursor.getDouble(3));
        this.cacheId = cursor.getLong(0);
        this.gcCode = new CharSequenceArray(cursor.getString(1));
        short typeOrigin = cursor.getShort(4);
        this.type = CacheTypes.get(typeOrigin);
        this.isStart = cursor.getInt(5) > 0;
        this.syncExclude = cursor.getInt(6) > 0;
        this.userWaypoint = cursor.getInt(7) > 0;
        this.title = new CharSequenceArray(cursor.getString(8));
    }

    //################################################################################
    //# properties retained at the class
    ///###############################################################################


    @Override
    public CharSequence getGcCode() {
        return this.gcCode;
    }

    @Override
    public CacheTypes getType() {
        return this.type;
    }

    @Override
    public CharSequence getTitle() {
        return this.title;
    }

    @Override
    public long getCacheId() {
        return this.cacheId;
    }

    @Override
    public boolean isUserWaypoint() {
        return this.userWaypoint;
    }

    @Override
    public boolean isSyncExcluded() {
        return this.syncExclude;
    }

    @Override
    public boolean isStart() {
        return this.isStart;
    }




    //################################################################################
    //# method's that throws exceptions
    ///###############################################################################

    private void throwNotChangeable(String propertyName) {
        throw new RuntimeException("'" + propertyName + "' is not changeable! Use WaypointImport.class instead of Waypoint3.class");
    }

    @Override
    public void setGcCode(String gcCode) {
        throwNotChangeable("GcCode");
    }


    @Override
    public void setType(CacheTypes type) {
        throwNotChangeable("Type");
    }

    @Override
    public void setTitle(String title) {
        throwNotChangeable("Title");
    }

    @Override
    public void setDescription(String description) {
        throwNotChangeable("Description");
    }

    @Override
    public void setCacheId(long cacheId) {
        throwNotChangeable("CacheId");
    }

    @Override
    public void setUserWaypoint(boolean userWaypoint) {
        throwNotChangeable("UserWaypoint");
    }

    @Override
    public void setSyncExcluded(boolean syncExcluded) {
        throwNotChangeable("SyncExcluded");
    }


    @Override
    public void setStart(boolean start) {
        throwNotChangeable("IsStart");
    }

    //################################################################################
    //# properties that not retained at the class but read/write directly from/to DB
    ///###############################################################################


    @Override
    public CharSequence getDescription(Database database) {
        SQLiteGdxDatabaseCursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT Description FROM WaypointsText WHERE GcCode=?", new String[]{this.gcCode.toString()});
            cursor.moveToFirst();
            return new CharSequenceArray(cursor.getString(0));
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Override
    public CharSequence getClue(Database database) {
        SQLiteGdxDatabaseCursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT Clue FROM WaypointsText WHERE GcCode=?", new String[]{this.gcCode.toString()});
            cursor.moveToFirst();
            return new CharSequenceArray(cursor.getString(0));
        } finally {
            if (cursor != null) cursor.close();
        }
    }


//---------------------------------------------------------------------------------------------

    @Override
    public float distance() {
        return 0;
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
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object instanceof AbstractWaypoint) {
            AbstractWaypoint other = (AbstractWaypoint) object;
            return this.getGcCode().equals(other.getGcCode());
        }
        return false;
    }


}
