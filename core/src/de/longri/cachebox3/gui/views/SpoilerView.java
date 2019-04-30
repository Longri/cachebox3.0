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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.widgets.GalleryView;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.sqlite.Import.ImporterProgress;
import de.longri.cachebox3.sqlite.dao.ImageDAO;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.utils.ImageLoader;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import static de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber.GrabImagesSelectedByCache;

/**
 * Created by Longri on 14.09.2016.
 */
public class SpoilerView extends AbstractView {
    static final Logger log = LoggerFactory.getLogger(SpoilerView.class);

    private final GalleryView galleryView = new GalleryView();

    private AbstractCache actCache;
    private boolean forceReload = false;

    public SpoilerView(BitStore reader) {
        super(reader);
    }

    public SpoilerView() {
        super("SpoilerView");
        this.addChild(galleryView);
    }

    public void forceReload() {
        this.forceReload = true;
    }

    private boolean cacheLoaded() {
        if (!forceReload && EventHandler.getSelectedCache().equals(actCache)) return true;
        forceReload = false;

        actCache = EventHandler.getSelectedCache();
        if (actCache == null) return true; // don't load spoiler from NULL!

        // load spoiler resources for act selected Cache on EventHandler!
        EventHandler.actCacheHasSpoiler();
        return false;
    }


    @Override
    public void onShow() {

        if (cacheLoaded()) return;

        Array<ImageEntry> spoilerResources = EventHandler.getSelectedCacheSpoiler();

        if (EventHandler.actCacheHasSpoiler()) {
            ImageDAO imageDAO = new ImageDAO();
            Array<ImageEntry> dbImages = imageDAO.getImagesForCache(actCache.getGcCode());

            for (int i = 0, n = spoilerResources.size; i < n; i++) {
                ImageEntry imageEntry = spoilerResources.get(i);

                String description = "";

                String localName = Utils.getFileNameWithoutExtension(imageEntry.LocalPath);
                for (ImageEntry dbImage : dbImages) {
                    String localNameFromDB = Utils.getFileNameWithoutExtension(dbImage.LocalPath);
                    if (localNameFromDB.equals(localName)) {
                        // Description
                        description = dbImage.Name + "\n" + dbImage.Description;
                        break;
                    } else {
                        if (Utils.getFileNameWithoutExtension(dbImage.Name).equals(localName)) {
                            // Spoiler CacheWolf
                            description = dbImage.Description;
                            break;
                        } else {
                            if (localName.contains(Utils.getFileNameWithoutExtension(dbImage.Name))) {
                                // Spoiler ACB
                                description = localName + "\n" + dbImage.Description;
                                break;
                            }
                        }
                    }
                }

                String label;
                if (description.length() > 0)
                    label = removeHashFromLabel(description);
                else {
                    label = removeHashFromLabel(Utils.getFileNameWithoutExtension(imageEntry.Name));
                }
                galleryView.addItem(imageEntry, label);
            }
            galleryView.galleryChanged();
        } else {
            galleryView.clearGallery();
        }

    }

    @Override
    public void dispose() {

    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        galleryView.setSize(this.getWidth(), this.getHeight());
        invalidateHierarchy();
        layout();
    }

    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu contextMenu = new Menu("SpoilerViewContextMenu");

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

    private static String removeHashFromLabel(String label) {
        int p1 = label.indexOf(" - ");
        if (p1 < 0)
            p1 = 0;
        else
            p1 = p1 + 3;
        int p2 = label.indexOf("@");
        if (p2 < 0)
            label = label.substring(p1);
        else
            label = label.substring(p1, p2);
        return label.trim();
    }


}
