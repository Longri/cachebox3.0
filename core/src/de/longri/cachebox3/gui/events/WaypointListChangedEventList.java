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
package de.longri.cachebox3.gui.events;


import de.longri.cachebox3.types.Cache;

import java.util.ArrayList;

public class WaypointListChangedEventList {
	public static ArrayList<WaypointListChangedEvent> list = new ArrayList<WaypointListChangedEvent>();

	public static void Add(WaypointListChangedEvent event) {
		synchronized (list) {
			if (!list.contains(event))
				list.add(event);
		}
	}

	public static void Remove(WaypointListChangedEvent event) {
		synchronized (list) {
			list.remove(event);
		}
	}

	public static void Call(final Cache cache) {
		// Aufruf aus in einen neuen Thread packen
		if (cache != null) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (list) {
						for (WaypointListChangedEvent event : list) {
							event.WaypointListChanged(cache);
						}
					}
				}
			});

			thread.run();
		}

	}

}
