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
package de.longri.cachebox3.apis.groundspeak_api;

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 26.03.2018.
 */
class PocketQueryTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getPqListOnlineTest() throws InterruptedException {
        if (isDummy) return;

        Array<PocketQuery.PQ> pqList = new Array<>();

        PocketQuery pq = new PocketQuery(apiKey, null, pqList);

        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final ApiResultState[] state = new ApiResultState[1];
        pq.post(new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {
                state[0] = value;
                WAIT.set(false);
            }
        });

        while (WAIT.get()) {
            Thread.sleep(100);
        }

        assertThat("Result state should be IO", state[0] == ApiResultState.IO);
        assertThat("PQ list size should >0", pqList.size > 0);
    }
}