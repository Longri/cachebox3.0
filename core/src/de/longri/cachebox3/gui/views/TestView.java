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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.help.GestureHelp;
import de.longri.cachebox3.gui.help.HelpWindow;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.widgets.ColorWidget;
import de.longri.cachebox3.gui.widgets.NumPad;
import de.longri.cachebox3.settings.Config;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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
                Menu icm = new Menu("Test Menu");

                MenuItem mi = icm.addItem(MenuID.MI_LAYER, "Layer");

                mi.setMoreMenu(getMoreMenu1());


                mi = icm.addItem(MenuID.MI_RENDERTHEMES, "Renderthemes");


                MenuItem item = icm.addItem(MenuID.MI_MAPVIEW_OVERLAY_VIEW, "overlays");
                item.setIcon(new SpriteDrawable(CB.getSprite("closeIcon")));
                item = icm.addCheckableItem(MenuID.MI_ALIGN_TO_COMPSS, "AlignToCompass", true);
                item.setIcon(new SpriteDrawable(CB.getSprite("closeIcon")));

                icm.addItem(MenuID.MI_CENTER_WP, "CenterWP");
                // icm.addItem(MenuID.MI_SETTINGS, "settings", Sprites.getSprite(IconName.settings.name()));
                // icm.addItem(MenuID.MI_SEARCH, "search", SpriteCache.Icons.get(27));
                item = icm.addItem(MenuID.MI_MAPVIEW_VIEW, "view");
                item.setIcon(new SpriteDrawable(CB.getSprite("cache")));


                //icm.addItem(MenuID.MI_TREC_REC, "RecTrack");
                //   icm.addItem(MenuID.MI_MAP_DOWNOAD, "MapDownload");

                icm.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public boolean onItemClick(MenuItem item) {
                        log.debug(item.toString() + " clicked");
                        return true;
                    }
                });

                icm.show();
            }
        });

        VisTextButton test2Button = new VisTextButton("Toast");
        test2Button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                CB.viewmanager.toast("Test langer Text der dann selbst umgebrochen werden sollte");
            }
        });


        final VisTextButton test3Button = new VisTextButton("HelpWindow");
        test3Button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                HelpWindow helpWindow = new HelpWindow(GestureHelp.getHelpEllipseFromActor(test3Button));
                helpWindow.show();
            }
        });


        VisTextButton numPadOkCancel = new VisTextButton("o/c");
        numPadOkCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (numPad != null) {
                    TestView.this.removeChild(numPad);
                }
                numPad = new NumPad(NumPad.OptionalButton.OK, NumPad.OptionalButton.CANCEL);
                numPad.pack();
                numPad.setPosition(0, 150);
                TestView.this.addActor(numPad);
            }
        });

        VisTextButton numDotPadOkCancel = new VisTextButton("d/o/c");
        numDotPadOkCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (numPad != null) {
                    TestView.this.removeChild(numPad);
                }
                numPad = new NumPad(NumPad.OptionalButton.OK, NumPad.OptionalButton.CANCEL, NumPad.OptionalButton.DOT);
                numPad.pack();
                numPad.setPosition(0, 150);
                TestView.this.addActor(numPad);
            }
        });

        VisTextButton numDot = new VisTextButton("dot");
        numDot.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (numPad != null) {
                    TestView.this.removeChild(numPad);
                }
                numPad = new NumPad(NumPad.OptionalButton.DOT);
                numPad.pack();
                numPad.setPosition(0, 150);
                TestView.this.addActor(numPad);
            }
        });

        VisTextButton num = new VisTextButton("none");
        num.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (numPad != null) {
                    TestView.this.removeChild(numPad);
                }
                numPad = new NumPad(CB.scaledSizes.WINDOW_WIDTH);
                numPad.pack();
                numPad.setPosition(0, 150);
                TestView.this.addActor(numPad);
            }
        });


        VisTable table = new VisTable();
        table.add(numPadOkCancel);
        table.add(numDotPadOkCancel);
        table.add(numDot);
        table.add(num);


        table.row();
        table.add(testButton);
        table.add(test2Button);
        table.add(test3Button);
        table.setPosition(0, 0);
        table.pack();
        this.addActor(table);

    }

    NumPad numPad;

    private void addNumpad() {

    }

    private Menu getMoreMenu1() {

        Menu menu = new Menu("More1");

        MenuItem item = menu.addItem(1, "item1");
        item = menu.addItem(2, "item2");
        item.setMoreMenu(getMoreMenu2());
        item = menu.addItem(3, "item3");
        item = menu.addItem(4, "item4");
        item = menu.addItem(5, "item5");
        item = menu.addItem(6, "item6");
        item = menu.addItem(7, "item7");
        item = menu.addItem(8, "item8");
        item = menu.addItem(9, "item9");
        item = menu.addItem(10, "item10");
        item = menu.addItem(11, "item11");
        item = menu.addItem(12, "item12");
        item = menu.addItem(13, "item13");
        item = menu.addItem(14, "item14");
        item = menu.addItem(15, "item15");


        menu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                log.debug("item: More1/" + item.getName() + " clicked");
                return true;
            }
        });


        return menu;

    }

    private Menu getMoreMenu2() {

        Menu menu = new Menu("More2");

        MenuItem item = menu.addItem(1, "item1");
        item = menu.addItem(2, "item2");
        item = menu.addItem(3, "item3");
        item = menu.addItem(4, "item4");
        item = menu.addItem(5, "item5");
        item = menu.addItem(6, "item6");
        item = menu.addItem(7, "item7");
        item = menu.addItem(8, "item8");
        item = menu.addItem(9, "item9");
        item = menu.addItem(10, "item10");
        item = menu.addItem(11, "item11");
        item = menu.addItem(12, "item12");
        item = menu.addItem(13, "item13");
        item = menu.addItem(14, "item14");
        item = menu.addItem(15, "item15");


        menu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                log.debug("item: More2/" + item.getName() + " clicked");
                return true;
            }
        });


        return menu;

    }


    @Override
    public void onShow() {
        StringBuilder sb = new StringBuilder();
        sb.append("LaunchCount:" + Config.AppRaterlaunchCount.getValue());
        nameLabel.setText(sb.toString());
    }


    @Override
    public void dispose() {

    }
}
