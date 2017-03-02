/* 
 * Copyright (C) 2011 team-cachebox.de
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


/// <summary>
/// Diese klasse ist von DynamicList(Of T) abgeleitet und implementiert Methoden zum bewegen der elemente in einer DynamicList(Of T)
/// </summary>
/// 
/// 
///  <example>
///  <para><b>Der Aufruf folgender Methoden l�sen ein Changed Event aus:</b></para>
/// 
/// <para>
///  add /
///  AddRange / 
///  Clear / 
///  Insert / 
///  InsertRange / 
///  remove /
///  RemoveAll / 
///  RemoveAt / 
///  RemoveRange / (geerbt von DynamicList(Of T)
///  </para>
/// 
/// 
/// <para><b>Neu implementierte Methoden, welche ein Changed Event aus l�sen:</b></para>
/// <para >
///  MoveItem /
///  MoveItemFirst /
///  MoveItemLast /
///  MoveItemsLeft /
///  MoveItemsRight /
/// 
/// </para>
/// 
/// <code lang="VB" title=" Kompletter Code dieser Klasse: " source="C:\@Work\Longri_Sammlung\coc_VB01\MoveableDynamicList.vb"/>
/// 
/// 
/// </example>
/// 
/// 
/// <typeparam name="T">Der Typ der Elemente in der Liste. </typeparam>
/// <remarks>
/// 
/// Auf Elemente in dieser Auflistung kann mithilfe eines ganzzahligen Index zugegriffen werden. Diese Auflistung verwendet nullbasierte Indizes. 
/// 
///
///</remarks>

/**
 * @author Longri
 */
public class MoveableList<T> extends CB_List<T> {

	private static final long serialVersionUID = -3030926604332765746L;
	protected CB_List<IChanged> ChangedEventList = new CB_List<IChanged>();

	public MoveableList() {
		super();
	}

	public MoveableList(MoveableList<T> list) {
		super(list);
	}

	protected void fireChangedEvent() {
		if (dontFireEvent)
			return;
		synchronized (ChangedEventList) {
			for (int i = 0, n = ChangedEventList.size(); i < n; i++) {
				IChanged event = ChangedEventList.get(i);
				event.isChanged();
			}
		}

	}

	public void addChangedEventListener(IChanged listener) {
		synchronized (ChangedEventList) {
			if (!ChangedEventList.contains(listener))
				ChangedEventList.add(listener);
		}
	}

	public void removeChangedEventListener(IChanged listener) {
		synchronized (ChangedEventList) {
			ChangedEventList.remove(listener);
		}
	}

	private void PrivateMoveItem(int CutItem, int InsertItem) {
		T CutItemInfo = this.get(CutItem);

		this.remove(CutItem);
		this.add(InsertItem, CutItemInfo);

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

	@Override
	public int add(T t) {
		int ID = super.add(t);
		fireChangedEvent();
		return ID;
	}

	@Override
	public void add(int index, T t) {
		super.add(index, t);
		fireChangedEvent();
	}

	@Override
	public void addAll(CB_List<T> t) {
		super.addAll(t);
		fireChangedEvent();
	}

	@Override
	public void addAll(int index, CB_List<T> t) {
		super.addAll(index, t);
		fireChangedEvent();
	}

	@Override
	public void clear() {
		super.clear();
		fireChangedEvent();
	}

	@Override
	public T remove(int index) {
		T t = super.remove(index);
		fireChangedEvent();
		return t;
	}

	public void MoveItemsLeft() {
		PrivateMoveItem(0, this.size() - 1);
		_MoveResultIndex = -1;
	}

	public void MoveItemsRight() {
		PrivateMoveItem(this.size() - 1, 0);
		_MoveResultIndex = -1;
	}

	public void MoveItemFirst(int index) {
		PrivateMoveItem(index, 0);
		_MoveResultIndex = 0;

	}

	public void MoveItemLast(int index) {
		PrivateMoveItem(index, this.size() - 1);
		_MoveResultIndex = this.size() - 1;

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
		if (((Pos < this.size()) & (Pos >= 0)))
			return Pos;

		if (Negative) {
			Pos += this.size();
			Pos = ChkNewPos(Pos, true);
		} else {
			Pos -= this.size();
			Pos = ChkNewPos(Pos);
		}
		return Pos;
	}

	private int ChkNewPos(int Pos) {
		return this.ChkNewPos(Pos, false);
	}

	private int _MoveResultIndex;

	// / <summary>
	// / Gigbt die Position der letzten Move Methode zur�ck.
	// / </summary>
	// / <returns>Null-Basierender Index, des Ergebnisses der letzten Move Nethode</returns>
	// / <remarks>Bei den Methoden <see cref="MoveItemsLeft">[MoveItemsLeft]</see> und <see cref="MoveItemsRight">[MoveItemsRight]</see>
	// / wird die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> auf <b>-1</b> gesetzt, <b>da alle Items bewegt wurden.</b>
	// </remarks>
	public int MoveResultIndex() {
		return _MoveResultIndex;
	}

	public Iterator<T> reverseIterator() {
		final MoveableList<T> that = this;

		Iterator<T> iterator = new Iterator<T>() {
			int aktItem = that.size() - 1;

			@Override
			public boolean hasNext() {
				if (aktItem >= 0)
					return true;
				return false;
			}

			@Override
			public T next() {
				if (that.size() == 0 || that.size() < aktItem) {
					aktItem = -1;
					return null;
				}

				T ret = that.get(aktItem);
				aktItem--;
				return ret;
			}

			@Override
			public void remove() {

			}
		};

		return iterator;
	}

	public void remove(MoveableList<T> items) {
		super.removeAll(items);
	}

	public void dispose() {
		super.dispose();
	}

}
