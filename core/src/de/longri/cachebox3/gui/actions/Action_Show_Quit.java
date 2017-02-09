/* 
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.actions;


import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.dialogs.OnMsgBoxClickListener;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Action_Show_Quit extends AbstractAction {
    final static Logger log = LoggerFactory.getLogger(Action_Show_Quit.class);

    public Action_Show_Quit() {
        super("quit", MenuID.AID_SHOW_QUIT);
    }

    @Override
    public void execute() {

        String Msg = Translation.Get("QuitReally");
        String Title = Translation.Get("Quit?");

        //Name, msg, title, buttons, icon, OnMsgBoxClickListener
        Window dialog = new ButtonDialog("QuitDialog", Msg, Title, MessageBoxButtons.OKCancel, MessageBoxIcon.Stop, new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                if (which == ButtonDialog.BUTTON_POSITIVE) {
                    log.debug("\r\n Quit");
                    CB.callQuit();
                }
                return true;
            }
        });
        dialog.show();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getIcon.Close;
    }
}
