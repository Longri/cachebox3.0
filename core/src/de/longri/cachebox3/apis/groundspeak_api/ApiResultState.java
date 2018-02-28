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

/**
 * Created by Longri on 07.10.2017.
 */
public enum ApiResultState {

    IO(1), API_IS_UNAVAILABLE(-1), API_ERROR(-2), API_DOWNLOAD_LIMIT(-7), CONNECTION_TIMEOUT(-3), CANCELED(-4), UNKNOWN(0),
    MEMBERSHIP_TYPE_VALID(1), MEMBERSHIP_TYPE_INVALID(-5), EXPIRED_API_KEY(-6),
    MEMBERSHIP_TYPE_GUEST(1), MEMBERSHIP_TYPE_BASIC(2), MEMBERSHIP_TYPE_PREMIUM(3), NO_API_KEY(-7);

    private final int state;

    ApiResultState(int state) {
        this.state = state;
    }

    public boolean isErrorState() {
        return state < 0;
    }

    public static ApiResultState fromState(int state) {
        switch (state) {
            case 1:
                return ApiResultState.MEMBERSHIP_TYPE_GUEST;
            case 2:
                return ApiResultState.MEMBERSHIP_TYPE_BASIC;
            case 3:
                return ApiResultState.MEMBERSHIP_TYPE_PREMIUM;
            case -6:
                return ApiResultState.EXPIRED_API_KEY;

        }
        return ApiResultState.MEMBERSHIP_TYPE_INVALID;
    }

    public int getState() {
        return state;
    }
}
