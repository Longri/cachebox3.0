/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.activities.BlockUiProgress_Activity;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.skin.styles.*;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.*;
import de.longri.cachebox3.gui.widgets.list_view.DefaultListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewType;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(TestView.class);

    private final AtomicBoolean showing = new AtomicBoolean(true);

    public TestView(BitStore reader) {
        super(reader);
    }

    public TestView() {
        super("TestView");
//        this.setDebug(true, true);
        CB.assertGlThread();
        if (!CB.isMocked()) createIconTable();
    }

    private VisScrollPane scrollPane;

    private void createIconTable() {
        this.clear();
        VisTable contentTable = new VisTable();

//        contentTable.setDebug(true);
        contentTable.setRound(false);

        scrollPane = new VisScrollPane(contentTable);
        float contentWidth = (Gdx.graphics.getWidth() * 0.75f);


        {// test ListView scissor

            VisLabel label3 = new VisLabel("List View Scissor");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            ListView emptyListView = new ListView(ListViewType.VERTICAL, false);
            emptyListView.setBackground(VisUI.getSkin().get(ActivityBase.ActivityBaseStyle.class).background);
            emptyListView.setEmptyString("EMPTY LISTVIEW EMPTY LISTVIEW EMPTY LISTVIEW ");
            emptyListView.setAdapter(null);
            contentTable.add(emptyListView).width(new Value.Fixed(contentWidth)).height(new Value.Fixed(contentWidth / 2)).pad(4);
            contentTable.row();


            final ListView listView = new ListView(ListViewType.VERTICAL, false);
            listView.setBackground(VisUI.getSkin().get(ActivityBase.ActivityBaseStyle.class).background);
            listView.setEmptyString("EMPTY LISTVIEW EMPTY LISTVIEW EMPTY LISTVIEW ");
            contentTable.add(listView).width(new Value.Fixed(contentWidth)).height(new Value.Fixed(contentWidth)).pad(4);
            contentTable.row();

            int itemCount = 10;
            final DefaultListViewAdapter items = new DefaultListViewAdapter();
            for (int i = 0; i < itemCount; i++) {
                MenuItem item = null;
                if (i == 0) {
                    item = new MenuItem(i, i, "Historische Objekte (Bodendenkmal, Grenzstein, Burg, Gedenkstätte, Ruine, Wegkreuz/Schrein, Wrack)", null);
                    item.setCheckable(true);
                } else {
//                    item = new MenuItem(i, i, "ITEM " + Integer.toString(i), null);
                }
                if (item != null) {
                    item.setTitle(item.getName());
                    item.pack();
                    items.add(item);
                }
            }

            listView.showWorkAnimationUntilSetAdapter();

            CB.postOnGLThreadDelayed(1000, new NamedRunnable("") {
                @Override
                public void run() {
                    listView.setAdapter(items);
                }
            });
        }


        {// test ScrollLabel

            VisLabel label3 = new VisLabel("Scroll AligmentLabel");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();


            Label.LabelStyle style = new Label.LabelStyle(VisUI.getSkin().get(Label.LabelStyle.class));
            style.background = VisUI.getSkin().get(EditTextStyle.class).background;

            String text = "123456789";
            String textLong = "123456789 123456789 123456789 123456789";
            String textSuperLong = "123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 " +
                    "123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 " +
                    "123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 " +
                    "123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ";

            contentTable.add(new ScrollLabel(text, style)).width(new Value.Fixed(contentWidth)).pad(4);
            contentTable.row();

            contentTable.add(new ScrollLabel(textLong, style)).width(new Value.Fixed(contentWidth)).pad(4);
            contentTable.row();

            contentTable.add(new ScrollLabel(textSuperLong, style)).width(new Value.Fixed(contentWidth)).pad(4);
            contentTable.row();


        }


//        {// testSensor record
//
//            VisLabel label3 = new VisLabel("Sensor record");
//            Table lineTable = new Table();
//            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
//            lineTable = new Table();
//            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
//            lineTable.add(label3);
//            contentTable.add(lineTable).left().expandX().fillX();
//            contentTable.row();
//
//
//            final VisTextButton recBtn = new VisTextButton(CB.sensoerIO.isRecord() ? " Stop Record" : "Start Record");
//            recBtn.addListener(new ClickLongClickListener() {
//                @Override
//                public boolean clicked(InputEvent event, float x, float y) {
//
//                    if (CB.sensoerIO.isRecord()) {
//                        CB.sensoerIO.stop();
//                    } else {
//                        CB.sensoerIO.start();
//                    }
//                    recBtn.setText(CB.sensoerIO.isRecord() ? " Stop Record" : "Start Record");
//                    return true;
//                }
//
//                @Override
//                public boolean longClicked(Actor actor, float x, float y) {
//                    return false;
//                }
//            });
//
//            final VisTextButton playBtn = new VisTextButton(CB.sensoerIO.isPlay() ? "Stop play" : "Start play");
//            playBtn.addListener(new ClickLongClickListener() {
//                @Override
//                public boolean clicked(InputEvent event, float x, float y) {
//                    if (CB.sensoerIO.isPlay()) {
//                        CB.sensoerIO.stopPlay();
//                    } else {
//                        fileChooser.setDirectory(Gdx.files.absolute(CB.WorkPath));
//                        fileChooser.setSelectionReturnListener(new FileChooser.SelectionReturnListner() {
//                            @Override
//                            public void selected(FileHandle fileHandle) {
//                                if (fileHandle != null) {
//                                    CB.sensoerIO.play(fileHandle);
//                                }
//                            }
//                        });
//                        fileChooser.show();
//                    }
//                    playBtn.setText(CB.sensoerIO.isPlay() ? "Stop play" : "Start play");
//                    return true;
//                }
//
//                @Override
//                public boolean longClicked(Actor actor, float x, float y) {
//                    return false;
//                }
//            });
//
//            contentTable.add(recBtn);
//            contentTable.row();
//            contentTable.add(playBtn);
//            contentTable.row();
//
//        }


        {// test Map Info Bubble

            AbstractCache cache = new MutableCache(0, 0);
            cache.setSize(CacheSizes.regular);
            cache.setType(CacheTypes.Traditional);
            cache.setName("CacheName CacheName CacheName CacheName");
            cache.setOwner("CacheOwner");
            cache.setFavoritePoints(1345); //TODO debug!
            cache.setFavorite(true);
            cache.setNumTravelbugs((short) 12);
            CacheListItem cacheListItem = (CacheListItem) CacheListItem.getListItem(0, cache, contentWidth * 1.2f);

            ListViewStyle style = VisUI.getSkin().get(ListViewStyle.class);

            cacheListItem.setBackground(style.firstItem);

            MapBubble mapBubble = new MapBubble(cache);
            mapBubble.layout();
            VisLabel label3 = new VisLabel("Map Info Bubble");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            contentTable.add(cacheListItem).width(new Value.Fixed(contentWidth * 1.2f)).pad(5);
            contentTable.row();

            contentTable.add(mapBubble).pad(20).height(new Value.Fixed(mapBubble.getHeight()));
            contentTable.row();

        }


        {// test FloatControl

            final FloatControl floatControl = new FloatControl(0f, 100f, 1f, true, new FloatControl.ValueChangeListener() {
                @Override
                public void valueChanged(float value, boolean dragged) {
                    log.debug("FloatControl value changed to {} with drag {}", value, dragged);
                }
            });


            VisLabel label3 = new VisLabel("FloatControl");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            contentTable.add(floatControl).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();

            final FloatControl floatControl2 = new FloatControl(0f, 100f, 1f, false, new FloatControl.ValueChangeListener() {
                @Override
                public void valueChanged(float value, boolean dragged) {
                    log.debug("FloatControl value changed to {} with drag {}", value, dragged);
                }
            });

            floatControl2.setValue(30f);

            contentTable.add(floatControl2).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();

            CB.postAsync(new NamedRunnable("TestView:Progress") {
                float value = 0;

                @Override
                public void run() {
                    while (showing.get()) {
                        value += 1f;
                        if (value >= 200) value = 0;
                        final float progressValue = value < 50 ? 0 : value > 150 ? 100 : value - 50;
                        CB.postOnGlThread(new NamedRunnable("TestView") {
                            @Override
                            public void run() {
                                floatControl.setValue(progressValue);
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


        {
            VisLabel label3 = new VisLabel("CB_ProgressBar SvgNinePatch");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            final CB_ProgressBar progress1 = new CB_ProgressBar(0, 100, 1, false, "default");
            contentTable.add(progress1).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();

            CB.postAsync(new NamedRunnable("TestView:Progress") {
                float value = 0;

                @Override
                public void run() {
                    while (showing.get()) {
                        value += 1f;
                        if (value >= 200) value = 0;
                        final float progressValue = value < 50 ? 0 : value > 150 ? 100 : value - 50;
                        CB.postOnGlThread(new NamedRunnable("TestView") {
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
            contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2 * 2));
            contentTable.row();
        }

        {// test Circle Drawable Widget

            final CircularProgressWidget circPro = new CircularProgressWidget();

            VisLabel label3 = new VisLabel("Progress Test");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            contentTable.add(circPro).pad(20);
            contentTable.row();

            circPro.setProgressMax(100);
            circPro.setProgress(0);

            circPro.addListener(new ClickLongClickListener() {
                @Override
                public boolean clicked(InputEvent event, float x, float y) {

                    final BlockUiProgress_Activity activity = new BlockUiProgress_Activity();
                    activity.show();
                    CB.postAsync(new NamedRunnable("TestView:CircleProgress") {
                        float value = 0;

                        @Override
                        public void run() {
                            while (value < 200) {
                                value += 1f;
                                final float progressValue = value < 50 ? 0 : value > 150 ? 100 : value - 50;
                                EventHandler.fire(new IncrementProgressEvent((int) progressValue,
                                        "Progress Test AligmentLabel", progressValue == 0 ? -1 : 100));
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    return true;
                }

                @Override
                public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                    return true;
                }
            });

            CB.postAsync(new NamedRunnable("TestView:CircleProgress") {
                float value = 0;

                @Override
                public void run() {
                    while (showing.get()) {
                        value += 1f;
                        if (value >= 200) value = 0;
                        final float progressValue = value < 50 ? -1 : value > 150 ? 100 : value - 50;
                        CB.postOnGlThread(new NamedRunnable("TestView") {
                            @Override
                            public void run() {
                                circPro.setProgressMax(progressValue >= 0 ? 100 : -1);
                                circPro.setProgress((int) progressValue);
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


        {// test Spinner with MessageBox Icons

            Array<MessageBoxIcon> itemList = new Array<>();
            itemList.add(MessageBoxIcon.Asterisk);
            itemList.add(MessageBoxIcon.Error);
            itemList.add(MessageBoxIcon.Exclamation);
            itemList.add(MessageBoxIcon.Hand);
            itemList.add(MessageBoxIcon.Information);
            itemList.add(MessageBoxIcon.None);
            itemList.add(MessageBoxIcon.Question);
            itemList.add(MessageBoxIcon.Stop);
            itemList.add(MessageBoxIcon.Warning);
            itemList.add(MessageBoxIcon.Powerd_by_GC_Live);
            itemList.add(MessageBoxIcon.GC_Live);
            itemList.add(MessageBoxIcon.ExpiredApiKey);


            final SelectBox<MessageBoxIcon> selectBox = new SelectBox();
            selectBox.setHideWithItemClick(false);
            selectBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    MessageBoxIcon icon = selectBox.getSelected();
                    if (icon != MessageBoxIcon.None) {
                        MessageBox.show("MessageBox with \n" + icon.getName(), "MessageBoxTitle", MessageBoxButtons.OK, icon, null);
                        selectBox.select(MessageBoxIcon.None);
                    }
                }
            });
            selectBox.set(itemList);
            selectBox.select(MessageBoxIcon.None);


            VisLabel label3 = new VisLabel("MessageBox Icon Test");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            contentTable.add(selectBox).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();
        }


        {// test CancelProgressDialog
            VisTextButton button = new VisTextButton("CancelProgressDialog");
            button.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    new CancelProgressDialog("test", "Test Progress Dialog",
                            new ProgressCancelRunnable() {
                                float value = 0;
                                float progressValue = 0;

                                @Override
                                public void canceled() {
                                    log.debug("Progress canceled ");
                                }

                                @Override
                                public void run() {
                                    while (!isCanceled()) {
                                        value += 1f;
                                        if (value >= 200) value = 0;
                                        progressValue = value < 50 ? 0 : value > 150 ? 100 : value - 50;
                                        setProgress(progressValue, "Progress:" + Float.toString(progressValue));
                                        Gdx.graphics.requestRendering();
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    log.debug("Canceled Async Task");
                                }
                            }).show();
                }
            });

            VisLabel label3 = new VisLabel("CancelProgressDialog");
            Table lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable = new Table();
            lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);
            lineTable.add(label3);
            contentTable.add(lineTable).left().expandX().fillX();
            contentTable.row();

            contentTable.add(button).width(new Value.Fixed(contentWidth)).pad(20);
            contentTable.row();
        }


        {// test AdjustableStarWidget

            IntProperty property = new IntProperty();
            StarsStyle starsStyle = VisUI.getSkin().get("cachelist", StarsStyle.class);
            CacheSizeStyle cacheSizeStyle = VisUI.getSkin().get("cachelist", CacheSizeStyle.class);
            final AdjustableStarWidget adjustableStarWidget = new AdjustableStarWidget(AdjustableStarWidget.Type.STAR,
                    "Title", property, starsStyle, cacheSizeStyle);
            adjustableStarWidget.setValue(6);


            ListViewStyle listViewStyle = CB.getSkin().get(ListViewStyle.class);
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
                    lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + (CB.scaledSizes.MARGINx2) * 1.5f));
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
                    lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + (CB.scaledSizes.MARGINx2) * 1.5f));
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
                            lineBreakStep = lineBreak = (int) (Gdx.graphics.getWidth() / (iconWidth + (CB.scaledSizes.MARGINx2) * 1.5f));
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
        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2 * 5));
        contentTable.row();


        {
            CharSequence Msg = Translation.get("QuitReally");
            CharSequence Title = Translation.get("Quit?");
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

            contentTable.add(dialog).width(new Value.Fixed(dialog.getPrefWidth())).pad(20);
            contentTable.row();
        }


        this.addActor(scrollPane);
    }

    @Override
    public void onShow() {
        sizeChanged();
        CB.postAsyncDelayd(2000, new NamedRunnable("Try exception indicator") {
            @Override
            public void run() {
                int i = 12 / 0;
                System.out.print(i);
            }
        });
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


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {

        Menu cm = new Menu("TestViewContextMenu");

        cm.addItem(10000, "show MSG Box 1", true);


        cm.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {

                switch (item.getMenuItemId()) {
                    case 10000:
                        String title = "Fehler";
                        String msg = "Der Cache [2 Advent: der Wherigo (wo ist der Weihnachtsmann)] ist nicht in der aktuellen DB.\\nDiese Draft kann nicht gewählt werden!";
                        MessageBoxButtons btn = MessageBoxButtons.OK;
                        MessageBoxIcon icn = MessageBoxIcon.Error;

                        MessageBox.show(msg, title, btn, icn, null);
                        break;
                }

                return false;
            }
        });

        return cm;
    }
}
