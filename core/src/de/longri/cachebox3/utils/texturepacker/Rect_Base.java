package de.longri.cachebox3.utils.texturepacker;

import java.util.ArrayList;

public abstract class Rect_Base {
	public static Rect_Base that;
	public Object image;
	public String name;
	public int offsetX;
	public int offsetY;
	public int originalWidth;
	public int originalHeight;
	public int x;
	public int y;
	public int width;
	public int height;
	public int index;
	public boolean rotated;
	public ArrayList<Rect_Base> aliases = new ArrayList<Rect_Base>();
	public int[] splits;
	public int[] pads;
	public boolean canRotate = true;
	protected int score1;
	protected int score2;

	public abstract Rect_Base getInstanz();

	public abstract Rect_Base getInstanz(Rect_Base rec);

	public Rect_Base() {
	}

	protected void setSize(Rect_Base freeNode) {
		x = freeNode.x;
		y = freeNode.y;
		width = freeNode.width;
		height = freeNode.height;
	}

	protected Rect_Base(Rect_Base freeNode) {
		setSize(freeNode);
	}

	protected void set(Rect_Base rect) {
		name = rect.name;
		image = rect.image;
		offsetX = rect.offsetX;
		offsetY = rect.offsetY;
		originalWidth = rect.originalWidth;
		originalHeight = rect.originalHeight;
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
		index = rect.index;
		rotated = rect.rotated;
		aliases = rect.aliases;
		splits = rect.splits;
		pads = rect.pads;
		canRotate = rect.canRotate;
		score1 = rect.score1;
		score2 = rect.score2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rect_Base other = (Rect_Base) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + "[" + x + "," + y + " " + width + "x" + height + "]";
	}

	public abstract int getWidth();

	public abstract int getHeight();

}