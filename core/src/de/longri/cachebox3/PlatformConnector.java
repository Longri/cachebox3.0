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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.NamedRunnable;
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

    public static String createThumb(String path, int scaledWidth, String thumbPrefix) {
        return platformConnector._createThumb(path, scaledWidth, thumbPrefix);
    }

    protected abstract String _createThumb(String path, int scaledWidth, String thumbPrefix);

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

    public static void getDescriptionView(GenericCallBack<PlatformDescriptionView> callBack) {
        platformConnector.getPlatformDescriptionView(callBack);
    }

    public static void setDescriptionViewToNULL() {
        platformConnector.descriptionViewToNull();
    }

    protected abstract void descriptionViewToNull();

    public static void _openUrlExtern(final String link) {
        if (CB.isGlThread()) {
            platformConnector.openUrlExtern(link);
        } else {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    platformConnector.openUrlExtern(link);
                }
            });
        }
    }

    public abstract void openUrlExtern(String link);

    public static void callQuit() {
        platformConnector._callQuit();
    }

    protected abstract void _callQuit();

    protected abstract void _postOnMainThread(NamedRunnable runnable);

    public static void postOnMainThread(NamedRunnable runnable) {
        platformConnector._postOnMainThread(runnable);
    }

    public static void runOnBackGround(Runnable backgroundTask) {
        platformConnector._runOnBackGround(backgroundTask);
    }

    protected abstract void _runOnBackGround(Runnable backgroundTask);

    public static void playNotifySound(FileHandle soundFileHandle) {
        platformConnector._playNotifySound(soundFileHandle);
    }

    protected abstract void _playNotifySound(FileHandle soundFileHandle);

    // SVG implementations #############################################################################################
    public enum SvgScaleType {
        SCALED_TO_WIDTH, SCALED_TO_HEIGHT, DPI_SCALED, NONE, SCALED_TO_WIDTH_OR_HEIGHT
    }

    public static Bitmap getSvg(String name, InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        return platformConnector.getRealScaledSVG(name, stream, scaleType, scaleValue);
    }


    public abstract Bitmap getRealScaledSVG(String name, InputStream stream,
                                            SvgScaleType scaleType, float scaleValue) throws IOException;


    public static FileHandle getSandboxFileHandle(String fileName) {
        return platformConnector._getSandBoxFileHandle(fileName);
    }

    public abstract FileHandle _getSandBoxFileHandle(String fileName);

    public static String getWorkPath() {
        return platformConnector._getWorkPath();
    }

    protected abstract String _getWorkPath();

    protected abstract void generateApiKey(GenericCallBack<String> callBack);

    protected abstract void getPlatformDescriptionView(GenericCallBack<PlatformDescriptionView> callBack);


    //Text Input
    public static void getSinglelineTextInput(Input.TextInputListener listener, CharSequence title, CharSequence text, CharSequence hint) {
        Gdx.input.getTextInput(listener, title.toString(), text.toString(), hint.toString());
    }

    public abstract void _getMultilineTextInput(Input.TextInputListener listener, String title, String text, String hint);

    public static void getMultilineTextInput(Input.TextInputListener listener, String title, String text, String hint) {
        platformConnector._getMultilineTextInput(listener, title, text, hint);
    }
}
