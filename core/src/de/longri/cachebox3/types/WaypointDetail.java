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

import java.io.Serializable;

public class WaypointDetail implements Serializable {
	private static final long serialVersionUID = -3177862382324983452L;

	// / Lösung einer QTA
	private byte[] Clue;
	// / Kommentartext
	private byte[] Description;

	public int checkSum = 0; // for replication

	public WaypointDetail() {

	}

	public String getDescription() {
		if (Description == null)
			return Waypoint.EMPTY_STRING;
		return new String(Description, Waypoint.UTF_8);
	}

	public void setDescription(String description) {
		if (description == null) {
			Description = null;
			return;
		}
		Description = description.getBytes(Waypoint.UTF_8);
	}

	public String getClue() {
		if (Clue == null)
			return Waypoint.EMPTY_STRING;
		return new String(Clue, Waypoint.UTF_8);
	}

	public void setClue(String clue) {
		if (clue == null) {
			Clue = null;
			return;
		}
		Clue = clue.getBytes(Waypoint.UTF_8);
	}

	public void setCheckSum(int i) {
		checkSum = i;
	}

	public void dispose() {

	}

}
