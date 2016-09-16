package de.longri.cachebox3.gui.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.LayerRenderer;

/**
 * Created by Longri on 16.09.2016.
 */
public class MyLocationLayer extends Layer {

    private final Environment lights;
    private final MapCamera cam;
    ModelBatch modelBatch = new ModelBatch();
    private ModelInstance modelInstance;


    public MyLocationLayer(Map map) {
        super(map);

        cam = new MapCamera(mMap);
        lights = new Environment();
        lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));


        this.mRenderer = new LayerRenderer() {
            @Override
            public void update(GLViewport viewport) {
                this.setReady(true);
            }


            @Override
            public void render(GLViewport viewport) {
                if (modelInstance == null) {
                    ObjLoader loader = new ObjLoader();

                    Model model;

                    model = loader.loadModel(Gdx.files.internal("skins/day/3d_model/Pfeil.obj"));

//                    model = new ModelBuilder().createBox(40, 40, 40,
//                            new Material(ColorAttribute.createDiffuse(new Color(0, 1, 0, 0.4f)))
//                            , VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);


                    model =  new ModelBuilder().createArrow(20f,20f,100, -20f,-20f,200, 0.1f, 0.1f, 5,
                            GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.RED)),
                            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

                    modelInstance = new ModelInstance(model);
                }


                float zoom = 67;
                PerspectiveCamera myCam = new PerspectiveCamera(zoom, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                myCam.position.set(200, 400, 200);
                myCam.lookAt(0f, 0, 0f);

                myCam.near = 0.00001f;
                myCam.far = 3000.0f;

                myCam.update();


//                modelInstance.transform.setToTranslation(0, 0, 0);
//                modelInstance.transform.setToRotation(1, 1, 0, 180);

                modelBatch.begin(myCam);
                //modelBatch.render(modelInstance, lights);
                modelBatch.render(modelInstance);
                modelBatch.end();

            }
        };
    }
}
