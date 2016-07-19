package de.longri.cachebox3;


import org.oscim.android.canvas.RealAndroidSvgBitmap;
import org.oscim.backend.canvas.Bitmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class AndroidPlatformConnector extends PlatformConnector {

    @Override
    public Bitmap getRealScaledSVG(InputStream stream) throws IOException {
        return new RealAndroidSvgBitmap(stream);
    }
}
