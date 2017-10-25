/*
 * Copyright (C)  2017 team-cachebox.de
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
package de.longri.cachebox3.utils.lists;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Longri on 21.04.2017.
 */
public class CB_List<T> extends Array<T> {

    //TODO delete CB_List<T> and replace method with same function of Array<T>

    //TODO replace with Array<T>.set(int index, T value)
    public void replace(T item, int index) {
        this.items[index] = item;
    }

    //TODO replace with Array<T>.peek ()
    public T last() {
        return items[size - 1];
    }
}
