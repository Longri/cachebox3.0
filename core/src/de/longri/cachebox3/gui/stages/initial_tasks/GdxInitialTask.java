package de.longri.cachebox3.gui.stages.initial_tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 02.08.16.
 */
public final class GdxInitialTask extends AbstractInitTask {

    public GdxInitialTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void RUNABLE() {
        CB.inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(CB.inputMultiplexer);
    }
}