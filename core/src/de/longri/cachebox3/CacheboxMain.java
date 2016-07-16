package de.longri.cachebox3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.longri.cachebox3.gui.stages.Splash;

public class CacheboxMain extends ApplicationAdapter {
    SpriteBatch batch;


    Splash splash;

    @Override
    public void create() {
        batch = new SpriteBatch();

        splash = new Splash();

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        splash.draw();
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();

    }
}
