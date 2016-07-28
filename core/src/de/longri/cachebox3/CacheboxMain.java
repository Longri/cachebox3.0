/*
 * Copyright (C) 2016 team-cachebox.de
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
package de.longri.cachebox3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.longri.cachebox3.gui.stages.Splash;
import de.longri.cachebox3.gui.stages.ViewManager;
import org.slf4j.impl.LibgdxLogger;

import static org.slf4j.impl.LibgdxLogger.DEFAULT_LOG_LEVEL_KEY;

public class CacheboxMain extends ApplicationAdapter {

    static {

        System.setProperty(DEFAULT_LOG_LEVEL_KEY, CB.USED_LOG_LEVEL);
        LibgdxLogger.init();

    }

    SpriteBatch batch;
    Stage stage;

    @Override
    public void create() {
        batch = new SpriteBatch();
        stage = new Splash(new Splash.LoadReady() {
            @Override
            public void ready() {
                // Splash is ready with initialisation
                // now switch Stage to ViewManager
                synchronized (stage) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            stage = new ViewManager();

                            // add stage to input prozessor
                            CB.inputMultiplexer.addProcessor(stage);
                        }
                    });
                }
            }
        });

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(CB.backgroundColor.r, CB.backgroundColor.g, CB.backgroundColor.b, CB.backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        synchronized (stage) {
            stage.draw();
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
