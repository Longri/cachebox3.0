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
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.VisUI;

import java.util.ArrayList;

/**
 * Created by Longri on 24.07.16.
 */
public class GestureButton extends Button {

    private GestureButtonStyle style;
    private final ArrayList<ActionButton> mButtonActions;


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
        mButtonActions = new ArrayList<ActionButton>();
    }

    public void addAction(ActionButton Action) {
        mButtonActions.add(Action);

//        // disable Gesture ?
//        if (!CB_UI_Base_Settings.GestureOn.getValue())
//            Action.setGestureDirection(ActionButton.GestureDirection.None);
//
//        ActionButton.GestureDirection gestureDirection = Action.getGestureDirection();
//        if (gestureDirection != ActionButton.GestureDirection.None) {
//            if (help == null) {
//                float h = GL_UISizes.BottomButtonHeight * 2;
//                help = new GestureHelp(new SizeF(h, h), "help");
//            }
//
//            NinePatch ninePatch = null;
//            if (this.drawableNormal instanceof NinePatchDrawable) {
//                ninePatch = ((NinePatchDrawable) this.drawableNormal).getPatch();
//            } else if (this.drawableNormal instanceof SpriteDrawable) {
//                int p = Sprites.patch;
//                Sprite s = ((SpriteDrawable) this.drawableNormal).getSprite();
//                ninePatch = new NinePatch(s, p, p, p, p);
//            }
//
//            help.addBtnIcon(ninePatch);
//
//            if (gestureDirection == ActionButton.GestureDirection.Up) {
//                help.addUp(Action.getIcon());
//            } else if (gestureDirection == ActionButton.GestureDirection.Down) {
//                help.addDown(Action.getIcon());
//            } else if (gestureDirection == ActionButton.GestureDirection.Left) {
//                help.addLeft(Action.getIcon());
//            } else if (gestureDirection == ActionButton.GestureDirection.Right) {
//                help.addRight(Action.getIcon());
//            }
//        }
    }
}
