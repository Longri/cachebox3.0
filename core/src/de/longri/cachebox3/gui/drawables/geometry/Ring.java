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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds the center point, the inner and outer radius of a ring geometry <br>
 * <br>
 * With the method compute() will calculate the vertices and triangle indices.
 *
 * @author Longri
 */
public class Ring implements IGeometry {

    protected float centerX;
    protected float centerY;
    protected float innerRadius;
    protected float outerRadius;
    protected float[] vertices;
    protected short[] triangleIndices;
    protected AtomicBoolean isDisposed = new AtomicBoolean(false);
    /**
     * false, if vertices and triangle indices are calculated for actual circle values;
     */
    protected boolean isDirty = true;

    /**
     * Constructor
     *
     * @param x           center.x
     * @param y           center.y
     * @param innerRadius radius closer the center point
     * @param outerRadius radius away from center point
     */
    public Ring(float x, float y, float innerRadius, float outerRadius) {
        centerX = x;
        centerY = y;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
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
    public Ring(float x, float y, float innerRadius, float outerRadius, boolean compute) {
        this(x, y, innerRadius, outerRadius);
        if (compute)
            compute();
    }

    /**
     * Set the X value of center point.<br>
     * <br>
     * After change the vertices and triangles must new calculated!
     *
     * @param x
     */
    public void setCenterX(float x) {
        centerX = x;
        isDirty = true;
    }

    /**
     * Set the Y value of center point.<br>
     * <br>
     * After change the vertices and triangles must new calculated!
     *
     * @param y
     */
    public void setCenterY(float y) {
        centerY = y;
        isDirty = true;
    }

    /**
     * Set the radius there is closer to the center point of this ring.<br>
     * <br>
     * After change the vertices and triangles must new calculated!
     *
     * @param r Radius
     */
    public void setInnerRadius(float r) {
        innerRadius = r;
        isDirty = true;
    }

    /**
     * Set the radius there is away from the center point of this ring.<br>
     * <br>
     * After change the vertices and triangles must new calculated!
     *
     * @param r Radius
     */
    public void setOuterRadius(float r) {
        outerRadius = r;
        isDirty = true;
    }

    /**
     * Calculate the vertices of this circle with a minimum segment length of 10. <br>
     * OR a minimum segment count of 18. <br>
     * <br>
     * For every segment are compute a triangle from the segment start, end and the center of this circle.
     */
    public void compute() {
        if (!isDirty)
            return; // Nothing to do

        // calculate segment count
        double alpha = (360 * MIN_CIRCLE_SEGMENTH_LENGTH) / (MathUtils.PI2 * outerRadius);
        int segmente = Math.max(MIN_CIRCLE_SEGMENTH_COUNT, (int) (360 / alpha));

        // calculate theta step
        double thetaStep = (MathUtils.PI2 / segmente);

        // initialize arrays
        vertices = new float[(segmente) * 4];
        triangleIndices = new short[(segmente) * 6];


        int index = 0;
        int triangleIndex = 0;
        short verticeIdex = 0;
        boolean beginnTriangles = false;
        for (float i = 0; index < (segmente * 4); i += thetaStep) {
            vertices[index++] = centerX + innerRadius * MathUtils.cos(i);
            vertices[index++] = centerY + innerRadius * MathUtils.sin(i);
            vertices[index++] = centerX + outerRadius * MathUtils.cos(i);
            vertices[index++] = centerY + outerRadius * MathUtils.sin(i);

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

        // last two Triangles
        triangleIndices[triangleIndex++] = verticeIdex++;
        triangleIndices[triangleIndex++] = verticeIdex;
        triangleIndices[triangleIndex++] = 0;

        triangleIndices[triangleIndex++] = 0;
        triangleIndices[triangleIndex++] = 1;
        triangleIndices[triangleIndex++] = verticeIdex;

        isDirty = false;
    }

    @Override
    public void setSize(float width, float height) {
        float ratio = outerRadius / innerRadius;
        setOuterRadius(Math.max(width, height) / 2);
        setInnerRadius(outerRadius * ratio);
        setCenterX(outerRadius);
        setCenterY(outerRadius);
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

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        synchronized (isDisposed) {
            if (isDisposed.get())
                return;
            vertices = null;
            triangleIndices = null;
            isDisposed.set(true);
        }
    }
}
