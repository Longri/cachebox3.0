/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Timer;
import de.longri.cachebox3.gui.skin.styles.FrameAnimationStyle;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by longri on 22.04.17.
 */
public class FrameAnimationDrawable extends AbstractAnimationDrawable implements Layout {


    private Animation<TextureRegion> animation;
    private FrameAnimationStyle style;
    private float prefHeight = -1, prefWidth = -1;

    public FrameAnimationDrawable(FrameAnimationStyle style) {
        this.style = style;
    }

    private final AtomicBoolean taskSchudled = new AtomicBoolean(false);

    @Override
    public void drawAnimation(Batch batch, float x, float y, float width, float height) {
        if (animation == null) {
            if (style != null) {
                if (style.frames == null || style.frames.size == 0) return;
                animation = new Animation(style.frameDuration, style.frames, style.playMode);
                Gdx.graphics.requestRendering();
            }
        } else {
            TextureRegion region = animation.getKeyFrame(animationTime);
            if (region != null) batch.draw(region, x, y, width, height);
            if (animation.getPlayMode().ordinal() > 1 || !animation.isAnimationFinished(animationTime)) {
                if (!taskSchudled.get()) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            taskSchudled.set(false);
                            Gdx.graphics.requestRendering();
                        }
                    }, animation.getFrameDuration());
                    taskSchudled.set(true);
                }
            }
        }
    }

    @Override
    public void layout() {
        setPrefSize();
    }

    private void setPrefSize() {
        if (prefWidth != -1 || style.frames == null || style.frames.size == 0) return;
        prefWidth = style.frames.first().getRegionWidth();
        prefHeight = style.frames.first().getRegionWidth();
    }

    @Override
    public void invalidate() {

    }

    @Override
    public void invalidateHierarchy() {

    }

    @Override
    public void validate() {

    }

    @Override
    public void pack() {

    }

    @Override
    public void setFillParent(boolean fillParent) {

    }

    @Override
    public void setLayoutEnabled(boolean enabled) {

    }

    @Override
    public float getPrefWidth() {
        setPrefSize();
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        setPrefSize();
        return prefHeight;
    }

    @Override
    public float getMaxWidth() {
        setPrefSize();
        return prefWidth * 2;
    }

    @Override
    public float getMaxHeight() {
        setPrefSize();
        return prefHeight * 2;
    }

    @Override
    public float getMinWidth() {
        setPrefSize();
        return prefWidth;
    }

    @Override
    public float getMinHeight() {
        setPrefSize();
        return prefHeight;
    }

    public FrameAnimationStyle getStyle() {
        return style;
    }
}
