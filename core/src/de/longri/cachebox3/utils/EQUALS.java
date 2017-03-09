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
package de.longri.cachebox3.utils;

import org.oscim.core.MapPosition;

/**
 * For classes with not implemented equals() method
 * Created by Longri on 09.03.2017.
 */
public class EQUALS {

    public static boolean is(MapPosition p1, MapPosition p2) {
        if (p1 == p2) return true;
        if (p1 == null || p2 == null) return false;
        if (p1.getX() != p2.getX()) return false;
        if (p1.getY() != p2.getY()) return false;
        if (p1.getScale() != p2.getScale()) return false;
        if (p1.getTilt() != p2.getTilt()) return false;
        if (p1.getBearing() != p2.getBearing()) return false;
        return true;
    }

}
