/* 
 * Copyright (C) 2016 team-cachebox.de
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


/**
 * Interface used to allow the creator of a dialog to run some code when an item on the dialog is clicked..<br>
 *
 * Created by Longri on 05.08.16.
 */
public interface OnMsgBoxClickListener {
    /**
     * This method will be invoked when a button in the dialog is clicked.
     *
     * @param which
     *            The button that was clicked ( the position of the item clicked.
     * @return
     */
    public boolean onClick(int which, Object data);
}
