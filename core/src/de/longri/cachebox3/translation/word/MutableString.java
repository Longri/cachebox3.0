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
 * Created by Longri on 25.10.17.
 */
public class MutableString implements StringSequence {

    protected StringSequence head, next;
    protected final CharArray storage;
    protected int ptr;
    protected int length;

    public MutableString(CharArray storage, CharSequence string) {
        this.storage = storage;
        if (string == null) return;

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

    public MutableString(CharArray storage, int ptr, int length) {
        this.storage = storage;
        this.ptr = ptr;
        this.length = length;
    }

    public MutableString(MutableString mutableString) {
        this(mutableString.storage, mutableString.ptr, mutableString.length);
    }

    @Override
    public int length() {
        if (next == null)
            return length;

        StringSequence act = this;
        int l = this.length;
        while (act.getNext() != null) {
            act = act.getNext();
            l += act.getSequenceLength();
        }
        return l;
    }

    @Override
    public int getSequenceLength() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if (index >= length) {
            if (next == null) throw new RuntimeException("Index out of range");
            return next.charAt(index - length);
        }
        return storage.get(ptr + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (next == null) {
            // we must not store this sub, only set new values for ptr and length
            return new MutableString(this.storage, this.ptr + start, end - start);
        }
        return toString().subSequence(start, end);
    }

    @Override
    public StringSequence add(CharSequence string) {
        if (this.head == null) this.head = this;

        StringSequence last = this;
        while (last.getNext() != null) {
            last = last.getNext();
        }
        StringSequence addSequence;

        if (string instanceof MutableString) {
            addSequence = (MutableString) string;

            if (addSequence.getNext() != null) {
                //MutableString is in use of other StringSequence, so create a copy
                addSequence = new MutableString((MutableString) string);
            }

        } else if (string instanceof ReplacedStringSequence) {
            addSequence = (StringSequence) string;
        } else {
            //create a ReplacedStringSequence instance
            addSequence = new ReplacedStringSequence(string.toString());
            addSequence.setHead(this.getHead());
        }
        addSequence.setHead(this.getHead());
        last.setNext(addSequence);
        return this.head;
    }


    @Override
    public String toString() {
        char[] chars = new char[this.length()];

        if (next == null) {
            System.arraycopy(this.storage.items, this.ptr, chars, 0, this.length);
        } else {
            StringSequence act = this;
            int dest = 0;
            do {
                if (act instanceof MutableString) {
                    MutableString mutableString = (MutableString) act;
                    System.arraycopy(mutableString.storage.items, mutableString.ptr, chars, dest, mutableString.length);
                } else if (act instanceof ReplacedStringSequence) {
                    char[] arr = ((ReplacedStringSequence) act).storage.shrink();
                    System.arraycopy(arr, 0, chars, dest, arr.length);
                } else {
                    throw new RuntimeException("Class '" + act.getClass() + "' not implemented");
                }
                dest += act.getSequenceLength();
                act = act.getNext();
            } while (act != null);
        }
        return new String(chars);
    }

    @Override
    public void setHead(StringSequence string) {
        this.head = string;
    }

    @Override
    public void setNext(StringSequence string) {
        this.next = string;
    }

    @Override
    public StringSequence getHead() {
        return this.head;
    }

    @Override
    public StringSequence getNext() {
        return this.next;
    }
}
