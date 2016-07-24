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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 23.07.16.
 */
public class SvgNinePatchDrawable extends BaseDrawable {

    private NinePatch patch;

    String name;
    int left, right, top, bottom;

    public SvgNinePatchDrawable() {
    }

    public SvgNinePatchDrawable(SvgNinePatchDrawable drawable) {
        super(drawable);
        setPatch(drawable.patch);
    }


    public void draw(Batch batch, float x, float y, float width, float height) {
        if (this.patch == null) {
            // get texture region
            TextureRegion textureRegion = VisUI.getSkin().getRegion(this.name);

            //scale nine patch regions
            left= CB.getScaledInt(left);

            setPatch(new NinePatch(textureRegion, left, right, top, bottom));
        }
        patch.draw(batch, x, y, width, height);
    }

    public void setPatch(NinePatch patch) {
        this.patch = patch;
        setMinWidth(patch.getTotalWidth());
        setMinHeight(patch.getTotalHeight());
        setTopHeight(patch.getPadTop());
        setRightWidth(patch.getPadRight());
        setBottomHeight(patch.getPadBottom());
        setLeftWidth(patch.getPadLeft());
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
        return left + right;
    }

    public float getMinHeight() {
        return top + bottom;
    }


    /**
     * Creates a new drawable that renders the same as this drawable tinted the specified color.
     */
    public SvgNinePatchDrawable tint(Color tint) {
        SvgNinePatchDrawable drawable = new SvgNinePatchDrawable(this);
        drawable.setPatch(new NinePatch(drawable.getPatch(), tint));
        return drawable;
    }

}
