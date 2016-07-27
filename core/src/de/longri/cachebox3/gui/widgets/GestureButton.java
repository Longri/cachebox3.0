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

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;

import java.util.ArrayList;

/**
 * Created by Longri on 24.07.16.
 */
public class GestureButton extends Button {

    private static int idCounter = 0;

    private GestureButtonStyle style;
    private final ArrayList<ActionButton> buttonActions;
    private final int ID;

    static public class GestureButtonStyle extends ButtonStyle {
        Drawable select;
    }

    public GestureButton(String styleName) {
        style = VisUI.getSkin().get(styleName, GestureButtonStyle.class);
        style.checked = style.select;
        this.setStyle(style);
        this.ID = idCounter++;
        buttonActions = new ArrayList<ActionButton>();
    }

    public boolean equals(Object other) {
        if (other instanceof GestureButton) {
            return ((GestureButton) other).ID == ID;
        }
        return false;
    }

    public void addAction(ActionButton action) {
        buttonActions.add(action);
    }

    public void executeDefaultAction() {
        for (ActionButton action : buttonActions) {
            if (action.isDefaultAction()) {
                action.getAction().callExecute();
                return;
            }
        }

        //if no default button so take the first
        ActionButton action =buttonActions.get(0);
        if(action!=null)action.getAction().callExecute();
    }

    public void executeAction(ActionButton.GestureDirection direction) {
        for (ActionButton action : buttonActions) {
            if (action.getGestureDirection() == direction) {
                action.getAction().callExecute();
                return;
            }
        }
    }
}
