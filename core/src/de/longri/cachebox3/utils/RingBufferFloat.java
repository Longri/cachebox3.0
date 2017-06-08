/*
 * Copyright (C)  2017 team-cachebox.de
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
package de.longri.cachebox3.utils;


/**
 * Created by Longri on 08.06.2017.
 */
public class RingBufferFloat {

    private final float[] items;
    private int pointer = 0;
    private boolean full = false;

    public RingBufferFloat(int size) {
        items = new float[size];
    }

    /**
     * Returns the average value of all added values
     * @param value
     * @return
     */
    public float add(float value) {
        items[pointer++] = value;
        if (pointer > items.length - 1) {
            pointer = 0;
            full = true;
        }

        float sum = items[0];
        for (int i = 1, n = items.length - 1; i < n; i++)
            sum += items[i];
        return sum / (full ? items.length : pointer);
    }

}
