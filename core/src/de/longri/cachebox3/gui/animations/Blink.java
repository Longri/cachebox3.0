package de.longri.cachebox3.gui.animations;

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
