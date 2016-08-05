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

public class DownloadAnimation extends FrameAnimation {

	private final static DownloadAnimation mINSTANCE = new DownloadAnimation();
	private static final int ANIMATION_DURATION = 1000;

	public static DownloadAnimation GetINSTANCE() {
		return mINSTANCE;
	}

	public static DownloadAnimation GetINSTANCE(CB_RectF rec) {
		mINSTANCE.setRec(rec);
		return mINSTANCE;
	}

	public AnimationBase INSTANCE() {
		return mINSTANCE;
	}

	public AnimationBase INSTANCE(CB_RectF rec) {
		mINSTANCE.setRec(rec);
		return mINSTANCE;
	}

	private DownloadAnimation() {
		super(new CB_RectF(0, 0, 50, 50), "DownloadAnimation");

		addFrame(CB.getSprite("download-1"));
		addFrame(CB.getSprite("download-2"));
		addFrame(CB.getSprite("download-3"));
		addFrame(CB.getSprite("download-4"));
		addFrame(CB.getSprite("download-5"));
		play(ANIMATION_DURATION);
	}


	@Override
	public void play() {
		play(ANIMATION_DURATION);
	}

	@Override
	public void stop() {

	}

	@Override
	public void pause() {

	}

	@Override
	public void dispose() {

	}
}
