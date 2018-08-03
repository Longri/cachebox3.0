/*
 * Copyright (C) 2018 team-cachebox.de
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

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by Longri on 27.02.2018.
 */
public class ProgressForm {
    protected final Stage dialogStage;
    protected final ProgressIndicator pin = new ProgressIndicator();
    protected final Label label = new Label();

    public ProgressForm() {
        label.setWrapText(true);
        pin.setPrefWidth(80);
        pin.setPrefHeight(80);
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setWidth(200);
        dialogStage.setHeight(200);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        label.setText("...\n...");

        pin.setProgress(-1F);

        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(pin, label);

        Scene scene = new Scene(vb);
        dialogStage.setScene(scene);
    }

    public void activateProgressBar(final Task<?> task) {
        pin.progressProperty().bind(task.progressProperty());
        dialogStage.show();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setText(final String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }
}
