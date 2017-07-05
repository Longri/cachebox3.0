/*
 * Copyright (C) 2017 team-cachebox.de
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
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.NinePatch;

/**
 * Created by Longri on 05.07.2017.
 */
public class CB_NinePatchDrawable extends NinePatchDrawable {

    public CB_NinePatchDrawable(NinePatch patch) {
        super(patch);
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
