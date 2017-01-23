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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.widgets.ColorDrawable;
import de.longri.cachebox3.gui.widgets.MapCompass;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import org.oscim.backend.canvas.Bitmap;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(TestView.class);


    public static class TestStyle {
        public Bitmap region;
    }


    MapCompass mapCompass;
    TestStyle style;

    public TestView() {
        super("TestView");

    }


    protected void create() {
        this.clear();

        mapCompass = new MapCompass(getWidth() / 2, getWidth() / 2);

        style = VisUI.getSkin().get("default", TestStyle.class);


        this.addActor(mapCompass);


        ColorDrawable backgroundDrawable = new ColorDrawable(Color.FOREST);
        mapCompass.setDebug(true);
        mapCompass.setPosition(30, 30);


    }


    @Override
    public void onShow() {
        create();
        mapCompass.addAction(Actions.moveTo(100, 200, 5));
    }


    @Override
    public void draw(Batch batch, float parentColor) {
        super.draw(batch, parentColor);

    }


    @Override
    public void dispose() {

    }

    @Override
    protected void sizeChanged() {
        // create();
    }
}
