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
public abstract class PlatformConnector {

    static PlatformConnector platformConnector;

    public static void init(PlatformConnector connector) {
        platformConnector = connector;
    }

    // SVG implementations #############################################################################################
    public enum SvgScaleType {
        SCALED_TO_WIDTH, SCALED_TO_HEIGHT, DPI_SCALED
    }

    public static Bitmap getSvg(InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        return platformConnector.getRealScaledSVG(stream, scaleType, scaleValue);
    }

    public abstract Bitmap getRealScaledSVG(InputStream stream,
                                            SvgScaleType scaleType, float scaleValue) throws IOException;


    public abstract void initialLocationReciver();

    public static void initLocationListener() {
        platformConnector.initialLocationReciver();
    }


    public static FileHandle getSandboxFileHandle(String fileName) {
        return platformConnector._getSandBoxFileHandle(fileName);
    }

    public abstract FileHandle _getSandBoxFileHandle(String fileName);

    public static String getWorkPath(){
        return platformConnector._getWorkPath();
    }

    protected abstract String _getWorkPath();

}
