/*
 * Copyright (C) 2020 team-cachebox.de
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

import de.longri.cachebox3.gui.activities.GetApiKey_Activity;
import de.longri.cachebox3.translation.Translation;

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
                MessageBoxButton.YesNo, icon,
                (which, data) -> {
                    if (which == BUTTON_POSITIVE) {
                        new GetApiKey_Activity().show();
                    }
                    return true;
                });
    }
}
