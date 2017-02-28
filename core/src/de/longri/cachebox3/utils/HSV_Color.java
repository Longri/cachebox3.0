/*
 * Copyright (C) 2016-2017 team-cachebox.de
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

import com.badlogic.gdx.graphics.Color;

/**
 * Erweitert die LibGdx Color um die HSV Werte
 *
 * @author Longri
 */
public class HSV_Color extends Color {


    private float h;
    private float s;
    private float v;

    public HSV_Color(Color color) {
        super(color);
        clamp();
    }

    /**
     * Constructor for color as Hex String WITHOUT # RGBA or RGB
     *
     * @param hex
     */
    public HSV_Color(String hex) {
        if (hex.length() != 6 && hex.length() != 8)
            throw new IllegalArgumentException("wrong argument: " + hex);

        int values = hex.length() / 2;

        int[] ret = new int[values];
        for (int i = 0; i < values; i++) {
            ret[i] = hexToInt(hex.charAt(i * 2), hex.charAt(i * 2 + 1));
        }

        if (values == 4) {
            r = ret[0] / 255f;
            g = ret[1] / 255f;
            b = ret[2] / 255f;
            a = ret[3] / 255f;
        } else {
            a = 1f;
            r = ret[0] / 255f;
            g = ret[1] / 255f;
            b = ret[2] / 255f;
        }

        clamp();

    }

    /**
     * Constructor
     *
     * @param a Alpha 0-255
     * @param r Red 0-255
     * @param g Green 0-255
     * @param b Blue 0-255
     */
    public HSV_Color(int a, int r, int g, int b) {
        super(r / 255f, g / 255f, b / 255f, a / 255f);
        clamp();
    }

    public HSV_Color(int color) {
        a = ((color & 0xff000000) >>> 24) / 255f;
        r = ((color & 0xff0000) >>> 16) / 255f;
        g = ((color & 0xff00) >>> 8) / 255f;
        b = (color & 0xff) / 255f;
        clamp();
    }

    public HSV_Color(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    private int hexToInt(char c1, char c2) {
        String s = String.valueOf(c1) + String.valueOf(c2);
        int z = Integer.parseInt(s, 16);

        return z;
    }

    @Override
    public Color clamp() {
        Color ret = super.clamp();

        calculateHSV();
        return ret;

    }

    private void calculateHSV() {
        float max = (r > g) ? r : g;
        max = (max > b) ? max : b;

        float min = (r < g) ? r : g;
        min = (min < b) ? min : b;

        float delta;

        v = max; // v

        delta = max - min;

        if (max != 0)
            s = delta / max; // s
        else {
            // r = g = b = 0 // s = 0, v is undefined
            s = 0;
            h = 0;
            return;
        }

        if (r == max)
            h = (g - b) / delta; // between yellow & magenta
        else if (g == max)
            h = 2 + (b - r) / delta; // between cyan & yellow
        else
            h = 4 + (r - g) / delta; // between magenta & cyan

        h *= 60; // degrees
        if (h < 0)
            h += 360;

    }

    public void convertHSVtoRGB() {
        float hue = h / 60;

        int i = (int) Math.floor(hue);
        float f = hue - i; // factorial part of h
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));

        switch (i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:
                r = v;
                g = p;
                b = q;
                break;
        }
    }

    public void setHue(float hue) {
        h = hue;
        convertHSVtoRGB();
    }

    public float getHue() {
        return h;
    }

    public float getSat() {
        return s;
    }

    public float getVal() {
        return v;
    }

    public void setSat(float sat) {
        s = sat;
        convertHSVtoRGB();
    }

    public void setVal(float val) {
        v = val;
        convertHSVtoRGB();
    }

    public int toInt() {
        return (((int) (a * 255f)) & 0xff) << 24 | (((int) (r * 255f)) & 0xff) << 16 | (((int) (g * 255f)) & 0xff) << 8 | (((int) (b * 255f)) & 0xff);
    }

    public void dispose() {
        // nothing to do
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || (getClass() != o.getClass() && !(o instanceof Color)))
            return false;
        Color color = (Color) o;
        return toIntBits() == color.toIntBits();
    }

    public static int colorMatrixManipulation(int c, float[] matrix) {
        int[] color = new int[4];

        color[0] = (c >> 24) & (0xff);
        color[1] = ((c << 8) >> 24) & (0xff);
        color[2] = ((c << 16) >> 24) & (0xff);
        color[3] = ((c << 24) >> 24) & (0xff);

        int R = color[1];
        int G = color[2];
        int B = color[3];
        int A = color[0];

        color[1] = Math.max(0, Math.min(255, (int) ((matrix[0] * R) + (matrix[1] * G) + (matrix[2] * B) + (matrix[3] * A) + matrix[4])));
        color[2] = Math.max(0, Math.min(255, (int) ((matrix[5] * R) + (matrix[6] * G) + (matrix[7] * B) + (matrix[8] * A) + matrix[9])));
        color[3] = Math.max(0, Math.min(255, (int) ((matrix[10] * R) + (matrix[11] * G) + (matrix[12] * B) + (matrix[13] * A) + matrix[14])));
        color[0] = Math.max(0, Math.min(255, (int) ((matrix[15] * R) + (matrix[16] * G) + (matrix[17] * B) + (matrix[18] * A) + matrix[19])));

        return ((color[0] & 0xFF) << 24) | ((color[1] & 0xFF) << 16) | ((color[2] & 0xFF) << 8) | ((color[3] & 0xFF));
    }

    /**
     * Night Color Matrix <br>
     * <br>
     * R= -1.0f, 0.0f, 0.0f, 0.0f, 255.0f, <br>
     * G= 0.0f, -1.5f, 0.0f, 0.0f, 200.0f, <br>
     * B= 0.0f, 0.0f, -1.5f, 0.0f, 0.f, <br>
     * A= 0.0f, 0.0f, 0.0f, 0.0f, 255f <br>
     */
    public static final float[] NIGHT_COLOR_MATRIX = { /* */
            -1.0f, 0.0f, 0.0f, 0.0f, 255.0f, /* */
            0.0f, -1.5f, 0.0f, 0.0f, 200.0f, /* */
            0.0f, 0.0f, -1.5f, 0.0f, 0.f, /* */
            0.0f, 0.0f, 0.0f, 0.0f, 255f};


    public static final float[] GRAYSCALE_COLOR_MATRIX = new float[]{
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0, 0, 0, 1, 0,};


    public static Color colorMatrixManipulation(Color color, float[] nightColorMatrix) {
        return new HSV_Color(colorMatrixManipulation(color.toIntBits(), nightColorMatrix));
    }
}
