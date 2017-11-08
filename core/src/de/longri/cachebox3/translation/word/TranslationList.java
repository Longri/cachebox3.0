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
    private final IntMap<CompoundCharSequence> MAP = new IntMap();

    public void load(FileHandle file) {
        MAP.clear();
        STORE.clear();

        String text = file.readString("UTF-8");

        String[] lines = text.split("\n");

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
            String replacedRead = readTransl.trim().replace("\\n", Character.toString('\n'));

            if (replacedRead.endsWith("\"")) {
                replacedRead = replacedRead.substring(0, replacedRead.length() - 1);
            }
            if (replacedRead.startsWith("\"")) {
                replacedRead = replacedRead.substring(1);
            }


            ParameterStringSequence pss = new ParameterStringSequence(STORE, replacedRead);
            CompoundCharSequence compoundCharSequence = new CompoundCharSequence(pss);

            MAP.put(readID.hashCode(), compoundCharSequence);
        }
    }

    public CompoundCharSequence get(String id, CharSequence... para) {
        return get(id.hashCode(), para);
    }

    public CompoundCharSequence get(int id, CharSequence... para) {

        CompoundCharSequence compound = MAP.get(id);

        if (compound == null) return null;
        compound.clean();

        ParameterStringSequence pss = (ParameterStringSequence) compound.get(0);
        if (pss != null && para.length > 0) {
            int replaceCount = pss.replace(para);
            if (replaceCount < para.length) {
                //add not replaced paras
                compound.add(para, replaceCount);
            }
        }
        return compound;
    }


    public int length() {
        return MAP.size;
    }
}
