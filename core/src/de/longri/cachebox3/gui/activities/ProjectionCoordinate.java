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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 14.11.2017.
 */
public class ProjectionCoordinate extends ActivityBase {

    private final Coordinate coord;

    protected ProjectionCoordinate(Coordinate coordinate) {
        super("project");
        this.coord = coordinate;
        createOkCancel();
    }

    public void callBack(Coordinate coordinate) {
    }

    private final ClickListener cancelListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            callBack(null);
            finish();
        }
    };
    private final ClickListener okListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            finish();
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float distance = 0;
                    float direction = 0;
                    Coordinate project = Coordinate.Project(coord, direction, distance);
                    callBack(project);
                }
            });
        }
    };

    private void createOkCancel() {
        this.row();
        Table cancelOkTable = new Table();

        CharSequenceButton btnOk = new CharSequenceButton(Translation.get("ok"));
        CharSequenceButton btnCancel = new CharSequenceButton(Translation.get("cancel"));

        btnOk.addListener(okListener);
        btnCancel.addListener(cancelListener);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN_HALF * 3) / 2;

        cancelOkTable.add(btnOk).width(new Value.Fixed(btnWidth));
        cancelOkTable.add(btnCancel).width(new Value.Fixed(btnWidth));

        this.add(cancelOkTable).bottom().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
    }

}
