/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.show_activities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.activities.SelectDB_Activity;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 21.06.19.
 */
public class Action_Explore extends AbstractAction {
    final static Logger log = LoggerFactory.getLogger(Action_Explore.class);

    public Action_Explore() {
        super(IMPLEMENTED, "FileXplore", MenuID.AID_FILE_X_PLORE);
    }

    @Override
    public void execute() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                final FileChooser fileChooser = new FileChooser(Translation.get("Xplore"),
                        FileChooser.Mode.BROWSE, FileChooser.SelectionMode.ALL);
                fileChooser.setDirectory(CB.WorkPathFileHandle, true);
                fileChooser.show();
            }
        });
    }


    @Override
    public Drawable getIcon() {
        //TODO create icon like finder/explorer
        return CB.getSkin().getMenuIcon.todo;
    }
}
