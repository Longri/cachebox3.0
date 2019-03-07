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
package de.longri.cachebox3.file_transfer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationLogger;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import de.longri.cachebox3.utils.BuildInfo;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Created by Longri on 01.11.2017.
 */
public class MainWindow extends Application {

    private ConnectPane connectPane;
    Stage primaryStage;
//    static ImageView FOLDER_ICON = new ImageView(new Image(getClass().getResourceAsStream("duke_44x80.png")));


    @Override
    public void init() {

        if (Gdx.net != null) return;
        BuildInfo.setTestBuildInfo("JUnitTest");

        Gdx.app = new HeadlessApplication(new Game() {
            @Override
            public void create() {

            }
        });
        Gdx.net = Gdx.app.getNet();
        Gdx.files =  Gdx.app.getFiles();

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        connectPane = new ConnectPane(this);

        primaryStage.setHeight(160);
        primaryStage.setWidth(300);

        Scene scene = new Scene(connectPane);

        primaryStage.setTitle("Cachebox File Transfer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}