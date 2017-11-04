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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.socket.filebrowser.FileBrowserClint;
import de.longri.cachebox3.socket.filebrowser.ServerFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Longri on 01.11.2017.
 */
public class FileBrowserPane extends BorderPane {


    private final FileBrowserClint clint;
    private final ObservableList<ServerFile> files = FXCollections.observableArrayList();
    private final ListView<ServerFile> listView = new ListView<>();
    TreeView<ServerFile> treeView;
    private ServerFile selectedDir;
    private ServerFile currentListItemSelected;
    Map<ServerFile, ServerFileTreeItem> map = new HashMap<>();


    public FileBrowserPane(FileBrowserClint clint) {
        this.clint = clint;

        selectedDir = clint.getFiles();



        treeView = new TreeView<>(new ServerFileTreeItem(selectedDir));


        populateMap(treeView.getRoot());

        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().add(treeView);
        splitPane.getItems().add(listView);

        this.setCenter(splitPane);
        setList(selectedDir);

        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                setList(((TreeItem<ServerFile>) newValue).getValue());
            }

        });

        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2) {
                    currentListItemSelected = listView.getSelectionModel().getSelectedItem();
                    selectDir(currentListItemSelected);
                }
            }
        });

    }

    private void populateMap(TreeItem<ServerFile> item) {
        if (item.getChildren().size() > 0) {
            for (TreeItem<ServerFile> subItem : item.getChildren()) {
                populateMap(subItem);
            }
        } else {
            ServerFile node = item.getValue();
            map.put( node, (ServerFileTreeItem) item);
        }
    }

    private void selectDir(ServerFile currentListItemSelected) {
        ServerFileTreeItem treeItem = map.get(currentListItemSelected);
        treeView.getSelectionModel().select(treeItem);
    }

    private void setList(ServerFile file) {
        files.clear();
        Array<ServerFile> serverFiles = file.getFiles();
        int n = serverFiles.size;
        while (n-- > 0) {
            ServerFile f = serverFiles.get(n);
            files.add(f);
        }

        //TODO sort files (Dir's first)

        listView.setItems(files);
    }

    private class ServerFileTreeItem extends TreeItem<ServerFile> {

        private boolean isFirstTimeChildren = true;
        private boolean isFirstTimeLeaf = true;
        private boolean isLeaf;
        private ServerFile file;

        public ServerFileTreeItem(ServerFile file) {
            super(file);
            this.file = file;
            if (file.isDirectory()) {
//                super.setGraphic(MainWindow.FOLDER_ICON);
            }
        }

        private ObservableList<TreeItem<ServerFile>> buildChildren(TreeItem<ServerFile> TreeItem) {
            ServerFile f = TreeItem.getValue();
            if (f != null && f.isDirectory()) {
                Array<ServerFile> files = f.getFiles();

                if (files != null) {
                    ObservableList<TreeItem<ServerFile>> children = FXCollections.observableArrayList();
                    for (ServerFile childFile : files) {
                        children.add(new ServerFileTreeItem(childFile));
                    }
                    return children;
                }
            }
            return FXCollections.emptyObservableList();
        }

        public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                ServerFile f = getValue();
                isLeaf = !f.isDirectory();
            }
            return isLeaf;
        }

        @Override
        public ObservableList<TreeItem<ServerFile>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }

        public ServerFile getServerFile() {
            return this.file;
        }
    }
}
