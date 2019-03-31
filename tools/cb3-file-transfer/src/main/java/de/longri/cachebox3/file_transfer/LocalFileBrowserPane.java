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
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.interfaces.ProgressHandler;
import de.longri.cachebox3.socket.filebrowser.FileBrowserClint;
import de.longri.cachebox3.socket.filebrowser.ServerFile;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
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
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Longri on 01.11.2017.
 */
class LocalFileBrowserPane extends BorderPane {


    private static final Logger log = LoggerFactory.getLogger(LocalFileBrowserPane.class);
    private final ListView<File> listView = new ListView<>();
    private final Stage primaryStage;

    private File selectedDir;
    private final ObservableList<File> files = FXCollections.observableArrayList();
    private File currentListItemSelected;
    private String lastStyle = "";
    private Node actIntersectedNode = null;
    private TreeView treeView;
    private ObjectMap<File, FilePathTreeItem> map = new ObjectMap<>();
    private final FileBrowserClint clint;

    LocalFileBrowserPane(FileBrowserClint clint, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.clint = clint;

        VBox vbox = new VBox();

        TreeItem<String> root = new TreeItem<>();

        File[] paths;
        paths = File.listRoots();

        if (FileIconUtils.IS_WINDOWS) {
            for (File path : paths) {
                TreeItem<String> croot = createNode(path);
                root.getChildren().add(croot);
            }
        } else if (FileIconUtils.IS_Mac) {
            paths = new File("/Volumes").listFiles();
            for (File path : paths) {
                TreeItem<String> croot = createNode(path);
                root.getChildren().add(croot);
            }
        } else {
            File path = new File("/");
            TreeItem<String> croot = createNode(path);
            root.getChildren().add(croot);
        }


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

        iniDrop(listView);

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
        ClipboardContent content = new ClipboardContent();
        content.putFiles(java.util.Collections.singletonList(file));
        dragboard.setContent(content);

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

    private void mouseDragOver(final Event e) {
        final Dragboard db = ((DragEvent) e).getDragboard();

        if (db.getContent(MainPane.SERVER_FILE_DATA_FORMAT) != null) {
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

    private void mouseDragDropped(final Event e) {
        final Dragboard db = ((DragEvent) e).getDragboard();
        boolean success = false;
        if (db.getContent(MainPane.SERVER_FILE_DATA_FORMAT) != null) {
            success = true;

            ByteBuffer byteBuffer = (ByteBuffer) db.getContent(MainPane.SERVER_FILE_DATA_FORMAT);

            final ServerFile deserializeServerFile = new ServerFile();
            deserializeServerFile.deserialize(new BitStore(byteBuffer.array()));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    File target = null;

                    // get DropPath
                    if (actIntersectedNode == listView) {
                        target = selectedDir;
                    } else if (actIntersectedNode instanceof TreeCell) {
                        // get path from TreeviewItem
                        TreeCell cell = (TreeCell) actIntersectedNode;
                        FilePathTreeItem item = (FilePathTreeItem) cell.getTreeItem();
                        target = item.file;
                    }

                    log.debug("Drop {} ServerFiles to path {}", deserializeServerFile.getName(), target);
                    startTransfer(deserializeServerFile, target);
                    if (actIntersectedNode != null) actIntersectedNode.setStyle(lastStyle);
                }
            });
        }
        ((DragEvent) e).setDropCompleted(success);
        e.consume();
    }

    private void startTransfer(final ServerFile serverFile, final File target) {

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

                CB.postAsync(new NamedRunnable("LocalFileBrowserPane") {
                    @Override
                    public void run() {
                        clint.receiveFile(progressHandler, serverFile, target);
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
                updateFileList();
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

    private void updateFileList() {
        //TODO
    }


}
