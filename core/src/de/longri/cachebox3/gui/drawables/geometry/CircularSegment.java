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
 * Holds the center point and radius of a circle geometry and the start and end angle of the segment. <br>
 * <br>
 * With the method compute() will calculate the vertices and triangle indices.
 * 
 * @author Longri
 */
public class CircularSegment extends Circle {
	private float start;
	private float end;

	/**
	 * Constructor
	 * 
	 * @param x
	 *            center.x
	 * @param y
	 *            center.y
	 * @param r
	 *            radius
	 * @param startAngle
	 *            start angle of the segment
	 * @param endAngle
	 *            end angle of the segment
	 */
	public CircularSegment(float x, float y, float r, float startAngle, float endAngle) {
		super(x, y, r);
		this.start = startAngle;
		this.end = endAngle;
		chkStartEnd();
	}

	/**
	 * Constructor
	 * 
	 * @param x
	 *            center.x
	 * @param y
	 *            center.y
	 * @param r
	 *            radius
	 * @param startAngle
	 *            start angle of the segment
	 * @param endAngle
	 *            end angle of the segment
	 * @param compute
	 *            true for call compute() with constructor
	 */
	public CircularSegment(float x, float y, float r, float startAngle, float endAngle, boolean compute) {
		this(x, y, r, startAngle, endAngle);
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

		while (end <= 0)
			end += 360;

		while (start >= 360)
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
			return; // Nothing to do

		chkStartEnd();

		// calculate segment count
		double alpha = (360 * MIN_CIRCLE_SEGMENTH_LENGTH) / (MathUtils.PI2 * radius);
		int segmente = Math.max(MIN_CIRCLE_SEGMENTH_COUNT, (int) (360 / alpha));

		// calculate beginn and end
		float length = end - start;
		segmente = (int) ((segmente * (Math.abs(length) / 360) + 0.5));
		float thetaBeginn = start;
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

		for (float i = thetaBeginn; !(i > thetaEnd); i += thetaStep) {

			float rad = MathUtils.degreesToRadians * i;

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

		// last triangle
		// triangleIndices[triangleIndex++] = 0;
		// triangleIndices[triangleIndex++] = verticeIdex;
		// triangleIndices[triangleIndex++] = 1;

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
