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

import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by longri on 01.07.17.
 */
class LimitTest {

    static {
        TestUtils.initialGdx();
    }

    @Test
    void waitForCall() {

        int calendarField = Calendar.SECOND;
        int calendarAmount = 10;
        
        Limit limit = new Limit(2, calendarField, calendarAmount);

        Calendar cal = Calendar.getInstance();
        long start = cal.getTimeInMillis();
        cal.add(calendarField, calendarAmount);
        long oneMinute = cal.getTimeInMillis();

        limit.waitForCall();
        long firstCall = Calendar.getInstance().getTimeInMillis();

        limit.waitForCall();
        long secondCall = Calendar.getInstance().getTimeInMillis();

        limit.waitForCall();
        long thirdCall = Calendar.getInstance().getTimeInMillis();

        assertThat("", firstCall < oneMinute);
        assertThat("", secondCall < oneMinute);
        assertThat("", thirdCall > oneMinute);


    }

}