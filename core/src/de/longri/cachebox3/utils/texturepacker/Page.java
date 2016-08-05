package de.longri.cachebox3.utils.texturepacker;

import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class Page {
	public String imageName;
	public Array<Rect_Base> outputRects, remainingRects;
	public float occupancy;
	public int x, y, width, height;
}
