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
package de.longri.cachebox3.gui.utils;

import com.badlogic.gdx.utils.Array;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Created by Longri on 17.10.2017.
 */
public class CharArray extends Array<Character> implements CharSequence {

    public CharArray(String string) {
        for (int i = 0, n = string.length(); i < n; i++) {
            this.add(string.charAt(i));
        }
    }

    private CharArray() {
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public char charAt(int index) {
        return this.get(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        CharArray array = new CharArray();
        array.addAll(this.items, start, end - start);
        return array;
    }

    @Override
    public IntStream chars() {
        return null;
    }

    @Override
    public IntStream codePoints() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super Character> action) {

    }

    @Override
    public Spliterator<Character> spliterator() {
        return null;
    }
}
