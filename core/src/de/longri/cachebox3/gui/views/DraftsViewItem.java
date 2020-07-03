/*
 * Copyright (C) 2020-2018 team-cachebox.de
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.activities.EditDraft;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.activities.InputString;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.skin.styles.CacheTypeStyle;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.converter.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static de.longri.cachebox3.apis.GroundspeakAPI.OK;
import static de.longri.cachebox3.gui.views.DraftsView.drafts;

/**
 * Created by Longri on 31.08.2017.
 */
public class DraftsViewItem extends ListViewItem {
    private static final Logger log = LoggerFactory.getLogger(DraftsViewItem.class);
    private final static SimpleDateFormat postFormatter = new SimpleDateFormat("dd.MMM.yy (HH:mm)", Locale.US);

    final private DraftListItemStyle draftListItemStyle;

    private boolean needsLayout = true;
    private final Draft draft;
    private VisTable headerTable;

    public DraftsViewItem(int listIndex, Draft fromDraft, DraftListItemStyle _draftListItemStyle) {
        super(listIndex);
        draft = fromDraft;
        draftListItemStyle = _draftListItemStyle;
    }

    @Override
    public synchronized void layout() {
        // setDebug(true, true);
        if (!needsLayout) {
            super.layout();
            return;
        }

        clear();

        Label.LabelStyle headerLabelStyle = new Label.LabelStyle();
        headerLabelStyle.font = draftListItemStyle.headerFont;
        headerLabelStyle.fontColor = draftListItemStyle.headerFontColor;

        Label.LabelStyle commentLabelStyle = new Label.LabelStyle();
        commentLabelStyle.font = draftListItemStyle.descriptionFont;
        commentLabelStyle.fontColor = draftListItemStyle.descriptionFontColor;

        headerTable = new VisTable();
        headerTable.add(new Image(draft.type.getDrawable(draftListItemStyle.logTypesStyle))).left();
        if (draft.uploaded) headerTable.add(new Image(draftListItemStyle.uploadedIcon)).left();
        // headerTable.add((Actor) null).left().padLeft(CB.scaledSizes.MARGINx2).expandX().fillX();

        String foundNumber = "";
        if (draft.foundNumber > 0) {
            foundNumber = "#" + draft.foundNumber + " @ ";
        }

        VisLabel dateLabel = new VisLabel(foundNumber + postFormatter.format(draft.timestamp), headerLabelStyle);
        dateLabel.setAlignment(Align.right);
        headerTable.add(dateLabel).expandX().fillX(); // .padRight(CB.scaledSizes.MARGINx2).right()
        // headerTable.pack();
        // headerTable.layout();
        add(headerTable).left().expandX().fillX();

        headerTable.addListener(new ClickLongClickListener() {
            @Override
            public boolean clicked(InputEvent event, float x, float y) {
                return onHeaderClicked();
            }

            @Override
            public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                return onHeaderClicked();
            }
        });

        row().padTop(CB.scaledSizes.MARGINx2);

        VisTable entryTable = new VisTable();


        VisTable cacheTable = new VisTable();

        VisTable iconTable = new VisTable();
        iconTable.add(draft.cacheType.getCacheWidget(draftListItemStyle.cacheTypeStyle, null, null, null, null));
        iconTable.pack();
        iconTable.layout();

        cacheTable.add(iconTable).left().padRight(CB.scaledSizes.MARGINx2);

        VisLabel nameLabel = new VisLabel(draft.CacheName, headerLabelStyle);
        nameLabel.setWrap(true);
        cacheTable.add(nameLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        cacheTable.row();

        cacheTable.add((Actor) null).left().padRight(CB.scaledSizes.MARGINx2);

        VisLabel gcLabel = new VisLabel(draft.gcCode, headerLabelStyle);
        gcLabel.setWrap(true);
        cacheTable.add(gcLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();


        entryTable.add(cacheTable).top().expandX().fillX();
        entryTable.row().padTop(CB.scaledSizes.MARGINx2);


        VisLabel commentLabel = new VisLabel(draft.comment, commentLabelStyle);
        commentLabel.setWrap(true);
        entryTable.add(commentLabel).expand().fill();


        if (draft.uploaded) entryTable.setColor(new Color(1, 1, 1, 0.4f));


        add(entryTable).expand().fill();
        entryTable.addListener(new ClickLongClickListener() {
            @Override
            public boolean clicked(InputEvent event, float x, float y) {
                editDraft();
                return true;
            }

            @Override
            public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                editDraft();
                return true;
            }
        });
        super.layout();
        needsLayout = false;
    }

    private boolean onHeaderClicked() {
        Menu cm = new Menu("DraftItemMenuTitle");
        cm.addMenuItem("SelectCache", ":\n" + draft.CacheName, draft.cacheType.getDrawable(CB.getSkin().get("Size48", CacheTypeStyle.class)), this::selectCacheFromDraft);
        cm.addMenuItem("edit", CB.getSkin().menuIcon.editDraft, this::editDraft);
        if (draft.GcId.startsWith("GL")) {
            cm.addMenuItem("uploadLogImage", CB.getSkin().menuIcon.uploadDraft, this::uploadLogImage);
            cm.addMenuItem("BrowseLog", null, () -> PlatformConnector.callUrl("https://coord.info/" + draft.GcId));
        }
        cm.addMenuItem("uploadDrafts", CB.getSkin().menuIcon.uploadDraft, () -> DraftsView.getInstance().logOnline(draft, false));
        cm.addMenuItem("directLog", CB.getSkin().menuIcon.me2Logbook, () -> DraftsView.getInstance().logOnline(draft, true));
        cm.addMenuItem("delete", CB.getSkin().menuIcon.delete, this::deleteDraft);
        cm.show();
        return true;
    }

    private void selectCacheFromDraft() {
        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        String statement = "SELECT * FROM CacheCoreInfo core WHERE Id = " + draft.CacheId;
        DaoFactory.CACHE_LIST_DAO.readCacheList(Database.Data, lCaches, statement, false, false);
        AbstractCache tmpCache = null;
        if (lCaches.size > 0)
            tmpCache = lCaches.get(0);
        AbstractCache cache = tmpCache;
        if (cache == null) {
            String message = Translation.get("cacheOtherDb", draft.CacheName.toString()).toString();
            message += "\n" + Translation.get("DraftNoSelect");
            MessageBox.show(message, Translation.get("Error"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
            return;
        }

        synchronized (Database.Data.cacheList) {
            cache = Database.Data.cacheList.getCacheByGcCode(draft.gcCode);
        }

        if (cache == null) {
            Database.Data.cacheList.add(tmpCache);
            cache = Database.Data.cacheList.getCacheByGcCode(draft.gcCode);
        }

        AbstractWaypoint finalWp = null;
        if (cache != null) {
            if (cache.hasFinalWaypoint())
                finalWp = cache.getFinalWaypoint();
            else if (cache.hasStartWaypoint())
                finalWp = cache.getStartWaypoint();
            EventHandler.fireSelectedWaypointChanged(cache, finalWp);
        }
    }

    private void uploadLogImage() {
        String mPath = Config.ImageUploadLastUsedPath.getValue();
        if (mPath.length() == 0) {
            mPath = CB.WorkPath + "/User/Media/";
        }
        // PlatformConnector.getFile(mPath, "*.jpg", Translation.get("SelectImage"), Translation.get("SelectImageButton"), PathAndName -> { });
        FileChooser fileChooser = new FileChooser(Translation.get("SelectImage"), FileChooser.SelectionMode.FILES, "jpg", "png");
        fileChooser.setSelectionReturnListener(fileHandle -> {
            if (fileHandle != null) {
                InputString inputDescription = new InputString("imageDescription", null) {
                    public void callBack(String description) {
                        CB.postAsync(new NamedRunnable("uploadImage") {
                            @Override
                            public void run() {
                                Config.ImageUploadLastUsedPath.setValue(fileHandle.parent().path());
                                Config.AcceptChanges();
                                try {
                                    String image = Base64.encodeBytes(fileHandle.readBytes());
                                    GroundspeakAPI.getInstance().uploadLogImage(draft.GcId, image, description);
                                    if (GroundspeakAPI.getInstance().APIError == OK) {
                                        MessageBox.show(Translation.get("ok") + ":\n", Translation.get("uploadLogImage"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                                    } else {
                                        MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get("uploadLogImage"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                                    }
                                } catch (Exception ex) {
                                    MessageBox.show(ex.toString(), Translation.get("uploadLogImage"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
                                }
                            }
                        });
                    }
                };
                inputDescription.show();
            }
        });
        fileChooser.setDirectory(Gdx.files.absolute(mPath), false);
        fileChooser.show();
    }

    private void editDraft() {
        EditDraft efnActivity = EditDraft.getInstance();
        efnActivity.setDraft(draft, DraftsView.getInstance()::addOrChangeDraft, false);
        efnActivity.show();
    }

    private void deleteDraft() {

        AbstractCache tmpAbstractCache = null;
        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();

        String statement = "SELECT * FROM CacheCoreInfo core WHERE Id = " + draft.CacheId;

        DaoFactory.CACHE_LIST_DAO.readCacheList(Database.Data, lCaches, statement, false, false);
        if (lCaches.size > 0)
            tmpAbstractCache = lCaches.get(0);
        final AbstractCache abstractCache = tmpAbstractCache;

        if (abstractCache == null && !draft.isTbDraft) {
            CharSequence message = new CompoundCharSequence(Translation.get("cacheOtherDb", draft.CacheName.toString())
                    , "\n", Translation.get("draftNoDelete"));

            MessageBox.show(message, null, MessageBoxButton.OK, MessageBoxIcon.Exclamation, null);
            return;
        }

        CharSequence message;
        if (draft.isTbDraft) {
            message = Translation.get("confirmDraftDeletionTB", draft.getTypeString(), draft.TbName);
        } else {
            message = Translation.get("confirmDraftDeletion", draft.getTypeString(), draft.CacheName.toString());
        }

        MessageBox.show(message, Translation.get("deleteDraft"), MessageBoxButton.YesNo, MessageBoxIcon.Question, (which, data) -> {
            switch (which) {
                case ButtonDialog.BUTTON_POSITIVE:
                    // Yes button clicked
                    // delete aktDraft
                    if (abstractCache != null) {
                        if (abstractCache.isFound()) {
                            abstractCache.setFound(false);
                            abstractCache.updateBooleanStore();
                            DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, abstractCache);
                            Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                            Config.AcceptChanges();
                            // jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
                            // damit das Smiley Symbol aus der Map und der CacheList verschwindet
                            synchronized (Database.Data.cacheList) {
                                AbstractCache tc = Database.Data.cacheList.getCacheById(abstractCache.getId());
                                if (tc != null) {
                                    tc.setFound(false);
                                    tc.updateBooleanStore();
                                }
                            }
                        }
                    }
                    drafts.deleteDraftById(draft.Id);
                    DraftsView.notifyDataSetChanged();
                    Drafts.createVisitsTxt(Config.DraftsGarminPath.getValue());
                    break;
                case ButtonDialog.BUTTON_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;
        });

    }

    @Override
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        super.drawBackground(batch, parentAlpha, x, y);
        super.drawBackground(batch, 0.4f, x, y);
        if (draftListItemStyle.headerBackground != null && headerTable != null) {
            float height = headerTable.getHeight() + getPadTop() + getPadBottom();
            batch.setColor(1, 1, 1, 1);
            draftListItemStyle.headerBackground.draw(batch, x, y + (getHeight() - height), getWidth(), height);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (draft.uploaded && draftListItemStyle.uploadedOverlay != null) {
            //draw uploaded overlay
            draftListItemStyle.uploadedOverlay.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public void dispose() {

    }
}
