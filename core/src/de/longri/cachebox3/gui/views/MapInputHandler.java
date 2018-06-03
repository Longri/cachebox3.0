package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import org.oscim.layers.GenericLayer;
import org.oscim.layers.TileGridLayer;
import org.oscim.map.Map;
import org.oscim.map.ViewController;
import org.oscim.theme.VtmThemes;

/**
 * Created by Longri on 12.09.2016.
 */
public class MapInputHandler implements InputProcessor {

    private final ViewController mViewport;
    private final Map map;
    private final MapStateButton mapStateButton;
    private GenericLayer mGridLayer;

    public MapInputHandler(Map map, MapStateButton mapStateButton) {
        this.map = map;
        mViewport = this.map.viewport();
        this.mapStateButton = mapStateButton;
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

            case 63:
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
                mActiveRotate = true;
                mActiveTilt = true;
                mPosX = Gdx.input.getX();
                mPosY = Gdx.input.getY();
                break;

            case Input.Keys.UP:
                mViewport.tiltMap(+10);
                map.updateMap(true);
                break;
            case Input.Keys.DOWN:
                mViewport.tiltMap(-10);
                map.updateMap(true);
                break;
            case Input.Keys.LEFT:
                mViewport.rotateMap(-0.0490873852, 0, 0);
                map.updateMap(true);
                map.events.fire(Map.ROTATE_EVENT, map.getMapPosition());
                break;
            case Input.Keys.RIGHT:
                mViewport.rotateMap(0.0490873852, 0, 0);
                map.updateMap(true);
                map.events.fire(Map.ROTATE_EVENT, map.getMapPosition());
                break;
            case Input.Keys.M:
                mViewport.scaleMap(1.05f, 0, 0);
                map.updateMap(true);
                break;
            case Input.Keys.N:
                mViewport.scaleMap(0.95f, 0, 0);
                map.updateMap(true);
                break;
            case Input.Keys.NUM_1:
                map.animator().animateZoom(500, 0.5, 0, 0);
                map.updateMap(false);
                break;
            case Input.Keys.NUM_2:
                map.animator().animateZoom(500, 2, 0, 0);
                map.updateMap(false);
                break;

            case Input.Keys.D:
                map.setTheme(VtmThemes.DEFAULT);
                map.updateMap(false);
                break;

            case Input.Keys.T:
                map.setTheme(VtmThemes.TRONRENDER);
                map.updateMap(false);
                break;

            case Input.Keys.R:
                map.setTheme(VtmThemes.OSMARENDER);
                map.updateMap(false);
                break;

            case Input.Keys.G:
                if (mGridLayer == null) {
                    mGridLayer = new TileGridLayer(map);
                    mGridLayer.setEnabled(true);
                    map.layers().add(mGridLayer);
                } else {
                    if (mGridLayer.isEnabled()) {
                        mGridLayer.setEnabled(false);
                        map.layers().remove(mGridLayer);
                    } else {
                        mGridLayer.setEnabled(true);
                        map.layers().add(mGridLayer);
                    }
                }
                map.render();
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
            case 63:
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
        if (CB.lastMapState.getMapMode() == MapMode.CAR || CB.lastMapState.getMapMode() == MapMode.LOCK) return true;
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
            rotateByUser();
        }

        if (changed) {
            map.updateMap(true);
        }
        return true;
    }

    public void rotateByUser() {
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

            map.animator().animateZoom(250, 0.75f, 0, 0);
        } else {
            float fx = mPosX - map.getWidth() / 2;
            float fy = mPosY - map.getHeight() / 2;

            map.animator().animateZoom(250, 1.333f, fx, fy);
        }
        map.updateMap(false);

        return true;
    }
}
