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
package de.longri.cachebox3;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import de.longri.cachebox3.locator.manager.IOS_LocationHandler;
import org.oscim.backend.DateTime;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.ios.backend.IosGL;
import org.oscim.ios.backend.IosGraphics;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.glkit.GLKViewDrawableMultisample;
import org.robovm.apple.glkit.GLKViewDrawableStencilFormat;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;

public class IOS_Launcher extends IOS_Launcher_BackgroundHandling {

    final static Logger log = LoggerFactory.getLogger(IOS_Launcher.class);


    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        boolean retValue = super.didFinishLaunching(application, launchOptions);

        Gdx.app.setApplicationLogger(new IOS_ApplicationLogger());
        float displayHeightCentimeter = Gdx.graphics.getHeight() / Gdx.graphics.getPpiY() * 2.54f;
        if (displayHeightCentimeter < 9) {
            CB.setGlobalScale(0.8f);
        } else {
            CB.setGlobalScale(1f);
        }

        CB.locationHandler = new IOS_LocationHandler();


        return retValue;
    }


    @Override
    protected IOSApplication createApplication() {


        final String appDir = System.getenv("HOME");
        final String localPath = appDir + "/Library/local/Cachebox3/";

        LibgdxLogger.PROPERTIES_FILE_HANDLE = new LibgdxLoggerIosFileHandle(localPath, Files.FileType.Absolute).child(LibgdxLogger.CONFIGURATION_FILE_XML);
        LibgdxLogger.initial(LibgdxLogger.PROPERTIES_FILE_HANDLE);


        //initialize platform bitmap factory
        IosGraphics.init();


        //initialize platform connector
        PlatformConnector.init(new IOS_PlatformConnector(this));
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.multisample = GLKViewDrawableMultisample._4X;
        config.orientationLandscape = false;
        config.orientationPortrait = true;
        config.stencilFormat = GLKViewDrawableStencilFormat._8;
        config.allowIpod = true;
        config.preferredFramesPerSecond = 24;
        GdxAssets.init("assets/");
        GLAdapter.init(new IosGL());

        DateTimeAdapter.init(new DateTime());

        return new IOSApplication(new CacheboxMain(), config);
    }

//    public int getIosVersion() {
//        String systemVersion = UIDevice.getCurrentDevice().getSystemVersion().substring(0, 1);
//        return Integer.parseInt(systemVersion);
//    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOS_Launcher.class);
        pool.close();
    }
}