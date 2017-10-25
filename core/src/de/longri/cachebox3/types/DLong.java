/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.types;

import java.io.Serializable;

public class DLong implements Serializable {
    private static final long serialVersionUID = -3369610485833873224L;
    public static final long UL1 = 1l;
    private long low;
    private long high;

    // / <summary>
    // / Constructor for create with lower and higher Long
    // / </summary>
    // / <param name="High">higher Long</param>
    // / <param name="Low">lower Long</param>
    public DLong(long High, long Low) {
        low = Low;
        high = High;
    }

    public long getLow() {
        return low;
    }

    public long getHigh() {
        return high;
    }

    public void setLow(long value) {
        low = value;
    }

    public void setHigh(long value) {
        high = value;
    }

    public static DLong shift(int value) {
        long low = 0;
        long high = 0;

        if (value > 62) {
            high = UL1 << (value - 63);
        } else {
            low = UL1 << value;
        }

        return new DLong(high, low);
    }

    public DLong bitAdd(DLong value) {
        low = this.low + value.getLow();
        high = this.high + value.getHigh();

        return this;
    }

    public DLong BitAnd(DLong value) {
        low = this.low & value.getLow();
        high = this.high & value.getHigh();

        return this;
    }

    public DLong BitOr(DLong value) {
        low = this.low | value.getLow();
        high = this.high | value.getHigh();

        return this;
    }

    public boolean BitAndBiggerNull(DLong value) {
        boolean bLow = (this.low & value.getLow()) > 0;
        boolean bHigh = (this.high & value.getHigh()) > 0;

        return (bLow || bHigh) ? true : false;
    }

    public String toString() {
        StringBuilder Sb = new StringBuilder();

        Sb.append("low =" + String.valueOf(this.low));
        Sb.append("high=" + String.valueOf(this.high));
        Sb.append("high:" + getUInt64BitString(high) + "  low:" + getUInt64BitString(low));
        Sb.append("True Bits[]=" + getTrueArray(low) + getTrueArray(high, 64));

        return Sb.toString();
    }

    private String getUInt64BitString(long value) {
        String bin = Long.toBinaryString(value);

        return bin;
    }

    private String getTrueArray(long value) {
        return getTrueArray(value, 0);
    }

    private String getTrueArray(long value, int add) {
        StringBuilder Sb = new StringBuilder();

        for (int i = 0; i < 64; i++) {
            long mask = UL1 << i;
            if ((mask & value) > 0) {
                Sb.append("[" + String.valueOf((add + i)) + "],");
            }

        }
        return Sb.toString();
    }

    public void reset() {
        this.low = 0l;
        this.high = 0l;
    }
}
