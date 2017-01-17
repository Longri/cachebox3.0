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

import com.badlogic.gdx.scenes.scene2d.ui.GetName;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.coregraphics.CGSize;
import org.robovm.apple.uikit.UIImage;
import svg.SVGRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Longri on 19.07.16.
 */
public class IOS_RealSvgBitmap extends org.oscim.ios.backend.IosBitmap implements GetName {

    public String name;

    public String getName() {
        return name;
    }

    private static UIImage getUIImage(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) {
        String svg = getStringFromInputStream(inputStream);
        SVGRenderer renderer = new SVGRenderer(svg);
        CGRect viewRect = renderer.getViewRect();


        double scale = 1;

        switch (scaleType) {

            case SCALED_TO_WIDTH:
                scale = scaleValue / viewRect.getWidth();
                break;
            case SCALED_TO_HEIGHT:
                scale = scaleValue / viewRect.getHeight();
                break;
            case DPI_SCALED:
                scale = CB.getScaledFloat(scaleValue);
                break;
        }

        double bitmapWidth = viewRect.getWidth() * scale;
        double bitmapHeight = viewRect.getHeight() * scale;

        return renderer.asImageWithSize(new CGSize(bitmapWidth, bitmapHeight), 1);
    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public IOS_RealSvgBitmap(InputStream stream, PlatformConnector.SvgScaleType scaleType, float scaleValue) {
        super(getUIImage(stream, scaleType, scaleValue));
    }


}
