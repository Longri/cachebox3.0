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
import com.badlogic.gdx.utils.CharArray;

import java.util.*;

/**
 * Created by Longri on 17.10.2017.
 */
public class CharSequenceArray extends CharArray implements CharSequence {

    private int hash;

    public CharSequenceArray(String string) {
        if (string != null) {
            for (int i = 0, n = string.length(); i < n; i++) {
                this.add(string.charAt(i));
            }
        }
    }

    private CharSequenceArray() {
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public char charAt(int index) {
        return this.items[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        CharSequenceArray array = new CharSequenceArray();
        array.addAll(this.items, start, end - start);
        return array;
    }

    /**
     * Returns a hash code for this string. The hash code for a
     * {@code String} object is computed as
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using {@code int} arithmetic, where {@code s[i]} is the
     * <i>i</i>th character of the string, {@code n} is the length of
     * the string, and {@code ^} indicates exponentiation.
     * (The hash value of the empty string is zero.)
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        int h = hash;
        if (h == 0 && size > 0) {

            for (int i = 0; i < size; i++) {
                h = 31 * h + items[i];
            }
            hash = h;
        }
        return h;
    }


    @Override
    public String toString() {
        return new String(this.shrink());
    }

    public boolean equals(String other) {
        if (this.hashCode() != ((CharSequence) other).hashCode()) return false;
            int n = this.size;
            int i = 0;
            while (n-- != 0) {
                if (this.items[i] != ((CharSequence) other).charAt(i))
                    return false;
                i++;
            }
            return true;
    }


    @Override
    public boolean equals(Object other) {

        if (other instanceof CharSequenceArray) {
            CharSequenceArray o = (CharSequenceArray) other;
            if (this.hashCode() != o.hashCode()) return false;
            return Arrays.equals(this.items, o.items);
        }

        if (other instanceof CharSequence) {
            CharSequence o = (CharSequence) other;
            if (this.hashCode() != o.hashCode()) return false;
            int n = this.size;
            int i = 0;
            while (n-- != 0) {
                if (this.items[i] != o.charAt(i))
                    return false;
                i++;
            }
            return true;
        }
        return false;
    }
}
