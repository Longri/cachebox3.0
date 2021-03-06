/*
 * Copyright (C) 2016 - 2020 team-cachebox.de
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
package de.longri.cachebox3.gui.menu.quickBtns;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.skin.styles.LogTypesStyle;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.gui.views.DraftsView;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Drafts;
import de.longri.cachebox3.types.LogType;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_QuickDraft extends AbstractAction {

    public Action_QuickDraft() {
        super("QuickDraft", MenuID.AID_QuickDraft);
    }

    @Override
    public void execute() {
        Menu cm = new Menu("QuickDraft");
        AbstractCache cache = EventHandler.getSelectedCache();
        LogTypesStyle logTypesStyle = VisUI.getSkin().get("LogTypesSize48", LogTypesStyle.class);

        switch (cache.getType()) {
            case Event:
            case MegaEvent:
            case Giga:
            case CITO:
                cm.addMenuItem("attended", logTypesStyle.attended, () -> {
                    DraftsView.getInstance().addNewDraft(LogType.attended, true);
                    finalHandling(true, cache);
                });
                break;
            case Camera:
                cm.addMenuItem("webCamFotoTaken", logTypesStyle.webcam_photo_taken, () -> {
                    DraftsView.getInstance().addNewDraft(LogType.webcam_photo_taken, true);
                    finalHandling(true, cache);
                });
                cm.addMenuItem("DNF", logTypesStyle.didnt_find, () -> {
                    finalHandling(false, cache);
                });
                break;
            default:
                cm.addMenuItem("found", logTypesStyle.found, () -> {
                    DraftsView.getInstance().addNewDraft(LogType.found, true);
                    finalHandling(true, cache);
                });
                cm.addMenuItem("DNF", logTypesStyle.didnt_find, () -> {
                    DraftsView.getInstance().addNewDraft(LogType.didnt_find, true);
                    finalHandling(false, cache);
                });
                break;
        }
        cm.show();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.draft;
    }

    private void finalHandling(boolean found, AbstractCache cache) {
        cache.setFound(found);
        cache.updateBooleanStore();
        // todo check if following is perhaps necessary
        // DaoFactory.CACHE_LIST_DAO.reloadCache(Database.Data, Database.Data.cacheList, cache);

        Config.FoundOffset.setValue(Config.FoundOffset.getValue() + (cache.isFound() ? 1 : -1));
        Config.AcceptChanges();

        EventHandler.fire(new CacheListChangedEvent());
        EventHandler.fire(new SelectedCacheChangedEvent(cache));
        /*
        todo following and perhaps more
        QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(found);
        pop.show(PopUp_Base.SHOW_TIME_SHORT);

        PlatformConnector.vibrate();
         */
        Drafts.createVisitsTxt(Config.DraftsGarminPath.getValue());
    }

}
