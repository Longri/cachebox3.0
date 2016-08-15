package de.longri.cachebox3.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 07.08.16.
 */
public class ScaledSizes {

    public static float ICON_HEIGHT = -1;
    public static float ICON_WIDTH = -1;
    public final float BUTTON_HEIGHT;
    public final float BUTTON_WIDTH;
    public final float BUTTON_WIDTH_WIDE;
    public final float MARGIN;
    public final float MARGINx2;
    public final float MARGIN_HALF;
    public final float CHECK_BOX_HEIGHT;
    public final float WINDOW_WIDTH;
    public final float WINDOW_HEIGHT;
    public final float WINDOW_MARGIN;


    public ScaledSizes(float button_width, float button_height, float button_width_wide, float margin,
                       float check_box_height, float window_margin) {
        BUTTON_HEIGHT = button_height;
        BUTTON_WIDTH = button_width;
        BUTTON_WIDTH_WIDE = button_width_wide;
        MARGIN = margin;
        MARGINx2 = MARGIN * 2;
        MARGIN_HALF = MARGIN / 2;
        CHECK_BOX_HEIGHT = check_box_height;
        WINDOW_MARGIN = window_margin;
        WINDOW_WIDTH = Gdx.graphics.getWidth() - (2 * window_margin);
        WINDOW_HEIGHT = Gdx.graphics.getHeight() - (2 * window_margin);
    }

    public static void checkMaxIconSize() {
        if (ICON_WIDTH > -1) return;

        // ref icon size is the 'closeIcon' size
        Sprite tmp = CB.getSprite("closeIcon");
        ICON_WIDTH = tmp.getWidth();
        ICON_HEIGHT = tmp.getHeight();
    }
}
