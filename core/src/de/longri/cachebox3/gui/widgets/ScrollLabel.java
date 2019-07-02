/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Actor;

/**
 * Created by Longri on 09.09.16.
 */
public class ScrollLabel extends Label {
    static private final Color tempColor = new Color();

    private final Rectangle scissorRec = new Rectangle();
    private final Rectangle localRec = new Rectangle();
    private final LabelStyle style;
    private final AnimationActor animationActor = new AnimationActor();
    private final Vector2 stagePos = new Vector2();

    private RepeatAction animationSequens;

    public ScrollLabel(final CharSequence text, LabelStyle style) {
        super(text, style);
        this.style = style;
        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                ScrollLabel.this.setText(text);
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        BitmapFontCache cache = super.getBitmapFontCache();

        validate();
        Color color = tempColor.set(getColor());
        color.a *= parentAlpha;

        localRec.x = getX();
        localRec.y = getY();

        if (style.background != null) {
            batch.setColor(color.r, color.g, color.b, color.a);
            style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
            localRec.x += style.background.getLeftWidth();
            localRec.y += style.background.getBottomHeight();
            batch.flush();
        }
        if (style.fontColor != null) color.mul(style.fontColor);
        cache.tint(color);
        cache.setPosition(getX() + scrollPosition, getY());

        getStage().getViewport().calculateScissors(batch.getTransformMatrix(), localRec, scissorRec);

        batch.flush();
        if (ScissorStack.pushScissors(scissorRec)) {
            cache.draw(batch);
            batch.flush();
            ScissorStack.popScissors();
        } else {
            cache.draw(batch);
        }
    }


    @Override
    protected void positionChanged() {
        localRec.setPosition(getX() + 1, getY() + 1);
    }

    @Override
    protected void sizeChanged() {
        if (localRec == null) return;
        float width = getWidth();
        float height = getHeight();
        if (style.background != null) {
            width -= style.background.getLeftWidth() + style.background.getRightWidth();
            height -= style.background.getTopHeight() + style.background.getBottomHeight();
        }
        localRec.setSize(width, height);
    }

    public void setText(CharSequence newText) {
        CB.assertGlThread();
        super.setText(newText);
        ScrollLabel.this.layout();

        //remove maybe running animations
        animationActor.removeAction(animationSequens);

        GlyphLayout layout = ScrollLabel.this.getGlyphLayout();
        if (layout.width > ScrollLabel.this.getWidth()) {
            ScrollLabel.this.getParent().addActor(animationActor);
            ScrollLabel.this.setAlignment(Align.left);

            float lastGlyphWidth = layout.runs.peek().glyphs.peek().width;

            float animationWidth = (layout.width + lastGlyphWidth) - ScrollLabel.this.getWidth();
            float animationTime = animationWidth / CB.getScaledFloat(40.0f);

            animationSequens = Actions.forever(Actions.sequence(
                    Actions.moveTo(0, 0),
                    Actions.delay(2),
                    Actions.moveTo(-animationWidth, 0, animationTime),
                    Actions.delay(2)));

            animationActor.addAction(animationSequens);
        } else {
            if (ScrollLabel.this.getParent() != null)
                ScrollLabel.this.getParent().removeActor(animationActor);
            ScrollLabel.this.setAlignment(Align.center);
        }

        // reset last scroll position
        scrollPosition = 0;
    }

    private float scrollPosition = 0;

    private class AnimationActor extends Catch_Actor {
        @Override
        protected void positionChanged() {
            scrollPosition = getX();
        }
    }
}
