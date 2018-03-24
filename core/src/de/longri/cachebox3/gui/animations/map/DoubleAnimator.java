/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 28.03.2017.
 */
public class DoubleAnimator {

    public final static float DEFAULT_DURATION = 1.0f; // 1000 ms

    private static Logger log = LoggerFactory.getLogger(DoubleAnimator.class);

    private final Interpolation interpolation;
    private double start, end, act;
    private float time;
    private float duration;
    private boolean finish;

    public DoubleAnimator() {
        this.interpolation = null;
        finish = true;
    }

    public DoubleAnimator(Interpolation interpolation) {
        this.interpolation = interpolation;
        finish = true;
    }

    public void start(float duration, double start, double end) {
        this.start(duration, start, end, 0);
    }

    public void start(float duration, double start, double end, double precision) {
        if (equals(start, end, precision)) return;
        log.debug("{} animation", finish ? "start" : "restart");
        this.duration = duration;
        this.start = start;
        this.end = end;
        this.time = 0;
        finish = false;
        if (Gdx.graphics != null) Gdx.graphics.requestRendering();
    }

    public boolean update(float delta) {
        if (finish) return false;
        time += delta;
        boolean complete = time >= duration;
        float percent;
        if (complete) {
            act = end;
            finish = true;
            log.debug("finish animation");
            return true;
        } else {
            percent = time / duration;
            if (interpolation != null) percent = interpolation.apply(percent);
        }
        act = start + (end - start) * percent;
        return true;
    }

    public double getAct() {
        return this.act;
    }

    /**
     * http://stackoverflow.com/questions/356807/java-double-comparison-epsilon
     * <p>
     * Returns true if two doubles are considered equal. Tests if the absolute
     * difference between the two doubles has a difference less then a given
     * double (epsilon). Determining the given epsilon is highly dependant on the
     * precision of the doubles that are being compared.
     *
     * @param a         double to compare.
     * @param b         double to compare
     * @param precision double which is compared to the absolute difference of two
     *                  doubles to determine if they are equal.
     * @return true if a is considered equal to b.
     */
    private static boolean equals(double a, double b, double precision) {
        return a == b || Math.abs(a - b) < precision;
    }


    public void setAct(double value) {
        this.act = value;
        finish = true;
    }

    public boolean isFinish() {
        return finish;
    }
}
