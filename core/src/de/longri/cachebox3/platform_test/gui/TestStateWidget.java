/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.platform_test.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CircularProgressStyle;
import de.longri.cachebox3.gui.widgets.CircularProgressWidget;


/**
 * Created by Longri on 18.03.2019.
 */
public class TestStateWidget extends WidgetGroup {

    // widget with three visual states
    // -- checked
    // -- unchecked
    // -- inProgress


    final CircularProgressWidget circPro;
    final Image checkImage = new Image(new TextureRegionDrawable(CB.getSprite("check_on")), Scaling.fill, Align.center);
    final Image checkOff = new Image(new TextureRegionDrawable(CB.getSprite("check_off")), Scaling.fill, Align.center);
    final Image checkNot = new Image(new TextureRegionDrawable(CB.getSprite("check_not")), Scaling.fill, Align.center);
    final float prefSize;

    private Actor actActor;

    public TestStateWidget() {

        //reduce pref size
        CircularProgressStyle style = new CircularProgressStyle(VisUI.getSkin().get("circularProgressStyle", CircularProgressStyle.class));
        style.scaledPreferedRadius = CB.getScaledFloat(5);
        prefSize = style.scaledPreferedRadius * 4;
        style.unknownColor = Color.BLACK;

        this.circPro = new CircularProgressWidget(style);
        this.addActor(checkOff);
        this.actActor = checkOff;

        this.circPro.setProgress(-10);

//        this.setDebug(true);
    }

    private void setState() {
        this.removeActor(circPro);
        this.addActor(checkNot);
    }

    public float getPrefWidth() {
        return prefSize;
    }

    public float getPrefHeight() {
        return prefSize;
    }


    @Override
    protected void sizeChanged() {
        this.circPro.setBounds(0, 0, this.getWidth(), this.getHeight());
        this.checkImage.setBounds(0, 0, this.getWidth(), this.getHeight());
        this.checkOff.setBounds(0, 0, this.getWidth(), this.getHeight());
        this.checkNot.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    public void setState(PlatformTestViewItem.State state) {

        Actor newActor = checkOff;

        switch (state) {

            case NOT_TESTED:
                newActor = checkOff;
                break;
            case TEST_OK:
                newActor = checkImage;
                break;
            case TEST_FAIL:
                newActor = checkNot;
                break;
            case IN_PROGRESS:
                newActor = circPro;
                break;
        }

        this.removeActor(actActor);
        this.addActor(newActor);
        this.actActor = newActor;

    }
}
