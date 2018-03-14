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
 * Created by Longri on 04.07.2017.
 */
public class ApiCallLimitEvent extends AbstractEvent<Long> {
    private final long waitTime;

    public ApiCallLimitEvent(long waitTime) {
        super(Long.class);
        this.waitTime = waitTime;
    }

    public ApiCallLimitEvent(long waitTime, short id) {
        super(Long.class, id);
        this.waitTime = waitTime;
    }

    @Override
    public Class getListenerClass() {
        return ApiCallLimitListener.class;
    }

    public long getWaitTime() {
        return this.waitTime;
    }
}
