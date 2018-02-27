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
package de.longri.cachebox3.socket.filebrowser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.interfaces.ProgressHandler;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserClint {

    public final static int BUFFER_SIZE = 512;


    private final Logger log = LoggerFactory.getLogger(FileBrowserServer.class);

    static final String CONNECT = "Connect";
    static final String SENDFILE = "sendFiles";
    static final String GETFILES = "getFiles";
    static final String GETFILE = "getServerFile";
    final static String CONNECTED = "Connected";
    static final String CLOSE = "close";
    static final String FILE_DOSENTEXIST = "file dosen't exist";
    static final String START_FEILE_TRANSFER = "start File transfer";

    private final String serverAddress;
    private final int serverPort;


    public FileBrowserClint(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    Socket socket;
    OutputStream os;
    BufferedOutputStream bos;
    DataOutputStream dos;
    InputStream in;
    BufferedInputStream bis;
    DataInputStream dis;


    public boolean connect() {

        if (socket != null) {
            if (socket.isConnected())
                return true;
        }

        try {
            SocketHints hints = new SocketHints();
            socket = Gdx.net.newClientSocket(Net.Protocol.TCP, serverAddress, serverPort, hints);

            os = socket.getOutputStream();
            bos = new BufferedOutputStream(os, BUFFER_SIZE);
            dos = new DataOutputStream(bos);
            in = socket.getInputStream();
            bis = new BufferedInputStream(in, BUFFER_SIZE);
            dis = new DataInputStream(bis);

            dos.writeUTF(CONNECT);
            dos.flush();

            String response = dis.readUTF();
            log.debug("got server message: " + response);

            if (response.equals(CONNECTED)) {
                return true;
            }
        } catch (Exception e) {
            log.error("an error occured", e);
        }
        return false;
    }

    public ServerFile getFiles() {
        ServerFile root = new ServerFile();

        try {

            dos.writeUTF(GETFILES);
            dos.flush();

            int length = dis.readInt();
            byte[] data = new byte[length];

            int offset = 0;
            while (offset < length) {
                int readLength = length - offset;
                if (readLength > BUFFER_SIZE) {
                    readLength = BUFFER_SIZE;
                }
                dis.read(data, offset, readLength);
                offset += readLength;
            }


            ServerFile deserializeServerFile = new ServerFile();
            deserializeServerFile.deserialize(new BitStore(data));

            return deserializeServerFile;
        } catch (IOException e) {
            Gdx.app.log("PingPongSocketExample", "an error occured", e);
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }
        return root;
    }


    public boolean sendFiles(ProgressHandler progressHandler, ServerFile path, ServerFile workingDir, List<File> files) {

        if (!path.isDirectory()) throw new RuntimeException("Path must be a directory!");


        ObjectMap<String, FileHandle> fileMap = new ObjectMap<>();
        addToFileList(fileMap, path, workingDir, files);

        long sendet = 0;
        long completeLength = 0;
        //iterate over all for calculate complete length
        for (ObjectMap.Entry<String, FileHandle> entry : fileMap.iterator()) {
            completeLength += entry.value.length();
        }

        boolean error = false;

        //iterate over all for send
        for (ObjectMap.Entry<String, FileHandle> entry : fileMap.iterator()) {
            try {
                dos.writeUTF(SENDFILE);
                dos.writeUTF(entry.key);
                long length = entry.value.length();
                dos.writeLong(length);
                dos.flush();

                InputStream fis = entry.value.read();
                BufferedInputStream bis = new BufferedInputStream(fis);

                if (progressHandler != null) {
                    progressHandler.start();
                    progressHandler.updateProgress("", 0, completeLength);
                }
                int theByte = 0;

                int left = progressHandler != null ? 0 : -1;
                while ((theByte = bis.read()) != -1) {
                    try {
                        bos.write(theByte);
                    } catch (IOException e) {
                        //TODO handle broken Pipe with "java.net.SocketException: Broken pipe (Write failed)"
                        e.printStackTrace();
                    }

                    if (sendet % 1024 == left) {
                        progressHandler.updateProgress("", sendet, completeLength);
                    }
                    sendet++;
                }
                if (progressHandler != null) {
                    progressHandler.updateProgress("", sendet, completeLength);
                }
                bis.close();
                bos.flush();
                String response = dis.readUTF();// wait for next
                if (!response.equals(FileBrowserServer.TRANSFERRED)) {
                    error = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (progressHandler != null) progressHandler.success();
        return !error;
    }

    protected void addToFileList(ObjectMap<String, FileHandle> map, ServerFile path, ServerFile workingDir, List<File> files) {
        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().equals(".DS_Store")) continue; //don't store
                map.put(path.getTransferPath(workingDir, file), Gdx.files.absolute(file.getAbsolutePath()));
            } else if (file.isDirectory()) {
                List<File> subFiles = new ArrayList<>();
                ServerFile subDir = path.child(file.getName(), true);
                for (File f : file.listFiles())
                    subFiles.add(f);
                addToFileList(map, subDir, workingDir, subFiles);
            }
        }
    }


    public void sendCloseEvent() {
        try {
            dos.writeUTF(CLOSE);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveFile(final ProgressHandler progressHandler, final ServerFile serverFile, final File target) {

        if (serverFile.isDirectory()) {
            throw new RuntimeException("can't transfer directory");
        }

        CB.postAsync(new NamedRunnable("Receive Server file") {
            @Override
            public void run() {

                //serialise ServerFile
                BitStore store = new BitStore();
                try {
                    serverFile.serialize(store);
                } catch (NotImplementedException e) {
                    log.error("can't store ServerFile");
                    progressHandler.success();
                    return;
                }

                try {
                    byte[] serverFileBytes = store.getArray();
                    dos.writeUTF(GETFILE);
                    dos.flush();

                    int length = serverFileBytes.length;
                    dos.writeInt(length);
                    dos.flush();

                    int offset = 0;
                    while (offset < length) {
                        int writeLength = length - offset;
                        if (writeLength > FileBrowserClint.BUFFER_SIZE) {
                            writeLength = FileBrowserClint.BUFFER_SIZE;
                        }
                        dos.write(serverFileBytes, offset, writeLength);
                        dos.flush();
                        offset += writeLength;
                    }

                    String response = dis.readUTF();
                    if (response.equals(START_FEILE_TRANSFER)) {

                        //receive file data
                        long fileLength = dis.readLong();
                        FileOutputStream fos = new FileOutputStream(target);
                        BufferedOutputStream fbos = new BufferedOutputStream(fos);
                        for (int j = 0; j < fileLength; j++) {
                            fbos.write(bis.read());
                        }
                        fbos.close();
                    }

                    log.debug("got server message: " + response);

                } catch (NotImplementedException e) {
                    log.error("can't store ServerFile");
                    progressHandler.success();
                    return;
                } catch (IOException e) {
                    log.error("can't receive ServerFile");
                    progressHandler.success();
                    return;
                }


                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // finish
                log.debug("ServerFile transfer success");
                progressHandler.success();
            }
        });

    }
}
