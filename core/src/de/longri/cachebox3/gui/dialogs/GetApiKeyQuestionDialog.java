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
package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;

import static de.longri.cachebox3.apis.GroundspeakAPI.fetchMyUserInfos;
import static de.longri.cachebox3.apis.GroundspeakAPI.setAuthorization;
import static de.longri.cachebox3.settings.Settings.GcLogin;

/**
 * Created by longri on 11.06.17.
 */
public class GetApiKeyQuestionDialog extends ButtonDialog {


    public GetApiKeyQuestionDialog() {
        this(Translation.get("wantApi"));
    }

    public GetApiKeyQuestionDialog(CharSequence msg) {
        this(msg, MessageBoxIcon.Question);
    }

    public GetApiKeyQuestionDialog(CharSequence msg, MessageBoxIcon icon) {
        this(msg, Translation.get("apiKeyNeeded"), MessageBoxIcon.Question);
    }

    public GetApiKeyQuestionDialog(CharSequence msg, CharSequence title, MessageBoxIcon icon) {
        super("GetApiKeyQuestionDialog", msg, title,
                MessageBoxButtons.YesNo, icon,
                (which, data) -> {
                    if (which == BUTTON_POSITIVE) {
                        Gdx.app.postRunnable(() -> PlatformConnector.getApiKey(accessToken -> {
                            // store the encrypted AccessToken in the Config file
                            if (Config.UseTestUrl.getValue()) {
                                Config.AccessTokenForTest.setEncryptedValue(accessToken);
                            } else {
                                Config.AccessToken.setEncryptedValue(accessToken);
                            }
                            setAuthorization();
                            String userNameOfAuthorization = fetchMyUserInfos().username;
                            GcLogin.setValue(userNameOfAuthorization);
                            Config.AcceptChanges();
                        }));
                    }
                    return true;
                });
    }
}
