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

import de.longri.cachebox3.CB;
import org.oscim.core.MapPosition;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * For classes with not implemented toString() method
 * Created by Longri on 09.03.2017.
 */
public class DEBUG_SB {

    public final String BR = CB.br;

    public static String toString(MapPosition pos) {
        return (format("lat:{} lon:{} scale:{} bearing:{} tilt:{}", pos.getLatitude(), pos.getLatitude()
                , pos.getScale(), pos.getBearing(), pos.getTilt()));
    }

    public static String format(String format, Object... arguments) {
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        return tp.getMessage();
    }
}
