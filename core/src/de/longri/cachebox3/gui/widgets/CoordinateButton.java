/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.activities.CoordinateActivity;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_VisTextButton;
import de.longri.cachebox3.locator.Coordinate;

/**
 * Created by Longri on 04.02.17.
 */
public class CoordinateButton extends Catch_VisTextButton {

    private Coordinate coordinate;

    public CoordinateButton(Coordinate coordinate) {
        super(coordinate == null ? new Coordinate(0, 0).FormatCoordinate() :
                coordinate.FormatCoordinate(), VisUI.getSkin().get("coordinate", VisTextButtonStyle.class));
        this.coordinate = coordinate;
        this.addListener(clickListener);
    }


    ClickListener clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            CoordinateActivity coordinateActivity = new CoordinateActivity(CoordinateButton.this.coordinate) {
                public void callBack(Coordinate coordinate) {
                    if (coordinate != null) {
                        CoordinateButton.this.coordinate = coordinate;
                        CoordinateButton.this.setText(coordinate.FormatCoordinate());
                    }
                }
            };
            coordinateActivity.show();
        }
    };

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }
}
