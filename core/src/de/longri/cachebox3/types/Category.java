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
package de.longri.cachebox3.types;

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.GdxSqliteCursor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Category extends ArrayList<GpxFilename> implements Comparable<Category> {
    /**
     *
     */
    private static final long serialVersionUID = -7257078663021910097L;
    public long Id;
    public String GpxFilename;
    public boolean pinned;
    public boolean Checked;

    public Category() {
    }

    /**
     * Does not check if filename not already exists in this category
     *
     * @param path
     * @return
     */
    public GpxFilename addGpxFilename(String path) {

        String filename = Gdx.files.absolute(path).name();

        Database.Parameters args = new Database.Parameters();
        args.put("GPXFilename", filename);
        args.put("CategoryId", this.Id);
        String stimestamp = Database.cbDbFormat.format(new Date());
        args.put("Imported", stimestamp);
        try {
            Database.Data.insert("GpxFilenames", args);
        } catch (Exception exc) {
            //Log.err(log, "CreateNewGpxFilename", filename, exc);
        }

        long GPXFilename_ID = 0;

        GdxSqliteCursor reader = Database.Data.rawQuery("Select max(ID) from GpxFilenames", (String[]) null);
        reader.moveToFirst();
        if (!reader.isAfterLast()) {
            GPXFilename_ID = reader.getLong(0);
        }
        reader.close();
        GpxFilename result = new GpxFilename(GPXFilename_ID, filename, this.Id);
        this.add(result);
        return result;
    }


    public int CacheCount() {
        int result = 0;
        for (GpxFilename gpx : this)
            result += gpx.CacheCount;
        return result;
    }

    public Date LastImported() {
        if (size() == 0) return new Date();
        return this.get(this.size() - 1).Imported;
    }

    public String GpxFilenameWoNumber() {
        // Nummer der PocketQuery weglassen, wenn dahinter noch eine Bezeichnung kommt.
        String name = GpxFilename;
        int pos = name.indexOf('_');
        if (pos < 0) return name;
        String part = name.substring(0, pos);
        if (part.length() < 7) return name;
        try {
            // Vorderen Teil nur dann weglassen, wenn dies eine Zahl ist.
            Integer.valueOf(part);
        } catch (Exception exc) {
            return name;
        }

        name = name.substring(pos + 1, name.length());

        if (name.toLowerCase().indexOf(".gpx") == name.length() - 4) name = name.substring(0, name.length() - 4);
        return name;
    }

    @Override
    public int compareTo(Category o) {
        if (o.Id > this.Id) return 1;
        else if (o.Id < this.Id) return -1;
        else
            return 0;
    }

    /**
     * gibt den chk status der enthaltenen GpxFiles zur�ck </br> 0 = keins
     * ausgew�hlt </br> 1 = alle ausgew�hlt </br> -1 = nicht alle, aber
     * mindestens eins ausgew�hlt
     *
     * @return
     */
    public int getChek() {
        int result = 0;

        int chkCounter = 0;
        int counter = 0;
        for (GpxFilename gpx : this) {
            if (gpx.Checked) chkCounter++;

            counter++;
        }

        if (chkCounter == 0) result = 0;
        else if (chkCounter == counter) result = 1;
        else
            result = -1;

        return result;
    }

}
