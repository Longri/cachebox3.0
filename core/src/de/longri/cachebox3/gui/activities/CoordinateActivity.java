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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 04.02.17.
 */
public class CoordinateActivity extends ActivityBase {

    private boolean isCreated = false;
    final private Coordinate coordinate;

    public CoordinateActivity(Coordinate coordinate) {
        super("CorrdinateActivity");
        this.setDebug(true);
        this.coordinate = coordinate;
    }


    @Override
    public void layout() {
        super.layout();
        if (!isCreated) create();
    }


    private void create() {

        this.defaults().pad(CB.scaledSizes.MARGIN);


        Table cancelOkTable = new Table();

        VisTextButton btnOk = new VisTextButton(Translation.Get("ok"));
        VisTextButton btnCancel = new VisTextButton(Translation.Get("cancel"));

        btnOk.addListener(okListener);
        btnCancel.addListener(cancelListener);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN * 3) / 2;

        cancelOkTable.add(btnOk).width(new Value.Fixed(btnWidth));
        cancelOkTable.add(btnCancel).width(new Value.Fixed(btnWidth));


        this.add(cancelOkTable).bottom().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));

        isCreated = true;
    }


    ClickListener cancelListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            callBack(null);
            finish();
        }
    };


    ClickListener okListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            callBack(CoordinateActivity.this.coordinate);
            finish();
        }
    };

    public void callBack(Coordinate coordinate) {
    }
}
