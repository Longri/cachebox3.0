/*
 * Copyright (C) 2016 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 09.09.16.
 */
public class ScrollLabel extends Label {
    static private final Color tempColor = new Color();

    private final Rectangle scissorRec = new Rectangle();
    private final Rectangle localRec = new Rectangle();
    private LabelStyle style;
    private final AnimationActor animationActor = new AnimationActor();

    public ScrollLabel(CharSequence text, LabelStyle style) {
        super(text, style);
        this.style = style;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        BitmapFontCache cache = super.getBitmapFontCache();

        validate();
        Color color = tempColor.set(getColor());
        color.a *= parentAlpha;
        if (style.background != null) {
            batch.setColor(color.r, color.g, color.b, color.a);
            style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
        if (style.fontColor != null) color.mul(style.fontColor);
        cache.tint(color);
        cache.setPosition(getX() + scrollPosition, getY());

        getStage().calculateScissors(localRec, scissorRec);
        ScissorStack.pushScissors(scissorRec);
        cache.draw(batch);
        batch.flush();
        ScissorStack.popScissors();
    }


    @Override
    protected void positionChanged() {
        localRec.setPosition(getX(), getY());
    }

    @Override
    protected void sizeChanged() {
        localRec.setSize(getWidth(), getHeight());
    }

    public void setText(CharSequence newText) {
        super.setText(newText);

        super.layout();
        GlyphLayout layout = super.getGlyphLayout();

        if (layout.width > this.getWidth()) {
            super.getParent().addActor(animationActor);
            super.setAlignment(Align.left);

            float animationWidth = layout.width - this.getWidth();
            float animationTime = CB.getScaledFloat(0.03f) * animationWidth;


            RepeatAction animationSequens = Actions.forever(Actions.sequence(
                    Actions.moveTo(0, 0),
                    Actions.delay(1),
                    Actions.moveTo(-animationWidth, 0, animationTime),
                    Actions.delay(1)));

            animationActor.addAction(animationSequens);
        } else {
            super.getParent().removeActor(animationActor);
            super.setAlignment(Align.center);
        }


    }

    private float scrollPosition = 0;

    private class AnimationActor extends Actor {
        @Override
        protected void positionChanged() {
            scrollPosition = getX();
        }
    }
}
