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
import de.longri.cachebox3.gui.widgets.ColorWidget;
import de.longri.cachebox3.sqlite.Database;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {

    public TestView() {
        super("TestView");
    }

    protected void create() {
        // create a Label with name for default
        nameLabel = new VisLabel(this.name);
        nameLabel.setAlignment(Align.center);
        nameLabel.setPosition(10, 10);

        colorWidget = new ColorWidget(CB.getSkin().getColor("abstract_background"));
        colorWidget.setBounds(0, 0, this.getWidth(), this.getHeight());

        this.addActor(colorWidget);
        this.addActor(nameLabel);
    }


    @Override
    public void reloadState() {
        try {
            Database db = new Database(Database.DatabaseType.CacheBox);
            db.startUp("Cachebox/test.db");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveState() {

    }

    @Override
    public void dispose() {

    }
}
