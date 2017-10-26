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

import com.badlogic.gdx.utils.CharArray;

/**
 * Created by Longri on 25.10.17.
 */
public class MutableString implements CharSequence {


    private final CharArray storage;
    private int ptr;
    private int length;

    public MutableString(CharArray storage, CharSequence string) {
        this.storage = storage;
        ptr = storage.size;
        length = string.length();

        if (string instanceof MutableString) {
            MutableString mutableString = (MutableString) string;
            if (storage == mutableString.storage) {
                //same storage, so copy pointer
                ptr = mutableString.ptr;
            } else {
                // add char[]
                this.storage.addAll(mutableString.storage.items, mutableString.ptr, length);
            }
        } else if (string instanceof String) {
            char[] arr = ((String) string).toCharArray();
            this.storage.addAll(arr);
        } else {
            //copy char by char
            for (int i = 0; i < length; i++) {
                this.storage.add(string.charAt(i));
            }
        }
    }

    private MutableString(CharArray storage, int ptr, int length) {
        this.storage = storage;
        this.ptr = ptr;
        this.length = length;
    }


    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return storage.get(ptr + index);
    }

    @Override
    public MutableString subSequence(int start, int end) {
        // we must not store this sub, only set new values for ptr and length
        return new MutableString(this.storage, this.ptr + start, end - start);
    }

    @Override
    public String toString() {
        char[] chars = new char[this.length];
        System.arraycopy(this.storage.items, this.ptr, chars, 0, this.length);
        return new String(chars);
    }
}
