/* 
 * Copyright (C) 2013-2016 team-cachebox.de
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

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A Structure for ID as String and this Translation as String
 *
 * @author Longri
 */
public class Translations {

    public final static int lang = "lang".hashCode();
    public final static int Misc = "Misc".hashCode();

    protected static final Charset UTF_8 = Charset.forName("UTF-8");
    private static byte[] TranslationBytes = new byte[2000];
    private static int lastIndex = 0;
    private final int TranslationByteIndex;
    private final short TranslationByteLength;

    /**
     * Constructor
     *
     * @param ID    as String
     * @param Trans as String
     */
    public Translations(String ID, String Trans) {
        this.Id = ID.hashCode();

        byte[] b = Trans.getBytes(UTF_8);
        TranslationByteLength = (short) b.length;

        TranslationByteIndex = lastIndex;
        lastIndex += TranslationByteLength;

        if (TranslationBytes.length < lastIndex)
            ensureCapacity(TranslationByteIndex + TranslationByteLength);
        System.arraycopy(b, 0, TranslationBytes, TranslationByteIndex, TranslationByteLength);
    }

    private void ensureCapacity(int newSize) {
        TranslationBytes = Arrays.copyOf(TranslationBytes, newSize);
    }

    public CompoundCharSequence getTranslation() {
        byte[] b = new byte[TranslationByteLength];
        System.arraycopy(TranslationBytes, TranslationByteIndex, b, 0, TranslationByteLength);
        return new CompoundCharSequence(new String(b, UTF_8));
        // return Translation;
    }

    public int getIdString() {
        return Id;
    }

    private final int Id;

}
