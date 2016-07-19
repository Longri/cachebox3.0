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

    private static UIImage getUIImage(InputStream stream) {
        String svg = getStringFromInputStream(stream);
        SVGRenderer renderer = new SVGRenderer(svg);
        CGRect viewRect = renderer.getViewRect();

        float scale = CanvasAdapter.dpi / 240;

        float bitmapWidth = (float) (viewRect.getWidth() * scale);
        float bitmapHeight = (float) (viewRect.getHeight() * scale);

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

    public RealIosSvgBitmap(InputStream stream) {
        super(getUIImage(stream));
    }


}
