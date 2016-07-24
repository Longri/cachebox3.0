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
package de.longri.cachebox3.gui.stages;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.AboutView;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.ButtonBar;

/**
 * Created by Longri on 20.07.2016.
 */
public class ViewManager extends Stage {

    private AbstractView actView;
    private final float bottonsize, width, height;
    private final ButtonBar mainButtonBar;

    public ViewManager() {

        bottonsize = CB.getScaledFloat(64);
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        mainButtonBar = new ButtonBar(CB.getSkin().get("main_button_bar", ButtonBar.ButtonBarStyle.class),
                ButtonBar.Type.DISTRIBUTED);
        mainButtonBar.setBounds(0, 0, width, bottonsize);
        this.addActor(mainButtonBar);

        Gdx.app.log("ScaleFactor", Float.toString(CB.getScaledFloat(1)));
        Gdx.app.log("Width", Float.toString(Gdx.graphics.getWidth()));
        Gdx.app.log("Height", Float.toString(Gdx.graphics.getHeight()));

        Gdx.app.log("PPI", Float.toString(Gdx.graphics.getPpiX()));

        Button testButton = new Button(VisUI.getSkin(), "default");
        testButton.setSize(bottonsize, bottonsize);
        mainButtonBar.addButton(testButton);

        Button testButton1 = new Button(VisUI.getSkin(), "default");
        testButton1.setSize(bottonsize, bottonsize);
        mainButtonBar.addButton(testButton1);


        Button testButton2 = new Button(VisUI.getSkin(), "default");
        testButton2.setSize(bottonsize, bottonsize);
        mainButtonBar.addButton(testButton2);

        Button testButton3 = new Button(VisUI.getSkin(), "default");
        testButton3.setSize(bottonsize, bottonsize);
        mainButtonBar.addButton(testButton3);

        Button testButton4 = new Button(VisUI.getSkin(), "default");
        testButton4.setSize(bottonsize, bottonsize);
        mainButtonBar.addButton(testButton4);

        mainButtonBar.layout();
        showView(new AboutView());
    }

    public void showView(AbstractView view) {
        if (actView != null) {
            this.getRoot().removeActor(actView);
            actView.saveState();
            actView.dispose();
        }

        this.actView = view;
        this.addActor(view);
        setActViewBouns();
        this.actView.reloadState();
    }

    private void setActViewBouns() {
        this.actView.setBounds(0, bottonsize, width, height);
    }
}
