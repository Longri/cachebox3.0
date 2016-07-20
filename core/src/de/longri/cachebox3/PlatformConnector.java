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


    // SVG implementations #############################################################################################
    public enum SvgScaleType {
        SCALED_TO_WIDTH, SCALED_TO_HEIGHT, DPI_SCALED
    }

    public static Bitmap getSvg(InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        return platformConnector.getRealScaledSVG(stream, scaleType, scaleValue);
    }

    public abstract Bitmap getRealScaledSVG(InputStream stream,
                                            SvgScaleType scaleType, float scaleValue) throws IOException;


}
