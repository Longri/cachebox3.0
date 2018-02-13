package de.longri.cachebox3.gui.widgets.list_view;
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.SkinFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.utils.NamedRunnable;

/**
 * Created by Longri on 06.02.2018.
 */
public class TestActivity extends ActivityBase {

    final int COUNT = 30;

    TestNew n_ew;
    VisTable info;


    public TestActivity() {
        super("TestActivity");
//        this.old = new TestOld(COUNT);
//        this.addActor(old);
        this.n_ew = new TestNew(COUNT);
        this.addActor(n_ew);
        info = new VisTable();
        info.setBackground(new ColorDrawable(Color.RED));
        this.addActor(info);


        n_ew.setScrollChangedListener(new ScrollChangedEvent() {
            @Override
            public void scrollChanged(float x, float y) {
                newScrollPosLabel.setText("scroll: " + FloatString(y));
                newChildCountLabel.setText(n_ew.getChildCount());
            }
        });
        sizeChanged();
        layoutInfo();


        CB.postAsyncDelayd(500, new NamedRunnable("Test") {
            @Override
            public void run() {
                n_ew.selectItem(COUNT / 2);
            }
        });
    }

    private String FloatString(float value) {
        int intVa = (int) (value * 100);
        return Float.toString(intVa / 100f);
    }

    private VisLabel newScrollPosLabel;
    private VisLabel oldScrollPosLabel;
    private VisLabel newChildCountLabel;
    private VisLabel oldChildCountLabel;

    private void layoutInfo() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        String path = "skins/day/fonts/DroidSans.ttf";
        labelStyle.fontColor = Color.BLACK;
        labelStyle.font = new SkinFont(path, Gdx.files.internal(path), 14, null);

        Label.LabelStyle label2Style = new Label.LabelStyle();
        label2Style.fontColor = Color.BLACK;
        label2Style.font = new SkinFont(path, Gdx.files.internal(path), 10, null);

        info.add(new VisLabel("New ListView", labelStyle)).expandX().fillX();
        info.add(new VisLabel("Old ListView", labelStyle)).expandX().fillX();
        info.row();
        newScrollPosLabel = new VisLabel("scroll:", label2Style);
        oldScrollPosLabel = new VisLabel("scroll", label2Style);
        info.add(newScrollPosLabel).expandX().fillX();
        info.add(oldScrollPosLabel).expandX().fillX();

        info.row();
        newChildCountLabel = new VisLabel("childs:", label2Style);
        oldChildCountLabel = new VisLabel("childs", label2Style);
        info.add(newChildCountLabel).expandX().fillX();
        info.add(oldChildCountLabel).expandX().fillX();
    }

    @Override
    public void sizeChanged() {
        float half = this.getWidth() / 2;
        float height = this.getHeight() - CB.getScaledFloat(100);
        n_ew.setBounds(0, 0, this.getWidth(), height);
        info.setBounds(0, height, this.getWidth(), this.getHeight() - height);
    }
}
