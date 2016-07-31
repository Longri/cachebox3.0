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

package de.longri.cachebox3.utils.lists;


import de.longri.cachebox3.utils.MoveableList;

/**
 * The Stack class represents a last-in-first-out (LIFO) stack of objects. With option for max item Size.
 * 
 * @author Longri
 * @param <T>
 */
public class CB_Stack<T> {

	private MoveableList<T> items;
	private int maxItemSize = -1;

	public CB_Stack() {
		items = new MoveableList<T>();
	}

	/**
	 * Add an item onto the last of this stack.
	 * 
	 * @param item
	 */
	public void add(T item) {
		synchronized (items) {
			if (items.contains(item))
				return;
			items.add(item);
			// Log.debug(log, "STACK add SIZE=" + items.size + "  (item: " + item.toString() + ")");
			checkMaxItemSize();
		}
	}

	/**
	 * Removes the object at the top of this stack and returns that object as the value of this function.
	 * 
	 * @return
	 */
	public T get() {
		synchronized (items) {
			if (items.size == 0) {
				// Log.debug(log, "STACK empty Get");
				return null;
			}

			T ret = items.remove(0);

			// Log.debug(log, "STACK get SIZE=" + (items.size - 1) + "  (item: " + ret.toString() + ")");

			return ret;
		}
	}

	public boolean contains(T value) {
		synchronized (items) {
			return items.contains(value);
		}
	}

	public int getMaxItemSize() {
		return maxItemSize;
	}

	public void setMaxItemSize(int size) {
		maxItemSize = size;
		checkMaxItemSize();
	}

	/**
	 * Tests if this stack is empty.
	 * 
	 * @return
	 */
	public boolean empty() {
		synchronized (items) {
			return items.size <= 0;
		}
	}

	private void checkMaxItemSize() {
		synchronized (items) {
			if (maxItemSize < 1)
				return;
			if (items.size > maxItemSize) {
				int removeCount = items.size - maxItemSize;
				for (int i = 0; i < removeCount; i++) {
					items.remove(0);
				}
			}
		}
	}

	public int getSize() {
		synchronized (items) {
			return items.size;
		}
	}

	public void addAll_removeOther(CB_List<T> descList) {
		synchronized (items) {
			items.clear();
			items.addAll(descList);
		}
	}

	public interface iCompare<T> {
		public int compare(T item1, T item2);
	}

	public void sort(iCompare<T> comparable) {

		boolean change = false;

		do {
			change = false;
			for (int i = 0; i < items.size - 1; i++) {
				int compare = comparable.compare(items.get(i), items.get(i + 1));
				if (compare <= 0)
					continue; // no changes

				this.items.MoveItem(i + 1, -1);
				change = true;
				break;
			}
		} while (change);

	}
}
