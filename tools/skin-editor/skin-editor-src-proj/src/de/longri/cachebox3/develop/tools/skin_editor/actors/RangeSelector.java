package de.longri.cachebox3.develop.tools.skin_editor.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class RangeSelector extends Widget {

    private Drawable drawableWhite;
    private Drawable drawableBlack;
    private boolean vertical;
    public float rangeStart = 0.25f;
    public float rangeStop = 0.75f;

    public RangeSelector(final boolean vertical, SavableSvgSkin skin) {
        super();

        this.vertical = vertical;
        drawableWhite = skin.getDrawable("white");
        drawableBlack = skin.newDrawable("white", 0, 0, 0, 1);

        addListener(new InputListener() {

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                if (vertical == true) {

                    if (event.getButton() == 0) {
                        rangeStart = (getHeight() - y) / getHeight();
                    } else {
                        rangeStop = (getHeight() - y) / getHeight();
                    }

                } else {

                    if (event.getButton() == 0) {
                        rangeStart = x / getWidth();

                    } else {
                        rangeStop = x / getWidth();
                    }
                }


                if (rangeStart > rangeStop) {
                    float temp = rangeStop;
                    rangeStop = rangeStart;
                    rangeStart = temp;

                }

                if (rangeStop < rangeStart) {
                    float temp = rangeStop;
                    rangeStop = rangeStart;
                    rangeStart = temp;

                }

                fire(new ChangeListener.ChangeEvent());

                return false;
            }


        });

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        validate();
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        drawableWhite.draw(batch, getX(), getY(), getWidth(), getHeight());
        if (vertical == true) {

            float start = rangeStart * getHeight();
            float stop = rangeStop * getHeight();

            drawableBlack.draw(batch, getX(), getY() + getHeight() - start, getWidth(), -(stop - start));
        } else {
            float start = rangeStart * getWidth();
            float stop = rangeStop * getWidth();

            drawableBlack.draw(batch, getX() + start, getY(), stop - start, getHeight());
        }
    }
}
