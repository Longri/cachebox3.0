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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.CircularProgressWidget;

/**
 * Created by Longri on 01.12.2017.
 */
public class BlockUiProgress_Activity extends ActivityBase implements IncrementProgressListener {

    private final CircularProgressWidget progress = new CircularProgressWidget();

    public BlockUiProgress_Activity() {
        super("BlockUiActivity");
        this.add(progress);
        this.setBackground((Drawable) null);
    }

    @Override
    public void onShow() {
        EventHandler.add(this);
    }

    @Override
    public void onHide() {
        EventHandler.remove(this);
    }


    @Override
    public void incrementProgress(IncrementProgressEvent event) {
        int value = event.progressIncrement.incrementValue;
        int max = event.progressIncrement.incrementMaxValue;
        progress.setProgressMax(max);
        progress.setProgress(value);
        if (value > 0 && value >= max) {
            CB.postAsyncDelayd(500, new Runnable() {
                @Override
                public void run() {
                    BlockUiProgress_Activity.this.finish();
                }
            });
        }
    }
}
