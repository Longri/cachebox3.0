

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

import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 10.03.2017.
 */
public class MathUtilsTest {

    static {
        TestUtils.initialGdx();
    }

    final float[] recBB = new float[]{242, 390, -242, 390, -242, -390, 242, -390, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};


    @Test
    public void clampLineToIntersectRect() throws PlatformAssertionError {
        //setLine inside
        recBB[8] = 77;
        recBB[9] = -46;
        recBB[10] = -119;
        recBB[11] = 165;

        int ret = MathUtils.clampLineToIntersectRect(recBB, 0, 8, 12, 16);
        assertThat("ret must -1 no intersection, line complete inside", ret == -1);

        recBB[8] = -120;
        recBB[9] = 3;
        recBB[10] = -515;
        recBB[11] = 428;

        ret = MathUtils.clampLineToIntersectRect(recBB, 0, 8, 12, 16);
        assertThat("ret must 1 only one intersection", ret == 1);

        recBB[8] = 475;
        recBB[9] = -355;
        recBB[10] = 80;
        recBB[11] = 69;

        ret = MathUtils.clampLineToIntersectRect(recBB, 0, 8, 12, 16);
        assertThat("ret must 1 only one intersection", ret == 1);

        recBB[8] = 86;
        recBB[9] = -527;
        recBB[10] = -308;
        recBB[11] = -102;

        ret = MathUtils.clampLineToIntersectRect(recBB, 0, 8, 12, 16);
        assertThat("ret must 2, with two intersection", ret == 2);


        recBB[8] = -101;
        recBB[9] = -818;
        recBB[10] = -496;
        recBB[11] = -393;

        ret = MathUtils.clampLineToIntersectRect(recBB, 0, 8, 12, 16);
        assertThat("ret must 0, without any intersection", ret == 0);

    }

    @Test
    public void clampToMinMax() throws PlatformAssertionError {

        short maxValue = 250;
        final float[] bb = recBB.clone();

        for (int i = 0, n = 8; i < n; i++)
            MathUtils.clampToMinMax(bb, i, maxValue);

        assertThat("must not clump", bb[0] == 242);
        assertThat("must clump to 250", bb[1] == 250);
        assertThat("must not clump", bb[2] == -242);
        assertThat("must clump to 250", bb[3] == 250);
        assertThat("must not clump", bb[4] == -242);
        assertThat("must clump to -250", bb[5] == -250);
        assertThat("must not clump", bb[6] == 242);
        assertThat("must clump to -250", bb[7] == -250);

    }

}
