/*
 * Copyright (C) 2016-2020 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.extendsAbstractAction;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.menu.MenuID;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Switch_Torch extends AbstractAction {
    public Action_Switch_Torch() {
        super(!PlatformConnector.isTorchAvailable(), "Torch", MenuID.AID_TORCH);
    }

    @Override
    public Drawable getIcon() {
        if (!PlatformConnector.isTorchAvailable())
            return CB.getSkin().menuIcon.torchDisabled;
        if (PlatformConnector.isTorchOn()) {
            return CB.getSkin().menuIcon.torchOn;
        } else {
            return CB.getSkin().menuIcon.torchOff;
        }
    }

    @Override
    public void execute() {
        PlatformConnector.switchTorch();
    }
}
