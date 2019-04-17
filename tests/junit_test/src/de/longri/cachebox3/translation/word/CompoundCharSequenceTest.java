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
import de.longri.cachebox3.utils.CharSequenceUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 28.10.2017.
 */
class CompoundCharSequenceTest {

    @Test
    void add() {

        String t1 = "Test";
        StringBuilder sb = new StringBuilder();
        sb.append("String");

        MutableString mutable1 = new MutableString(new CharArray(), "Compound");

        String shouldSequence = "CompoundTestString";

        CompoundCharSequence sequence = new CompoundCharSequence();
        sequence.add(mutable1, t1, sb);

        assertThat("Length must charSequenceEquals  '" + shouldSequence.length() + "' : '" + sequence.length() + "'", shouldSequence.length() == sequence.length());
        assertThat("Sequence must charSequenceEquals  '" + shouldSequence + "' : '" + sequence + "'", CharSequenceUtil.equals(shouldSequence, sequence));
        assertThat("toString() must charSequenceEquals  '" + shouldSequence + "' : '" + sequence + "'", shouldSequence.equals(sequence.toString()));


    }
}