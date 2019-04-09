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
package de.longri.cachebox3.types;


import de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber;
import de.longri.gdx.sqlite.GdxSqliteCursor;

import java.io.Serializable;
import java.net.URI;

public class ImageEntry implements Serializable {

	private static final long serialVersionUID = 4216092006574290607L;

	/**
	 * Ignore Existing by Import
	 */
	public boolean ignoreExisting = false;

	/**
	 * Beschreibung des Bildes
	 */
	public String Description = "";

	/**
	 * name des Bildes
	 */
	public String Name = "";

	/**
	 * ImageUrl des Bildes
	 */
	public String ImageUrl = "";

	/**
	 * lokaler Pfad des Bildes
	 */
	public String LocalPath = "";

	/**
	 * Id des Caches
	 */
	public long CacheId = -1;

	/**
	 * GcCode des Caches
	 */
	public String GcCode = "";

	/**
	 * Ist das Bild aus der Cachebeschreibung
	 */
	public Boolean IsCacheImage = false;

	public ImageEntry() {
	}

	/**
	 * @param reader
     */
	public ImageEntry(GdxSqliteCursor reader) {
		CacheId = reader.getLong(0);
		GcCode = reader.getString(1).trim();
		Name = reader.getString(2);
		Description = reader.getString(3);
		ImageUrl = reader.getString(4);
		IsCacheImage = reader.getInt(5) == 1 ? true : false;

		LocalPath = DescriptionImageGrabber.BuildDescriptionImageFilename(GcCode, URI.create(ImageUrl));
	}

	public void clear() {
		Description = "";
		Name = "";
		ImageUrl = "";
		CacheId = -1;
		GcCode = "";
		IsCacheImage = false;
		LocalPath = "";
	}

	public void dispose() {
		Description = null;
		Name = null;
		ImageUrl = null;
		GcCode = null;
		LocalPath = null;
	}

}
