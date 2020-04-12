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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.CB_View_Base;
import de.longri.cachebox3.gui.widgets.ColorWidget;
import de.longri.cachebox3.utils.SkinColor;
import de.longri.serializable.BitStore;

/**
 * Created by Longri on 23.07.16.
 */
public abstract class AbstractView extends CB_View_Base {

    ColorWidget colorWidget;
    CB_Label nameLabel;

    public AbstractView(BitStore reader) {
        super(reader.readString());
        restoreInstanceState(reader);
    }

    public AbstractView(String name) {
        super(name);
    }


    protected void create() {
        // create a Label with name for default
        nameLabel = new CB_Label(name);
        nameLabel.setAlignment(Align.center);
        nameLabel.setPosition(10, 10);

        colorWidget = new ColorWidget(CB.getSkin().get("abstract_background", SkinColor.class));
        colorWidget.setBounds(0, 0, getWidth(), getHeight());

        addActor(colorWidget);
        addActor(nameLabel);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        boundsChanged(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        boundsChanged(getX(), getY(), getWidth(), getHeight());
    }

    protected void boundsChanged(float x, float y, float width, float height) {
        if (colorWidget != null) colorWidget.setBounds(0, 0, getWidth(), getHeight());
        if (nameLabel != null) nameLabel.setBounds(0, 0, getWidth(), getHeight());
    }

    public String getName() {
        return name;
    }

    /**
     * if returning false no longclick menu is shown (this is possibly not wanted)
     */
    public abstract boolean hasContextMenu();

    public abstract Menu getContextMenu();

    public final BitStore saveInstanceState() {
        BitStore store = new BitStore();
        store.write(getClass().getName());
        store.write(name);
        saveInstanceState(store);
        return store;
    }

    public void saveInstanceState(BitStore writer) {

    }

    protected void restoreInstanceState(BitStore reader) {

    }


}
