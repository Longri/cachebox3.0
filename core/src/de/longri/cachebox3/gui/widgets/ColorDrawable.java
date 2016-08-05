package de.longri.cachebox3.gui.widgets;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;

public class ColorDrawable extends EmptyDrawable {

    public static class ColorDrawableStyle {
        Color color;

        public ColorDrawableStyle() {
        }
    }

    /**
     * Da beim Zeichnen dieses Sprites, dieses nicht Manipuliert wird, brauchen wir hier nur eine einmalige Statische Instanz
     */
    private static Sprite pixelSprite;

    private Texture tex;
    private Pixmap pix;
    private Color mColor;

    public ColorDrawable(Color color) {
        setColor(color);
    }

    public ColorDrawable(ColorDrawableStyle style) {
        setColor(style.color);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {


        if (pixelSprite == null) {
            pixelSprite = CB.getSprite("color");
            if (pixelSprite == null) {
                setSpriteFromPixMap();
            }
        }

        if (pixelSprite != null) {
            Color altColor = batch.getColor();

            float r = altColor.r;
            float g = altColor.g;
            float b = altColor.b;
            float a = altColor.a;

            batch.setColor(mColor);
            batch.draw(pixelSprite, x, y, width, height);
            batch.setColor(r, g, b, a);
        }

    }

    private void setSpriteFromPixMap() {
        int w = 2;
        int h = 2;
        pix = new Pixmap(w, h, Pixmap.Format.RGB565);
        pix.setColor(Color.WHITE);

        pix.fillRectangle(0, 0, w, h);

        try {
            tex = new Texture(pix);
        } catch (Exception e) {
            tex = null;
        }

        pixelSprite = new Sprite(tex);

        pix.dispose();
    }

    public void setColor(Color color) {
        mColor = color;
    }
}
