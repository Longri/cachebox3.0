/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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

import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.Database.Parameters;
import de.longri.cachebox3.types.Category;
import de.longri.cachebox3.types.GpxFilename;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CategoryDAO {
    final static Logger log = LoggerFactory.getLogger(CategoryDAO.class);

    public Category ReadFromCursor(GdxSqliteCursor reader) {
        Category result = new Category();

        result.Id = reader.getLong(0);
        result.GpxFilename = reader.getString(1);
        result.pinned = reader.getInt(2) != 0;

        // alle GpxFilenames einlesen
        GdxSqliteCursor reader2 = Database.Data.rawQuery("select ID, GPXFilename, Imported, CacheCount from GpxFilenames where CategoryId=?", new String[]
                {String.valueOf(result.Id)});
        reader2.moveToFirst();
        while (reader2.isAfterLast() == false) {
            GpxFilenameDAO gpxFilenameDAO = new GpxFilenameDAO();
            GpxFilename gpx = gpxFilenameDAO.ReadFromCursor(reader2);
            result.add(gpx);
            reader2.moveToNext();
        }
        reader2.close();

        return result;
    }

    public Category CreateNewCategory(String filename) {
        filename = new File(filename).getName();

        // neue Category in DB anlegen
        Category result = new Category();

        Parameters args = new Parameters();
        args.put("GPXFilename", filename);
        try {
            Database.Data.insert("Category", args);
        } catch (Exception exc) {
            log.error(filename, exc);
        }

        long Category_ID = 0;

        GdxSqliteCursor reader = Database.Data.rawQuery("Select max(ID) from Category", (String[]) null);
        reader.moveToFirst();
        if (reader.isAfterLast() == false) {
            Category_ID = reader.getLong(0);
        }
        reader.close();
        result.Id = Category_ID;
        result.GpxFilename = filename;
        result.Checked = true;
        result.pinned = false;

        return result;
    }

    public GpxFilename CreateNewGpxFilename(Category category, String filename) {
        filename = new File(filename).getName();

        Parameters args = new Parameters();
        args.put("GPXFilename", filename);
        args.put("CategoryId", category.Id);
        String stimestamp = Database.cbDbFormat.format(new Date());
        args.put("Imported", stimestamp);
        try {
            Database.Data.insert("GpxFilenames", args);
        } catch (Exception exc) {
            log.error(filename, exc);
        }

        long GPXFilename_ID = 0;

        GdxSqliteCursor reader = Database.Data.rawQuery("Select max(ID) from GpxFilenames", (String[]) null);
        reader.moveToFirst();
        if (reader.isAfterLast() == false) {
            GPXFilename_ID = reader.getLong(0);
        }
        reader.close();
        GpxFilename result = new GpxFilename(GPXFilename_ID, filename, category.Id);
        category.add(result);
        return result;
    }

    public void SetPinned(Category category, boolean pinned) {
        if (category.pinned == pinned) return;
        category.pinned = pinned;

        Parameters args = new Parameters();
        args.put("pinned", pinned);
        try {
            Database.Data.update("Category", args, "Id=" + String.valueOf(category.Id), null);
        } catch (Exception exc) {
            log.error("SetPinned", exc);
        }
    }


//
//	public Category GetCategory(Categories categories, String filename)
//	{
//		filename = new File(filename).getName();
//		for (int i = 0, n = categories.size(); i < n; i++)
//		{
//			Category category = categories.get(i);
//			if (filename.toUpperCase().equals(category.GpxFilename.toUpperCase()))
//			{
//				return category;
//			}
//		}
//
//		Category cat = CreateNewCategory(filename);
//		categories.add(cat);
//		return cat;
//	}
//
//	public void DeleteEmptyCategories()
//	{
//		CoreSettingsForward.Categories.beginnTransaction();
//
//		Categories delete = new Categories();
//		for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++)
//		{
//			Category cat = CoreSettingsForward.Categories.get(i);
//			if (cat.CacheCount() == 0)
//			{
//				Database.Data.delete("Category", "Id=?", new String[]
//					{ String.valueOf(cat.Id) });
//				delete.add(cat);
//			}
//		}
//
//		for (int i = 0, n = delete.size(); i < n; i++)
//		{
//			CoreSettingsForward.Categories.remove(delete.get(i));
//		}
//		CoreSettingsForward.Categories.endTransaction();
//	}
}
