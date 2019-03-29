/* 
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 16.08.16.
 */
public class Action_Show_Help extends AbstractAction {
    final static Logger log = LoggerFactory.getLogger(Action_Show_Help.class);

    public Action_Show_Help() {
        super(IMPLEMENTED, "Help Online", MenuID.AID_HELP);
    }

    @Override
    public void execute() {
        //PlatformConnector.callUrl("http://www.team-cachebox.de/index.php/de/kurzanleitung");
        String friends = GroundspeakAPI.fetchFriends();
        if (GroundspeakAPI.APIError == 0) {
            Config.Friends.setValue(friends);
            Config.AcceptChanges();
            MessageBox.show(Translation.get("ok") + ":\n" + friends, Translation.get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
        } else {
            MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
        }

    }


    @Override
    public Drawable getIcon() {
        return CB.getSkin().getIcon.Help;
    }
}
