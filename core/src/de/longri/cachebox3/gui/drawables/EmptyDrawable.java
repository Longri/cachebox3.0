/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.longri.cachebox3.gui.drawables;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Drawable that stores the size information but doesn't draw anything.
 * 
 * @author Nathan Sweet
 */
public abstract class EmptyDrawable implements Drawable {
	private float leftWidth, rightWidth, topHeight, bottomHeight, minWidth, minHeight;

	public EmptyDrawable() {
	}

	/** Creates a new empty drawable with the same sizing information as the specified drawable. */
	public EmptyDrawable(Drawable drawable) {
		leftWidth = drawable.getLeftWidth();
		rightWidth = drawable.getRightWidth();
		topHeight = drawable.getTopHeight();
		bottomHeight = drawable.getBottomHeight();
		minWidth = drawable.getMinWidth();
		minHeight = drawable.getMinHeight();
	}

	@Override
	public abstract void draw(Batch batch, float x, float y, float width, float height);

	@Override
	public float getLeftWidth() {
		return leftWidth;
	}

	@Override
	public void setLeftWidth(float leftWidth) {
		this.leftWidth = leftWidth;
	}

	@Override
	public float getRightWidth() {
		return rightWidth;
	}

	@Override
	public void setRightWidth(float rightWidth) {
		this.rightWidth = rightWidth;
	}

	@Override
	public float getTopHeight() {
		return topHeight;
	}

	@Override
	public void setTopHeight(float topHeight) {
		this.topHeight = topHeight;
	}

	@Override
	public float getBottomHeight() {
		return bottomHeight;
	}

	@Override
	public void setBottomHeight(float bottomHeight) {
		this.bottomHeight = bottomHeight;
	}

	@Override
	public float getMinWidth() {
		return minWidth;
	}

	@Override
	public void setMinWidth(float minWidth) {
		this.minWidth = minWidth;
	}

	@Override
	public float getMinHeight() {
		return minHeight;
	}

	@Override
	public void setMinHeight(float minHeight) {
		this.minHeight = minHeight;
	}
}
