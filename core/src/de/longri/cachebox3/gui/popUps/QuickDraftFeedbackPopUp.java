/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.popUps;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Actor;

public class QuickDraftFeedbackPopUp extends Catch_Actor {

    public QuickDraftFeedbackPopUp(boolean found) {
//TODO
    }

    public void show() {
        if (CB.viewmanager != null) {
            CB.viewmanager.toast(this, ViewManager.ToastLength.NORMAL);
        }
    }
}
