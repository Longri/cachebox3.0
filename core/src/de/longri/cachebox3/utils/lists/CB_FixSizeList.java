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
package de.longri.cachebox3.utils.lists;

/**
 * A list of objects <T> with fix capacity.<p>
 * If the list is full und you put a new Object to this list, so the last object will loose!<p>
 * Created by Longri on 16.02.2016.
 */
public class CB_FixSizeList<T> extends CB_List<T> {

	final private int capacity;

	/**
	 * Creates an ordered array with the specified capacity.
	 */
	public CB_FixSizeList(int capacity) {
		this.capacity = capacity;
		items = this.createNewItems(capacity);
	}

	public T addAndGetLastOut(T value) {

		T loosed = null;
		if (this.size == this.capacity) {
			loosed = this.items[0];
		}

		add(value);

		return loosed;
	}

	public int add(T value) {
		if (size == getItemLength()) {
			// don't grow up this list, delete the last Item and put the new on the top!
			System.arraycopy(items, 1, items, 0, size - 1);
			size--;
		}
		int ID = size;
		this.items[size++] = value;
		return ID;
	}

}
