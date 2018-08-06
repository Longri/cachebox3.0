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

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by Longri on 11.05.2016.
 */
public class AndroidPermissionCheck {

    public static final int MY_PERMISSIONS_REQUEST = 11052;

    static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    static final String WAKE_LOCK = "android.permission.WAKE_LOCK";
    static final String INTERNET = "android.permission.INTERNET";
    static final String ACCESS_NETWORK_STATE = "android.permission.ACCESS_NETWORK_STATE";
    static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    static final String VIBRATE = "android.permission.VIBRATE";
    static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    static final String CAMERA = "android.permission.CAMERA";

    static final String FLASHLIGHT = "android.permission.FLASHLIGHT";
    static final String CAMERA2 = "android.hardware.camera";
    static final String AUTOFOCUS = "android.hardware.camera.autofocus";
    static final String FLASH = "android.hardware.camera.flash";


    static final String[] NEEDED_PERMISSIONS = new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, WAKE_LOCK
            , INTERNET, ACCESS_NETWORK_STATE, RECORD_AUDIO, CAMERA, VIBRATE, WRITE_EXTERNAL_STORAGE
            , READ_EXTERNAL_STORAGE, FLASHLIGHT, CAMERA2, AUTOFOCUS, FLASH};

    public static void checkNeededPermissions(Activity context) {
        ArrayList<String> DENIED_List = new ArrayList<String>();

        for (String permission : NEEDED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                DENIED_List.add(permission);
            }
        }

        if (!DENIED_List.isEmpty()) {
            String[] ar = DENIED_List.toArray(new String[DENIED_List.size()]);
            ActivityCompat.requestPermissions(context, ar, MY_PERMISSIONS_REQUEST);
        }

    }

}
