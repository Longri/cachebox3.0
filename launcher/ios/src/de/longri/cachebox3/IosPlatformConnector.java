package de.longri.cachebox3;

import com.badlogic.gdx.graphics.Pixmap;
import org.oscim.backend.canvas.Bitmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class IosPlatformConnector extends PlatformConnector {

    @Override
    public Bitmap getRealScaledSVG(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {
        return new RealIosSvgBitmap(inputStream,scaleType,scaleValue);
    }
}
