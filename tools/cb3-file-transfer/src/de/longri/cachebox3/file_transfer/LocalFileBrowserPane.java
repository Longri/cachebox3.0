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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Longri on 01.11.2017.
 */
public class LocalFileBrowserPane extends BorderPane {


    private static final Logger log = LoggerFactory.getLogger(LocalFileBrowserPane.class);

    private final ListView<ServerFile> listView = new ListView<>();

    private final Stage primaryStage;


    public LocalFileBrowserPane(Stage primaryStage) {
        this.primaryStage = primaryStage;


        VBox vbox = new VBox();

        TreeItem<String> root = createNode(new File("c:/"));
        TreeView treeView = new TreeView<String>(root);

        vbox.getChildren().add(treeView);


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

    private FilePathTreeItem createNode(final File f) {
        return new FilePathTreeItem(f) {

            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<String>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    File f = this.file;
                    if (f.isDirectory()) {
                        File[] files = f.listFiles();
                        isLeaf = (files == null) || files.length == 0;
                    } else {
                        isLeaf = f.isFile();
                    }
                }
                return isLeaf;
            }

            private ObservableList<FilePathTreeItem> buildChildren(
                    FilePathTreeItem treeItem) {
                File f = treeItem.file;
                if (f == null) {
                    return FXCollections.emptyObservableList();
                }
                if (f.isFile()) {
                    return FXCollections.emptyObservableList();
                }
                File[] files = f.listFiles();
                if (files != null) {
                    ObservableList<FilePathTreeItem> children = FXCollections
                            .observableArrayList();
                    for (File childFile : files) {
                        if (childFile.isDirectory())
                            children.add(createNode(childFile));
                    }
                    return children;
                }
                return FXCollections.emptyObservableList();
            }
        };
    }

}
