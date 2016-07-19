package de.longri.cachebox3;

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

    public static Bitmap getSvg(InputStream stream)throws IOException {
        return platformConnector.getRealScaledSVG(stream);
    }

    public abstract Bitmap getRealScaledSVG(InputStream stream) throws IOException;


}
