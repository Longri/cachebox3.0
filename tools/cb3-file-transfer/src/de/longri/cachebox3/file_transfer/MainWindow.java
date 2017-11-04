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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationLogger;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import de.longri.cachebox3.utils.BuildInfo;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.impl.DummyLogApplication;


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
        Gdx.net = new LwjglNet();
        Gdx.files = new LwjglFiles();
        Gdx.app = new DummyLogApplication() {
            @Override
            public ApplicationType getType() {
                return ApplicationType.HeadlessDesktop;
            }
        };
        Gdx.app.setApplicationLogger(new LwjglApplicationLogger());


    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        connectPane = new ConnectPane(this);

        primaryStage.setHeight(150);
        primaryStage.setWidth(250);

        Scene scene = new Scene(connectPane);

        primaryStage.setTitle("Cachebox File Transfer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}