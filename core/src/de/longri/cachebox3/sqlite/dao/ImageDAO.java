/* 
 * Copyright (C) 2014 team-cachebox.de
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
package de.longri.cachebox3.sqlite.dao;

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import de.longri.cachebox3.sqlite.Database.Parameters;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.ImageEntry;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ImageDAO {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(ImageDAO.class);

    /**
     * @param image
     * @param ignoreExisting
     */
    public void WriteToDatabase(ImageEntry image, Boolean ignoreExisting) {
        Parameters args = new Parameters();
        args.put("CacheId", image.CacheId);
        args.put("GcCode", image.GcCode);
        args.put("Name", image.Name);
        args.put("Description", image.Description);
        args.put("ImageUrl", image.ImageUrl);
        args.put("IsCacheImage", image.IsCacheImage);
        try {
            if (ignoreExisting) {
                Database.Data.insertWithConflictIgnore("Images", args);
            } else {
                Database.Data.insertWithConflictReplace("Images", args);
            }
        } catch (Exception exc) {
            log.error( "Write Image", "", exc);
        }
    }

    /**
     * @param GcCode
     * @return
     */
    public ArrayList<ImageEntry> getImagesForCache(String GcCode) {
        ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select CacheId, GcCode, Name, Description, ImageUrl, IsCacheImage from Images where GcCode=?", new String[]{GcCode});
        if (reader.getCount() > 0) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                ImageEntry image = new ImageEntry(reader);
                images.add(image);
                reader.moveToNext();
            }
        }
        reader.close();
        return images;
    }

    /**
     * @param GcCode
     */
    public void deleteImagesForCache(String GcCode) {
        Database.Data.execSQL("DELETE from Images where GcCode = '" + GcCode + "'");
    }

    /**
     * @param GcCode
     * @return
     */
    public ArrayList<ImageEntry> getDescriptionImagesForCache(String GcCode) {
        ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select CacheId, GcCode, Name, Description, ImageUrl, IsCacheImage from Images where GcCode=? and IsCacheImage=1", new String[]{GcCode});

        if (reader == null)
            return images;

        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            ImageEntry image = new ImageEntry(reader);
            images.add(image);
            reader.moveToNext();
        }
        reader.close();

        return images;
    }

    public ArrayList<String> getImageURLsForCache(String GcCode) {
        ArrayList<String> images = new ArrayList<String>();

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select ImageUrl from Images where GcCode=?", new String[]{GcCode});

        if (reader == null)
            return images;
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            images.add(reader.getString(0));
            reader.moveToNext();
        }
        reader.close();

        return images;
    }

    public int getImageCount(String whereClause) {
        int count = 0;

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select count(id) from Images where GcCode in (select GcCode from Caches " + ((whereClause.length() > 0) ? "where " + whereClause : whereClause) + ")", null);

        if (reader == null)
            return 0;
        reader.moveToFirst();

        if (!reader.isAfterLast()) {
            count = reader.getInt(0);
        }
        reader.close();

        return count;
    }

}
