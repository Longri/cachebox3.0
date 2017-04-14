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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.activities.FileChooser;
import de.longri.cachebox3.settings.Config;
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

        VisTextButton apiKey = new VisTextButton("createApiKey");
        apiKey.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                log.debug("Create Api Key clicked");
                PlatformConnector.getApiKey(new GenericCallBack<String>() {
                    @Override
                    public void callBack(String accessToken) {
                        log.debug("return create ApiKey :{}", accessToken);

                        GroundspeakAPI.CacheStatusValid = false;
                        GroundspeakAPI.CacheStatusLiteValid = false;

                        // store the encrypted AccessToken in the Config file
                        // wir bekommen den Key schon verschlüsselt, deshalb muss er
                        // nicht noch einmal verschlüsselt werden!
                        if (Config.StagingAPI.getValue()) {
                            Config.GcAPIStaging.setEncryptedValue(accessToken);
                        } else {
                            Config.GcAPI.setEncryptedValue(accessToken);
                        }

                        Config.AcceptChanges();

                        String act = GroundspeakAPI.getAccessToken();
                        if (act.length() > 0) {
                            GroundspeakAPI.getMembershipType(new GenericCallBack<Integer>() {
                                @Override
                                public void callBack(Integer status) {
                                    if (status >= 0) {
                                        log.debug("Read User Name/State {}/{}", GroundspeakAPI.memberName, status);
                                        Config.GcLogin.setValue(GroundspeakAPI.memberName);
                                        Config.AcceptChanges();
                                        CB.viewmanager.toast("Welcome : " + GroundspeakAPI.memberName);
                                    } else {
                                        CB.viewmanager.toast("Welcome : " + GroundspeakAPI.memberName);
                                        log.debug("Can't read UserName State: {}", GroundspeakAPI.memberName, status);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        Table tbl = new Table();
        tbl.add(test);
        tbl.row();
        tbl.add(testFile);
        tbl.row();
        tbl.add(apiKey);
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
