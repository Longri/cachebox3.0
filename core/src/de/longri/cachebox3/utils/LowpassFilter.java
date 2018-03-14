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


import java.util.ArrayDeque;

/**
 * Created by Longri on 08.06.2017.
 */
public class LowpassFilter {

    private int smooth;

    private float sumSin, sumCos;

    private ArrayDeque<Float> queue = new ArrayDeque<Float>();

    public LowpassFilter(int size) {
        smooth = size;
    }

    /**
     * Returns the average value of all added values
     *
     * @param value
     * @return
     */
    public float add(float value) {
        if (smooth <= 0) return (float) Math.toDegrees(value);
        synchronized (queue) {
            sumSin += (float) Math.sin(value);
            sumCos += (float) Math.cos(value);
            queue.add(value);
            if (queue.size() > smooth) {
                float old = queue.poll();
                sumSin -= Math.sin(old);
                sumCos -= Math.cos(old);
            }
            float retValue = (float) Math.toDegrees(Math.atan2(sumSin / queue.size(), sumCos / queue.size()));
            return retValue;
        }
    }

    public void changeSmoothValue(int value) {
        synchronized (queue) {
            if (value < smooth) {
                while (queue.size() > value) {
                    float old = queue.poll();
                    sumSin -= Math.sin(old);
                    sumCos -= Math.cos(old);
                }
            }
            smooth = value;
        }
    }
}
