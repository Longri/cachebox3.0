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

import java.util.Date;

public class GpxFilename implements Comparable<GpxFilename> {
    public long Id;
    public String GpxFileName;
    public Date Imported;
    public int CacheCount;
    public long CategoryId;
    public boolean Checked;

    public GpxFilename(long Id, String GpxFileName, long categoryId)
    {
        this.Id = Id;
        this.GpxFileName = GpxFileName;
        this.Imported = new Date();
        this.CategoryId = categoryId;
    }

	@Override
	public int compareTo(GpxFilename arg0) {
		 
		return 0;
	}

}
