/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.stages.initial_tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.ImportProgressChangedEvent;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.gui.stages.NamedStage;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.IChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 02.08.16.
 */
public final class GdxInitialTask extends AbstractInitTask {

    private final static Logger log = LoggerFactory.getLogger(NamedStage.class);
    private static int VIBRATE_TIME_MSEC = Config.VibrateTime.getValue();
    private static boolean VIBRATE = Config.VibrateFeedback.getValue();


    public GdxInitialTask(String name) {
        super(name);
    }

    @Override
    public void runnable() {
        EventHandler.fire(new IncrementProgressEvent(2, "Initial openGL"));
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        CB.stageManager.setInputMultiplexer(inputMultiplexer);


        Config.VibrateTime.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                VIBRATE_TIME_MSEC = Config.VibrateTime.getValue();
            }
        });

        Config.VibrateFeedback.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                VIBRATE = Config.VibrateFeedback.getValue();
            }
        });


        VIBRATE = Config.VibrateFeedback.getValue();
        VIBRATE_TIME_MSEC = Config.VibrateTime.getValue();

        GestureDetector gestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {

            boolean longPressFired = false;

            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                longPressFired = false;
                return super.touchDown(x, y, pointer, button);
            }


            @Override
            public boolean tap(float x, float y, int count, int button) {
                log.debug("click on {} Stage", name);
                if (!longPressFired && VIBRATE) Gdx.input.vibrate(VIBRATE_TIME_MSEC);
                return false; //never set handled
            }

            @Override
            public boolean longPress(float x, float y) {
                log.debug("long click on {} Stage", name);
                if (VIBRATE) Gdx.input.vibrate(VIBRATE_TIME_MSEC);
                longPressFired = true;
                return false; //never set handled
            }
        });
        inputMultiplexer.addProcessor(gestureDetector);

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public int getProgressMax() {
        return 2;
    }
}