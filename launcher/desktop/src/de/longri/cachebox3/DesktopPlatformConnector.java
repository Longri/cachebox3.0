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

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class DesktopPlatformConnector extends PlatformConnector {

    static {
        CB.platform = CB.Platform.DESKTOP;
    }

    @Override
    protected boolean _isTorchAvailable() {
        return false;
    }

    @Override
    protected boolean _isTorchOn() {
        return false;
    }

    @Override
    protected void _switchTorch() {
        // is not implemented, do nothing
    }

    @Override
    public Bitmap getRealScaledSVG(InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        return new DesktopRealSvgBitmap(stream, scaleType, scaleValue);
    }

    @Override
    public void initialLocationReciver() {

    }

    @Override
    public FileHandle _getSandBoxFileHandle(String fileName) {
        return Gdx.files.local(fileName);
    }

    @Override
    protected String _getWorkPath() {
        return _getSandBoxFileHandle("Cachebox3").file().getAbsolutePath();
    }
}
