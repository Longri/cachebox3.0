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
package de.longri.cachebox3.translation;

import com.badlogic.gdx.utils.StringBuilder;

/**
 * Created by longri on 26.05.17.
 */
public enum Language {
    cs, de, en_GB, fr, hu, nl, pl, pt_PT;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("lang/");
        sb.append(super.toString());
        sb.append("/strings.ini");

        return sb.toString().replaceAll("_", "-");
    }
}

