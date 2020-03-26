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
import com.badlogic.gdx.utils.Clipboard;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.NamedRunnable;
import org.oscim.backend.canvas.Bitmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public abstract class PlatformConnector {

    public static final String REDIRECT_URL = "https://gc-oauth.longri.de/index.php?";
    public static final String REDIRECT_STAGING_URL = "https://staging.gc-oauth.longri.de/index.php?";

    static PlatformConnector platformConnector;
    private static Clipboard clipBoard;

    public static void init(PlatformConnector connector) {
        platformConnector = connector;
    }

    public static boolean isTorchAvailable() {
        return platformConnector._isTorchAvailable();
    }

    public static String createThumb(String path, int scaledWidth, String thumbPrefix) {
        return platformConnector._createThumb(path, scaledWidth, thumbPrefix);
    }

    public static void setClipboard(Clipboard _clipBoard) {
       clipBoard = _clipBoard;
    }

    public static Clipboard getClipboard() {
        if (clipBoard == null) {
            return null;
        } else {
            return clipBoard;
        }
    }

    public static boolean isTorchOn() {
        return platformConnector._isTorchOn();
    }

    public static void switchTorch() {
        platformConnector._switchTorch();
    }

    public static void getDescriptionView(GenericCallBack<PlatformWebView> callBack) {
        platformConnector.getPlatformDescriptionView(callBack);
    }

    public static void setDescriptionViewToNULL() {
        platformConnector.descriptionViewToNull();
    }

    public static void callUrl(final String link) {
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

    public static void callQuit() {
        platformConnector._callQuit();
    }

    public static void postOnMainThread(NamedRunnable runnable) {
        platformConnector._postOnMainThread(runnable);
    }

    public static void runOnBackGround(Runnable backgroundTask) {
        platformConnector._runOnBackGround(backgroundTask);
    }

    public static void playNotifySound(FileHandle soundFileHandle) {
        platformConnector._playNotifySound(soundFileHandle);
    }

    public static Bitmap getSvg(String name, InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        return platformConnector.getRealScaledSVG(name, stream, scaleType, scaleValue);
    }

    public static FileHandle getSandboxFileHandle(String fileName) {
        return platformConnector._getSandBoxFileHandle(fileName);
    }

    public static String getWorkPath() {
        return platformConnector._getWorkPath();
    }

    //Text Input
    public static void getSinglelineTextInput(Input.TextInputListener listener, int inputType, CharSequence title, CharSequence text, CharSequence hint) {
        // Gdx.input.getTextInput(listener, title.toString(), text.toString(), hint.toString()); // doesn't work on desktop launcher
        platformConnector._getTextInput(true, listener, inputType, title.toString(), text.toString(), hint.toString());
    }

    public static void getMultilineTextInput(Input.TextInputListener listener, int inputType, CharSequence title, CharSequence text, CharSequence hint) {
        platformConnector._getTextInput(false, listener, inputType, title.toString(), text.toString(), hint.toString());
    }

    protected abstract String _createThumb(String path, int scaledWidth, String thumbPrefix);

    protected abstract boolean _isTorchAvailable();

    protected abstract boolean _isTorchOn();

    protected abstract void _switchTorch();

    protected abstract void descriptionViewToNull();

    public abstract void openUrlExtern(String link);

    protected abstract void _callQuit();

    protected abstract void _postOnMainThread(NamedRunnable runnable);

    protected abstract void _runOnBackGround(Runnable backgroundTask);

    protected abstract void _playNotifySound(FileHandle soundFileHandle);

    public abstract Bitmap getRealScaledSVG(String name, InputStream stream,
                                            SvgScaleType scaleType, float scaleValue) throws IOException;

    public abstract FileHandle _getSandBoxFileHandle(String fileName);

    protected abstract String _getWorkPath();

    protected abstract void getPlatformDescriptionView(GenericCallBack<PlatformWebView> callBack);

    public abstract void _getTextInput(boolean singleLine, Input.TextInputListener listener, int inputType, String title, String text, String hint);

    // SVG implementations #############################################################################################
    public enum SvgScaleType {
        SCALED_TO_WIDTH, SCALED_TO_HEIGHT, DPI_SCALED, NONE, SCALED_TO_WIDTH_OR_HEIGHT
    }
}
