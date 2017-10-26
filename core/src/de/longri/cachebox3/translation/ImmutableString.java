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

/**
 * Created by Longri on 26.10.2017.
 */
public class ImmutableString implements StringSequence {

    private StringSequence head, next;
    final String string;

    public ImmutableString(String string) {
        this.string = string;
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
        return this.string.length();
    }

    @Override
    public char charAt(int index) {
        if (index >= this.string.length()) {
            if (next == null) throw new RuntimeException("Index out of range");
            return next.charAt(index - this.string.length());
        }
        return this.string.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.string.subSequence(start, end);
    }

    @Override
    public StringSequence add(CharSequence string) {
        if (this.head == null) this.head = this;
        if (string instanceof MutableString) {
            MutableString mutableString = (MutableString) string;
            mutableString.setHead(this.getHead());
            this.setNext(mutableString);

        } else {
            //create a ImmutableString instance
            ImmutableString immutableString = new ImmutableString(string.toString());
            immutableString.setHead(this.getHead());
            this.setNext(immutableString);
        }
        return this;
    }
}
