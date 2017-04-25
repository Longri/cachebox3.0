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
package de.longri.cachebox3.gui.drawables;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Created by longri on 22.04.17.
 */
public abstract class AbstractAnimationDrawable extends EmptyDrawable {

    protected float animationTime = 0;

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        animationTime += Gdx.graphics.getDeltaTime();
        drawAnimation(batch, x, y, width, height);
    }

    public abstract void drawAnimation(Batch batch, float x, float y, float width, float height);
}
