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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.IntMap;

/**
 * Created by Longri on 27.10.2017.
 */
public class TranslationList {

    private final CharArray STORE = new CharArray();
    private final IntMap<ParameterStringSequence> MAP = new IntMap();

    public void load(FileHandle file) {
        MAP.clear();
        STORE.clear();

        String text = file.readString("UTF-8");

        String[] lines = text.split("\r\n");

        for (String line : lines) {
            int pos;

            // skip empty lines
            if (line == "") {
                continue;
            }

            // skip comment line
            pos = line.indexOf("//");
            if (pos > -1) {
                continue;
            }

            // skip line without value
            pos = line.indexOf("=");
            if (pos == -1) {
                continue;
            }

            String readID = line.substring(0, pos).trim();
            String readTransl = line.substring(pos + 1);
            String replacedRead = readTransl.trim().replace("\\n", String.format("%n"));

            if (replacedRead.endsWith("\"")) {
                replacedRead = replacedRead.substring(0, replacedRead.length() - 1);
            }
            if (replacedRead.startsWith("\"")) {
                replacedRead = replacedRead.substring(1);
            }

            MAP.put(readID.hashCode(), new ParameterStringSequence(STORE, replacedRead));
        }
    }

    public CharSequence get(String id, String... para) {
        return get(id.hashCode(), para);
    }

    public CharSequence get(int id, String... para) {
        ParameterStringSequence pss = MAP.get(id);
        if (pss != null && para.length > 0) {
            pss.replace(para);
        }
        return pss;
    }


    public int length() {
        return MAP.size;
    }
}
