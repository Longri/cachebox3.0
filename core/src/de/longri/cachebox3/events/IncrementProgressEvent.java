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
public class IncrementProgressEvent extends AbstractEvent<IncrementProgressEvent.ProgressIncrement> {

    public static class ProgressIncrement{
        public final int incrementValue;
        public final String msg;
        public final int incrementMaxValue;

        private ProgressIncrement(int incrementValue, String msg, int incrementMaxValue) {
            this.incrementValue = incrementValue;
            this.msg = msg;
            this.incrementMaxValue = incrementMaxValue;
        }
    }


   public final ProgressIncrement progressIncrement;

    public IncrementProgressEvent(int incrementValue, String msg) {
        super(IncrementProgressEvent.ProgressIncrement.class);
        this.progressIncrement = new ProgressIncrement(incrementValue,msg,-1);
    }

    public IncrementProgressEvent(int incrementValue, String msg, short id) {
        super(IncrementProgressEvent.ProgressIncrement.class, id);
        this.progressIncrement = new ProgressIncrement(incrementValue,msg, -1);
    }

    public IncrementProgressEvent(int incrementValue, String msg, int incrementMaxValue) {
        super(IncrementProgressEvent.ProgressIncrement.class);
        this.progressIncrement = new ProgressIncrement(incrementValue, msg, incrementMaxValue);
    }

    @Override
    public Class getListenerClass() {
        return IncrementProgressListener.class;
    }
}
