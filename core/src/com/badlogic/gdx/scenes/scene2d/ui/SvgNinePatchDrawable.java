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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

/**
 * Created by Longri on 23.07.16.
 */
public class SvgNinePatchDrawable extends BaseDrawable {

    public static class SvgNinePatchDrawableUnScaledValues {
        // unscaled values from SvgSkon
        public int left, right, top, bottom, leftWidth, rightWidth, topHeight, bottomHeight;
    }


    NinePatch patch;

    public String name;
    private int left, right, top, bottom;
    private float additionalPrefWidth = 0, additionalPrefHeight = 0;
    public SvgNinePatchDrawableUnScaledValues values;

    public SvgNinePatchDrawable() {
    }

//    public SvgNinePatchDrawable(SvgNinePatchDrawable drawable) {
//        super(drawable);
//        setPatch(drawable.patch, drawable.leftWidth, drawable.rightWidth, drawable.topHeight, drawable.bottomHeight);
//    }

    public SvgNinePatchDrawable(NinePatch ninePatch, int leftWidth, int rightWidth, int topHeight, int bottomHeight) {
        setPatch(ninePatch, leftWidth, rightWidth, topHeight, bottomHeight);
    }


    public void draw(Batch batch, float x, float y, float width, float height) {
        if (patch != null) patch.draw(batch, x, y, width, height);
    }

    public void setPatch(NinePatch patch, int leftWidth, int rightWidth, int topHeight, int bottomHeight) {
        this.patch = patch;
        setMinWidth(patch.getTotalWidth());
        setMinHeight(patch.getTotalHeight());
        setTopHeight(topHeight);
        setRightWidth(rightWidth);
        setBottomHeight(bottomHeight);
        setLeftWidth(leftWidth);
    }

    public NinePatch getPatch() {
        return patch;
    }

    public float getLeftWidth() {
        return left;
    }

    public void setLeftWidth(float leftWidth) {
        this.left = (int) leftWidth;
    }

    public float getRightWidth() {
        return right;
    }

    public void setRightWidth(float rightWidth) {
        this.right = (int) rightWidth;
    }

    public float getTopHeight() {
        return top;
    }

    public void setTopHeight(float topHeight) {
        this.top = (int) topHeight;
    }

    public float getBottomHeight() {
        return bottom;
    }

    public void setBottomHeight(float bottomHeight) {
        this.bottom = (int) bottomHeight;
    }

    public float getMinWidth() {
        return left + right + additionalPrefWidth;
    }

    public float getMinHeight() {
        return top + bottom + additionalPrefHeight;
    }


    public void setAdditionalPrefWidth(float minWidth) {
        this.additionalPrefWidth = minWidth - (left + right);
    }


    public void setAdditionalPrefHeight(float minHeight) {
        this.additionalPrefHeight = minHeight - (top + bottom);
    }


}
