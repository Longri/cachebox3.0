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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.util.adapter.ListAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.ColorWidget;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Lang;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TestView.class);

    public TestView() {
        super("TestView");
    }


    static class StringListItem extends VisTable {
        public StringListItem(String s) {
            VisLabel label = new VisLabel(s);
            VisTable table = new VisTable();
            table.left();
            table.add(label);
            this.add(table);
            this.setBackground("listrec_first_drawable");
        }

        @Override
        public void finalize() {
            log.debug("finalize Item");
        }
    }


    protected void create() {
        // create a Label with name for default
        nameLabel = new VisLabel(this.NAME);
        nameLabel.setAlignment(Align.center);
        nameLabel.setPosition(10, 10);
        nameLabel.setWrap(true);

        colorWidget = new ColorWidget(CB.getSkin().getColor("abstract_background"));
        colorWidget.setBounds(0, 0, this.getWidth(), this.getHeight());


        final VisTextButton testButton = new VisTextButton("MenuTest");
        testButton.setSize(CB.scaledSizes.BUTTON_WIDTH_WIDE, CB.scaledSizes.BUTTON_HEIGHT);

        testButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

            }
        });

        //  this.addActor(colorWidget);
        this.addActor(nameLabel);
        this.addActor(testButton);


        final ArrayList<String> itemList = new ArrayList<String>();
        for (int i = 0; i < 20; i++) itemList.add(Integer.toString(i));

        de.longri.cachebox3.gui.views.ListView listView = new de.longri.cachebox3.gui.views.ListView(itemList.size()) {
            @Override
            public VisTable createView(Integer index) {
                VisLabel label = new VisLabel(itemList.get(index));

                VisTable table = new VisTable();
                table.left();
                table.add(label);
                return table;
            }
        };


        listView.getMainTable().setBounds(20, 20, 200, 400);
        this.addActor(listView.getMainTable());

    }


    @Override
    public void reloadState() {


        StringBuilder sb = new StringBuilder();

        sb.append("LaunchCount:" + Config.AppRaterlaunchCount.getValue());

        nameLabel.setText(sb.toString());


    }

    @Override
    public void saveState() {

    }

    @Override
    public void dispose() {

    }
}
