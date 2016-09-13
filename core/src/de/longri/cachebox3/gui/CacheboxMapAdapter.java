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
package de.longri.cachebox3.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import org.oscim.map.Map;

/**
 * Created by Longri on 08.09.2016.
 */
public class CacheboxMapAdapter extends Map {

    /* private */ boolean mRenderWait;
    /* private */ boolean mRenderRequest;
    /* private */ boolean mUpdateRequest;
    private int width = Gdx.graphics.getWidth(), height = Gdx.graphics.getHeight();

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setSize(float newWidth, float newHeight) {
        width = (int) newWidth;
        height = (int) newHeight;
    }

    private final Runnable mRedrawCb = new Runnable() {
        @Override
        public void run() {
            prepareFrame();
            Gdx.graphics.requestRendering();
        }
    };

    @Override
    public void updateMap(boolean forceRender) {
        synchronized (mRedrawCb) {
            if (!mRenderRequest) {
                mRenderRequest = true;
                Gdx.app.postRunnable(mRedrawCb);
            } else {
                mRenderWait = true;
            }
        }
    }

    @Override
    public void render() {
        synchronized (mRedrawCb) {
            mRenderRequest = true;
            if (mClearMap)
                updateMap(false);
            else {
                Gdx.graphics.requestRendering();
            }
        }
    }

    @Override
    public boolean post(Runnable runnable) {
        Gdx.app.postRunnable(runnable);
        return true;
    }

    @Override
    public boolean postDelayed(final Runnable action, long delay) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                action.run();
            }
        }, delay / 1000f);
        return true;
    }

    @Override
    public void beginFrame() {
    }

    @Override
    public void doneFrame(boolean animate) {
        synchronized (mRedrawCb) {
            mRenderRequest = false;
            if (animate || mRenderWait) {
                mRenderWait = false;
                updateMap(true);
            }
        }
    }
}

