/*
 * Copyright (C) 2017 team-cachebox.de
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

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 19.06.2017.
 */
public class AndroidFlashLight {

    final private AtomicBoolean isOn = new AtomicBoolean(false);
    final private boolean IS_AVAILABLE, USE_CAMERA2_API;
    private final String mCameraId;
    private CameraManager mCameraManager;
    private Camera deviceCamera;

    public AndroidFlashLight(Context context) {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            USE_CAMERA2_API = true;
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = null;
            String id = "";
            try {
                cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
                id = mCameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCameraId = id;
            IS_AVAILABLE = cameraCharacteristics == null ? false : cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        } else {
            USE_CAMERA2_API = false;
            boolean hardwareAvailable = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            boolean permission = false;
            if (hardwareAvailable) {
                //check for permission
                try {
                    deviceCamera = Camera.open();
                    deviceCamera.stopPreview();
                    deviceCamera.release();
                    deviceCamera = null;
                    permission = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            IS_AVAILABLE = hardwareAvailable && permission;
            mCameraId = "";
        }

    }


    public void switchOn() {
        if (this.USE_CAMERA2_API) {
            try {
                mCameraManager.setTorchMode(mCameraId, true);
                isOn.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            deviceCamera = Camera.open();
            Camera.Parameters p = deviceCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            deviceCamera.setParameters(p);
            deviceCamera.startPreview();
            isOn.set(true);
        }
    }

    public void switchOff() {
        if (this.USE_CAMERA2_API) {
            try {
                mCameraManager.setTorchMode(mCameraId, false);
                isOn.set(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            deviceCamera.stopPreview();
            deviceCamera.release();
            deviceCamera = null;
            isOn.set(false);
        }

    }

    public boolean isOn() {
        return isOn.get();
    }

    public boolean available() {
        return this.IS_AVAILABLE;
    }

}
