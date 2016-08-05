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


import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.IconNames;

public class WorkAnimation extends RotateAnimation {
	protected static WorkAnimation mINSTANCE = new WorkAnimation();

	public static AnimationBase GetINSTANCE() {
		return mINSTANCE;
	}

	public static AnimationBase GetINSTANCE(CB_RectF rec) {
		mINSTANCE.setRec(rec);
		return mINSTANCE;
	}

	@Override
	public AnimationBase INSTANCE() {
		return mINSTANCE;
	}

	@Override
	public AnimationBase INSTANCE(CB_RectF rec) {
		mINSTANCE.setRec(rec);
		return mINSTANCE;
	}

	public WorkAnimation() {
		super(new CB_RectF(0, 0, 50, 50), "DownloadAnimation");

		setSprite(CB.getSprite(IconNames.settings.name()));
		setOrigin(this.getWidth()/2, this.getHeight()/2);
		play(ANIMATION_DURATION);
	}


	@Override
	public void play() {
		play(ANIMATION_DURATION);
	}

	@Override
	public void dispose() {

	}
}
