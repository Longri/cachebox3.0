package de.longri.cachebox3.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.longri.cachebox3.AwtPlatformConnector;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.PlatformConnector;
import org.oscim.awt.AwtGraphics;

public class DesktopLauncher {
    public static void main(String[] arg) {

        //initialize platform bitmap factory
        AwtGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new AwtPlatformConnector());

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 411;
        config.height = 700;
        config.title = "Cachebox 3.0";
        new LwjglApplication(new CacheboxMain(), config);
    }
}
