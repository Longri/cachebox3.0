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

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.CB_View_Base;
import de.longri.cachebox3.gui.widgets.ColorWidget;

/**
 * Created by Longri on 23.07.16.
 */
public abstract class AbstractView extends CB_View_Base {

    ColorWidget colorWidget;
    VisLabel nameLabel;

    public AbstractView(String name) {
        super(name);
        create();
    }

    protected void create() {
        // create a Label with name for default
        nameLabel = new VisLabel(this.NAME);
        nameLabel.setAlignment(Align.center);
        nameLabel.setPosition(10, 10);

        colorWidget = new ColorWidget(CB.getSkin().getColor("abstract_background"));
        colorWidget.setBounds(0, 0, this.getWidth(), this.getHeight());

        this.addActor(colorWidget);
        this.addActor(nameLabel);
    }

    protected void sizeChanged() {
        super.sizeChanged();
        boundsChanged(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    protected void boundsChanged(float x, float y, float width, float height) {
        colorWidget.setBounds(0, 0, this.getWidth(), this.getHeight());
        nameLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    public String getName() {
        return this.NAME;
    }
}
