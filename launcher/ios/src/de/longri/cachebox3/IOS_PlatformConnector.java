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
import com.badlogic.gdx.files.FileHandle;
import org.oscim.backend.canvas.Bitmap;
import org.robovm.apple.avfoundation.AVCaptureDevice;
import org.robovm.apple.avfoundation.AVCaptureTorchMode;
import org.robovm.apple.avfoundation.AVMediaType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class IOS_PlatformConnector extends PlatformConnector {

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

        if (device.getTorchMode() == AVCaptureTorchMode.Off) {
            device.setTorchMode(AVCaptureTorchMode.On);
        } else {
            device.setTorchMode(AVCaptureTorchMode.Off);
        }
    }

    @Override
    public Bitmap getRealScaledSVG(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {
        return new IOS_RealSvgBitmap(inputStream, scaleType, scaleValue);
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
    public FileHandle _getSandBoxFileHandle(String fileName) {
        return new FileHandle(new File(System.getenv("HOME"), "Library/local/" + fileName).getAbsolutePath());
    }
}
