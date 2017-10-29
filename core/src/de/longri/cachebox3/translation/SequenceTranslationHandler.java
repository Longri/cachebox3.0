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

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.translation.word.TranslationList;
import de.longri.cachebox3.utils.CharSequenceUtil;

import java.io.IOException;

/**
 * Created by Longri on 28.10.2017.
 */
public class SequenceTranslationHandler extends AbstractTranslationHandler {

    private final TranslationList DEFAULT = new TranslationList();
    private final TranslationList translations = new TranslationList();


    public SequenceTranslationHandler(FileHandle workPath, String defaultLang) {
        super(workPath, defaultLang);
    }

    @Override
    public void loadTranslation(String langPath) throws IOException {
        if (!isInitial()) {
            //load default
            DEFAULT.load(defaultLang);
        }
        translations.load(workPath.child(langPath + "/strings.ini"));
    }

    @Override
    public CompoundCharSequence getTranslation(String stringId, CharSequence... params) {
        CompoundCharSequence ret = getTranslation(stringId.hashCode(), params);
        if (CharSequenceUtil.startsWith(ret, "$ID:")) {
            // replace ID hashCode with string
            ret.set(1, stringId);
        }
        return ret;
    }

    @Override
    public CompoundCharSequence getTranslation(int hashCode, CharSequence... params) {
        CompoundCharSequence trans = translations.get(hashCode, params);

        if (trans == null) {
            trans = DEFAULT.get(hashCode, params);
        }

        if (trans == null) {
            trans = new CompoundCharSequence("$ID:", Integer.toString(hashCode));
        }

        return trans;
    }

    @Override
    public boolean isInitial() {
        return DEFAULT.length() < 0;
    }

    @Override
    public String getLangNameFromFile(String path) throws IOException {
        return null;
    }

    public int getCount() {
        return DEFAULT.length();
    }
}
