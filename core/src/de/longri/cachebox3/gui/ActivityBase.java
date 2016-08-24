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
package de.longri.cachebox3.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;

/**
 * A wrapper class to bring the CB2 Activities to CB3
 * Created by Longri on 23.08.2016.
 */
public class ActivityBase extends Window implements Disposable {

    public final String name;
    protected final ActivityBaseStyle style;

    public ActivityBase(String name) {
        this(name, VisUI.getSkin().get("default", ActivityBaseStyle.class));
    }

    public ActivityBase(String name, ActivityBaseStyle style) {
        this.name = name;
        this.style = style;
        this.setStageBackground(style.background);
    }


    public void finish() {
        super.hide();
    }

    public void onShow() {

    }

    public void onHide() {

    }

    public void show() {
        super.show();

        //set to full screen
        this.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void dispose() {

    }

    public static class ActivityBaseStyle {
        Drawable background;
    }
}
