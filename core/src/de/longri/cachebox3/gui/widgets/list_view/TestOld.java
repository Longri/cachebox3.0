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
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;

import java.util.Arrays;


/**
 * Created by Longri on 06.02.2018.
 */
public class TestOld extends WidgetGroup {

    final int count;
    final ListView lv;
    static Label.LabelStyle labelStyle;


    TestOld(final int count) {
        labelStyle = new Label.LabelStyle();
        String path = "skins/day/fonts/DroidSans.ttf";
        labelStyle.fontColor = Color.BLACK;
        labelStyle.font = new SkinFont(path, Gdx.files.internal(path), 10, null);


        this.count = count;
        this.lv = new ListView();
        this.addActor(lv);

        lv.setBackground(new ColorDrawable(Color.LIME));

        lv.setAdapter(new Adapter() {
            @Override
            public int getCount() {
                return count;
            }

            @Override
            public ListViewItem getView(int index) {
                Item item = new Item(index);
                item.setPrefWidth(TestOld.this.getWidth());
                item.pack();
                return item;
            }

            @Override
            public void update(ListViewItem view) {

            }

            @Override
            public float getItemSize(int index) {
                return 0;
            }
        });

        sizeChanged();
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        lv.setBounds(0, 0, this.getWidth(), this.getHeight());

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

    public void setScrollPos(float y) {
        lv.setScrollPos(y);
    }

    public String getChildCount() {
        Actor[] childs = ((Group) (lv.scrollPane.getActor())).getChildren().begin();
        int[] idxArr = new int[childs.length];
        int count = 0;
        for (Actor act : childs) {
            if (act == null) continue;
            idxArr[count++] = ((ListViewItem) act).getListIndex();
        }
        ((Group) (lv.scrollPane.getActor())).getChildren().end();

        Arrays.sort(idxArr);

        try {
            return "count:" + count + "  idx: " + idxArr[(idxArr.length - count) <= 0 ? 0 : idxArr.length - count] + "-" + idxArr[idxArr.length - 1];
        } catch (Exception e) {
            return "Error";
        }
    }
}
