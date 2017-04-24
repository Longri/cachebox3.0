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

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.longri.cachebox3.gui.skin.styles.FrameAnimationStyle;

/**
 * Created by longri on 22.04.17.
 */
public class FrameAnimationDrawable extends AbstractAnimationDrawable {


    private Animation<TextureRegion> animation;
    private FrameAnimationStyle style;

    public FrameAnimationDrawable(FrameAnimationStyle style) {
        this.style = style;
    }


    @Override
    public void drawAnimation(Batch batch, float x, float y, float width, float height) {
        if (animation == null) {
            if (style != null) {
                if(style.frames==null || style.frames.size==0)return;
                animation = new Animation(style.frameDuration, style.frames, style.playMode);
            }
        } else {
            batch.draw(animation.getKeyFrame(animationTime), x, y, width, height);
        }
    }

}
