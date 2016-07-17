package de.longri.cachebox3;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import org.oscim.backend.CanvasAdapter;
import org.oscim.ios.backend.IosGraphics;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIScreen;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {

        float scale = (float) (getIosVersion() >= 8 ? UIScreen.getMainScreen().getNativeScale() : UIScreen.getMainScreen().getScale());
        CanvasAdapter.dpi *= scale;

        //initialize platform bitmap factory
        IosGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new IosPlatformConnector());


        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        return new IOSApplication(new CacheboxMain(), config);
    }

    private int getIosVersion() {
        String systemVersion = UIDevice.getCurrentDevice().getSystemVersion().substring(0, 1);
        return Integer.parseInt(systemVersion);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}