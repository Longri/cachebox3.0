/*
 * Copyright (C) 2017 team-cachebox.de
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

import de.longri.cachebox3.socket.filebrowser.FileBrowserClint;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

/**
 * Created by Longri on 02.11.2017.
 */
public class ConnectPane extends BorderPane {


    private final MainWindow main;
    private FileBrowserClint clint;

    ConnectPane(MainWindow main) {
        this.main = main;

        VBox vb = new VBox();

        Label label0 = new Label("connect to Cachebox");

        Label label1 = new Label("Ip Address:");
        final TextField textField = new TextField();
        HBox hb = new HBox();
        hb.getChildren().addAll(label1, textField);
        hb.setSpacing(10);

        Label label2 = new Label("port:");
        final TextField textField2 = new TextField("9988");
        HBox hb2 = new HBox();
        hb2.getChildren().addAll(label2, textField2);
        hb2.setSpacing(10);


        Button connectButton = new Button("Connect");
        connectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                connect(textField.getText(), textField2.getText());
            }
        });


        vb.getChildren().addAll(label0, hb, hb2, connectButton);
        vb.setSpacing(10);
        this.setCenter(vb);


        main.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                if (clint != null) {
                    clint.sendCloseEvent();
                }
            }
        });


    }


    void connect(String address, String port) {
        clint = new FileBrowserClint(address, Integer.parseInt(port));
        if (clint.connect()) {
            MainPane fb = new MainPane(clint, this.main.primaryStage);

            Scene scene = new Scene(fb);
            this.main.primaryStage.setScene(scene);
            this.main.primaryStage.setHeight(500);
            this.main.primaryStage.setWidth(1000);

        }
    }


}
