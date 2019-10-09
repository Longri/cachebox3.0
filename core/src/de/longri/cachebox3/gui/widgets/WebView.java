/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 27.09.2019.
 * <p>
 * WebView is an Actor that displays a platform depended WebView
 */
public class WebView extends Actor implements Disposable {

    Logger log = LoggerFactory.getLogger(WebView.class);

    private boolean DEBUG = true;
    private PlatformDescriptionView view;
    private float lastWidth, lastHeight, lastX, lastY;

    public WebView() {
        showPlatformWebView();
    }

    private void showPlatformWebView() {
        final AtomicBoolean WAIT = new AtomicBoolean(false);
        if (view == null) {
            WAIT.set(true);
            PlatformConnector.getDescriptionView(new GenericCallBack<PlatformDescriptionView>() {
                @Override
                public void callBack(PlatformDescriptionView descriptionView) {
                    view = descriptionView;
//                    view.setShouldOverrideUrlLoadingCallBack(shouldOverrideUrlLoadingCallBack);
                    Vector2 vector = WebView.this.localToStageCoordinates(new Vector2(0, 0));
                    boundsChanged(vector.x, vector.y, WebView.this.getWidth(), WebView.this.getHeight());
                    WAIT.set(false);
                }
            });
        }
        CB.wait(WAIT);
        view.display();
    }

    @Override
    public void dispose() {
        view.close();
    }

    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (DEBUG) {//draw red filled rec
            this.setDebug(true);
        }
    }

    @Override
    public void positionChanged() {
        super.positionChanged();
        Vector2 vector = WebView.this.localToStageCoordinates(new Vector2(0, 0));
        boundsChanged(vector.x, vector.y, this.getWidth(), this.getHeight());
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        Vector2 vector = WebView.this.localToStageCoordinates(new Vector2(0, 0));
        boundsChanged(vector.x, vector.y, this.getWidth(), this.getHeight());
    }


    protected void boundsChanged(float x, float y, float width, float height) {
        if (view != null) {
            view.setBounding(x, y, width, height, Gdx.graphics.getHeight());
        }
    }


    /**
     * Draws this actor's debug lines if {@link #getDebug()} is true and, regardless of {@link #getDebug()}, calls
     * {@link Actor#drawDebug(ShapeRenderer)} on each child.
     */
    public void drawDebug(ShapeRenderer shapes) {
//        shapes.set(ShapeRenderer.ShapeType.Filled);
//        shapes.setColor(Color.CYAN);
//        shapes.rect(this.getX(), this.getY(), this.getOriginX(), this.getOriginY(), this.getWidth(), this.getHeight(),
//                this.getScaleX(), this.getScaleY(), this.getRotation());
        super.drawDebug(shapes);
    }

    public void show() {
        log.debug("show");
        view.display();
        Vector2 vector = WebView.this.localToStageCoordinates(new Vector2(0, 0));
        boundsChanged(vector.x, vector.y, this.getWidth(), this.getHeight());
    }

    public void hide() {
        log.debug("hide");
        view.close();
    }

    public void loadUrl(String url) {
        log.debug("load url: " + url);
        view.loadUrl(url);
    }

    public void setShouldOverrideUrlLoadingCallBack(GenericHandleCallBack<String> callback) {
        view.setShouldOverrideUrlLoadingCallBack(callback);
    }

    public void setFinishLoadingCallBack(GenericHandleCallBack<String> callback) {
        view.setFinishLoadingCallBack(callback);
    }

    public String getContentAsString() {
        return view.getContentAsString();
    }
}
