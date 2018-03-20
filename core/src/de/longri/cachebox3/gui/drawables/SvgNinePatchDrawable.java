/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.drawables;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Created by Longri on 23.07.16.
 */
public class SvgNinePatchDrawable extends NinePatchDrawable {


    public static class SvgNinePatchDrawableUnScaledValues {
        // unscaled values from SvgSkin
        public int left, right, top, bottom, leftWidth, rightWidth, topHeight, bottomHeight;
    }

    public String name;
    public SvgNinePatchDrawableUnScaledValues values;

    public SvgNinePatchDrawable() {
        super();
    }

    public SvgNinePatchDrawable(NinePatch ninePatch) {
        super(ninePatch);
    }

    public SvgNinePatchDrawable(NinePatch ninePatch, int leftWidth, int rightWidth, int topHeight, int bottomHeight) {
        super(ninePatch);
        if (topHeight >= 0) setTopHeight(topHeight);
        if (rightWidth >= 0) setRightWidth(rightWidth);
        if (bottomHeight >= 0) setBottomHeight(bottomHeight);
        if (leftWidth >= 0) setLeftWidth(leftWidth);
    }

    public void setPatch(NinePatch patch) {
        super.setPatch(patch);
        setMinWidth(patch.getPadLeft() + patch.getPadRight());
        setMinHeight(patch.getPadTop() + patch.getPadBottom());
        setTopHeight(patch.getPadTop());
        setRightWidth(patch.getPadRight());
        setBottomHeight(patch.getPadBottom());
        setLeftWidth(patch.getPadLeft());
    }
}
