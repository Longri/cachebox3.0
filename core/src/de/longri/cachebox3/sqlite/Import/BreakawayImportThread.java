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
package de.longri.cachebox3.sqlite.Import;

import java.util.concurrent.atomic.AtomicBoolean;

public class BreakawayImportThread extends Thread {

	private static BreakawayImportThread that;

	private static AtomicBoolean isCanceled = new AtomicBoolean(false);

	public BreakawayImportThread() {
		if (that != null && that.isAlive())
			throw new IllegalStateException("Is running");
		that = this;
		isCanceled.set(false);
	}

	public static boolean isCanceled() {
		return isCanceled.get();
	}

	public void cancel() {
		isCanceled.set(true);
		this.interrupt();
	}

	public static void reset() {
		if (that != null) {
			that.interrupt();
			that = null;
		}

		isCanceled.set(false);
	}

}
