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
package de.longri.cachebox3.events;

/**
 * Created by Longri on 11.07.2017.
 */
public class IncrementProgressEvent extends AbstractPoolableEvent {

    @Override
    public void reset() {
        progressIncrement.incrementMaxValue = -1;
        progressIncrement.msg = null;
        progressIncrement.incrementValue = 0;
    }

    public void set(int incrementValue, String msg) {
        progressIncrement.incrementMaxValue = incrementValue;
        progressIncrement.msg = msg;
    }

    public void set(int incrementValue, String msg, int incrementMaxValue) {
        progressIncrement.incrementValue = incrementValue;
        progressIncrement.msg = msg;
        progressIncrement.incrementMaxValue = incrementMaxValue;
    }

    public int getIncrementValue() {
        return progressIncrement.incrementValue;
    }

    public int getIncrementMaxValue() {
        return progressIncrement.incrementMaxValue;
    }

    public String getMsg() {
        return progressIncrement.msg;
    }

    public static class ProgressIncrement {
        public int incrementValue;
        public String msg;
        public int incrementMaxValue;

        private ProgressIncrement() {
        }
    }

    private final ProgressIncrement progressIncrement;

    protected IncrementProgressEvent() {
        super(IncrementProgressEvent.ProgressIncrement.class);
        this.progressIncrement = new ProgressIncrement();
    }

    @Override
    public Class getListenerClass() {
        return IncrementProgressListener.class;
    }
}
