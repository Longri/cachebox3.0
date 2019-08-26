/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.platform_test;

/**
 * Created by Longri on 26.08.2019.
 */
public class StyleEntry {
    public Class clazz;
    public String name;

    public StyleEntry(String styleName, Class clazz) {
        this.clazz = clazz;
        this.name = styleName;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StyleEntry) {
            StyleEntry otherEntry = (StyleEntry) other;
            if (!otherEntry.name.equals(this.name)) return false;
            if (otherEntry.clazz.equals(this.clazz)) return true;
            return false;
        }
        return false;
    }
}
