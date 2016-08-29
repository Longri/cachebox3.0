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

public enum ChangeType implements Serializable {
	Undefined, // 0
	SolverText, // 1
	NotesText, // 2
	WaypointChanged, // 3
	NewWaypoint, // 4
	DeleteWaypoint, // 5
	Found, // 6
	NotFound, // 7
	Archived, // 8
	NotArchived, // 9
	Available, // 10
	NotAvailable, // 11
	NumTravelbugs // 12

}
