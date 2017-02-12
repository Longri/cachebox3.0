/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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

import com.badlogic.gdx.graphics.g2d.Batch;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.locator.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(TestView.class);


    public TestView() {
        super("TestView");

    }

    protected void create() {
        this.clear();

        Coordinate coordinate = new Coordinate(53.0, 14.0);

        CoordinateButton coordinateButton = new CoordinateButton(coordinate);


        this.addActor(coordinateButton);

        coordinateButton.setBounds(20, 100, 300, 50);


    }


    @Override
    public void onShow() {

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
