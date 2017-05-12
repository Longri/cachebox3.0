/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.locator.geocluster.ClusterRunnable;
import de.longri.cachebox3.settings.Config;
import org.oscim.backend.canvas.Bitmap;
import org.robovm.apple.avfoundation.AVCaptureDevice;
import org.robovm.apple.avfoundation.AVCaptureTorchMode;
import org.robovm.apple.avfoundation.AVMediaType;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class IOS_PlatformConnector extends PlatformConnector {
    final static Logger log = LoggerFactory.getLogger(IOS_PlatformConnector.class);

    final IOS_Launcher ios_launcher;

    public IOS_PlatformConnector(IOS_Launcher ios_launcher) {
        super();
        this.ios_launcher = ios_launcher;
    }

    @Override
    protected boolean _isTorchAvailable() {
        AVCaptureDevice device = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);
        return device.hasTorch();
    }

    @Override
    protected boolean _isTorchOn() {
        AVCaptureDevice device = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);
        return (device.getTorchMode() == AVCaptureTorchMode.On);
    }

    @Override
    protected void _switchTorch() {
        AVCaptureDevice device = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);

        try {
            device.lockForConfiguration();
            if (device.getTorchMode() == AVCaptureTorchMode.Off) {
                log.debug("Switch torch on");
                device.setTorchMode(AVCaptureTorchMode.On);
            } else {
                log.debug("Switch torch off");
                device.setTorchMode(AVCaptureTorchMode.Off);
            }
        } catch (NSErrorException e) {
            e.printStackTrace();
        }


    }

    @Override
    public Bitmap getRealScaledSVG(String name, InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {

        IOS_RealSvgBitmap bmp = new IOS_RealSvgBitmap(inputStream, scaleType, scaleValue);
        bmp.name = name;
        return bmp;
    }


    IOS_LocationListener locationManager;

    @Override
    public void initialLocationReciver() {
        Gdx.app.log("step", "1");
        locationManager = new IOS_LocationListener();
        locationManager.createLocationManager();
    }

    @Override
    protected String _getWorkPath() {
        return _getSandBoxFileHandle("Cachebox3").file().getAbsolutePath();
    }


    @Override
    protected void generateApiKey(GenericCallBack<String> callBack) {
        log.debug("Show WebView for get API key");
        try {
            UIViewController mainViewController = ((IOSApplication) Gdx.app).getUIWindow().getRootViewController();
            GenerateApiKeyWebViewController controller = new GenerateApiKeyWebViewController(callBack, mainViewController);
            ((IOSApplication) Gdx.app).getUIWindow().setRootViewController(controller);
            ((IOSApplication) Gdx.app).getUIWindow().makeKeyAndVisible();
        } catch (Exception e) {
            log.error("show web view", e);
        }
    }

    @Override
    protected void getPlatformDescriptionView(final GenericCallBack<PlatformDescriptionView> callBack) {
        log.debug("Show WebView as descriptionView");
        try {
            UIViewController mainViewController = ((IOSApplication) Gdx.app).getUIWindow().getRootViewController();
            IOS_DescriptionView view = new IOS_DescriptionView(mainViewController);


            callBack.callBack(view);

        } catch (Exception e) {
            log.error("show web view", e);
        }

    }

    @Override
    protected void descriptionViewToNull() {
//TODO set descriptionViewToNull
    }

    @Override
    public void openUrlExtern(String link) {
        //TODO openUrlExtern
    }

    @Override
    public FileHandle _getSandBoxFileHandle(String fileName) {
        return new FileHandle(new File(System.getenv("HOME"), "Library/local/" + fileName).getAbsolutePath());
    }
}
