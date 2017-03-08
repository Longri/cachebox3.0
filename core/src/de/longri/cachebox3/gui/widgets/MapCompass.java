/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.layer.MapOrientationMode;

/**
 * A clickable 3D actor with three states for compass.
 * {NORTH, COMPASS, USER}
 * <p>
 * <p>
 * Created by Longri on 18.11.2016.
 */
public class MapCompass extends Group implements Disposable {

    private final static float MODEL_SCALE = 0.15f;//0.075f;

    private final Actor3D actor3D_north, actor3D_compass, actor3D_user;
    private MapOrientationMode mode = MapOrientationMode.NORTH;
    private Actor3D actor3D_act;
    private float tilt = 0;
    private float orientation = 0;
    private Quaternion tiltQuaternion = new Quaternion();
    private Quaternion orientationQuaternion = new Quaternion();

    public MapCompass(float width, float height) {

        //initial the three mode models
        actor3D_north = new Actor3D("NorthCompass", CB.getSkin().get("compassGrayModel", Model.class));
        actor3D_compass = new Actor3D("MagneticCompass", CB.getSkin().get("compassModel", Model.class));
        actor3D_user = new Actor3D("UserCompass", CB.getSkin().get("compassYellowModel", Model.class));
        actor3D_north.setModelScale(MODEL_SCALE);
        actor3D_compass.setModelScale(MODEL_SCALE);
        actor3D_user.setModelScale(MODEL_SCALE);

        // initialisation with size
        setSize(width, height);

        this.addListener(clickListener);

        setStateModel();
    }

    public void dispose() {
        actor3D_act = null;
        tiltQuaternion = null;
        orientationQuaternion = null;
    }

    private void setStateModel() {
        switch (mode) {
            case NORTH:
                this.addActor(actor3D_north);
                this.removeActor(actor3D_compass);
                this.removeActor(actor3D_user);
                actor3D_act = actor3D_north;
                break;
            case COMPASS:
                this.removeActor(actor3D_north);
                this.addActor(actor3D_compass);
                this.removeActor(actor3D_user);
                actor3D_act = actor3D_compass;
                break;
            case USER:
                this.removeActor(actor3D_north);
                this.removeActor(actor3D_compass);
                this.addActor(actor3D_user);
                actor3D_act = actor3D_user;
                break;
        }
        setModelBounds();
        Gdx.graphics.requestRendering();
    }

    @Override
    public void positionChanged() {
        setModelBounds();
    }

    @Override
    public void sizeChanged() {
        setModelBounds();
    }

    private void setModelBounds() {

        if (actor3D_act == null) {
            actor3D_north.setBounds(0, 0, getWidth(), getHeight());
            actor3D_compass.setBounds(0, 0, getWidth(), getHeight());
            actor3D_user.setBounds(0, 0, getWidth(), getHeight());

            //force position changed event, for recalculate 3D model position on Stage
            actor3D_north.positionChanged();
            actor3D_compass.positionChanged();
            actor3D_user.positionChanged();
        } else {
            actor3D_act.setBounds(0, 0, getWidth(), getHeight());

            //force position changed event, for recalculate 3D model position on Stage
            actor3D_act.positionChanged();
        }


    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
        setRotation();
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
        setRotation();
    }

    private void setRotation() {
        if (actor3D_act != null) {
            tiltQuaternion.set(Vector3.X, tilt);
            orientationQuaternion.set(Vector3.Y, orientation);
            actor3D_act.setQuaternion(tiltQuaternion.mul(orientationQuaternion));
        }
    }

    @Override
    public void draw(Batch batch, float parentColor) {
        super.draw(batch, parentColor);
    }

    public void setUserRotation() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                mode = MapOrientationMode.USER;
                setStateModel();
            }
        });
    }


    ClickListener clickListener = new ClickListener() {

        public void clicked(InputEvent event, float x, float y) {

            if (mode == MapOrientationMode.NORTH) {
                mode = MapOrientationMode.COMPASS;
            } else {
                mode = MapOrientationMode.NORTH;
            }

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    setStateModel();
                }
            });
        }
    };

    public boolean isNorthOriented() {
        return mode == MapOrientationMode.NORTH;
    }

    public boolean isUserRotate() {
        return mode == MapOrientationMode.USER;
    }

    public void setMode(MapOrientationMode mode) {
        this.mode = mode;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                setStateModel();
            }
        });

    }

    public MapOrientationMode getMode() {
        return this.mode;
    }
}
