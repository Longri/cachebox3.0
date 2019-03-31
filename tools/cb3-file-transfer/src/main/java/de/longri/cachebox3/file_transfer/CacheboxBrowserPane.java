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
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.interfaces.ProgressHandler;
import de.longri.cachebox3.socket.filebrowser.FileBrowserClint;
import de.longri.cachebox3.socket.filebrowser.ServerFile;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static de.longri.cachebox3.file_transfer.MainPane.SERVER_FILE_DATA_FORMAT;

/**
 * Created by Longri on 01.11.2017.
 */
public class CacheboxBrowserPane extends BorderPane {


    private static final Logger log = LoggerFactory.getLogger(CacheboxBrowserPane.class);

    private final FileBrowserClint clint;
    private final ObservableList<ServerFile> files = FXCollections.observableArrayList();
    private final ListView<ServerFile> listView = new ListView<>();
    private final ServerFile workingDir;
    private final Stage primaryStage;
    TreeView<ServerFile> treeView;
    private ServerFile selectedDir;
    private ServerFile currentListItemSelected;
    ObjectMap<ServerFile, ServerFileTreeItem> map = new ObjectMap<>();
    String lastStyle = "";
    Node actIntersectedNode = null;


    public CacheboxBrowserPane(FileBrowserClint clint, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.clint = clint;
        workingDir = clint.getFiles();
        treeView = new TreeView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        Label lbl = new Label("Cachebox");
        BorderPane lablePane = new BorderPane(lbl);
        this.setTop(lablePane);


        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().add(treeView);
        splitPane.getItems().add(listView);
        this.setCenter(splitPane);

        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    selectedDir = ((TreeItem<ServerFile>) newValue).getValue();
                    setList(selectedDir);
                }
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
        updateFileList(workingDir);
    }


    private void updateFileList(final ServerFile root) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ServerFile lastSelectedDir = selectedDir;
                selectedDir = root == null ? clint.getFiles() : root;
                treeView.setRoot(new ServerFileTreeItem(selectedDir));
                populateMap(treeView.getRoot());

                if (lastSelectedDir != null) {
                    //get new read selected Dir
                    ServerFileTreeItem item = map.get(lastSelectedDir);
                    selectedDir = item.getServerFile();
                    selectDir(selectedDir);
                } else {
                    setList(selectedDir);
                }
                iniDrop(listView);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Set<Node> treeCells = treeView.lookupAll(".tree-cell");
                        for (Node cell : treeCells) {
                            iniDrop(cell);
                        }
                    }
                });
            }
        });
    }


    private void populateMap(TreeItem<ServerFile> item) {
        if (item.getChildren().size() > 0) {

//            iniDrop((ServerFileTreeItem)item);

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


        listView.setCellFactory(new Callback<ListView<ServerFile>, ListCell<ServerFile>>() {
            @Override
            public ListCell<ServerFile> call(ListView<ServerFile> list) {
                final AttachmentListCell cell = new AttachmentListCell();
                iniDrag(cell);
                ContextMenu contextMenu = new ContextMenu();
                MenuItem deleteItem = new MenuItem();
                deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
                deleteItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        final ServerFile severFile = cell.getItem();
                        log.debug("Delete ServerFile {}", severFile.getName());

                        CB.postAsync(new NamedRunnable("Delete ServerFile") {
                            @Override
                            public void run() {
                                if (!clint.delete(severFile)) {
                                    // show failed msg box
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setTitle("Error");
                                            alert.setHeaderText("Delete failed");
                                            alert.showAndWait();

                                        }
                                    });
                                } else {
                                    // get new ServerFile root
                                    updateFileList(null);
                                }
                            }
                        });

                    }
                });
                contextMenu.getItems().addAll(deleteItem);
                cell.setContextMenu(contextMenu);
                return cell;
            }
        });

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
                        if (childFile.isDirectory())
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

    private static class AttachmentListCell extends ListCell<ServerFile> {
        @Override
        public void updateItem(ServerFile item, boolean empty) {
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


    //#############################################################################
    //  Drag&Drop
    //#############################################################################

    private void iniDrop(final Node node) {
        node.setOnDragOver(new EventHandler() {
            @Override
            public void handle(final Event event) {
//                log.debug("OnDragOver");
                mouseDragOver(event);
            }
        });

        node.setOnDragDropped(new EventHandler() {
            @Override
            public void handle(final Event event) {
//                log.debug("OnDragDropped");
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

    private void mouseDragDropped(final Event e) {
        final Dragboard db = ((DragEvent) e).getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            final List<File> files = db.getFiles();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ServerFile path = null;

                    // get DropPath
                    if (actIntersectedNode == listView) {
                        path = selectedDir;
                    } else if (actIntersectedNode instanceof TreeCell) {
                        // get path from TreeviewItem
                        TreeCell cell = (TreeCell) actIntersectedNode;
                        ServerFileTreeItem item = (ServerFileTreeItem) cell.getTreeItem();
                        path = item.getServerFile();
                    }

                    log.debug("Drop {} files to path {}", files.size(), path.getAbsolute());
                    startTransfer(path, files);
                }
            });
        }
        ((DragEvent) e).setDropCompleted(success);
        e.consume();
    }

    private void startTransfer(final ServerFile path, final List<File> files) {

        final de.longri.cachebox3.file_transfer.ProgressForm pForm = new ProgressForm();


        final AtomicLong progressMax = new AtomicLong(0);
        final AtomicLong progressValue = new AtomicLong(0);
        final AtomicBoolean wait = new AtomicBoolean(true);
        final ProgressHandler progressHandler = new ProgressHandler() {

            long startTime;
            final StringBuilder sb = new StringBuilder();

            @Override
            public void start() {
                startTime = System.currentTimeMillis();
            }

            @Override
            public void updateProgress(CharSequence msg, long value, long maxValue) {
                progressMax.set(maxValue);
                progressValue.set(value);

                try {
                    sb.length = 0;//reset StringBuilder
                    int speedInKBps = 0;
                    double speedInBps = 0.0;
                    long timeInSecs = (System.currentTimeMillis() - startTime) / 1000;
                    speedInBps = value / timeInSecs;
                    speedInKBps = (int) (speedInBps / 1024D);
                    long remainingTime = (long) ((maxValue / speedInBps) - timeInSecs);

                    sb.append(speedInKBps).append(" KB/s\n remaining time: ");

                    if (remainingTime > 60) {
                        double min = (double) remainingTime / 60.0D;
                        sb.append(String.format("%.2f", min)).append(" min");
                    } else {
                        sb.append(remainingTime).append(" sec");
                    }


                    pForm.setText(sb.toString());
                } catch (ArithmeticException e) {
                    //do nothing
                }
            }

            @Override
            public void success() {
                wait.set(false);
            }
        };

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {

                final AtomicBoolean WAIT_READY = new AtomicBoolean(true);

                CB.postAsync(new NamedRunnable("CacheboxBrowserPane") {
                    @Override
                    public void run() {
                        clint.sendFiles(progressHandler, path, workingDir, files);
                        WAIT_READY.set(false);
                    }
                });


                while (WAIT_READY.get()) {
                    try {
                        updateProgress(progressValue.get(), progressMax.get());
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                updateProgress(20, 20);
                updateFileList(null);
                return null;
            }
        };

        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        // in real life this method would get the result of the task
        // and update the UI based on its value:
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                pForm.getDialogStage().close();
            }
        });

        Stage dialogStage = pForm.getDialogStage();
        dialogStage.show();
        dialogStage.setX(primaryStage.getX() + (primaryStage.getWidth() / 2) - 50);
        dialogStage.setY(primaryStage.getY() + (primaryStage.getHeight() / 2) - 25);

        Thread thread = new Thread(task);
        thread.start();

        actIntersectedNode.setStyle(lastStyle);
        actIntersectedNode = null;
    }

    private void mouseDragOver(final Event e) {
        final Dragboard db = ((DragEvent) e).getDragboard();

        if (db.hasFiles()) {
            Node node = ((DragEvent) e).getPickResult().getIntersectedNode();
            if (node instanceof ListCell) {
                node = ((ListCell) node).getListView();
            }

            if (node == listView || node instanceof TreeCell) {
                if (node != actIntersectedNode) {
                    if (actIntersectedNode != null) {
                        actIntersectedNode.setStyle(lastStyle);
                    }
                    actIntersectedNode = node;
                    lastStyle = actIntersectedNode.getStyle();
                }
                actIntersectedNode.setStyle("-fx-border-color: red;"
                        + "-fx-border-width: 3;"
                        + "-fx-background-color: #C6C6C6;"
                        + "-fx-border-style: solid;");
                ((DragEvent) e).acceptTransferModes(TransferMode.COPY);
            }
        } else {
            e.consume();
        }
    }


    private void dragDetected(MouseEvent event, Node node) {

        ServerFile file = ((AttachmentListCell) node).getItem();


        // User can drag only when there is text in the source field
        String sourceText = file.getName();

        if (sourceText == null || sourceText.trim().equals("")) {
            event.consume();
            return;
        }

        // Initiate a drag-and-drop gesture
        Dragboard dragboard = node.startDragAndDrop(TransferMode.COPY_OR_MOVE);
//      TODO  file.setDragBoard(dragboard);


        // Add the source text to the Dragboard
        ClipboardContent content = new ClipboardContent();

        BitStore writer = new BitStore();
        file.serialize(writer);

        ByteBuffer byteBuffer = null;
        byteBuffer = ByteBuffer.wrap(writer.getArray());

        content.put(SERVER_FILE_DATA_FORMAT, byteBuffer);
        dragboard.setContent(content);

        event.consume();
    }

    private void dragDone(DragEvent event) {
        ServerFile file = ((AttachmentListCell) event.getSource()).getItem();
        copyFileToClipBoard(file);
        event.consume();
    }


    private void copyFileToClipBoard(ServerFile file) {
        try {

            Clipboard clipboard = Clipboard.getSystemClipboard();
            //TODO    Dragboard db = file.getDragBoard();

            ClipboardContent content = new ClipboardContent();

            String name = "test";
            String ext = "txt";


            File temp = File.createTempFile(name, "." + ext);
            FileWriter fileWriter = new FileWriter(temp);
            fileWriter.write("Test Text");

            content.putFiles(java.util.Collections.singletonList(temp));
            //TODO     db.setContent(content);
            clipboard.setContent(content);

            temp.deleteOnExit();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
