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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
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
import de.longri.cachebox3.gui.widgets.EditTextBox;
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
        this.setDebug(true, true);
        // createIconTable();
        createSvgNinePatchTable();
    }

    VisScrollPane scrollPane;

    protected void createIconTable() {
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
                lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + (CB.scaledSizes.MARGINx4) * 1.5f));
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

        attTable.row();
        //Menu Icons
        VisLabel label3 = new VisLabel("Menu Icons");
        lineTable = new Table();
        lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
        lineTable.add(label3);
        attTable.add(lineTable).left().expandX().fillX();
        attTable.row();
        iconWidth = 0;


        Field[] fields = ClassReflection.getFields(MenuIconStyle.class);

        for (int i = 0, n = fields.length; i < n; i++) {
            Field field = fields[i];
            if (field.getType() == Drawable.class)
                try {
                    Drawable drawable = (Drawable) field.get(CB.getSkin().getMenuIcon);
                    if (iconWidth == 0) {
                        iconWidth = CB.getScaledFloat(50);
                        iconHeight = CB.getScaledFloat(50);
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
                        attTable.add(lineTable).left();
                        attTable.row();
                        lineTable = new Table();
                        lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
                        lineBreak += lineBreakStep + 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


        }
        attTable.add(lineTable).left();

        this.addActor(scrollPane);
    }

    protected void createSvgNinePatchTable() {
        this.clear();

        float contentWidth = (Gdx.graphics.getWidth() * 0.75f) ;

        VisTable contentTable = new VisTable();
        contentTable.setDebug(true);
        scrollPane = new VisScrollPane(contentTable);
        {
            VisLabel label3 = new VisLabel("ProgressBar NinePatch");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
            int patch = 12;

            style.background = Utils.get9PatchFromSvg(Gdx.files.internal("progress_back.svg").read(),
                    patch, patch, patch, patch);
            style.knob = Utils.get9PatchFromSvg(Gdx.files.internal("progress_foreground.svg").read(),
                    patch, patch, patch, patch);
            style.knobBefore = Utils.get9PatchFromSvg(Gdx.files.internal("progress_foreground.svg").read(),
                    patch, patch, patch, patch);
            style.background.setLeftWidth(0);
            style.background.setRightWidth(0);
            style.background.setTopHeight(0);
            style.background.setBottomHeight(0);

            style.knob.setLeftWidth(0);
            style.knob.setRightWidth(0);
            style.knob.setTopHeight(0);
            style.knob.setBottomHeight(0);

            style.knobBefore.setLeftWidth(0);
            style.knobBefore.setRightWidth(0);
            style.knobBefore.setTopHeight(0);
            style.knobBefore.setBottomHeight(0);

            final VisProgressBar progress1 = new VisProgressBar(0f, 100f, 1f, false, style);
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
        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx4));
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

            final VisProgressBar progress1 = new VisProgressBar(0, 100, 1, false, "default");
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
            ButtonDialogStyle defaultButtonDialogStyle = VisUI.getSkin().get("default", ButtonDialogStyle.class);
            ButtonDialogStyle buttonDialogStyle = new ButtonDialogStyle();
            buttonDialogStyle.titleFont = defaultButtonDialogStyle.titleFont;
            buttonDialogStyle.titleFontColor = defaultButtonDialogStyle.titleFontColor;




            buttonDialogStyle.title = Utils.get9PatchFromSvg(Gdx.files.internal("skins/day/svg/dialog_title.svg").read(),
                    16, 39, 18, 33);

            buttonDialogStyle.footer = Utils.get9PatchFromSvg(Gdx.files.internal("skins/day/svg/dialog_footer.svg").read(),
                    16, 16, 1, 16);

            buttonDialogStyle.center = Utils.get9PatchFromSvg(Gdx.files.internal("skins/day/svg/dialog_center.svg").read(),
                    16, 16, 16, 1);

            buttonDialogStyle.header = Utils.get9PatchFromSvg(Gdx.files.internal("skins/day/svg/dialog_header.svg").read(),
                    16, 16, 16, 1);

            Window dialog = new ButtonDialog("QuitDialog", ButtonDialog.getMsgContentTable(Msg, MessageBoxIcon.Stop)
                    , Title, MessageBoxButtons.YesNo, null, buttonDialogStyle);

            dialog.setStageBackground(null);

            VisLabel label3 = new VisLabel("DialogWindow NinePatch");
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
        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx4));
        contentTable.row();
        {
            String Msg = Translation.Get("QuitReally");
            String Title = Translation.Get("Quit?");
            Window dialog = new ButtonDialog("QuitDialog", Msg, Title, MessageBoxButtons.YesNo, MessageBoxIcon.Stop, null) ;

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
