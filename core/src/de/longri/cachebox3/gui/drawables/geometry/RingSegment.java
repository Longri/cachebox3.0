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

/**
 * Holds the center point, the inner and outer radius of a ring geometry and the start and end angle of the segment <br>
 * <br>
 * With the method compute() will calculate the vertices and triangle indices.
 *
 * @author Longri
 */
public class RingSegment extends Ring {

    private float start;
    private float end;

    /**
     * Constructor
     *
     * @param x           center.x
     * @param y           center.y
     * @param innerRadius radius closer the center point
     * @param outerRadius radius away from center point
     */
    public RingSegment(float x, float y, float innerRadius, float outerRadius, float startAngle, float endAngle) {
        super(x, y, innerRadius, outerRadius);
        this.start = startAngle;
        this.end = endAngle;
        chkStartEnd();
    }

    /**
     * Constructor
     *
     * @param x           center.x
     * @param y           center.y
     * @param innerRadius radius closer the center point
     * @param outerRadius radius away from center point
     * @param compute     true for call compute() with constructor
     */
    public RingSegment(float x, float y, float innerRadius, float outerRadius, float startAngle, float endAngle, boolean compute) {
        this(x, y, innerRadius, outerRadius, startAngle, endAngle);
        if (compute)
            compute();
    }

    /**
     * Set the start angle value of this segment.<br>
     * <br>
     * After change the vertices and triangles must new calculated!
     *
     * @param startAngle
     */
    public void setStartAngle(float startAngle) {
        this.start = startAngle;
        chkStartEnd();
        isDirty = true;
    }

    /**
     * Set the end angle value of this segment.<br>
     * <br>
     * After change the vertices and triangles must new calculated!
     *
     * @param endAngle
     */
    public void setEndAngle(float endAngle) {
        this.end = endAngle;
        chkStartEnd();
        isDirty = true;
    }

    private void chkStartEnd() {
        while (start < 0)
            start += 360;

        while (end < 0)
            end += 360;

        while (start > 360)
            start -= 360;

        while (end > 360)
            end -= 360;

    }

    /**
     * Calculate the vertices of this circle with a minimum segment length of 10. <br>
     * OR a minimum segment count of 18. <br>
     * <br>
     * For every segment are compute a triangle from the segment start, end and the center of this circle.
     */
    @Override
    public void compute() {
        if (!isDirty)
            return; // Nothing todo

        // calculate segment count
        double alpha = (360 * MIN_CIRCLE_SEGMENTH_LENGTH) / (MathUtils.PI2 * outerRadius);

        if (start > end) {
            alpha *= -1;
        }

        int segmente = (int) (((end - start) / alpha) + 1);

        // calculate theta step
        double thetaStep = alpha;

        // initialize arrays
        vertices = new float[(segmente + 1) * 4];
        triangleIndices = new short[(segmente) * 6];

        int index = 0;
        int triangleIndex = 0;
        short verticeIdex = 0;
        boolean beginnTriangles = false;
        for (float i = start; index < ((segmente + 1) * 4); i += thetaStep) {
            if (i > end)
                i = end;
            vertices[index++] = centerX + innerRadius * MathUtils.cos(i * MathUtils.degRad);
            vertices[index++] = centerY + innerRadius * MathUtils.sin(i * MathUtils.degRad);
            vertices[index++] = centerX + outerRadius * MathUtils.cos(i * MathUtils.degRad);
            vertices[index++] = centerY + outerRadius * MathUtils.sin(i * MathUtils.degRad);

            if (!beginnTriangles) {
                if (index % 8 == 0)
                    beginnTriangles = true;
            }

            if (beginnTriangles) {
                triangleIndices[triangleIndex++] = verticeIdex++;
                triangleIndices[triangleIndex++] = verticeIdex++;
                triangleIndices[triangleIndex++] = verticeIdex--;

                triangleIndices[triangleIndex++] = verticeIdex++;
                triangleIndices[triangleIndex++] = verticeIdex++;
                triangleIndices[triangleIndex++] = verticeIdex--;
            }

        }

        isDirty = false;
    }

    @Override
    public float[] getVertices() {
        if (isDirty)
            compute();
        return vertices;
    }

    @Override
    public short[] getTriangles() {
        if (isDirty)
            compute();
        return triangleIndices;
    }
}
