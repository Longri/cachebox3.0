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
package de.longri.cachebox3.gui.actions.show_vies;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.*;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.CompassView;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.SettingBool;

/**
 * Created by Longri on 24.07.16.
 */
public class Action_Show_CompassView extends Abstract_Action_ShowView {
    public Action_Show_CompassView() {
        super("Compass", MenuID.AID_SHOW_COMPASS);
    }

    @Override
    public void execute() {
        if (isActVisible()) return;

        CompassView view = new CompassView();
        CB.viewmanager.showView(view);
    }

    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.compassIcon;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }


    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof CompassView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(CompassView.class.getName());
    }

    public Menu getContextMenu() {
        Menu icm = new Menu("menu_compassView");
        icm.setOnItemClickListener(onItemClickListener);

        icm.addItem(MenuID.MI_COMPASS_SHOW, "view");

        return icm;
    }

    private void showOtionMenu() {
        OptionMenu icm = new OptionMenu("menu_compassView");
        icm.setOnItemClickListener(onItemClickListener);
        MenuItem mi;

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_MAP, "CompassShowMap");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowMap.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_NAME, "CompassShowWP_Name");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowWP_Name.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ICON, "CompassShowWP_Icon");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowWP_Icon.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ATTRIBUTES, "CompassShowAttributes");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowAttributes.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_GC_CODE, "CompassShowGcCode");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowGcCode.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_COORDS, "CompassShowCoords");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowCoords.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_WP_DESC, "CompassShowWpDesc");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowWpDesc.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SAT_INFO, "CompassShowSatInfos");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowSatInfos.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SUN_MOON, "CompassShowSunMoon");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowSunMoon.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_TARGET_DIRECTION, "CompassShowTargetDirection");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowTargetDirection.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_S_D_T, "CompassShowSDT");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowSDT.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_LAST_FOUND, "CompassShowLastFound");
        mi.setCheckable(true);
        mi.setChecked(Config.CompassShowLastFound.getValue());

        icm.show();

    }

    private final OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public boolean onItemClick(MenuItem item) {
            switch (item.getMenuItemId()) {
                case MenuID.MI_COMPASS_SHOW:
                    showOtionMenu();
                    return true;
                case MenuID.MI_COMPASS_SHOW_MAP:
                    toggleSetting(Config.CompassShowMap,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_NAME:
                    toggleSetting(Config.CompassShowWP_Name,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_ICON:
                    toggleSetting(Config.CompassShowWP_Icon,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_ATTRIBUTES:
                    toggleSetting(Config.CompassShowAttributes,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_GC_CODE:
                    toggleSetting(Config.CompassShowGcCode,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_COORDS:
                    toggleSetting(Config.CompassShowCoords,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_WP_DESC:
                    toggleSetting(Config.CompassShowWpDesc,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_SAT_INFO:
                    toggleSetting(Config.CompassShowSatInfos,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_SUN_MOON:
                    toggleSetting(Config.CompassShowSunMoon,item);
                    return true;

                case MenuID.MI_COMPASS_SHOW_TARGET_DIRECTION:
                    toggleSetting(Config.CompassShowTargetDirection,item);
                    return true;
                case MenuID.MI_COMPASS_SHOW_S_D_T:
                    toggleSetting(Config.CompassShowSDT,item);
                    return true;
                case MenuID.MI_COMPASS_SHOW_LAST_FOUND:
                    toggleSetting(Config.CompassShowLastFound,item);
                    return true;
            }
            return false;
        }
    };

    private void toggleSetting(SettingBool setting, MenuItem item) {
        boolean newValue = !setting.getValue();
        setting.setValue(newValue);
        item.setChecked(newValue);
        Config.AcceptChanges();

        if(isActVisible()){
            CompassView compassView= (CompassView) CB.viewmanager.getActView();
            compassView.resetLayout();
        }
    }
}
