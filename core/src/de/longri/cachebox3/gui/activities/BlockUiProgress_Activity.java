/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.events.IncrementProgressListener;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.CircularProgressWidget;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Longri on 01.12.2017.
 */
public class BlockUiProgress_Activity extends ActivityBase implements IncrementProgressListener {

    private Logger log = LoggerFactory.getLogger(BlockUiProgress_Activity.class);
    private final CircularProgressWidget progress = new CircularProgressWidget();
    private final Label msgLabel;

    public BlockUiProgress_Activity() {
        this("");
    }

    public BlockUiProgress_Activity(CharSequence msg) {
        super("BlockUiActivity");

//        this.setDebug(true);
        msgLabel = new VisLabel(msg);
        Label.LabelStyle msgDefaultStyle = msgLabel.getStyle();
        Label.LabelStyle newStyle = new Label.LabelStyle();
        newStyle.fontColor = Color.WHITE;
        newStyle.font = msgDefaultStyle.font;
        msgLabel.setStyle(newStyle);
        msgLabel.setAlignment(Align.center);

        this.add(msgLabel).expandX().fillX().pad(CB.scaledSizes.MARGINx2);
        this.row();
        this.add(progress);
        this.setBackground((Drawable) null);
    }

    @Override
    public void onShow() {
        EventHandler.add(this);
        log.debug("OnShow: set Continues rendering");
        Gdx.graphics.setContinuousRendering(true);
    }

    @Override
    public void onHide() {
        EventHandler.remove(this);
        log.debug("OnHide");
        Gdx.graphics.setContinuousRendering(false);
    }


    @Override
    public void incrementProgress(final IncrementProgressEvent event) {
        CB.postOnGlThread(new NamedRunnable("Test Add") {
            @Override
            public void run() {
                int value = event.progressIncrement.incrementValue;
                int max = event.progressIncrement.incrementMaxValue;
                String msg = event.progressIncrement.msg;
                progress.setProgressMax(max);
                progress.setProgress(value);
                msgLabel.setText(msg);
                if (value > 0 && value >= max) {
                    log.debug("Increment Progress: post Finish");
                    CB.postAsyncDelayd(500, new NamedRunnable("BlockUiProgress:Close") {
                        @Override
                        public void run() {
                            BlockUiProgress_Activity.this.finish();
                        }
                    });
                }
            }
        });
    }
}
