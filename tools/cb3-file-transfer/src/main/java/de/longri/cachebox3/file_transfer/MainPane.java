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

import de.longri.cachebox3.socket.filebrowser.FileBrowserClint;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by Longri on 25.02.18.
 */
public class MainPane extends BorderPane {

    private final Stage primaryStage;
    public final static DataFormat SERVER_FILE_DATA_FORMAT = new DataFormat("ServerFile");

    public MainPane(FileBrowserClint clint, Stage primaryStage) {
        this.primaryStage = primaryStage;

        CacheboxBrowserPane fb = new CacheboxBrowserPane(clint, primaryStage);
        LocalFileBrowserPane lfb = new LocalFileBrowserPane(clint, primaryStage);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().add(lfb);
        splitPane.getItems().add(fb);


        this.setCenter(splitPane);


    }

}
