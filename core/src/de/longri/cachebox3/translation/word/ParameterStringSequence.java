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
import com.badlogic.gdx.utils.CharArray;

import java.util.Comparator;

/**
 * Created by Longri on 27.10.2017.
 */
public class ParameterStringSequence implements CharSequence {

    private final CharArray store;
    private final StringSequence sequence;
    private final Array<ReplacedStringSequence> replaceArray = new Array();


    public ParameterStringSequence(String string) {
        this(new CharArray(), string);
    }

    public ParameterStringSequence(CharArray storage, String string) {
        this.store = storage;
        int pos = string.indexOf("{");

        if (pos < 0) {
            sequence = new MutableString(this.store, string);
        } else {
            sequence = new MutableString(this.store, string.substring(0, pos));
            do {
                int pos2 = string.indexOf("}", pos) + 1;

                String para = string.substring(pos, pos2);
                ReplacedStringSequence rss = new ReplacedStringSequence(para);
                replaceArray.add(rss);
                sequence.add(rss);
                pos = string.indexOf("{", pos2);
                if (pos < 0) {
                    // add last and break
                    sequence.add(new MutableString(this.store, string.substring(pos2, string.length())));
                    break;
                }
                sequence.add(new MutableString(this.store, string.substring(pos2, pos)));

            } while (pos >= 0);

            replaceArray.sort(new Comparator<ReplacedStringSequence>() {
                @Override
                public int compare(ReplacedStringSequence o1, ReplacedStringSequence o2) {
                    String s1 = o1.toString().replace("{", "").replace("}", "");
                    String s2 = o2.toString().replace("{", "").replace("}", "");

                    int i1 = s1.isEmpty() ? 0 : Integer.parseInt(s1);
                    int i2 = s2.isEmpty() ? 0 : Integer.parseInt(s2);
                    return i1 - i2;
                }
            });
        }
    }


    @Override
    public int length() {
        return sequence.length();
    }

    @Override
    public char charAt(int index) {
        return sequence.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sequence.subSequence(start, end);
    }

    @Override
    public String toString() {
        return sequence.toString();
    }

    public int replace(CharSequence... params) {
        for (int i = 0, n = replaceArray.size; i < n; i++) {
            ReplacedStringSequence rss = replaceArray.get(i);
            CharSequence replacement = i >= params.length ? "" : params[i];
            rss.replace(replacement);
        }
        return replaceArray.size;
    }
}
