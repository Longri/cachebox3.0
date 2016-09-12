package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import org.oscim.gdx.GdxMap;
import org.oscim.layers.GenericLayer;
import org.oscim.layers.TileGridLayer;
import org.oscim.map.Map;
import org.oscim.map.ViewController;
import org.oscim.theme.VtmThemes;

/**
 * Created by Longri on 12.09.2016.
 */
public class MapInputHandler implements InputProcessor {

    private ViewController mViewport;
    private final Map mMap;
    private GenericLayer mGridLayer;

    public MapInputHandler(Map map) {
        mMap = map;
        mViewport = mMap.viewport();
    }

    private boolean mActiveScale;
    private boolean mActiveTilt;
    private boolean mActiveRotate;

    private int mPosX, mPosY;

    @Override
    public boolean keyDown(int keycode) {

        switch (keycode) {
            case Input.Keys.ESCAPE:
                Gdx.app.exit();
                break;

            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                mActiveScale = true;
                mPosY = Gdx.input.getY();
                break;

            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
                mActiveRotate = true;
                mActiveTilt = true;
                mPosX = Gdx.input.getX();
                mPosY = Gdx.input.getY();
                break;

            case Input.Keys.UP:
                mViewport.moveMap(0, -50);
                mMap.updateMap(true);
                break;
            case Input.Keys.DOWN:
                mViewport.moveMap(0, 50);
                mMap.updateMap(true);
                break;
            case Input.Keys.LEFT:
                mViewport.moveMap(-50, 0);
                mMap.updateMap(true);
                break;
            case Input.Keys.RIGHT:
                mViewport.moveMap(50, 0);
                mMap.updateMap(true);
                break;
            case Input.Keys.M:
                mViewport.scaleMap(1.05f, 0, 0);
                mMap.updateMap(true);
                break;
            case Input.Keys.N:
                mViewport.scaleMap(0.95f, 0, 0);
                mMap.updateMap(true);
                break;
            case Input.Keys.NUM_1:
                mMap.animator().animateZoom(500, 0.5, 0, 0);
                mMap.updateMap(false);
                break;
            case Input.Keys.NUM_2:
                mMap.animator().animateZoom(500, 2, 0, 0);
                mMap.updateMap(false);
                break;

            case Input.Keys.D:
                mMap.setTheme(VtmThemes.DEFAULT);
                mMap.updateMap(false);
                break;

            case Input.Keys.T:
                mMap.setTheme(VtmThemes.TRONRENDER);
                mMap.updateMap(false);
                break;

            case Input.Keys.R:
                mMap.setTheme(VtmThemes.OSMARENDER);
                mMap.updateMap(false);
                break;

            case Input.Keys.G:
                if (mGridLayer == null) {
                    mGridLayer = new TileGridLayer(mMap);
                    mGridLayer.setEnabled(true);
                    mMap.layers().add(mGridLayer);
                } else {
                    if (mGridLayer.isEnabled()) {
                        mGridLayer.setEnabled(false);
                        mMap.layers().remove(mGridLayer);
                    } else {
                        mGridLayer.setEnabled(true);
                        mMap.layers().add(mGridLayer);
                    }
                }
                mMap.render();
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                mActiveScale = false;
                break;
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
                mActiveRotate = false;
                mActiveTilt = false;
                break;

        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            mActiveScale = true;
            mPosY = screenY;
        } else if (button == Input.Buttons.RIGHT) {
            mActiveRotate = true;
            mPosX = screenX;
            mPosY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        mActiveScale = false;
        mActiveRotate = false;
        mActiveTilt = false;

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean changed = false;

        if (!(mActiveScale || mActiveRotate || mActiveTilt))
            return false;

        if (mActiveTilt) {
            changed = mViewport.tiltMap((screenY - mPosY) / 5f);
            mPosY = screenY;

        }

        if (mActiveScale) {
            changed = mViewport.scaleMap(1 - (screenY - mPosY) / 100f, 0, 0);
            mPosY = screenY;
        }

        if (mActiveRotate) {
            mViewport.rotateMap((screenX - mPosX) / 500f, 0, 0);
            mPosX = screenX;
            mViewport.tiltMap((screenY - mPosY) / 10f);
            mPosY = screenY;
            changed = true;
        }

        if (changed) {
            mMap.updateMap(true);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        mPosX = screenX;
        mPosY = screenY;
        return false;
    }

    @Override
    public boolean scrolled(int amount) {

        if (amount > 0) {

            mMap.animator().animateZoom(250, 0.75f, 0, 0);
        } else {
            float fx = mPosX - mMap.getWidth() / 2;
            float fy = mPosY - mMap.getHeight() / 2;

            mMap.animator().animateZoom(250, 1.333f, fx, fy);
        }
        mMap.updateMap(false);

        return true;
    }
}
