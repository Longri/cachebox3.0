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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.ios.backend.IosGL;
import org.oscim.ios.backend.IosGraphics;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.glkit.GLKViewDrawableMultisample;
import org.robovm.apple.glkit.GLKViewDrawableStencilFormat;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIScreen;

public class IOS_Launcher extends IOSApplication.Delegate {

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        boolean retValue = super.didFinishLaunching(application, launchOptions);

        Gdx.app.setApplicationLogger(new IOS_ApplicationLogger());

        return retValue;
    }


    @Override
    protected IOSApplication createApplication() {
//
//        float scale = (float) (getIosVersion() >= 8 ? UIScreen.getMainScreen().getNativeScale() : UIScreen.getMainScreen().getScale());
////        CanvasAdapter.dpi *= scale;
//
//        CanvasAdapter.dpi = 20 * scale;

        //initialize platform bitmap factory
        IosGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new IOS_PlatformConnector());
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.multisample = GLKViewDrawableMultisample._4X;
        config.orientationLandscape = false;
        config.orientationPortrait = true;
        config.stencilFormat = GLKViewDrawableStencilFormat._8;
        GdxAssets.init("assets/");
        GLAdapter.init(new IosGL());

        return new IOSApplication(new CacheboxMain(), config);
    }

    private int getIosVersion() {
        String systemVersion = UIDevice.getCurrentDevice().getSystemVersion().substring(0, 1);
        return Integer.parseInt(systemVersion);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOS_Launcher.class);
        pool.close();
    }
}