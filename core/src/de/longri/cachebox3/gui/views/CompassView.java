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

import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CompassViewStyle;
import de.longri.cachebox3.gui.widgets.Compass;

/**
 * Created by Longri on 24.07.16.
 */
public class CompassView extends AbstractView {

    private final Compass compass;
    private final VisSplitPane splitPane;
    private final Table topTable, botomTable;

    public CompassView() {
        super("CompassView");

        CompassViewStyle style = VisUI.getSkin().get("compassViewStyle", CompassViewStyle.class);


        topTable = new Table();
        botomTable = new Table();

        topTable.setDebug(true, true);
        botomTable.setDebug(true, true);

        topTable.setBackground(style.splitBackground);
        botomTable.setBackground(style.splitBackground);


        VisSplitPane.VisSplitPaneStyle visSplitPaneStyle = new VisSplitPane.VisSplitPaneStyle();
        visSplitPaneStyle.handle = style.splitHandle;
        splitPane = new VisSplitPane(topTable, botomTable, true, visSplitPaneStyle);
        this.addChild(splitPane);

        compass = new Compass(style);
        botomTable.add(compass).expand().fill().center();

    }

    @Override
    public void layout() {
        super.layout();
        splitPane.setBounds(0, 0, this.getWidth(), this.getHeight());
    }


    @Override
    public void dispose() {

    }

    /**
     * Called when the actor's size has been changed.
     */
    protected void sizeChanged() {
        super.sizeChanged();
    }
}
