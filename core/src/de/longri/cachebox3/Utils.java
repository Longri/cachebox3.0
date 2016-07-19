package de.longri.cachebox3;

import com.badlogic.gdx.graphics.Pixmap;
import org.oscim.backend.canvas.Bitmap;

/**
 * Created by Longri on 18.07.16.
 */
public class Utils {


    public static Pixmap getPixmapFromBitmap(Bitmap bitmap) {
        byte[] encodedData = bitmap.getPngEncodedData();
        return new Pixmap(encodedData, 0, encodedData.length);
    }


}
