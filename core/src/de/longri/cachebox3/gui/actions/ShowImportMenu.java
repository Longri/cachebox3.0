/*
 * Copyright (C) 2017 team-cachebox.de
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

import de.longri.cachebox3.gui.activities.ImportGcPos;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;

/**
 * Created by Longri on 12.04.2017.
 */
public class ShowImportMenu extends Menu {
    public ShowImportMenu() {
        super("ImportMenu");
        this.setOnItemClickListener(clickListener);

        addItem(MenuID.MI_IMPORT_GPX, "GPX_IMPORT");
        addItem(MenuID.MI_EXPORT_RUN, "export");

        addItem(MenuID.MI_IMPORT_GS, "API_IMPORT").setMoreMenu(getGcImportMenu());

//        if (!StringH.isEmpty(Config.CBS_IP.getValue()))
//            addItem(MenuID.MI_IMPORT_CBS, "CB-Server");

        addItem(MenuID.MI_IMPORT_GCV, "GC_Vote");
        addItem(MenuID.MI_IMPORT, "moreImport");

    }

    private final OnItemClickListener clickListener = new OnItemClickListener() {
        @Override
        public boolean onItemClick(MenuItem item) {
            switch (item.getMenuItemId()) {
                case MenuID.MI_IMPORT_GS:
                    //do nothing, will show more menu
                    break;
            }
            return true;
        }
    };


    private Menu getGcImportMenu() {
        Menu menu = new Menu("GcImportMenu");
        menu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {

                switch (item.getMenuItemId()) {
                    case MenuID.MI_IMPORT_GS_PQ:
//                        Import imp = new Import(MenuID.MI_IMPORT_GS_PQ);
//                        imp.show();
                        return true;
                    case MenuID.MI_IMPORT_GS_API_POSITION:
                        new ImportGcPos().show();
                        return true;
                    case MenuID.MI_IMPORT_GS_API_SEARCH:
//                        SearchOverNameOwnerGcCode.ShowInstanz();
                        return true;
                }

                return true;
            }
        });
        menu.addItem(MenuID.MI_IMPORT_GS_PQ, "API_PocketQuery");
        menu.addItem(MenuID.MI_IMPORT_GS_API_POSITION, "API_IMPORT_OVER_POSITION");
        menu.addItem(MenuID.MI_IMPORT_GS_API_SEARCH, "API_IMPORT_NAME_OWNER_CODE");

        return menu;
    }
}
