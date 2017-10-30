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
public class WordStore {

    protected final CharArray storage;
    private final boolean asSingleWord;

    public WordStore() {
        this(false);
    }

    public WordStore(boolean splitToWords) {
        this.storage = new CharArray();
        this.asSingleWord = splitToWords;
    }

    public StringSequence add(String string) {

        if (asSingleWord) {
            String[] split = string.split(" ");
            MutableString mutableString = addWord(split[0]);
            if (split.length > 1) {
                mutableString.add(new Space());
                count++;
            }
            for (int i = 1, n = split.length, m = n - 1; i < n; i++) {
                mutableString.add(addWord(split[i]));
                if (i < m) {
                    mutableString.add(new Space());
                    count++;
                }
            }
            return mutableString;
        } else {
            return addWord(string);
        }
    }

    public static int count = 0;

    private MutableString addWord(String word) {
        // check if storage contains the word
        int pos = indexOf(storage.items, 0, storage.size, word.toCharArray(), 0, word.length(), 0);
        count++;
        if (pos < 0) {
            //create and add a new
            return new MutableString(storage, word);
        }
        return new MutableString(storage, pos, word.length());
    }

    static int indexOf(char[] source, int sourceOffset, int sourceCount,
                       char[] target, int targetOffset, int targetCount,
                       int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++)
                    ;

                if (j == end) {
                    /* Found whole storage. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    public static class Space implements StringSequence {

        protected StringSequence head, next;
        protected final static char SPACE = ' ';

        @Override
        public void setHead(StringSequence string) {
            head = string;
        }

        @Override
        public void setNext(StringSequence string) {
            next = string;
        }

        @Override
        public StringSequence getHead() {
            return head;
        }

        @Override
        public StringSequence getNext() {
            return next;
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
        public int getSequenceLength() {
            return 1;
        }

        @Override
        public int length() {
            if (next == null)
                return 1;

            StringSequence act = this;
            int l = 1;
            while (act.getNext() != null) {
                act = act.getNext();
                l += act.getSequenceLength();
            }
            return l;
        }

        @Override
        public char charAt(int index) {
            if (index >= 1) {
                if (next == null) throw new RuntimeException("Index out of range");
                return next.charAt(index - 1);
            }
            return SPACE;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new RuntimeException("can't return a sub sequence from single char ");
        }

        @Override
        public String toString() {
            char[] chars = new char[this.length()];

            if (next == null) {
                return " ";
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
    }

}
