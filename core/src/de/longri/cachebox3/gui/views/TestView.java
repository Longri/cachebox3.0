/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.drawables.FrameAnimationDrawable;
import de.longri.cachebox3.gui.skin.styles.AttributesStyle;
import de.longri.cachebox3.gui.skin.styles.FrameAnimationStyle;
import de.longri.cachebox3.gui.skin.styles.LogTypesStyle;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.EditTextBox;
import de.longri.cachebox3.gui.widgets.SelectBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.LogTypes;
import org.oscim.utils.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(TestView.class);


    static private FileChooser fileChooser = new FileChooser("select folder", FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES);


    public TestView() {
        super("TestView");
        this.setDebug(true, true);
        create();
    }

    VisScrollPane scrollPane;

    protected void create() {
        this.clear();
        VisTable attTable = new VisTable();
        attTable.setDebug(true);
        scrollPane = new VisScrollPane(attTable);


        AttributesStyle attStyle = VisUI.getSkin().get("CompassView", AttributesStyle.class);
        Attributes[] valuesPos = Attributes.values();
        ArrayList<Attributes> attList = new ArrayList<>();

        for (int i = 0, n = valuesPos.length; i < n; i++) {
            Attributes pos = valuesPos[i];
            Attributes neg = valuesPos[i];
            attList.add(pos);
            attList.add(neg);
        }

        float iconWidth = 0, iconHeight = 0;
        int lineBreak = 0, lineBreakStep = 0;
        Table lineTable = new Table();
        lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);

        VisLabel label = new VisLabel("Attribute Icons");
        lineTable.add(label);


        attTable.add(lineTable).left().expandX().fillX();
        attTable.row();
        for (int i = 0, n = attList.size(); i < n; i++) {
            Attributes att = attList.get(i);
            if ((i % 2) != 0) {
                att.setNegative();
            }

            Drawable attDrawable = att.getDrawable(attStyle);

            if (iconWidth == 0) {
                iconWidth = CB.getScaledFloat(47);//attDrawable.getMinWidth();
                iconHeight = CB.getScaledFloat(47);// attDrawable.getMinHeight();
                lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + CB.scaledSizes.MARGINx4));
                lineTable = new Table();
                lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            }

            if (attDrawable != null) {
                lineTable.add(new Image(attDrawable)).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
            } else {
                lineTable.add(new VisLabel(Integer.toString(i / 2))).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
            }


            if (i >= lineBreak) {
                attTable.add(lineTable).left();
                attTable.row();
                lineTable = new Table();
                lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                lineBreak += lineBreakStep + 1;
            }

        }
        attTable.add(lineTable).left();


        attTable.row();
        //LogTypes
        VisLabel label2 = new VisLabel("Log Type Icons");
        lineTable = new Table();
        lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
        lineTable.add(label2);
        attTable.add(lineTable).left().expandX().fillX();
        attTable.row();
        iconWidth = 0;
        LogTypes[] logTypes = LogTypes.values();
        LogTypesStyle logTypesStyle = VisUI.getSkin().get("logViewLogStyles", LogTypesStyle.class);
        for (int i = 0, n = logTypes.length; i < n; i++) {
            LogTypes logType = logTypes[i];

            Drawable attDrawable = logType.getDrawable(logTypesStyle);

            if (iconWidth == 0) {
                iconWidth = CB.getScaledFloat(40);//attDrawable.getMinWidth();
                iconHeight = CB.getScaledFloat(40);// attDrawable.getMinHeight();
                lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + CB.scaledSizes.MARGINx4));
                lineTable = new Table();
                lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            }

            if (attDrawable != null) {
                lineTable.add(new Image(attDrawable)).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
            } else {
                lineTable.add(new VisLabel(Integer.toString(i))).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
            }


            if (i >= lineBreak) {
                attTable.add(lineTable).left();
                attTable.row();
                lineTable = new Table();
                lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                lineBreak += lineBreakStep + 1;
            }

        }
        attTable.add(lineTable).left();

        this.addActor(scrollPane);
    }


    @Override
    public void onShow() {
        sizeChanged();
    }


    @Override
    public void draw(Batch batch, float parentColor) {
        super.draw(batch, parentColor);
    }

    @Override
    public void dispose() {

    }

    @Override
    protected void sizeChanged() {
        scrollPane.setBounds(0, 0, this.getWidth(), this.getHeight());
    }
}
