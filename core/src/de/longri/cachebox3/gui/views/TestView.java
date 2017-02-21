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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.gui.activities.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(TestView.class);

    VisTextButton test;

    static private FileChooser fileChooser = new FileChooser("select folder", FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES);


    public TestView() {
        super("TestView");

    }

    protected void create() {
        this.clear();
        test = new VisTextButton("SelectFolder");
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

        Table tbl = new Table();
        tbl.add(test);
        tbl.row();
        tbl.add(testFile);
        tbl.setBounds(10, 10, 300, 300);
        this.addActor(tbl);
    }


    @Override
    public void onShow() {

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
