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

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.Locator;
import de.longri.cachebox3.locator.events.PositionChangedEvent;
import de.longri.cachebox3.locator.events.PositionChangedEventList;

/**
 * Created by Longri on 23.07.16.
 */
public class AboutView extends AbstractView implements PositionChangedEvent {

    VisLabel coordinateLabel;

    public AboutView() {
        super("AboutView");
    }

    @Override
    protected void create() {
        coordinateLabel = new VisLabel(this.NAME);
        coordinateLabel.setAlignment(Align.center);
        coordinateLabel.setPosition(10, 10);
        this.addActor(coordinateLabel);

        //register as Location receiver
        PositionChangedEventList.Add(this);
    }


    @Override
    public void dispose() {

    }

    @Override
    public void PositionChanged() {
        Coordinate coordinate = Locator.getCoordinate();
        coordinateLabel.setText(coordinate.formatCoordinateLineBreak());
        CB.requestRendering();
    }

    @Override
    public void OrientationChanged() {

    }

    @Override
    public void SpeedChanged() {

    }

    @Override
    public String getReceiverName() {
        return "AboutView";
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    protected void boundsChanged(float x, float y, float width, float height) {
        coordinateLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
    }
}
