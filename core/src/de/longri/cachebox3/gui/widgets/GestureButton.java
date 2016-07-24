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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;

/**
 * Created by Longri on 24.07.16.
 */
public class GestureButton extends Button {

    private GestureButtonStyle style;


    static public class GestureButtonStyle extends ButtonStyle {
        Drawable select;
    }

    public GestureButton(String styleName) {
        style = VisUI.getSkin().get(styleName, GestureButtonStyle.class);
        style.checked = style.select;
        this.setStyle(style);
        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    Gdx.app.log("", "Toggle checked");
                    GestureButton.this.setChecked(GestureButton.this.isChecked());
                }
            }
        });
    }


}
