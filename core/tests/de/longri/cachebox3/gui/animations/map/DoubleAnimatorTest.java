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
package de.longri.cachebox3.gui.animations.map;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 28.03.2017.
 */
class DoubleAnimatorTest {


    @Test
    void update() {
        DoubleAnimator animator = new DoubleAnimator(); // linear
        animator.start(10, 0, 1);

        assertEquals(0.1, animator.update(1), 0.00001, "Value must 0.1");
        assertEquals(0.2, animator.update(1), 0.00001, "Value must 0.2");
        assertEquals(0.3, animator.update(1), 0.00001, "Value must 0.3");
        assertEquals(0.4, animator.update(1), 0.00001, "Value must 0.4");
        assertEquals(0.5, animator.update(1), 0.00001, "Value must 0.5");
        assertEquals(0.6, animator.update(1), 0.00001, "Value must 0.6");
        assertEquals(0.7, animator.update(1), 0.00001, "Value must 0.7");
        assertEquals(0.8, animator.update(1), 0.00001, "Value must 0.8");
        assertEquals(0.9, animator.update(1), 0.00001, "Value must 0.9");
        assertEquals(1.0, animator.update(1), 0.00001, "Value must 1.0");


    }

}