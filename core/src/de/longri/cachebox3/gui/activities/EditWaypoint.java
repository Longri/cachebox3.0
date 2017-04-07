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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.skin.styles.EditWaypointStyle;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Longri on 07.04.2017.
 */
public class EditWaypoint extends ActivityBase {
    private final static Logger log = LoggerFactory.getLogger(EditWaypoint.class);

    private final EditWaypointStyle style;

    private VisTextButton btnOk, btnCancel;

    public EditWaypoint() {
        super("EditWaypoint");
        style = null;
//        style = VisUI.getSkin().get("default", EditWaypointStyle.class);
        createButtons();
    }

    public void create() {

    }

    private void createButtons() {

        btnOk = new VisTextButton(Translation.Get("save"));
        btnCancel = new VisTextButton(Translation.Get("cancel"));

        this.addActor(btnOk);
        this.addActor(btnCancel);

        btnOk.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                finish();
            }
        });

        btnCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                finish();
            }
        });
    }

    @Override
    public void layout() {
        super.layout();

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);

        x -= CB.scaledSizes.MARGIN + btnOk.getWidth();

        btnOk.setPosition(x, y);

    }
}
