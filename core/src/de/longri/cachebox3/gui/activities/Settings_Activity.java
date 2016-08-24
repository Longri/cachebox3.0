package de.longri.cachebox3.gui.activities;

import de.longri.cachebox3.gui.ActivityBase;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.08.2016.
 */
public class Settings_Activity extends ActivityBase {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(Settings_Activity.class);

    public Settings_Activity() {
        super("Settings_Activity");
    }

    @Override
    public void onShow() {
        log.debug("Show Settings");
    }

}
