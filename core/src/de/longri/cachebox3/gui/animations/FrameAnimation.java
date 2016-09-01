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
package de.longri.cachebox3.gui.animations;

import java.util.ArrayList;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.utils.CB_RectF;

public abstract class FrameAnimation extends AnimationBase {
	ArrayList<Drawable> frames;

	public FrameAnimation(float X, float Y, float Width, float Height, String Name) {
		super(X, Y, Width, Height, Name);
	}

	public FrameAnimation(CB_RectF rec, String Name) {
		super(rec, Name);
	}

	int count = 0;

	int getFrameIndex(int Duration, int Frames) {
		// Duration != 0
		// Frames != 0
		return (1 + ((int) (CB.stateTime * 1000) % Duration) / (Duration / Frames));
	}

	public void addFrame(Sprite frame) {
		if (frames == null)
			frames = new ArrayList<Drawable>();

		frames.add(new SpriteDrawable(frame));
	}

	public void addLastFrame(Sprite frame) {
		mSpriteWidth = frame.getWidth();
		mSpriteHeight = frame.getHeight();
		frames.add(new SpriteDrawable(frame));
	}

	@Override
	protected void render(Batch batch) {

		if (frames == null || frames.size() == 0)
			return;

		int Frameindex = getFrameIndex(mDuration, frames.size());

		count++;
		if (count > frames.size() - 2)
			count = 0;

		Drawable mDrawable = mPlaying ? frames.get(Frameindex - 1) : frames.get(0);

		if (mDrawable != null) {
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
			mDrawable.draw(batch, drawX, drawY, drawwidth, drawHeight);
		}
	}

	protected void play(int duration) {
		this.mDuration = duration;
		mPlaying = true;
	}
}
