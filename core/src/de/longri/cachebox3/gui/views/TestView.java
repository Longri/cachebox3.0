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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.gui.drawables.FrameAnimationDrawable;
import de.longri.cachebox3.gui.skin.styles.FrameAnimationStyle;
import de.longri.cachebox3.gui.widgets.SelectBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.CacheTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(TestView.class);


    static private FileChooser fileChooser = new FileChooser("select folder", FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES);


    public TestView() {
        super("TestView");
        this.setDebug(true, true);

    }

    protected void create() {
        this.clear();
        VisTextButton test = new VisTextButton("SelectFolder");
        test.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                FileHandle directory = Gdx.files.absolute("./");
                fileChooser.setDirectory(directory);

                fileChooser.setSelectionReturnListener(new FileChooser.SelectionReturnListner() {
                    @Override
                    public void selected(FileHandle fileHandle) {
                        log.debug("Selected Folder: " + fileHandle);
                    }
                });

                //displaying chooser with fade in animation
                fileChooser.show();
                // getStage().addActor(fileChooser.fadeIn());

            }
        });

        VisTextButton testFile = new VisTextButton("SelectFile");
        testFile.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                FileChooser fileChooser = new FileChooser("select file", FileChooser.Mode.OPEN,
                        FileChooser.SelectionMode.FILES, "map");

                FileHandle directory = Gdx.files.absolute("./");
                fileChooser.setDirectory(directory);

                fileChooser.setSelectionReturnListener(new FileChooser.SelectionReturnListner() {
                    @Override
                    public void selected(FileHandle fileHandle) {
                        log.debug("Selected file: " + fileHandle);
                    }
                });

                //displaying chooser with fade in animation
                fileChooser.show();
                // getStage().addActor(fileChooser.fadeIn());

            }
        });

        VisTextButton apiKey = new VisTextButton("TextInput");
        apiKey.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                log.debug("Create Api Key clicked");
//                PlatformConnector.getSinglelineTextInput(new Input.TextInputListener() {
                PlatformConnector.getMultilineTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {

                    }

                    @Override
                    public void canceled() {

                    }
                }, "Platform Title", "MultiLine \nText ", "hinweis");
            }
        });


        SelectBox<CacheTypes> selectBox = new SelectBox();
        Array<CacheTypes> list = new Array<>();
        list.add(CacheTypes.ParkingArea);
        list.add(CacheTypes.ReferencePoint);
        list.add(CacheTypes.Trailhead);

        selectBox.set(list);
        selectBox.select(CacheTypes.ReferencePoint);

        FrameAnimationStyle style = VisUI.getSkin().get("download-animation", FrameAnimationStyle.class);
        FrameAnimationDrawable drawable = new FrameAnimationDrawable(style);
        Image image = new Image(drawable);

        Table tbl = new Table();
        tbl.setFillParent(true);
        tbl.defaults().pad(CB.scaledSizes.MARGIN);

        tbl.add(image);
        tbl.row();

        tbl.add(test);
        tbl.row();
        tbl.add(testFile);
        tbl.row();
        tbl.add(apiKey);
        tbl.row();
        tbl.add(selectBox);
        tbl.row().expandY().fillY().bottom();

        this.addActor(tbl);
    }


    @Override
    public void onShow() {
        create();
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
        // create();
    }
}
