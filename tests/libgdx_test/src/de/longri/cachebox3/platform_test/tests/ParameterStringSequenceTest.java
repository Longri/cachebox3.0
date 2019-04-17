

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
public class ParameterStringSequenceTest {
    @Test
    public void replace() throws PlatformAssertionError {

        String txt = "Error: Function {1}/Parameter {2} ({3}) is no valid {4}: [{5}]";
        String replaced = "Error: Function first/Parameter second (third) is no valid fourth: [fifth]";

        ParameterStringSequence pss = new ParameterStringSequence(txt);
        assertThat("ParameterStringSequence must charSequenceEquals  '" + txt + "' : '" + pss + "'", CharSequenceUtil.equals(txt, pss));


        pss.replace("first", "second", "third", "fourth", "fifth");
        assertThat("ParameterStringSequence must charSequenceEquals  '" + replaced + "' : '" + pss + "'", CharSequenceUtil.equals(replaced, pss));


        txt = "text {2} Test {1}";
        replaced = "text second Test first";
        pss = new ParameterStringSequence(txt);
        assertThat("ParameterStringSequence must charSequenceEquals  '" + txt + "' : '" + pss + "'", CharSequenceUtil.equals(txt, pss));
        pss.replace("first", "second", "third", "fourth", "fifth");
        assertThat("ParameterStringSequence must charSequenceEquals  '" + replaced + "' : '" + pss + "'", CharSequenceUtil.equals(replaced, pss));


        txt = "text {} Test {}";
        replaced = "text first Test second";
        pss = new ParameterStringSequence(txt);
        assertThat("ParameterStringSequence must charSequenceEquals  '" + txt + "' : '" + pss + "'", CharSequenceUtil.equals(txt, pss));
        pss.replace("first", "second", "third", "fourth", "fifth");
        assertThat("ParameterStringSequence must charSequenceEquals  '" + replaced + "' : '" + pss + "'", CharSequenceUtil.equals(replaced, pss));


        txt = "text ";
        replaced = "text ";
        pss = new ParameterStringSequence(txt);
        assertThat("ParameterStringSequence must charSequenceEquals  '" + txt + "' : '" + pss + "'", CharSequenceUtil.equals(txt, pss));
        pss.replace("first", "second", "third", "fourth", "fifth");
        assertThat("ParameterStringSequence must charSequenceEquals  '" + replaced + "' : '" + pss + "'", CharSequenceUtil.equals(replaced, pss));


        txt = "Error: Function {1}/Parameter {2} ({3}) is no valid {4}: [{5}]";
        replaced = "Error: Function first/Parameter second (third) is no valid : []";
        pss = new ParameterStringSequence(txt);
        assertThat("ParameterStringSequence must charSequenceEquals  '" + txt + "' : '" + pss + "'", CharSequenceUtil.equals(txt, pss));
        pss.replace("first", "second", "third");
        assertThat("ParameterStringSequence must charSequenceEquals  '" + replaced + "' : '" + pss + "'", CharSequenceUtil.equals(replaced, pss));

    }
}
