/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.desktop;

import ch.fhnw.imvs.gpssimulator.components.LocationPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;

/**
 * Created by Longri on 09.03.18.
 */
public class DesktopMain extends CacheboxMain {

    static DesktopMain main;
    private boolean pause = false;

    public DesktopMain() {
        main = this;
        LocationPanel.pauseResumeInterface = new LocationPanel.PauseResumeInterface() {
            @Override
            public void pause() {
                onPause();
            }

            @Override
            public void resume() {
                onResume();
            }
        };
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    private void onPause() {
        super.pause();
        pause = true;
        CB.requestRendering();
    }

    private void onResume() {
        super.resume();
        pause = false;
        CB.requestRendering();
    }

    @Override
    public void render() {
        super.render();
        if (pause) {
            //Draw Black Overlay
            Batch batch = CB.stageManager.getBatch();
            if (!batch.isDrawing())
                batch.begin();
            Color lastColor = batch.getColor();
            batch.setColor(0.0f, 0.0f, 0.0f, 0.8f);
            batch.draw(FpsInfoSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(lastColor);
            batch.end();

        }
    }
}
