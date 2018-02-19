/*
 * Copyright (C) 2018 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CompassViewStyle;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * holds the Compass, the distance label and the Sun/Moon drawables
 * Created by Longri on 19.02.2018.
 */
public class CompassPanel extends WidgetGroup {

    private final static Logger log = LoggerFactory.getLogger(CompassPanel.class);

    private final Compass compass;
    private final Label distance, accurate;

    public CompassPanel(CompassViewStyle style) {
        compass = new Compass(style, true);

        Label.LabelStyle distanceStyle = new Label.LabelStyle();
        Label.LabelStyle accurateStyle = new Label.LabelStyle();

        distanceStyle.background = style.distanceBackground;
        distanceStyle.font = style.distnaceFont;
        distanceStyle.fontColor = style.distanceColor;

        accurateStyle.font = style.accurateFont;
        accurateStyle.fontColor = style.accurateColor;

        distance = new Label("distance", distanceStyle);
        accurate = new Label("accurate", accurateStyle);

        distance.setAlignment(Align.left);
        accurate.setAlignment(Align.right);

        this.addActor(distance);
        this.addActor(accurate);
        this.addActor(compass);
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        float height = distance.getPrefHeight();
        float yPos = this.getHeight() - (height /*+ CB.scaledSizes.MARGIN*/);

        compass.setBounds(0, CB.scaledSizes.MARGINx4, this.getWidth(), this.getHeight() - height + CB.scaledSizes.MARGINx2);
        distance.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);
        accurate.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);

        //reset x/y
        this.setPosition(0, 0);
    }

    public void setInfo(float distance, float heading, float bearing, float accurate) {
        this.distance.setText(UnitFormatter.distanceString(distance, false));
        this.accurate.setText("  +/- " + UnitFormatter.distanceString(accurate, true)+"  ");
        compass.setBearing(heading);
        compass.setHeading(bearing - heading);
    }

    public float getMinHeight() {
        return compass.getMinHeight() + distance.getMinHeight() - CB.scaledSizes.MARGINx4;
    }
}
