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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 21.08.16.
 */
public class GestureHelpAnimation extends SequenceAction {


    public GestureHelpAnimation(final Vector2 start, final Vector2 end) {
        // create finger actor
        final Image finger = new Image(CB.getSprite("finger"));
        final Image fingerClick = new Image(CB.getSprite("finger_click"));


        //set start and end to finger of hand

        float fingerY = finger.getHeight() * 0.7f;
        float fingerX = finger.getWidth() * 0.2f;
        start.y -= fingerY;
        start.x += fingerX;
        end.y -= fingerY;
        end.x += fingerX;


        finger.addAction(Actions.moveTo(start.x, start.y));
        fingerClick.addAction(Actions.moveTo(start.x, start.y));

        this.addAction(new AddActorAction(finger));
        this.addAction(Actions.delay(0.7f));
        this.addAction(Actions.removeActor(finger));
        this.addAction(new AddActorAction(fingerClick));
        RunnableAction runnableAction = new RunnableAction();
        runnableAction.setRunnable(new Runnable() {
            @Override
            public void run() {
                RunnableAction runnableAction = new RunnableAction();
                runnableAction.setRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fingerClick.addAction(Actions.sequence(Actions.delay(0.7f), Actions.removeActor()));
                    }
                });

                Action action = Actions.sequence(Actions.moveTo(end.x, end.y, 1.0f, Interpolation.pow2), runnableAction);
                fingerClick.addAction(action);
                finger.addAction(Actions.moveTo(end.x, end.y));
            }
        });

        this.addAction(runnableAction);

    }
}
