package de.longri.cachebox3;

import com.badlogic.gdx.graphics.Pixmap;
import org.oscim.awt.RealAwtSvgBitmap;
import org.oscim.backend.canvas.Bitmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class AwtPlatformConnector extends PlatformConnector {

    @Override
    public Bitmap getRealScaledSVG(InputStream stream) throws IOException {
        return new RealAwtSvgBitmap(stream);
    }
}
