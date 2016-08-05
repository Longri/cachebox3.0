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



import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import de.longri.cachebox3.gui.widgets.CB_View_Base;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.SizeF;

public abstract class AnimationBase extends CB_View_Base {

	protected boolean mPlaying = false;
	protected int mDuration = 1;
	protected float mSpriteWidth;
	protected float mSpriteHeight;
	protected boolean mPause = false;

	public AnimationBase(String Name) {
		super(Name);
	}

	public AnimationBase(float X, float Y, float Width, float Height, String Name) {
		super(X, Y, Width, Height, Name);
	}

	public AnimationBase(CB_RectF rec, String Name) {
		super(rec, Name);
	}

	public AnimationBase(SizeF size, String Name) {
		super(size, Name);
	}

	protected abstract void render(Batch batch);

	public abstract void play();

	public abstract void stop();

	public abstract void pause();

	public abstract AnimationBase INSTANCE();

	public abstract AnimationBase INSTANCE(CB_RectF rec);

}