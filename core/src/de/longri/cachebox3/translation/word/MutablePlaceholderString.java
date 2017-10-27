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
package de.longri.cachebox3.translation.word;

import com.badlogic.gdx.utils.CharArray;

/**
 * Created by Longri on 26.10.2017.
 */
public class MutablePlaceholderString extends MutableString {


    /**
     * The given storage will split by {x} for replace the placeHolder
     *
     * @param storage
     * @param string
     */
    public MutablePlaceholderString(CharArray storage, CharSequence string) {
        super(storage, null); // don't add, we must split first!

    }
}
