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
package de.longri.cachebox3.gui.actions.show_activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.views.DraftsView;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.LogTypes;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_QuickDraft extends AbstractAction {

    public Action_QuickDraft() {
        super(IMPLEMENTED, "QuickDraft", MenuID.AID_QuickDraft);
    }

    @Override
    public void execute() {
        Menu cm = new Menu("QuickDraft");
        AbstractCache cache = EventHandler.getSelectedCache();
        DraftListItemStyle itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", DraftListItemStyle.class);

        switch (cache.getType()) {
            case Event:
            case MegaEvent:
            case Giga:
            case CITO:
                cm.addMenuItem("attended", itemStyle.typeStyle.attended, () -> {
                    DraftsView.addNewDraft(LogTypes.attended, true);
                    finalHandling(true, cache);
                });
                break;
            case Camera:
                cm.addMenuItem("webCamFotoTaken", itemStyle.typeStyle.webcam_photo_taken, () -> {
                    DraftsView.addNewDraft(LogTypes.webcam_photo_taken, true);
                    finalHandling(true, cache);
                });
                cm.addMenuItem("DNF", itemStyle.typeStyle.didnt_find, () -> {
                    finalHandling(false, cache);
                });
                break;
            default:
                cm.addMenuItem("found", itemStyle.typeStyle.found, () -> {
                    DraftsView.addNewDraft(LogTypes.found, true);
                    finalHandling(true, cache);
                });
                cm.addMenuItem("DNF", itemStyle.typeStyle.didnt_find, () -> {
                    DraftsView.addNewDraft(LogTypes.didnt_find, true);
                    finalHandling(false, cache);
                });
                break;
        }
        cm.show();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.draft;
    }

    private void finalHandling(boolean found, AbstractCache cache) {
        // CB2:
        // damit der Status geändert wird
        // damit die Icons in der Map aktualisiert werden
        /*
        CacheListChangedEventList.Call();
        SelectedCacheEventList.Call(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
        QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(found);
        pop.show(PopUp_Base.SHOW_TIME_SHORT);

        PlatformConnector.vibrate();
         */
        /*
        // todo show in CacheList and Map and others. Don't know what all has to be called!!!
        // CB3: supposed, but not complete
        cache.setFound(cache.isFound());
        cache.updateBooleanStore(Database.Data);
        DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, cache);
        Config.FoundOffset.setValue(Config.FoundOffset.getValue() + (cache.isFound() ? 1 : - 1));
        Config.AcceptChanges();
        // jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
        // damit das Smiley Symbol aus der Map und der CacheList verschwindet
        synchronized (Database.Data.cacheList) {
            AbstractCache tc = Database.Data.cacheList.GetCacheById(cache.getId());
            if (tc != null) {
                tc.setFound(cache.isFound());
                tc.updateBooleanStore(Database.Data);
            }
        }
        DraftList.createVisitsTxt(Config.DraftsGarminPath.getValue());
         */
    }

}
