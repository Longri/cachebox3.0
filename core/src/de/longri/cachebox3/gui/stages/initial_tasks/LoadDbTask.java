package de.longri.cachebox3.gui.stages.initial_tasks;

import de.longri.cachebox3.settings.Config;

/**
 * Created by Longri on 28.08.16.
 */
public class LoadDbTask extends AbstractInitTask {
    public LoadDbTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void RUNABLE() {
        String dbPath = Config.DatabasePath.getValue();
    }
}
