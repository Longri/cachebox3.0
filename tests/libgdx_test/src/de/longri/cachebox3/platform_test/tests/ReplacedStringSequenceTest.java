

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.translation.word.*;

import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 27.10.2017.
 */
public class ReplacedStringSequenceTest {
    @Test
    public void constructor() throws PlatformAssertionError {

        String t1 = "TestString";
        StringBuilder sb = new StringBuilder();
        sb.append(t1);

        ReplacedStringSequence mutable1 = new ReplacedStringSequence(t1);
        ReplacedStringSequence mutable2 = new ReplacedStringSequence(mutable1);
        ReplacedStringSequence mutable3 = new ReplacedStringSequence(sb);

        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable1));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable2));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable3));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable1, mutable2));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable1, mutable3));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable2, mutable3));

    }

    @Test
    public void length() throws PlatformAssertionError {
        String t1 = "TestString";
        ReplacedStringSequence mutable1 = new ReplacedStringSequence(t1);
        ReplacedStringSequence mutable2 = new ReplacedStringSequence(mutable1);

        assertThat("Length must: " + t1.length() + "but was: " + mutable1.length(), t1.length() == mutable1.length());
        assertThat("Length must: " + t1.length() + "but was: " + mutable2.length(), t1.length() == mutable2.length());
    }

    @Test
    public void charAt() throws PlatformAssertionError {
        String t1 = "TestString";
        ReplacedStringSequence mutable1 = new ReplacedStringSequence(t1);
        ReplacedStringSequence mutable2 = new ReplacedStringSequence(mutable1);

        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable1));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable2));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable1, mutable2));
    }

    @Test
    public void subSequence() throws PlatformAssertionError {
        String t1 = "TestString";
        CharSequence sub = t1.subSequence(4, 10);
        ReplacedStringSequence mutable1 = new ReplacedStringSequence(t1);
        ReplacedStringSequence mutable2 = new ReplacedStringSequence(mutable1);

        CharSequence sub1 = mutable1.subSequence(4, 10);
        CharSequence sub2 = mutable2.subSequence(4, 10);

        assertThat("MutableSubString must charSequenceEquals", CharSequenceUtil.equals(sub, sub1));
        assertThat("MutableSubString must charSequenceEquals", CharSequenceUtil.equals(sub, sub2));
    }


    @Test
    public void toStringTest() throws PlatformAssertionError {
        String t1 = "TestString";
        ReplacedStringSequence mutable1 = new ReplacedStringSequence(t1);
        ReplacedStringSequence mutable2 = new ReplacedStringSequence(mutable1);

        assertThat("ReplacedStringSequence must charSequenceEquals", t1.equals(mutable1.toString()));
        assertThat("ReplacedStringSequence must charSequenceEquals", t1.equals(mutable2.toString()));
        assertThat("ReplacedStringSequence must charSequenceEquals", mutable1.toString().equals(mutable2.toString()));

        assertThat("ReplacedStringSequence must charSequenceEquals", mutable1.toString().equals(t1));
        assertThat("ReplacedStringSequence must charSequenceEquals", mutable2.toString().equals(t1));
        assertThat("ReplacedStringSequence must charSequenceEquals", mutable1.toString().equals(mutable2.toString()));
    }


    @Test
    public void replace() throws PlatformAssertionError {
        String t1 = "TestString";
        StringBuilder sb = new StringBuilder(t1);

        ReplacedStringSequence mutable1 = new ReplacedStringSequence(t1);
        ReplacedStringSequence mutable2 = new ReplacedStringSequence(mutable1);
        ReplacedStringSequence mutable3 = new ReplacedStringSequence(sb);

        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable1));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable2));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(t1, mutable3));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable1, mutable2));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable1, mutable3));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable2, mutable3));

        String replaceString = "Replaced string";
        StringBuilder sb2 = new StringBuilder(replaceString);

        mutable1.replace(replaceString);
        mutable2.replace(mutable1);
        mutable3.replace(sb2);

        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(replaceString, mutable1));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(replaceString, mutable2));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(replaceString, mutable3));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable1, mutable2));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable1, mutable3));
        assertThat("ReplacedStringSequence must charSequenceEquals", CharSequenceUtil.equals(mutable2, mutable3));

    }

}
