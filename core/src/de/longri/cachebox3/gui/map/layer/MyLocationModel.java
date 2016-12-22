package de.longri.cachebox3.gui.map.layer;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import org.oscim.backend.GL;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.event.Event;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.map.Viewport;
import org.oscim.renderer.GLState;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.LayerRenderer;

import static org.oscim.backend.GLAdapter.gl;

public class MyLocationModel extends Layer implements Map.UpdateListener {

    static final Logger log = LoggerFactory.getLogger(MyLocationModel.class);

    private double longitude = 13.397124;
    private double latitude = 52.579274;
    private float bearing = 0;
    private float modelScale = 1;
    private SharedModel sharedModel;


    public MyLocationModel(Map map, Model model) {
        super(map);
        mRenderer = new GdxRenderer3D();
        setModel(model);
    }


    @Override
    public void onMapEvent(Event e, MapPosition mapPosition) {
        //do nothing
    }

    public void setPosition(double latitude, double longitude, double bearing) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = (float) bearing;
    }

    public void setModelScale(float scale) {
        this.modelScale = scale;
    }

    public void setModel(Model model) {
        if (model == null) throw new NullPointerException("Model can not NULL");
        sharedModel = new SharedModel(model);
    }

    private class GdxRenderer3D extends LayerRenderer {
        private MapCamera mapCamera;
        private Environment lights;
        private Shader shader;
        private RenderContext renderContext;
        private float[] mBox = new float[8];
        private Renderable r = new Renderable();

        @Override
        public boolean setup() {
            lights = new Environment();
            lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 1.0f, 1.0f, 1.0f, 1.f));
            lights.add(new DirectionalLight().set(0.3f, 0.3f, 0.3f, 0, 1, -0.2f));
            mapCamera = new MapCamera(mMap);
            renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
            return true;
        }

        @Override
        public synchronized void update(GLViewport v) {
            if (!isReady()) {
                setReady(true);
            }
        }


        @Override
        public void render(GLViewport v) {
            gl.depthMask(true);
            gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, 0);

            // set state that is expected after modelBatch.end();
            // modelBatch keeps track of its own state
            GLState.enableVertexArrays(-1, -1);
            GLState.bindTex2D(-1);
            GLState.useProgram(-1);
            GLState.test(false, false);
            GLState.blend(true);

            mapCamera.update(v);
            Viewport p = mMap.viewport();
            p.getMapExtents(mBox, 10);
            float scale = (float) (mapCamera.mMapPosition.scale / v.pos.scale);

            float dx = (float) (mapCamera.mMapPosition.x - v.pos.x)
                    * (Tile.SIZE << mapCamera.mMapPosition.zoomLevel);
            float dy = (float) (mapCamera.mMapPosition.y - v.pos.y)
                    * (Tile.SIZE << mapCamera.mMapPosition.zoomLevel);

            for (int i = 0; i < 8; i += 2) {
                mBox[i] *= scale;
                mBox[i] -= dx;
                mBox[i + 1] *= scale;
                mBox[i + 1] -= dy;
            }
            synchronized (this) {
                renderContext.begin();
                if (shader == null) {
                    r = sharedModel.getRenderable(r);
                    DefaultShader.Config c = new DefaultShader.Config();
                    c.numBones = 0;
                    c.defaultDepthFunc = 0;
                    c.numDirectionalLights = 1;
                    r.environment = lights;
                    shader = new DefaultShader(r, c);
                    shader.init();
                }

                mapCamera.mMapPosition.setPosition(latitude, longitude);
                mapCamera.mMapPosition.setScale(0.0172 * mMap.getMapPosition().getScale());

                shader.begin(mapCamera, renderContext);
                sharedModel.transform.idt();

                float s = CB.getScalefactor();
                sharedModel.transform.scale(s, s, s);

                sharedModel.transform.rotate(0, 0, 1, bearing);
                sharedModel.getRenderable(r);
                shader.render(r);
                shader.end();
                renderContext.end();

            }

            gl.depthMask(false);
            gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, 0);
            gl.bindBuffer(GL.ARRAY_BUFFER, 0);
            gl.flush();
        }
    }

}
