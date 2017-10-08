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
import de.longri.cachebox3.gui.widgets.ApiButton;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by longri on 11.06.17.
 */
public class GetApiKeyQuestionDialog extends ButtonDialog {


    public GetApiKeyQuestionDialog() {
        this(Translation.Get("wantApi"));
    }

    public GetApiKeyQuestionDialog(String msg) {
        this(msg, MessageBoxIcon.Question);
    }

    public GetApiKeyQuestionDialog(String msg,MessageBoxIcon icon) {
        this(msg,Translation.Get("apiKeyNeeded"), MessageBoxIcon.Question);
    }

    public GetApiKeyQuestionDialog(String msg,String title, MessageBoxIcon icon) {
        super("GetApiKeyQuestionDialog", msg, title,
                MessageBoxButtons.YesNo, icon,
                new OnMsgBoxClickListener() {
                    @Override
                    public boolean onClick(int which, Object data) {
                        if (which == BUTTON_POSITIVE) {
                            // open create api key dialog
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    new ApiButton().generateKey();
                                }
                            });
                        }
                        return true;
                    }
                });
    }
}
