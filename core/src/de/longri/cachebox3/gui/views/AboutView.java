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
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.events.newT.*;
import de.longri.cachebox3.utils.BuildInfo;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 23.07.16.
 */
public class AboutView extends AbstractView implements PositionChangedListener, DistanceChangedListener {

    VisLabel coordinateLabel, versionLabel;

    public AboutView() {
        super("AboutView");
    }

    @Override
    protected void create() {
        coordinateLabel = new VisLabel(this.NAME);
        coordinateLabel.setAlignment(Align.center);
        coordinateLabel.setPosition(10, 10);
        this.addActor(coordinateLabel);

        versionLabel = new VisLabel("CB 3.0." + BuildInfo.getRevison() + "\n" + BuildInfo.getDetail());
        versionLabel.setAlignment(Align.center);
        versionLabel.setPosition(10, this.getHeight() / 2);
        this.addActor(versionLabel);

        //register as Location receiver
        EventHandler.add(this);
    }


    @Override
    public void dispose() {
        //register as Location receiver
        EventHandler.remove(this);
    }

    protected void boundsChanged(float x, float y, float width, float height) {
        coordinateLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
    }


    Coordinate pos;
    float distance = -1;

    @Override
    public void positionChanged(PositionChangedEvent event) {
        pos = event.pos;
        setText();
    }

    @Override
    public void distanceChanged(DistanceChangedEvent event) {
        distance = event.distance;
        setText();
    }

    private void setText() {
        StringBuilder sb = new StringBuilder();
        sb.append(pos != null ? pos.formatCoordinateLineBreak() : "???");
        sb.append("\n");
        sb.append(distance == -1 ? "???" : UnitFormatter.distanceString(distance,false));
        coordinateLabel.setText(sb);
        CB.requestRendering();
    }

    public String toString() {
        return "AboutView";
    }
}
