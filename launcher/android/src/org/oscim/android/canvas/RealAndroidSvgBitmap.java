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
package org.oscim.android.canvas;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.RectF;
import com.caverock.androidsvg.SVG;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Longri on 19.07.16.
 */
public class RealAndroidSvgBitmap extends AndroidBitmap {

    private static android.graphics.Bitmap getAndroidBitmap(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {
        synchronized (SVG.getVersion()) {
            try {
                SVG svg = SVG.getFromInputStream(inputStream);
                Picture picture = svg.renderToPicture();

                float scale = 1;

                switch (scaleType) {

                    case SCALED_TO_WIDTH:
                        scale = scaleValue / picture.getWidth();
                        break;
                    case SCALED_TO_HEIGHT:
                        scale = scaleValue / picture.getHeight();
                        break;
                    case DPI_SCALED:
                        scale = CB.getScaledFloat(scaleValue);
                        break;
                }

                float bitmapWidth = picture.getWidth() * scale;
                float bitmapHeight = picture.getHeight() * scale;

                android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap((int) Math.ceil(bitmapWidth),
                        (int) Math.ceil(bitmapHeight), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawPicture(picture, new RectF(0, 0, bitmapWidth, bitmapHeight));

                return bitmap;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    public RealAndroidSvgBitmap(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {
        super(getAndroidBitmap(inputStream, scaleType, scaleValue));
    }

}
