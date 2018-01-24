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
package de.longri.cachebox3.sqlite.dao;

import de.longri.cachebox3.types.GpxFilename;
import de.longri.gdx.sqlite.GdxSqliteCursor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GpxFilenameDAO
{
	public GpxFilename ReadFromCursor(GdxSqliteCursor reader)
	{
		long id;
		String gpxFileName;
		long categoryId = -1;

		id = reader.getLong(0);
		gpxFileName = reader.getString(1);

		GpxFilename result = new GpxFilename(id, gpxFileName, categoryId);

		if (reader.isNull(2)) result.Imported = new Date();
		else
		{
			String sDate = reader.getString(2);
			DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try
			{
				result.Imported = iso8601Format.parse(sDate);
			}
			catch (ParseException e)
			{
				result.Imported = new Date();
			}
		}

		if (reader.isNull(3)) result.CacheCount = 0;
		else
			result.CacheCount = reader.getInt(3);

		return result;
	}

//	public void GPXFilenameUpdateCacheCount()
//	{
//		// welche GPXFilenamen sind in der DB erfasst
//
//		DatabaseCiursor reader = GlobalData.Data.rawQuery(
//				"select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);
//
//		reader.moveToFirst();
//		while (reader.isAfterLast() == false)
//		{
//			Integer GPXFilename_ID = reader.getInt(0);
//			Integer CacheCount = reader.getInt(1);
//
//			Parameters args = new Parameters();
//			args.put("CacheCount", CacheCount);
//
//			GlobalData.Data.update("GPXFilenames", args, "ID=?", new String[]
//				{ String.valueOf(GPXFilename_ID) });
//			reader.moveToNext();
//		}
//
//		reader.close();
//
//		GlobalData.Data.delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
//		GlobalData.Data.delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
//	}

}
