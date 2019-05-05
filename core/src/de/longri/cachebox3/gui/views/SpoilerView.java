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
package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.sqlite.Import.ImporterProgress;
import de.longri.serializable.BitStore;

import static de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber.GrabImagesSelectedByCache;

/**
 * Created by Longri on 14.09.2016.
 */
public class SpoilerView extends AbstractView {

    public SpoilerView(BitStore reader) {
        super(reader);
    }

    public SpoilerView() {
        super("SpoilerView");
        MessageBox.show("Not implemented yet", "Not implemented", MessageBoxButtons.Cancel, MessageBoxIcon.Information, null);
    }

    @Override
    public void dispose() {

    }

    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu contextMenu = new Menu("SpoilerViewContextMenuTitle");

        contextMenu.addMenuItem("reloadSpoiler", CB.getSkin().getMenuIcon.importIcon, () -> {
            // todo inform the user about progress and give him the possibility to abort the image downloads
            GrabImagesSelectedByCache(new ImporterProgress(), true, false, EventHandler.getSelectedCache().getId(), EventHandler.getSelectedCache().getGcCode().toString(), "", "", false);
        });
        contextMenu.addMenuItem("LoadLogImages", CB.getSkin().getMenuIcon.downloadLogImages, () -> {
            // todo inform the user about progress and give him the possibility to abort the image downloads
            GrabImagesSelectedByCache(new ImporterProgress(), true, false, EventHandler.getSelectedCache().getId(), EventHandler.getSelectedCache().getGcCode().toString(), "", "", true);
        });


        /*
        contextMenu.addMenuItem("reloadSpoiler", null, (v, x, y, pointer, button) -> {
            GlobalCore.ImportSpoiler(false).setReadyListener(() -> {
                // do after import
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    SpoilerView.getInstance().ForceReload();
                    TabMainView.leftTab.ShowView(SpoilerView.getInstance());
                    SpoilerView.getInstance().onShow();
                }
            });
            return true;
        });

        contextMenu.addMenuItem("startPictureApp", Sprites.getSprite("image-export"), (v, x, y, pointer, button) -> {
            String file = SpoilerView.getInstance().getSelectedFilePath();
            if (file == null)
                return true;
            PlatformConnector.StartPictureApp(file);
            return true;
        });

         */

        return contextMenu;
    }
}
