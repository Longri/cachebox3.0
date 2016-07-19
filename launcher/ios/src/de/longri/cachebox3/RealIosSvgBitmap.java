package de.longri.cachebox3;

import org.oscim.backend.CanvasAdapter;
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
public class RealIosSvgBitmap extends org.oscim.ios.backend.IosBitmap {

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
                scale = (CanvasAdapter.dpi / 240) * scaleValue;
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

    public RealIosSvgBitmap(InputStream stream, PlatformConnector.SvgScaleType scaleType, float scaleValue) {
        super(getUIImage(stream, scaleType, scaleValue));
    }


}
