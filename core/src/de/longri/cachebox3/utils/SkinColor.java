/*
 * Copyright (C) 2017 team-cachebox.de
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
 * Created by Longri on 13.01.2017.
 */
public class SkinColor extends HSV_Color {

    public String skinName;


    public SkinColor() {
        super(0f, 0f, 0f, 0f);
    }

    public SkinColor(Color color) {
        super(color);
    }

    public SkinColor(String hex) {
        super(hex);
    }

    public SkinColor(int a, int r, int g, int b) {
        super(a, r, g, b);
    }

    public SkinColor(int color) {
        super(color);
    }

    public SkinColor(float r, float g, float b, float a) {
        super(r, g, b, a);
    }
}
