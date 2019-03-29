/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.settings;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.settings.types.*;

import static de.longri.cachebox3.settings.types.SettingCategory.*;
import static de.longri.cachebox3.settings.types.SettingStoreType.Global;
import static de.longri.cachebox3.settings.types.SettingUsage.ACB;

/**
 * Holds all skin relevant settings
 * <p>
 * Created by Longri on 12.01.2017.
 */
public abstract class Settings_Skin extends Settings_Map {

    public static final SettingFolder SkinFolder = (SettingFolder) settingsList.addSetting(new SettingFolder("SkinFolder", Skin, NEVER, CB.WorkPath + "/skins", Global, ACB, false));
    public static final SettingBool nightMode = (SettingBool) settingsList.addSetting(new SettingBool("nightMode", Skin, NEVER, false, Global, ACB));
    public static final SettingString daySkinName = (SettingString) settingsList.addSetting(new SettingString("daySkinName", Skin, NEVER, "internalDefault", Global, ACB));
    public static final SettingString nightSkinName = (SettingString) settingsList.addSetting(new SettingString("nightSkinName", Skin, NEVER, "internalNight", Global, ACB));

//    public static final SettingBool useMipMap = (SettingBool) settingsList.addSetting(new SettingBool("useMipMap", Skin, EXPERT, false, Global, ACB));
//    public static final SettingBool dontUseAmbient = (SettingBool) settingsList.addSetting(new SettingBool("dontUseAmbient", Skin, EXPERT, true, Global, ACB));
//    public static final SettingInt ambientTime = (SettingInt) settingsList.addSetting(new SettingInt("ambientTime", Skin, EXPERT, 10, Global, ACB));
//
//    public static final SettingColor SolvedMysteryColor = (SettingColor) settingsList.addSetting(new SettingColor("SolvedMysteryColor", Skin, EXPERT, new HSV_Color(0.2f, 1f, 0.2f, 1f), Global, ACB));

}
