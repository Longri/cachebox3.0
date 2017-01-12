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

import de.longri.cachebox3.utils.HSV_Color;

/**
 * Holds all skin relevant settings
 * <p>
 * Created by Longri on 12.01.2017.
 */
public abstract class Settings_Skin extends Settings_Const {

    public static final de.longri.cachebox3.settings.types.SettingFolder SkinFolder = (de.longri.cachebox3.settings.types.SettingFolder) de.longri.cachebox3.settings.types.SettingsList.addSetting(new de.longri.cachebox3.settings.types.SettingFolder("SkinFolder", de.longri.cachebox3.settings.types.SettingCategory.Folder, DEVELOPER, "default", de.longri.cachebox3.settings.types.SettingStoreType.Global, de.longri.cachebox3.settings.types.SettingUsage.ACB, false));

    public static final de.longri.cachebox3.settings.types.SettingBool useMipMap = (de.longri.cachebox3.settings.types.SettingBool) de.longri.cachebox3.settings.types.SettingsList.addSetting(new de.longri.cachebox3.settings.types.SettingBool("useMipMap", de.longri.cachebox3.settings.types.SettingCategory.Skin, EXPERT, false, de.longri.cachebox3.settings.types.SettingStoreType.Global, de.longri.cachebox3.settings.types.SettingUsage.ACB));
    public static final de.longri.cachebox3.settings.types.SettingBool dontUseAmbient = (de.longri.cachebox3.settings.types.SettingBool) de.longri.cachebox3.settings.types.SettingsList.addSetting(new de.longri.cachebox3.settings.types.SettingBool("dontUseAmbient", de.longri.cachebox3.settings.types.SettingCategory.Skin, EXPERT, true, de.longri.cachebox3.settings.types.SettingStoreType.Global, de.longri.cachebox3.settings.types.SettingUsage.ACB));
    public static final de.longri.cachebox3.settings.types.SettingInt ambientTime = (de.longri.cachebox3.settings.types.SettingInt) de.longri.cachebox3.settings.types.SettingsList.addSetting(new de.longri.cachebox3.settings.types.SettingInt("ambientTime", de.longri.cachebox3.settings.types.SettingCategory.Skin, EXPERT, 10, de.longri.cachebox3.settings.types.SettingStoreType.Global, de.longri.cachebox3.settings.types.SettingUsage.ACB));

    public static final de.longri.cachebox3.settings.types.SettingColor SolvedMysteryColor = (de.longri.cachebox3.settings.types.SettingColor) de.longri.cachebox3.settings.types.SettingsList.addSetting(new de.longri.cachebox3.settings.types.SettingColor("SolvedMysteryColor", de.longri.cachebox3.settings.types.SettingCategory.Skin, EXPERT, new HSV_Color(0.2f, 1f, 0.2f, 1f), de.longri.cachebox3.settings.types.SettingStoreType.Global, de.longri.cachebox3.settings.types.SettingUsage.ACB));

}
