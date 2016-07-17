package de.longri.cachebox3;

import com.badlogic.gdx.graphics.Pixmap;
import org.oscim.backend.canvas.Bitmap;

/**
 * Created by Longri on 17.07.16.
 */
public abstract class PlatformConnector {

    static PlatformConnector platformConnector;

    public static void init(PlatformConnector connector) {
        platformConnector = connector;
    }


    protected abstract Pixmap pixmapFromBitmap(Bitmap bitmap);


    public static Pixmap getPixmapFromBitmap(Bitmap bitmap) {
        return platformConnector.pixmapFromBitmap(bitmap);
    }

}
