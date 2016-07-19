package org.oscim.android.canvas;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.RectF;
import com.caverock.androidsvg.SVG;
import org.oscim.backend.CanvasAdapter;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Longri on 19.07.16.
 */
public class RealAndroidSvgBitmap extends AndroidBitmap {

    private static android.graphics.Bitmap getAndroidBitmap(InputStream inputStream) throws IOException {
        synchronized (SVG.getVersion()) {
            try {
                SVG svg = SVG.getFromInputStream(inputStream);
                Picture picture = svg.renderToPicture();

                float scale = CanvasAdapter.dpi / 240;

                float bitmapWidth = (float) (picture.getWidth() * scale);
                float bitmapHeight = (float) (picture.getHeight() * scale);

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

    public RealAndroidSvgBitmap(InputStream inputStream) throws IOException {
        super(getAndroidBitmap(inputStream));
    }

}
