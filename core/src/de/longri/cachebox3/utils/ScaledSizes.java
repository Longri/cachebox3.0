package de.longri.cachebox3.utils;

import com.badlogic.gdx.Gdx;

/**
 * Created by Longri on 07.08.16.
 */
public class ScaledSizes {

    public final float BUTTON_HEIGHT;
    public final float BUTTON_WIDTH;
    public final float BUTTON_WIDTH_WIDE;
    public final float MARGIN;
    public final float CHECK_BOX_HEIGHT;
    public final float WINDOW_WIDTH;
    public final float WINDOW_MARGIN;


    public ScaledSizes(float button_width, float button_height, float button_width_wide, float margin,
                       float check_box_height, float window_margin) {
        BUTTON_HEIGHT = button_height;
        BUTTON_WIDTH = button_width;
        BUTTON_WIDTH_WIDE = button_width_wide;
        MARGIN = margin;
        CHECK_BOX_HEIGHT = check_box_height;
        WINDOW_MARGIN = window_margin;
        WINDOW_WIDTH = Gdx.graphics.getWidth() - (2 * window_margin);
    }
}
