/* 
 * Copyright (C) 2013-2016 team-cachebox.de
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
package de.longri.cachebox3.gui.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.utils.CB_RectF;


/**
 * @author Longri
 */
public abstract class RotateAnimation extends AnimationBase {
    private float mOriginX;
    private float mOriginY;
    private float mScale = 1f;
    private float animateRotateValue = 0;
    private Sprite mDrawable;

    protected static final int ANIMATION_DURATION = 2000;

    public RotateAnimation(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
    }

    public RotateAnimation(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    /**
     * Time for 360°
     */
    protected void play(int duration) {
        this.mDuration = duration;
        mPlaying = true;
        mPause = false;
    }

    @Override
    public void stop() {
        mPlaying = false;
        mPause = false;
    }

    @Override
    public void pause() {
        mPause = true;
    }


    @Override
    public void setOrigin(float x, float y) {
        mOriginX = x;
        mOriginY = y;
    }

    @Override
    public void setScale(float scale) {
        mScale = scale;
    }

    public void setSprite(Sprite sprite) {
        mSpriteWidth = sprite.getWidth();
        mSpriteHeight = sprite.getHeight();
        mDrawable = sprite;// new SpriteDrawable(sprite);
    }

    @Override
    protected void render(Batch batch) {

        if (mDrawable == null)
            return;

        // ####################################################################
        // calc Rotation
        // ####################################################################

        animateRotateValue = (1 + ((int) (CB.stateTime * 1000) % this.mDuration) / (this.mDuration / 360));

        // ####################################################################
        // Draw
        // ####################################################################

        float drawwidth = getWidth();
        float drawHeight = getHeight();
        float drawX = 0;
        float drawY = 0;

        if (mSpriteWidth > 0 && mSpriteHeight > 0) {
            float proportionWidth = getWidth() / mSpriteWidth;
            float proportionHeight = getHeight() / mSpriteHeight;

            float proportion = Math.min(proportionWidth, proportionHeight);

            drawwidth = mSpriteWidth * proportion;
            drawHeight = mSpriteHeight * proportion;
            drawX = (getWidth() - drawwidth) / 2;
            drawY = (getHeight() - drawHeight) / 2;
        }

        batch.draw(mDrawable, drawX, drawY, mOriginX, mOriginY, drawwidth, drawHeight, mScale, mScale, animateRotateValue);

    }

}
