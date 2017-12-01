/* 
 * Copyright (C) 2014 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.drawables.geometry;

import com.badlogic.gdx.math.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the center point and radius of a circle geometry and the start and end angle of the segment. <br>
 * <br>
 * With the method compute() will calculate the vertices and triangle indices.
 *
 * @author Longri
 */
public class CircularSegment extends Circle {

    private static final Logger log = LoggerFactory.getLogger(CircularSegment.class);

    private float start;
    private float end;
    private boolean CW = false;

    /**
     * Constructor
     *
     * @param x          center.x
     * @param y          center.y
     * @param r          radius
     * @param startAngle start angle of the segment
     * @param endAngle   end angle of the segment
     */
    public CircularSegment(float x, float y, float r, float startAngle, float endAngle) {
        super(x, y, r);
        setAngles(startAngle, endAngle);
    }

    /**
     * Constructor
     *
     * @param x          center.x
     * @param y          center.y
     * @param r          radius
     * @param startAngle start angle of the segment
     * @param endAngle   end angle of the segment
     * @param compute    true for call compute() with constructor
     */
    public CircularSegment(float x, float y, float r, float startAngle, float endAngle, boolean compute) {
        this(x, y, r, startAngle, endAngle);
        if (compute)
            compute();
    }

    public void setAngles(float startAngle, float endAngle) {
        setAngles(startAngle, endAngle, false);
    }

    public void setAngles(float startAngle, float endAngle, boolean compute) {
        if (startAngle > endAngle) {
            float seg = 360 - (startAngle - endAngle);
            endAngle = startAngle + seg;
            CW = true;
        } else {
            CW = false;
        }
        this.start = startAngle;
        this.end = endAngle;
        isDirty.set(true);
        if (compute)
            compute();
    }

    /**
     * Calculate the vertices of this circle with a minimum segment length of 10. <br>
     * OR a minimum segment count of 18. <br>
     * <br>
     * For every segment are compute a triangle from the segment start, end and the center of this circle.
     */
    @Override
    public void compute() {
        synchronized (isDirty) {

            if (!isDirty.get())
                return; // Nothing to do

            if (start == end) return;

            // calculate segment count
            double alpha = (360 * MIN_CIRCLE_SEGMENTH_LENGTH) / (MathUtils.PI2 * radius);
            int segmente = Math.max(MIN_CIRCLE_SEGMENTH_COUNT, (int) (360 / alpha));

            // calculate begin and end
            float length = end - start;
            segmente = (int) ((segmente * (Math.abs(length) / 360) + 0.6));
            float thetaBegin = start;
            float thetaEnd = end;

            // calculate theta step
            double thetaStep = length / segmente;

            segmente++;

            // initialize arrays
            vertices = new float[(segmente + 1) * 2];
            triangleIndices = new short[(segmente) * 3];

            int index = 0;

            // first point is the center point
            vertices[index++] = centerX;
            vertices[index++] = centerY;

            int triangleIndex = 0;
            short verticeIdex = 1;
            boolean beginnTriangles = false;

            try {
                for (float i = thetaBegin; !(i > thetaEnd + (CW ? thetaStep : 0)); i += thetaStep) {
                    float rad = MathUtils.degreesToRadians * i;

                    if (index >= vertices.length) break;

                    vertices[index++] = centerX + radius * MathUtils.cos(rad);
                    vertices[index++] = centerY + radius * MathUtils.sin(rad);

                    if (!beginnTriangles) {
                        if (index % 6 == 0)
                            beginnTriangles = true;
                    }

                    if (beginnTriangles) {
                        triangleIndices[triangleIndex++] = 0;
                        triangleIndices[triangleIndex++] = verticeIdex++;
                        triangleIndices[triangleIndex++] = verticeIdex;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                vertices = null;
                triangleIndices = null;
            }
            isDirty.set(false);
        }
    }

    @Override
    public float[] getVertices() {
        if (isDirty.get())
            compute();
        return vertices;
    }

    @Override
    public short[] getTriangles() {
        if (isDirty.get())
            compute();
        return triangleIndices;
    }
}
