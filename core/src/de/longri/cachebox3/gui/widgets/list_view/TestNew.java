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
package de.longri.cachebox3.gui.widgets.list_view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.SkinFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.IntArray;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_WidgetGroup;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;


/**
 * Created by Longri on 06.02.2018.
 */
public class TestNew extends Catch_WidgetGroup {

    final int count;
    final ListView lv;
    static Label.LabelStyle labelStyle;

    TestNew(final int count) {
        labelStyle = new Label.LabelStyle();
        String path = "skins/day/fonts/DroidSans.ttf";
        labelStyle.fontColor = Color.BLACK;
        labelStyle.font = new SkinFont(path, Gdx.files.internal(path), 10, null);

        this.count = count;
        this.lv = new ListView(VERTICAL);
        this.addActor(lv);

        lv.setBackground(new ColorDrawable(Color.LIME));

        lv.setAdapter(new ListViewAdapter() {

            @Override
            public int getCount() {
                return count;
            }

            @Override
            public ListViewItem getView(int index) {
                Item item = new Item(index);
                item.setPrefWidth(TestNew.this.getWidth());
                item.pack();
                if (index % 5 == 1) item.setVisible(false);
                return item;
            }

            @Override
            public void update(ListViewItem view) {

            }

        });

        sizeChanged();
    }

    @Override
    public void sizeChanged() {
        lv.setBounds(0, 0, this.getWidth(), this.getHeight());

    }

    public void setScrollPos(float y) {
        lv.setScrollPos(y);
    }

    public String getChildCount() {
        Actor[] childs = ((Group) (lv.scrollPane.getActor())).getChildren().begin();
        IntArray idxArr = new IntArray();

        for (int i = 0, n = ((Group) (lv.scrollPane.getActor())).getChildren().size; i < n; i++) {
            idxArr.add(((ListViewItemInterface) childs[i]).getListIndex());
        }
        ((Group) (lv.scrollPane.getActor())).getChildren().end();

        idxArr.sort();

        try {
            return "count:" + idxArr.size + "  idx: " + idxArr.first() + "-" + idxArr.peek();
        } catch (Exception e) {
            return "Error";
        }

    }

    public void selectItem(int idx) {
        lv.setSelection(idx);
        lv.setSelectedItemVisible(true);
    }

    static class Item extends ListViewItem {

        VisLabel pos;

        public Item(int listIndex) {
            super(listIndex);
            VisLabel idxLabel = new VisLabel("idx: " + Integer.toString(listIndex));

            if (listIndex == 2 || listIndex == 9 || listIndex == 24) {
                idxLabel.setText("idx: " + Integer.toString(listIndex) + "\n\n second");
            }

            this.add(idxLabel).expandX().fillX();
            pos = new VisLabel("", labelStyle);
            this.row();
            this.add(pos);
        }

        @Override
        public void positionChanged() {
            pos.setText("x:" + FloatString(this.getX()) + " y: " + FloatString(this.getY()));
        }

        private String FloatString(float value) {
            int intVa = (int) (value * 100);
            return Float.toString(intVa / 100f);
        }


        @Override
        public void dispose() {

        }
    }

    public void setScrollChangedListener(ScrollChangedEvent listener) {
        lv.setScrollChangedListener(listener);
    }
}
