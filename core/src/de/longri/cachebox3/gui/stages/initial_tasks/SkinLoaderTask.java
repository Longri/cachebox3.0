package de.longri.cachebox3.gui.stages.initial_tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.DevicesSizes;
import de.longri.cachebox3.utils.SizeF;
import de.longri.cachebox3.utils.UI_Size_Base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 02.08.16.
 */
public final class SkinLoaderTask extends AbstractInitTask {

    public SkinLoaderTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void RUNABLE() {

        //initial sizes
        DevicesSizes ui = new DevicesSizes();
        ui.Window = new SizeF(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ui.Density = CB.getScalefactor();
        ui.isLandscape = false;
        new UI_Size_Base();
        UI_Size_Base.that.initial(ui);


        // the SvgSkin must create in a OpenGL context. so we post a runnable and wait!
        final AtomicBoolean wait = new AtomicBoolean(true);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                FileHandle svgFolder = Gdx.files.internal("skins/day/svg");
                FileHandle skinJson = Gdx.files.internal("skins/day/skin.json");
                CB.setActSkin(new SvgSkin(svgFolder, skinJson));
                CB.backgroundColor = CB.getColor("background");
                wait.set(false);
            }
        });

        while (wait.get()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }
}
