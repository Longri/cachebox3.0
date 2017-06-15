/* 
 * Copyright (C) 2011 - 2017 team-cachebox.de
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

package de.longri.cachebox3.utils;

import de.longri.cachebox3.utils.lists.CB_List;

import java.util.Iterator;


/**
 * @author Longri
 */
public class MoveableList<T> extends CB_List<T> {

	protected CB_List<IChanged> ChangedEventList = new CB_List<>();

	public MoveableList() {
		super();
	}


	protected void fireChangedEvent() {
		if (dontFireEvent)
			return;
		synchronized (ChangedEventList) {
			for (int i = 0, n = ChangedEventList.size; i < n; i++) {
				IChanged event = ChangedEventList.get(i);
				event.isChanged();
			}
		}

	}

	public void addChangedEventListener(IChanged listener) {
		synchronized (ChangedEventList) {
			if (!ChangedEventList.contains(listener,true))
				ChangedEventList.add(listener);
		}
	}

	public void removeChangedEventListener(IChanged listener) {
		synchronized (ChangedEventList) {
			ChangedEventList.removeValue(listener,true);
		}
	}

	private void PrivateMoveItem(int CutItem, int index) {
		T CutItemInfo = this.get(CutItem);

		this.remove(CutItem);
		this.insert(index, CutItemInfo);

		fireChangedEvent();

	}

	private boolean dontFireEvent = false;

	public void beginnTransaction() {
		dontFireEvent = true;
	}

	public void endTransaction() {
		dontFireEvent = false;
		fireChangedEvent();
	}

	public void add(T t) {
		 super.add(t);
		fireChangedEvent();

	}

//	@Override
//	public void add(int index, T t) {
//		super.add(index, t);
//		fireChangedEvent();
//	}


//	public void addAll(CB_List<T> t) {
//		super.addAll(t);
//		fireChangedEvent();
//	}
//
//
//	public void addAll(int index, CB_List<T> t) {
//		super.addAll(index, t);
//		fireChangedEvent();
//	}

	@Override
	public void clear() {
		super.clear();
		fireChangedEvent();
	}


	public T remove(int index) {
		T t = super.removeIndex(index);
		fireChangedEvent();
		return t;
	}

	public void MoveItemsLeft() {
		PrivateMoveItem(0, this.size - 1);
		_MoveResultIndex = -1;
	}

	public void MoveItemsRight() {
		PrivateMoveItem(this.size - 1, 0);
		_MoveResultIndex = -1;
	}

	public void MoveItemFirst(int index) {
		PrivateMoveItem(index, 0);
		_MoveResultIndex = 0;

	}

	public void MoveItemLast(int index) {
		PrivateMoveItem(index, this.size - 1);
		_MoveResultIndex = this.size - 1;

	}

	public int MoveItem(int index, int Step) {
		_MoveResultIndex = index;
		if (index < 0)
			throw new IndexOutOfBoundsException();
		int Insert = 0;
		if (Step == 0) {
			return _MoveResultIndex;
		} else if (Step > 0) {
			Insert = ChkNewPos(index + Step);
		} else {
			Insert = ChkNewPos(index + Step, true);
		}

		if (Insert == index)
			return _MoveResultIndex;

		PrivateMoveItem(index, Insert);
		_MoveResultIndex = Insert;
		return _MoveResultIndex;
	}

	public void MoveItem(int index) {
		this.MoveItem(index, 1);
	}

	private int ChkNewPos(int Pos, boolean Negative) {
		if (((Pos < this.size) & (Pos >= 0)))
			return Pos;

		if (Negative) {
			Pos += this.size;
			Pos = ChkNewPos(Pos, true);
		} else {
			Pos -= this.size;
			Pos = ChkNewPos(Pos);
		}
		return Pos;
	}

	private int ChkNewPos(int Pos) {
		return this.ChkNewPos(Pos, false);
	}

	private int _MoveResultIndex;


//
//
//	public void remove(MoveableList<T> items) {
//		super.removeAll(items,true);
//	}
//
//	public void dispose() {
//
//	}

}
