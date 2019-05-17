/* 
 * Copyright (C) 2011-2017 team-cachebox.de
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
package de.longri.cachebox3.settings.types;

/**
 * String is for translation
 */
public enum SettingCategory {
    Login("Login"), QuickList("QuickList"), Map("Map"), LiveMap("LiveMap"), Gps("Gps"), Compass("Compass"), Misc("Misc"), Sounds("Sounds"), Skin("Skin"), API("API"), Folder("Folder"), Templates("Templates"), Drafts("Drafts"), Internal(
            "Internal"), CarMode("CarMode"), RememberAsk("RememberAsk"), Debug("Debug"), Button("Button"), Positions("Positions"), CBS("CBS")
    , Locale("Locale");

    private String langString;

    SettingCategory(String langString) {
        this.langString = langString;
    }

    public String getLangString() {
        return langString;
    }

    public void setLangString(String langString) {
        this.langString = langString;
    }

    private boolean mIsCollapse = false;

    public boolean IsCollapse() {
        return mIsCollapse;
    }

    public void Toggle() {
        mIsCollapse = !mIsCollapse;
    }

    public void Toggle(boolean value) {
        mIsCollapse = value;
    }

}
