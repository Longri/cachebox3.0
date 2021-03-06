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
package de.longri.cachebox3.gui.menu.menuBtn4;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.activities.FileTransfer_Activity;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractAction;

/**
 * Created by Longri on 02.11.2017.
 */
public class Action_Start_FileTransfer extends AbstractAction {

    public Action_Start_FileTransfer() {
        super("StartFileTransfer", MenuID.AID_START_FILE_TRANSFER);
    }


    @Override
    public void execute() {
        Gdx.app.postRunnable(() -> new FileTransfer_Activity().show());
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.me4FileTransfer;
    }

}
