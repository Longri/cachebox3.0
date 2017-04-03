package de.longri.cachebox3.gui.skin.styles;

import com.badlogic.gdx.graphics.Color;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Paint;

/**
 * Created by Longri on 31.03.2017.
 */
public class DirectLineRendererStyle {
    public Color color = Color.RED.mul(1,1,1,0.8f);
    public float width= 5.5f;
    public Paint.Cap cap = Paint.Cap.ROUND;
    public Bitmap texture;
}
