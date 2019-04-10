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

import com.badlogic.gdx.utils.Array;


/**
 * Created by Longri on 28.10.2017.
 */
public class CompoundCharSequence implements CharSequence {


    private final Array<CharSequence> items = new Array<>();

    public CompoundCharSequence(CharSequence... sequences) {
        items.addAll(sequences);
    }

    public void add(CharSequence... sequences) {
        items.addAll(sequences);
    }

    public void add(CharSequence[] para, int start) {
        items.addAll(para, start, para.length - start);
    }


    @Override
    public int length() {
        int length = 0;
        int n = items.size;
        while (n-- > 0) {
            length += items.get(n).length();
        }
        return length;
    }

    @Override
    public char charAt(int index) {
        int actIdx = 0;
        int idx = index;

        while (idx >= items.get(actIdx).length()) {
            idx -= items.get(actIdx++).length();
            if (actIdx > items.size) {
                throw new RuntimeException("Index [" + index + "] out of range: " + length());
            }
        }
        return items.get(actIdx).charAt(idx);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = items.size; i < n; i++) {
            CharSequence item = items.get(i);
            if (item != null) {
                try {
                    String str = item.toString();
                    sb.append(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public CharSequence get(int index) {
        return items.get(index);
    }

    public void set(int index, CharSequence sequence) {
        this.items.set(index, sequence);
    }

    public void clean() {
        //remove all without first
        int end = items.size;
        if (end > 1)
            items.removeRange(1, end - 1);
    }


}
