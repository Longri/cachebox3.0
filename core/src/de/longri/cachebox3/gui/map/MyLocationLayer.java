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
import de.longri.cachebox3.locator.Location;
import de.longri.cachebox3.locator.Locator;
import org.oscim.core.MapPosition;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.LayerRenderer;

/**
 * Created by Longri on 16.09.2016.
 */
public class MyLocationLayer extends Layer {


    public MyLocationLayer(Map map) {
        super(map);


        this.mRenderer = new GdxModelRenderer(map);


        ObjLoader loader = new ObjLoader();
        Model model;

        model = loader.loadModel(Gdx.files.internal("skins/day/3d_model/Pfeil.obj"));

//                    model = new ModelBuilder().createBox(40, 40, 40,
//                            new Material(ColorAttribute.createDiffuse(new Color(0, 1, 0, 0.4f)))
//                            , VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);


        model = new ModelBuilder().createArrow(20f, 20f, 100, -20f, -20f, 200, 0.1f, 0.1f, 5,
                GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.RED)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        ((GdxModelRenderer) this.mRenderer).instances.add(new ModelInstance(model));

    }
}
