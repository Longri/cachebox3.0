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
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.socket.filebrowser.FileBrowserClint;
import de.longri.cachebox3.socket.filebrowser.ServerFile;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Longri on 01.11.2017.
 */
public class FileBrowserPane extends BorderPane {


    private static final Logger log = LoggerFactory.getLogger(FileBrowserPane.class);

    private final FileBrowserClint clint;
    private final ObservableList<ServerFile> files = FXCollections.observableArrayList();
    private final ListView<ServerFile> listView = new ListView<>();
    TreeView<ServerFile> treeView;
    private ServerFile selectedDir;
    private ServerFile currentListItemSelected;
    ObjectMap<ServerFile, ServerFileTreeItem> map = new ObjectMap<>();


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
                if (newValue != null) setList(((TreeItem<ServerFile>) newValue).getValue());
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


        iniDragAndDrop(listView);
//        iniDragAndDrop(treeView);

        treeView.setCellFactory(new Callback<TreeView<ServerFile>, TreeCell<ServerFile>>() {
            @Override
            public TreeCell<ServerFile> call(TreeView<ServerFile> param) {
                final Tooltip tooltip = new Tooltip();
                TreeCell<ServerFile> cell = new TreeCell<ServerFile>() {
                    @Override
                    public void updateItem(ServerFile item, boolean empty) {
                        super.updateItem(item, empty);
                    }
                };
                iniDragAndDrop(cell);
                return cell;
            }
        });
    }



    private void populateMap(TreeItem<ServerFile> item) {
        if (item.getChildren().size() > 0) {

//            iniDragAndDrop((ServerFileTreeItem)item);

            map.put(item.getValue(), (ServerFileTreeItem) item);
            for (TreeItem<ServerFile> subItem : item.getChildren()) {
                populateMap(subItem);
            }
        } else {
            ServerFile node = item.getValue();
            map.put(node, (ServerFileTreeItem) item);
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


    //#############################################################################
    //  Drag&Drop
    //#############################################################################

    private void iniDragAndDrop(Node node) {
        node.setOnDragOver(new EventHandler() {
            @Override
            public void handle(final Event event) {
                log.debug("OnDragOver");
                mouseDragOver(event);
            }
        });

        node.setOnDragDropped(new EventHandler() {
            @Override
            public void handle(final Event event) {
                log.debug("OnDragDropped");
                mouseDragDropped(event);
            }
        });

        node.setOnDragExited(new EventHandler() {

            @Override
            public void handle(final Event event) {
                log.debug("OnDragExited");
                Node node = ((DragEvent) event).getPickResult().getIntersectedNode();
                node.setStyle(lastStyle);
            }
        });
    }


    private void mouseDragDropped(final Event e) {
        final Dragboard db = ((DragEvent) e).getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    log.debug("Drop files", db.getFiles());

                    // Only get the first file from the list
                    final File file = db.getFiles().get(0);

                }
            });
        }
        ((DragEvent) e).setDropCompleted(success);
        e.consume();
    }

    String lastStyle = "";
    Node actIntersectedNode = null;

    private void mouseDragOver(final Event e) {
        final Dragboard db = ((DragEvent) e).getDragboard();


        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".map")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpg");

        if (db.hasFiles()) {
            if (isAccepted) {

                Node node = ((DragEvent) e).getPickResult().getIntersectedNode();

                if (node instanceof ListCell) {
                    node = ((ListCell) node).getListView();
                }

                if (node == listView || node instanceof TreeCell) {
                    if (node != actIntersectedNode) {
                        if (actIntersectedNode != null) {
//                        actIntersectedNode.setStyle("-fx-border-color: #C6C6C6;");
                            actIntersectedNode.setStyle(lastStyle);
                        }
                        actIntersectedNode = node;
                        lastStyle = actIntersectedNode.getStyle();
                    }

                    actIntersectedNode.setStyle("-fx-border-color: red;"
                            + "-fx-border-width: 5;"
                            + "-fx-background-color: #C6C6C6;"
                            + "-fx-border-style: solid;");
                    ((DragEvent) e).acceptTransferModes(TransferMode.COPY);
                }


            }
        } else {
            e.consume();
        }
    }



    





}
