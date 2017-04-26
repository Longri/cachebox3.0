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

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.callbacks.GenericCallBack;
import org.oscim.backend.canvas.Bitmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public abstract class PlatformConnector {

    static PlatformConnector platformConnector;

    public static void init(PlatformConnector connector) {
        platformConnector = connector;
    }

    public static boolean isTorchAvailable() {
        return platformConnector._isTorchAvailable();
    }

    protected abstract boolean _isTorchAvailable();

    public static boolean isTorchOn() {
        return platformConnector._isTorchOn();
    }

    protected abstract boolean _isTorchOn();

    public static void switchTorch() {
        platformConnector._switchTorch();
    }

    protected abstract void _switchTorch();

    public static void getApiKey(GenericCallBack<String> callBack) {
        platformConnector.generateApiKey(callBack);
    }

    public static PlatformDescriptionView getDescriptionView() {
        return platformConnector.getPlatformDescriptionView();
    }

    public static void setDescriptionViewToNULL() {
        platformConnector.descriptionViewToNull();
    }

    protected abstract void descriptionViewToNull();

    // SVG implementations #############################################################################################
    public enum SvgScaleType {
        SCALED_TO_WIDTH, SCALED_TO_HEIGHT, DPI_SCALED, NONE, SCALED_TO_WIDTH_OR_HEIGHT
    }

    public static Bitmap getSvg(String name, InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        return platformConnector.getRealScaledSVG(name, stream, scaleType, scaleValue);
    }


    public abstract Bitmap getRealScaledSVG(String name, InputStream stream,
                                            SvgScaleType scaleType, float scaleValue) throws IOException;


    public abstract void initialLocationReciver();

    public static void initLocationListener() {
        platformConnector.initialLocationReciver();
    }


    public static FileHandle getSandboxFileHandle(String fileName) {
        return platformConnector._getSandBoxFileHandle(fileName);
    }

    public abstract FileHandle _getSandBoxFileHandle(String fileName);

    public static String getWorkPath() {
        return platformConnector._getWorkPath();
    }

    protected abstract String _getWorkPath();

    protected abstract void generateApiKey(GenericCallBack<String> callBack);

    protected abstract PlatformDescriptionView getPlatformDescriptionView();

}
