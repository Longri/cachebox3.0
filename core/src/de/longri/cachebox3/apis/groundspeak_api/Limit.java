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
package de.longri.cachebox3.apis.groundspeak_api;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.ApiCallLimitEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by longri on 01.07.17.
 */
public class Limit {

    Logger log = LoggerFactory.getLogger(Limit.class);

    private final int callsPerCalendarValue, calendarField, calendarAmount;
    private final Array<Long> calls;
    private AtomicBoolean checkRuns = new AtomicBoolean(false);
    private AtomicBoolean isFull = new AtomicBoolean(false);
    private final long thradSleepTime;

    public Limit(int callsPerCalendarValue, int calendarField, int calendarAnmount) {
        this.callsPerCalendarValue = callsPerCalendarValue;
        this.calls = new Array<>(callsPerCalendarValue);
        this.calendarField = calendarField;
        this.calendarAmount = calendarAnmount;

        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.add(calendarField, calendarAnmount);
        long amount = cal.getTimeInMillis();
        thradSleepTime = (amount - now) / callsPerCalendarValue;
    }

    public synchronized void waitForCall() {
        waitForCall(null);
    }

    public synchronized void waitForCall(ICancel iCancel) {

        if (this.calls.size < this.callsPerCalendarValue) {
            log.debug("no restriction");
            Calendar cal = Calendar.getInstance();
            cal.add(this.calendarField, this.calendarAmount);
            calls.add(cal.getTimeInMillis());
            startCheck();
            if (this.calls.size >= this.callsPerCalendarValue)
                isFull.set(true);

        } else {
            // wait for free slot
            log.debug("wait for free slot");
            long desiredTime = calls.get(0) - Calendar.getInstance().getTimeInMillis();
            ApiCallLimitEvent event = new ApiCallLimitEvent(desiredTime);
            EventHandler.fire(event);
            CB.wait(isFull, iCancel);
            log.debug("have free slot, can call");
            Calendar cal = Calendar.getInstance();
            cal.add(this.calendarField, this.calendarAmount);
            calls.add(cal.getTimeInMillis());
            startCheck();
        }
    }

    private void startCheck() {
        if (this.checkRuns.get()) return;

        this.checkRuns.set(true);
        log.debug("start check");
        CB.postAsync(new NamedRunnable("Limit") {
            @Override
            public void run() {

                while (calls.size > 0) {

                    long now = Calendar.getInstance().getTimeInMillis();
                    IntArray clearList = new IntArray();

                    for (int i = 0, n = calls.size; i < n; i++) {
                        if (now > calls.get(i)) clearList.add(i);
                    }

                    try {
                        for (int i = 0, n = clearList.size; i < n; i++) {
                            calls.removeIndex(clearList.get(i) - i);
                        }
                    } catch (Exception e) {
                        log.error("at remove calls from stack");
                        calls.clear();
                    }
                    log.debug("wait for free slot {}", calls.size);
                    isFull.set(calls.size >= callsPerCalendarValue);

                    try {
                        Thread.sleep(thradSleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                checkRuns.set(false);
                isFull.set(false);
                log.debug("Stack is empty stop check");
            }
        });
    }

}
