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
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.*;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.dialogs.OnMsgBoxClickListener;
import de.longri.cachebox3.gui.drawables.FrameAnimationDrawable;
import de.longri.cachebox3.gui.skin.styles.*;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.widgets.AdjustableStarWidget;
import de.longri.cachebox3.gui.widgets.EditTextBox;
import de.longri.cachebox3.gui.widgets.ProgressBar;
import de.longri.cachebox3.gui.widgets.SelectBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.LogTypes;
import org.oscim.utils.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(TestView.class);

    static private FileChooser fileChooser = new FileChooser("select folder", FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES);
    private final AtomicBoolean showing = new AtomicBoolean(true);

    public TestView() {
        super("TestView");
//        this.setDebug(true, true);
        createIconTable();
    }

    VisScrollPane scrollPane;

    protected void createIconTable() {
        this.clear();
        VisTable contentTable = new VisTable();
        scrollPane = new VisScrollPane(contentTable);
        float contentWidth = (Gdx.graphics.getWidth() * 0.75f);

        {// test AdjustableStarWidget

            final AdjustableStarWidget adjustableStarWidget = new AdjustableStarWidget("Title");
            adjustableStarWidget.setValue(6);


            ListView.ListViewStyle listViewStyle = CB.getSkin().get(ListView.ListViewStyle.class);
            adjustableStarWidget.setBackground(listViewStyle.firstItem);

            VisLabel label3 = new VisLabel("AdjustableStarWidget");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            contentTable.add(adjustableStarWidget).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();
        }


        {
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


            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();
            for (int i = 0, n = attList.size(); i < n; i++) {
                Attributes att = attList.get(i);
                if ((i % 2) != 0) {
                    att.setNegative();
                }

                Drawable attDrawable = att.getDrawable(attStyle);

                if (iconWidth == 0) {
                    iconWidth = CB.getScaledFloat(47);//attDrawable.getMinWidth();
                    iconHeight = CB.getScaledFloat(47);// attDrawable.getMinHeight();
                    lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + (CB.scaledSizes.MARGINx4) * 1.5f));
                    lineTable = new Table();
                    lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                }

                if (attDrawable != null) {
                    lineTable.add(new Image(attDrawable)).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
                } else {
                    lineTable.add(new VisLabel(Integer.toString(i / 2))).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
                }


                if (i >= lineBreak) {
                    contentTable.add(lineTable).left();
                    contentTable.row();
                    lineTable = new Table();
                    lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                    lineBreak += lineBreakStep + 1;
                }

            }
            contentTable.add(lineTable).left();


            contentTable.row();
            //LogTypes
            VisLabel label2 = new VisLabel("Log Type Icons");
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label2);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();
            iconWidth = 0;
            LogTypes[] logTypes = LogTypes.values();
            LogTypesStyle logTypesStyle = VisUI.getSkin().get("logViewLogStyles", LogTypesStyle.class);
            for (int i = 0, n = logTypes.length; i < n; i++) {
                LogTypes logType = logTypes[i];

                Drawable drawable = logType.getDrawable(logTypesStyle);

                if (iconWidth == 0) {
                    iconWidth = drawable.getMinWidth();
                    iconHeight = drawable.getMinHeight();
                    lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + (CB.scaledSizes.MARGINx4) * 1.5f));
                    lineTable = new Table();
                    lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                }

                if (drawable != null) {
                    lineTable.add(new Image(drawable, Scaling.stretch)).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
                } else {
                    lineTable.add(new VisLabel(Integer.toString(i))).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
                }


                if (i >= lineBreak) {
                    contentTable.add(lineTable).left();
                    contentTable.row();
                    lineTable = new Table();
                    lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                    lineBreak += lineBreakStep + 1;
                }

            }
            contentTable.add(lineTable).left();

            contentTable.row();
            //Menu Icons
            VisLabel label3 = new VisLabel("Menu Icons");
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();
            iconWidth = 0;


            Field[] fields = ClassReflection.getFields(MenuIconStyle.class);

            for (int i = 0, n = fields.length; i < n; i++) {
                Field field = fields[i];
                if (field.getType() == Drawable.class)
                    try {
                        Drawable drawable = (Drawable) field.get(CB.getSkin().getMenuIcon);
                        if (iconWidth == 0) {
                            iconWidth = drawable.getMinWidth();
                            iconHeight = drawable.getMinHeight();
                            lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + (CB.scaledSizes.MARGINx4) * 1.5f));
                            lineTable = new Table();
                            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                        }

                        if (drawable != null) {
                            lineTable.add(new Image(drawable)).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
                        } else {
                            lineTable.add(new VisLabel(Integer.toString(i))).width(new Value.Fixed(iconWidth)).height(new Value.Fixed(iconHeight));
                        }


                        if (i >= lineBreak) {
                            contentTable.add(lineTable).left();
                            contentTable.row();
                            lineTable = new Table();
                            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                            lineBreak += lineBreakStep + 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


            }
            contentTable.add(lineTable).left();
            contentTable.row();
        }
        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx4 * 5));
        contentTable.row();


        {
            VisLabel label3 = new VisLabel("ProgressBar SvgNinePatch");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            final ProgressBar progress1 = new ProgressBar(0, 100, 1, false, "default");
            contentTable.add(progress1).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();

            CB.postAsync(new Runnable() {
                float value = 0;

                @Override
                public void run() {
                    while (showing.get()) {
                        value += 1f;
                        if (value >= 200) value = 0;
                        final float progressValue = value < 50 ? 0 : value > 150 ? 100 : value - 50;
                        CB.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                progress1.setValue(progressValue);
                            }
                        });
                        Gdx.graphics.requestRendering();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx4 * 2));
        contentTable.row();
        {
            String Msg = Translation.Get("QuitReally");
            String Title = Translation.Get("Quit?");
            Window dialog = new ButtonDialog("QuitDialog", Msg, Title, MessageBoxButtons.YesNo, MessageBoxIcon.Stop, null);

            dialog.setStageBackground(null);

            VisLabel label3 = new VisLabel("DialogWindow SvgNinePatch");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            contentTable.add(dialog).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();
        }



        this.addActor(scrollPane);
    }

    @Override
    public void onShow() {
        sizeChanged();
    }

    public void onHide() {
        showing.set(false);
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
