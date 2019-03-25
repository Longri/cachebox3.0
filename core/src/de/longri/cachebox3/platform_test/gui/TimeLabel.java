/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.platform_test.gui;

import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.NamedRunnable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 2019-03-24.
 */
public class TimeLabel extends VisLabel {


    private final DateFormat df;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final boolean isSec;

    long startTime;

    public TimeLabel(boolean sec) {
        super("          ", "AboutInfo", Color.BLACK);
        df = sec ? new SimpleDateFormat("mm:ss") : new SimpleDateFormat("ss:SSS");
        isSec = sec;
    }

    public void start() {
        isRunning.set(true);
        startTime = System.currentTimeMillis();
        CB.postAsync(new NamedRunnable("Test time updater") {
            @Override
            public void run() {

                while (isRunning.get()) {
                    TimeLabel.this.setText(df.format(new Date(System.currentTimeMillis() - startTime))
                            + (isSec ? " sec" : " msec"));
                    try {
                        Thread.sleep(isSec ? 500 : 50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void stop() {
        isRunning.set(false);
        this.setText(df.format(new Date(System.currentTimeMillis() - startTime))
                + (isSec ? " sec" : " msec"));
    }
}
