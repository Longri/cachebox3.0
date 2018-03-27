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
import com.badlogic.gdx.utils.Align;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CompassViewStyle;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_WidgetGroup;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * holds the Compass, the distanceLabel label and the Sun/Moon drawables
 * Created by Longri on 19.02.2018.
 */
public class CompassPanel extends Catch_WidgetGroup {

    private final static Logger log = LoggerFactory.getLogger(CompassPanel.class);

    private final Compass compass;
    private final Label distanceLabel, accurateLabel;

    public CompassPanel(CompassViewStyle style) {
        compass = new Compass(style, true);

        Label.LabelStyle distanceStyle = new Label.LabelStyle();
        Label.LabelStyle accurateStyle = new Label.LabelStyle();

        distanceStyle.background = style.distanceBackground;
        distanceStyle.font = style.distnaceFont;
        distanceStyle.fontColor = style.distanceColor;

        accurateStyle.font = style.accurateFont;
        accurateStyle.fontColor = style.accurateColor;

        distanceLabel = new Label("distanceLabel", distanceStyle);
        accurateLabel = new Label("accurateLabel", accurateStyle);

        distanceLabel.setAlignment(Align.left);
        accurateLabel.setAlignment(Align.right);

        this.addActor(distanceLabel);
        this.addActor(accurateLabel);
        this.addActor(compass);
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        float height = distanceLabel.getPrefHeight();
        float yPos = this.getHeight() - (height /*+ CB.scaledSizes.MARGIN*/);

        compass.setBounds(0, CB.scaledSizes.MARGINx2, this.getWidth(), this.getHeight() - height + CB.scaledSizes.MARGINx2);
        distanceLabel.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);
        accurateLabel.setBounds(CB.scaledSizes.MARGIN, yPos, this.getWidth() - CB.scaledSizes.MARGINx2, height);

        //reset x/y
        this.setPosition(0, 0);
    }

    public void setInfo(final float distance, final float heading, final float bearing, final float accurate) {
        CB.postOnGlThread(new NamedRunnable("Test Add") {
            @Override
            public void run() {
                distanceLabel.setText(UnitFormatter.distanceString(distance, false));
                accurateLabel.setText("  +/- " + UnitFormatter.distanceString(accurate, true) + "  ");
                compass.setBearing(heading);
                compass.setHeading(bearing - heading);
            }
        });
    }

    public float getMinHeight() {
        return compass.getMinHeight() + distanceLabel.getMinHeight() - CB.scaledSizes.MARGINx2;
    }
}
