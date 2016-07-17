package de.longri.cachebox3;

import com.badlogic.gdx.graphics.Pixmap;
import org.oscim.backend.canvas.Bitmap;

/**
 * Created by Longri on 17.07.16.
 */
public class AwtPlatformConnector extends PlatformConnector {
    @Override
    protected Pixmap pixmapFromBitmap(Bitmap bitmap) {
        return null;
    }
}
