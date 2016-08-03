package de.longri.cachebox3.gui.stages.initial_tasks;

/**
 * Created by Longri on 02.08.16.
 */
public abstract class AbstractInitTask {

    public final String name;
    public final int percent;

    public AbstractInitTask(String name, int percent) {
        this.name = name;
        this.percent = percent;
    }

    public abstract void RUNABLE();

}

