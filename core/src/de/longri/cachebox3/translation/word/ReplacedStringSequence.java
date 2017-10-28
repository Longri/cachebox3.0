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
public class ReplacedStringSequence implements StringSequence {

    private StringSequence head, next;
    final CharArray storage = new CharArray();

    public ReplacedStringSequence(String string) {
        this.storage.addAll(string.toCharArray());
    }

    public ReplacedStringSequence(ReplacedStringSequence string) {
        this.storage.addAll(string.storage.shrink());
    }

    public ReplacedStringSequence(StringBuilder stringBuilder) {
        this.storage.addAll(stringBuilder.toString().toCharArray());
    }

    public void replace(String string) {
        this.storage.clear();
        this.storage.addAll(string.toCharArray());
    }

    public void replace(ReplacedStringSequence string) {
        this.storage.clear();
        this.storage.addAll(string.storage.shrink());
    }

    public void replace(StringBuilder stringBuilder) {
        this.storage.clear();
        this.storage.addAll(stringBuilder.toString().toCharArray());
    }

    public void replace(CharSequence sequence) {
        this.storage.clear();
        this.storage.addAll(sequence.toString().toCharArray());
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
        return head;
    }

    @Override
    public StringSequence getNext() {
        return next;
    }

    @Override
    public int length() {
        if (next == null)
            return getSequenceLength();

        StringSequence act = this;
        int l = getSequenceLength();
        while (act.getNext() != null) {
            act = act.getNext();
            l += act.getSequenceLength();
        }
        return l;
    }

    @Override
    public int getSequenceLength() {
        return this.storage.size;
    }

    @Override
    public char charAt(int index) {
        if (index >= this.storage.size) {
            if (next == null) throw new RuntimeException("Index out of range");
            return next.charAt(index - this.storage.size);
        }
        return this.storage.get(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        int length = end - start;
        char[] chars = new char[length];
        System.arraycopy(this.storage.items, start, chars, 0, length);
        return new String(chars);
    }

    @Override
    public StringSequence add(CharSequence string) {
        if (this.head == null) this.head = this;
        if (string instanceof MutableString) {
            MutableString mutableString = (MutableString) string;
            mutableString.setHead(this.getHead());
            this.setNext(mutableString);

        } else {
            //create a ReplacedStringSequence instance
            ReplacedStringSequence immutableString = new ReplacedStringSequence(string.toString());
            immutableString.setHead(this.getHead());
            this.setNext(immutableString);
        }
        return this;
    }

    @Override
    public String toString() {
        return new String(this.storage.shrink());
    }

}