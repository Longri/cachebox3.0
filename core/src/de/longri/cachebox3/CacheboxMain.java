package de.longri.cachebox3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.longri.cachebox3.gui.stages.Splash;
import de.longri.cachebox3.gui.stages.ViewManager;

public class CacheboxMain extends ApplicationAdapter {

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
                        }
                    });
                }
            }
        });
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
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
