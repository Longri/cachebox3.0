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

import com.badlogic.gdx.utils.ObjectMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

/**
 * Created by Longri on 01.11.2017.
 */
public class LocalFileBrowserPane extends BorderPane {


    private static final Logger log = LoggerFactory.getLogger(LocalFileBrowserPane.class);
    private final ListView<File> listView = new ListView<>();
    private final Stage primaryStage;

    private File selectedDir;
    private final ObservableList<File> files = FXCollections.observableArrayList();
    private File currentListItemSelected;
    TreeView treeView;
    ObjectMap<File, FilePathTreeItem> map = new ObjectMap<>();

    public LocalFileBrowserPane(Stage primaryStage) {
        this.primaryStage = primaryStage;


        VBox vbox = new VBox();

        TreeItem<String> root = new TreeItem<>();

        TreeItem<String> croot = createNode(new File("c:/"));
        TreeItem<String> eroot = createNode(new File("e:/"));

        root.getChildren().add(croot);
        root.getChildren().add(eroot);


        treeView = new TreeView<>(root);
        treeView.setSkin(new FolderTreeViewSkin(treeView));


        vbox.getChildren().add(treeView);


        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    if (newValue instanceof FilePathTreeItem) {
                        selectedDir = ((FilePathTreeItem) newValue).file;
                        setList(((FilePathTreeItem) newValue));
                    } else {
                        selectedDir = null;
                        setList(null);
                    }
                }
            }
        });

        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    currentListItemSelected = listView.getSelectionModel().getSelectedItem();
                    selectDir(currentListItemSelected);
                }
            }
        });

        listView.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> list) {

                final AttachmentListCell cell = new AttachmentListCell();
                iniDrag(cell);
                return cell;
            }
        });

        Label lbl = new Label("LocalFileSystem");
        BorderPane lablePane = new BorderPane(lbl);
        this.setTop(lablePane);


        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().add(treeView);
        splitPane.getItems().add(listView);

        this.setCenter(splitPane);
    }

    private void selectDir(File currentListItemSelected) {
        FilePathTreeItem treeItem = map.get(currentListItemSelected);
        treeView.getSelectionModel().select(treeItem);

        int selectedIndex = treeView.getSelectionModel().getSelectedIndex();
        if (!((FolderTreeViewSkin) treeView.getSkin()).isIndexVisible(selectedIndex))
            treeView.scrollTo(selectedIndex);
    }


    private void setList(FilePathTreeItem item) {
        files.clear();
        if (item != null) {
            //load childs intern, for double click select
            item.getChildren();

            File[] allFiles = item.file.listFiles();
            if (allFiles != null) {
                Collections.addAll(files, allFiles);
            }
        }

        //TODO sort files (Dir's first)
        listView.setItems(files);

    }

    private void iniDrag(final Node node) {
        //#########################
        // Add mouse event handlers for the source


        node.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                log.debug("Event on Source: drag detected");
                dragDetected(event, node);
            }

        });

        node.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                log.debug("Event on Source: drag done");
                dragDone(event);
            }
        });

    }

    private void dragDetected(MouseEvent event, Node node) {

        File file = ((AttachmentListCell) node).getItem();

        // User can drag only when there is text in the source field
        String sourceText = file.getName();

        if (sourceText == null || sourceText.trim().equals("")) {
            event.consume();
            return;
        }

        // Initiate a drag-and-drop gesture
        Dragboard dragboard = node.startDragAndDrop(TransferMode.COPY_OR_MOVE);

        // Add the source text to the Dragboard

        try {
            File temp = File.createTempFile("test", "." + "txt");
            FileWriter fileWriter = new FileWriter(temp);
            fileWriter.write("Test Text");


            ClipboardContent content = new ClipboardContent();
            dragboard.setContent(content);


        } catch (IOException e) {
            e.printStackTrace();
        }


        event.consume();
    }

    private void dragDone(DragEvent event) {
        File file = ((AttachmentListCell) event.getSource()).getItem();
//        copyFileToClipBoard(file);
        event.consume();
    }

    private static class AttachmentListCell extends ListCell<File> {
        @Override
        public void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                Image fxImage = FileIconUtils.getFileIcon(item);
                ImageView imageView = new ImageView(fxImage);
                setGraphic(imageView);
                setText(item.getName());
            }
        }
    }

    private FilePathTreeItem createNode(final File f) {
        FilePathTreeItem item = new FilePathTreeItem(f) {

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
        map.put(f, item);
        return item;
    }

}
