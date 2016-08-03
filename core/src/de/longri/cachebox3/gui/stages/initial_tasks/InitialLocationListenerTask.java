package de.longri.cachebox3.gui.stages.initial_tasks;

import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.locator.Locator;

/**
 * Created by Longri on 02.08.16.
 */
public final class InitialLocationListenerTask extends AbstractInitTask {

    public InitialLocationListenerTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void RUNABLE() {

        //TODO initial with last saved location from settings
        new Locator(null);

        PlatformConnector.initLocationListener();
    }
}