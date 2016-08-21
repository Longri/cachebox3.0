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
package de.longri.cachebox3.gui.animations.actor_animations;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

/**
 * Created by Longri on 21.08.16.
 */
public class AddActorAction extends Action {
    private boolean added;
    private Actor actor;

    AddActorAction(Actor actor) {
        this.actor = actor;
    }

    public boolean act(float delta) {
        if (!added) {
            added = true;
            if (target instanceof WidgetGroup) {
                WidgetGroup group = (WidgetGroup) target;
                group.addActor(actor);
            }
        }
        return true;
    }

    public void restart() {
        added = false;
    }
}

