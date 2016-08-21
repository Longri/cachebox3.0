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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;

/**
 * Created by Longri on 20.08.16.
 */
public class Blink extends RepeatAction {

    public Blink() {
        this(0.15f,0.3f);
    }



    public Blink(float fadeTime, float stopTime){
        setCount(RepeatAction.FOREVER);

        Action blinkSequence = Actions.sequence(
                Actions.alpha(0, fadeTime),
                Actions.delay(stopTime),
                Actions.alpha(1, fadeTime),
                Actions.delay(stopTime));

        setAction(blinkSequence);
    }

}
