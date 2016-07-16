/*
 * Copyright 2016 Longri
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.ios.backend;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Canvas;
import org.oscim.backend.canvas.Paint;
import org.robovm.apple.coregraphics.CGSize;
import org.robovm.apple.uikit.UIImage;
import org.robovm.pods.ghs.svg.SVGRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * iOS specific implementation of {@link CanvasAdapter}<br>
 * <br>
 * Created by Longri on 25.06.16.
 */
public class IosGraphics extends CanvasAdapter {

    static final Logger log = LoggerFactory.getLogger(IosGraphics.class);

    public static void init() {
        CanvasAdapter.init(new IosGraphics());
    }

    @Override
    protected Canvas newCanvasImpl() {
        return new IosCanvas();
    }

    @Override
    protected Paint newPaintImpl() {
        return new IosPaint();
    }

    @Override
    protected Bitmap newBitmapImpl(int width, int height, int format) {
        return new IosBitmap(width, height, format);
    }

    @Override
    protected Bitmap decodeBitmapImpl(InputStream inputStream) {
        try {
            return new IosBitmap(inputStream);
        } catch (IOException e) {
            log.error("decodeBitmapImpl", e);
            return null;
        }
    }

    @Override
    protected Bitmap decodeSvgImpl(InputStream inputStream, float scaleFactor, int width, int height, int percent, int hash) throws IOException {

        String svg = getStringFromInputStream(inputStream);

        SVGRenderer renderer = new SVGRenderer(svg);
        UIImage image = renderer.asImageWithSize(new CGSize(width, height), scaleFactor);

        return new IosBitmap(image);
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


    @Override
    protected Bitmap loadBitmapAssetImpl(String fileName) {
        try {
            return new IosBitmap(fileName);
        } catch (IOException e) {
            log.error("loadBitmapAssetImpl", e);
            return null;
        }
    }
}
