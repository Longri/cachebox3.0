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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draw a 3D Model at Position of an Actor
 * <p>
 * Created by Longri on 06.11.16.
 */
public class Actor3D extends Actor {

    final static Logger log = LoggerFactory.getLogger(Actor3D.class);

    /**
     * myLocationModel batch must set from initialisation
     */
    public static ModelBatch modelBatch;

    private Model model;
    private ModelInstance modelInstance;
    private PerspectiveCamera cam;
    private Environment environment;
    private final String name;

    private float worldX, worldY, modelScale = 1;

    private Drawable background;

    public Actor3D(String name, Model model) {
        this(name, model, null);
    }

    public Actor3D(String name, Model model, Environment environment) {
        this.model = model;
        this.name = name;

        if (environment == null) {
            this.environment = new Environment();
            this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
            this.environment.add(new DirectionalLight().set(0.5f, 0.5f, 0.5f, -1f, -0.8f, -0.2f));
            this.environment.add(new DirectionalLight().set(0.5f, 0.5f, 0.5f, 1f, -0.8f, -0.2f));
        } else {
            this.environment = environment;
        }

        if (modelBatch == null) modelBatch = new ModelBatch();

        this.createModelInstance();
    }


    @Override
    public void draw(Batch batch, float parentColor) {

        //draw background
        if (background != null) {
            background.draw(batch, getX(), getY(), getWidth(), getHeight());
        }

        if (modelInstance != null) {
            //we must finish the 2D Batch for rendering 3D
            batch.flush();
            batch.end();

            // move viewport
            Gdx.gl.glViewport((int) worldX, (int) worldY, (int) getWidth(), (int) getHeight());

            // finish depth test for previews 3D rendering like Map
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);


            //render 3d model
            modelBatch.begin(cam);
            modelBatch.render(modelInstance, environment);
            modelBatch.end();

            // return viewport
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            // restart 2D Batch
            batch.begin();
        }
    }

    private void createModelInstance() {
        modelInstance = new ModelInstance(model);

        Vector3 dimensions = modelInstance.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3());
        float largest = dimensions.x;
        if (dimensions.y > largest) largest = dimensions.y;
        if (dimensions.z > largest) largest = dimensions.z;
        if (largest > 25) {
            modelScale = 25f / largest;
        } else if (largest < 0.1f) {
            modelScale = 5 / largest;
        }

        modelInstance.transform.setToScaling(modelScale, modelScale, modelScale);
        log.debug("Scaled Model" + this.name + " to: " + (modelScale * 100f) + "%");
    }

    public void resetCam() {
        cam = new PerspectiveCamera(30, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 10, 0);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        Gdx.graphics.requestRendering();
    }


    public void setModelScale(float scale) {
        modelScale *= scale;
        modelInstance.transform.scale(scale, scale, scale);
        modelInstance.calculateTransforms();
    }


    public void setQuaternion(final Quaternion q1) {
        if (CB.isMainThread()) {
            modelInstance.transform.set(q1);
            modelInstance.transform.scale(modelScale, modelScale, modelScale);
            modelInstance.calculateTransforms();
        } else {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    modelInstance.transform.set(q1);
                    modelInstance.transform.scale(modelScale, modelScale, modelScale);
                    modelInstance.calculateTransforms();
                }
            });
        }

        Gdx.graphics.requestRendering();
    }

    @Override
    protected void positionChanged() {
        resetCam();
        Vector2 vector2 = new Vector2();
        this.localToStageCoordinates(vector2);
        this.worldX = vector2.x;
        this.worldY = vector2.y;
        Gdx.graphics.requestRendering();
    }

    @Override
    protected void sizeChanged() {
        resetCam();
    }

    public void setBackground(de.longri.cachebox3.gui.drawables.ColorDrawable backgroundDrawable) {
        this.background = backgroundDrawable;
    }
}
