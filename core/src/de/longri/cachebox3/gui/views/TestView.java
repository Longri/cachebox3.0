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
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.Actor3D;
import de.longri.cachebox3.gui.widgets.ColorDrawable;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TestView.class);

    public TestView() {
        super("TestView");
    }

    Actor3D actor3D;

    protected void create() {
        this.clear();

        Model model = CB.getSkin().get("compassModel", Model.class);
        actor3D = new Actor3D("TestActor3D", model);

        this.addActor(actor3D);


       // ColorDrawable backgroundDrawable = new ColorDrawable(Color.FOREST);
        //actor3D.setBackground(backgroundDrawable);
        actor3D.setBounds(30, 30, getWidth() / 2, getHeight() / 2);


    }


    @Override
    public void onShow() {

        actor3D.addAction(Actions.moveTo(100, 200, 5));
        actor3D.setModelScale(0.075f);
      //  actor3D.setModelRotate(1, 0, 0, 90);


    }


    @Override
    public void draw(Batch batch, float parentColor) {
        super.draw(batch, parentColor);
        actor3D.setModelRotate(0, 1, 0, 1);
    }


    @Override
    public void dispose() {

    }

    @Override
    protected void sizeChanged() {
        create();
    }
}
