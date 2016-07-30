package de.longri.cachebox3.utils.lists;

import java.util.Arrays;

public class CB_StringList extends CB_List<String> {
	private static final long serialVersionUID = -3065632065615067769L;
	private String[] items;
	private int[] hashList;

	/** Creates an ordered String array with a capacity of 16. */
	public CB_StringList() {
		items = this.createNewItems(16);
		hashList = new int[16];
	}

	/** Creates an ordered String array with the specified capacity. */
	public CB_StringList(int capacity) {
		items = this.createNewItems(capacity);
		hashList = new int[capacity];
	}

	/**
	 * Creates a new String array containing the elements in the specific String array. The new array will be ordered if the specific array
	 * is ordered. The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be grown.
	 */
	public CB_StringList(CB_StringList array) {
		size = array.size;
		items = this.createNewItems(size);
		System.arraycopy(array.items, 0, items, 0, size);

		hashList = new int[size];
		System.arraycopy(array.hashList, 0, this.hashList, 0, size);
	}

	public CB_StringList(String[] array) {
		size = array.length;
		items = this.createNewItems(size);
		System.arraycopy(array, 0, items, 0, size);

		hashList = new int[size];
		for (int i = 0; i < size; i++) {
			hashList[i] = items[i].hashCode();
		}
	}

	@Override
	protected String[] createNewItems(int size) {
		return new String[size];
	}

	@Override
	protected String[] resize(int newSize) {
		this.hashList = Arrays.copyOf(this.hashList, newSize);
		this.items = Arrays.copyOf(this.items, newSize);
		return this.items;
	}

	@Override
	public int add(String value) {
		if (size == this.items.length) {
			resize(size + (size >> 1));
		}
		int ID = size;
		this.items[size] = value;
		hashList[size++] = value.hashCode();
		return ID;
	}

	@Override
	public boolean contains(String value) {
		return indexOf(value) >= 0;
	}

	@Override
	public int indexOf(String value) {
		if (value == null)
			return -1;
		int valueHash = value.hashCode();

		for (int i = 0, n = size; i < n; i++) {
			if (hashList[i] == valueHash) {
				if (items[i].equals(value))
					return i;
			}
		}

		return -1;
	}

	public int getHash(int index) {
		return hashList[index];
	}

}
