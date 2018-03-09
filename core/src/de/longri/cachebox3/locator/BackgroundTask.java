/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.locator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 09.03.18.
 */
public class BackgroundTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BackgroundTask.class);

    private AtomicBoolean cancel = new AtomicBoolean(false);
    private int sleepTime = 10000;
    private int waitTime = 0;

    public BackgroundTask() {

    }

    private void workCycle() {
        log.debug("Run on Background");
        playApproach();
    }

    private void playApproach() {
        FileHandle soundFileHandle;
        if (CanvasAdapter.platform != Platform.IOS) {
            soundFileHandle = Gdx.files.internal("sound/Approach.mp3");
        } else {
            soundFileHandle = Gdx.files.absolute(CB.WorkPath + "/data/sound/Approach.mp3");
        }
        PlatformConnector.playNotifySound(soundFileHandle);
    }


    @Override
    public void run() {
        while (!cancel.get()) {
            if (waitTime >= sleepTime) {
                workCycle();
                waitTime = 0;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitTime += 50;
        }
    }

    public void cancel() {
        cancel.set(true);
    }
}
