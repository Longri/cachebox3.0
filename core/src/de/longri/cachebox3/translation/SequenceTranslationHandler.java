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

import de.longri.cachebox3.translation.word.CompoundCharSequence;

import java.io.IOException;

/**
 * Created by Longri on 28.10.2017.
 */
public class SequenceTranslationHandler extends AbstractTranslationHandler {
    @Override
    public void loadTranslation(String langPath) throws IOException {

    }

    @Override
    public CompoundCharSequence getTranslation(String stringId, CharSequence... params) {
        return null;
    }

    @Override
    public CompoundCharSequence getTranslation(int hashCode, CharSequence... params) {
        return null;
    }

    @Override
    public boolean isInitial() {
        return false;
    }

    @Override
    public String getLangNameFromFile(String path) throws IOException {
        return null;
    }
}
