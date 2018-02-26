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

import de.longri.cachebox3.socket.filebrowser.ServerFile;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Created by Longri on 01.11.2017.
 */
public class LocalFileBrowserPane extends BorderPane {


    private static final Logger log = LoggerFactory.getLogger(LocalFileBrowserPane.class);

    private final ListView<ServerFile> listView = new ListView<>();

    private final Stage primaryStage;
    TreeView<String> treeView;
    private ServerFile selectedDir;
    private ServerFile currentListItemSelected;

    String lastStyle = "";
    Node actIntersectedNode = null;


    public LocalFileBrowserPane(Stage primaryStage) {
        this.primaryStage = primaryStage;


        VBox treeBox = new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);
        //setup the file browser root
        String hostName = "computer";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException x) {
        }
        TreeItem<String> rootNode = new TreeItem<>(hostName, new ImageView(CacheboxBrowserPane.getFileIcon("test.png")));
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path name : rootDirectories) {
            FilePathTreeItem treeNode = new FilePathTreeItem(name);
            rootNode.getChildren().add(treeNode);
        }
        rootNode.setExpanded(true);
        //create the tree view
        treeView = new TreeView<>(rootNode);
        //add everything to the tree pane
        treeBox.getChildren().addAll(new Label("File browser"), treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);


        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        Label lbl = new Label("LocalFileSystem");
        BorderPane lablePane = new BorderPane(lbl);
        this.setTop(lablePane);


        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().add(treeView);
        splitPane.getItems().add(listView);

        this.setCenter(splitPane);


    }


}
