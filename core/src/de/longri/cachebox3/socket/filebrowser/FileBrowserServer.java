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
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;

import static de.longri.cachebox3.socket.filebrowser.FileBrowserClint.BUFFER_SIZE;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserServer {

    private static Logger log = LoggerFactory.getLogger(FileBrowserServer.class);

    private final String GETFILES = "getFiles";
    final static String CONNECTED = "Connected";
    final static String ERROR = "ERROR";
    final static String TRANSFERRED = "Transferred";

    private final FileHandle workPath;
    private final int clintPort;
    private final ConectionCloesRemoteReciver closeReciver;
    private boolean listening = false;
    ServerSocket server;
    Socket socket;
    InputStream in;
    BufferedInputStream bis;
    DataInputStream dis;

    OutputStream os;
    BufferedOutputStream bos;
    DataOutputStream dos;


    public FileBrowserServer(FileHandle workPath, int clintPort, ConectionCloesRemoteReciver reciver) {
        this.workPath = workPath;
        this.clintPort = clintPort;
        this.closeReciver = reciver;
    }

    public void startListening() {
        // setup a server thread where we wait for incoming connections
        // to the server
        listening = true;
        log.debug("Start listening for FileTransfer");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocketHints hints = new ServerSocketHints();
                    hints.acceptTimeout = 0;
                    server = Gdx.net.newServerSocket(Net.Protocol.TCP, null, clintPort, hints);

                    SocketHints socketHints = new SocketHints();
                    socketHints.connectTimeout = 0;
                    socketHints.keepAlive = true;

                    // wait for the next client connection
                    socket = server.accept(socketHints);

                    in = socket.getInputStream();
                    bis = new BufferedInputStream(in, BUFFER_SIZE);
                    dis = new DataInputStream(bis);

                    os = socket.getOutputStream();
                    bos = new BufferedOutputStream(os, BUFFER_SIZE);
                    dos = new DataOutputStream(bos);
                } catch (Exception e) {
                    if (!listening) {
                        //connection is closed
                        return;
                    }
                }

                while (listening) {
                    try {
                        String message = null;
                        try {
                            message = dis.readUTF();
                        } catch (Exception e) {
                            if (!listening) {
                                //connection is closed
                                break;
                            }
                        }

                        if (message.equals(FileBrowserClint.CONNECT)) {
                            dos.writeUTF(FileBrowserClint.CONNECTED);
                            dos.flush();
                        } else if (message.equals(FileBrowserClint.CLOSE)) {
                            if (closeReciver != null) closeReciver.close();
                        } else if (message.equals(FileBrowserClint.GETFILES)) {
                            ServerFile root = ServerFile.getDirectory(workPath);
                            BitStore writer = new BitStore();
                            root.serialize(writer);

                            byte[] data = writer.getArray();
                            int length = data.length;
                            dos.writeInt(length);
                            dos.flush();

                            int offset = 0;
                            while (offset < length) {
                                int writeLength = length - offset;
                                if (writeLength > BUFFER_SIZE) {
                                    writeLength = BUFFER_SIZE;
                                }
                                dos.write(data, offset, writeLength);
                                dos.flush();
                                offset += writeLength;
                            }
                        } else if (message.equals(FileBrowserClint.SENDFILE)) {
                            String path = dis.readUTF();
                            FileHandle outputFile = workPath.child(path);
                            outputFile.parent().mkdirs();


                            long fileLength = dis.readLong();

                            FileOutputStream fos = new FileOutputStream(outputFile.file());
                            BufferedOutputStream fbos = new BufferedOutputStream(fos);


                            for (int j = 0; j < fileLength; j++) {
                                fbos.write(bis.read());
                            }
                            fbos.close();

                            dos.writeUTF(TRANSFERRED);
                            dos.flush();
                        } else if (message.equals(FileBrowserClint.GETFILE)) {

                            // read bytes for ServerFile store
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

                            // search file
                            FileHandle fileHandle = workPath.child(deserializeServerFile.getAbsoluteWithoutRoot());

                            if (fileHandle.exists()) {
                                dos.writeUTF(FileBrowserClint.START_FEILE_TRANSFER);
                                dos.flush();

                                dos.writeLong(fileHandle.length());
                                dos.flush();

                                InputStream fis = fileHandle.read();
                                BufferedInputStream bis = new BufferedInputStream(fis);
                                int theByte = 0;
                                while ((theByte = bis.read()) != -1) {
                                    try {
                                        bos.write(theByte);
                                    } catch (IOException e) {
                                        //TODO handle broken Pipe with "java.net.SocketException: Broken pipe (Write failed)"
                                        e.printStackTrace();
                                    }
                                }
                                bis.close();
                                bos.flush();
                            } else {
                                dos.writeUTF(FileBrowserClint.FILE_DOSENTEXIST);
                                dos.flush();
                            }


                        } else if (message.equals(FileBrowserClint.DELETE_SERVER_FILE)) {
                            // read bytes for ServerFile store
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

                            // search file
                            FileHandle fileHandle = workPath.child(deserializeServerFile.getAbsoluteWithoutRoot());

                            if (fileHandle.exists()) {
                                boolean delSuccess = false;
                                if (fileHandle.isDirectory()) {
                                    delSuccess = fileHandle.deleteDirectory();
                                } else {
                                    delSuccess = fileHandle.delete();
                                }

                                if (delSuccess) {
                                    dos.writeUTF(FileBrowserClint.DELETE_SUCCESS);
                                } else {
                                    dos.writeUTF(FileBrowserClint.DELETE_FAILED);
                                }
                                dos.flush();
                            } else {
                                dos.writeUTF(FileBrowserClint.FILE_DOSENTEXIST);
                                dos.flush();
                            }
                        }
                    } catch (Exception e) {
                        if (e.getCause() instanceof SocketTimeoutException) {
                            //ignore, while waiting
                        } else {
                            log.error("an error occured", e);
                        }
                    }
                }
                log.debug(" listening stopped");
            }
        }).start();
    }

    public void stopListening() {
        listening = false;
        log.debug("Stop listening for FileTransfer");
        if (server != null) server.dispose();
        server = null;

        if (socket != null) {
            socket.dispose();
            socket = null;
        }

        try {
            if (in != null) in.close();
            if (bis != null) bis.close();
            if (dis != null) dis.close();

            if (os != null) os.close();
            if (bos != null) bos.close();
            if (dos != null) dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface ConectionCloesRemoteReciver {
        void close();
    }


}
