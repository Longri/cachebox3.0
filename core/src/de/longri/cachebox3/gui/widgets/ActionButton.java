/*
 * Copyright (C) 2016-2017 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.gui.actions.AbstractAction;

/**
 * Created by Longri on 24.07.16.
 */
public class ActionButton {
    public enum GestureDirection {
        None, Right, Up, Left, Down
    }


    private final AbstractAction action;
    private final boolean defaultAction;
    private GestureDirection gestureDirection = GestureDirection.None;

    public ActionButton(AbstractAction action, boolean defaultAction, GestureDirection gestureDirection) {
        this.action = action;
        this.defaultAction = defaultAction;
        this.gestureDirection = gestureDirection;
    }

    public ActionButton(AbstractAction action, boolean defaultAction) {
        this(action, defaultAction, GestureDirection.None);
    }

    public AbstractAction getAction() {
        return action;
    }

    public boolean isDefaultAction() {
        return defaultAction;
    }

    public boolean getEnabled() {
        if (action == null)
            return false;
        return action.getEnabled();
    }

    public GestureDirection getGestureDirection() {
        return gestureDirection;
    }

    public Drawable getIcon() {
        return action.getIcon();
    }

    public void setGestureDirection(GestureDirection gesture) {
        gestureDirection = gesture;
    }
}
