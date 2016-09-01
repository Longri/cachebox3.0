/* 
 * Copyright (C) 2011-2016 team-cachebox.de
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

package de.longri.cachebox3.locator;

/**
 * Structur f�r die Signal St�rken der Satteliten
 *
 * @author Longri
 */
public class GpsStrength implements Comparable<GpsStrength> {
    /**
     * Constructor
     *
     * @param fixed
     * @param value
     */
    public GpsStrength(boolean fixed, float value) {
        mFixed = fixed;
        mSnr = value;
    }

    private boolean mFixed = false;
    private float mSnr = 0.0f;

    /**
     * Gibt zur�ck ob der Sattelit ein Fix hat
     *
     * @return boolean
     */
    public boolean getFixed() {
        return mFixed;
    }

    /**
     * Gibt die Signal St�rke des Sateliten zur�ck
     *
     * @return float
     */
    public float getStrength() {
        return mSnr;
    }

    @Override
    public int compareTo(GpsStrength c2) {
        int ret = 0;

        if (this.mFixed == c2.mFixed) {
            if (this.mSnr > c2.mSnr) {
                ret = -1;
            } else if (this.mSnr < c2.mSnr) {
                ret = 1;
            }
        } else {
            if (this.mFixed) {
                ret = -1;
            }
        }

        return ret;

    }

}