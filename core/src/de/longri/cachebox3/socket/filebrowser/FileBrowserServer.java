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
import de.longri.serializable.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;

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
    private boolean listining = false;

    public FileBrowserServer(FileHandle workPath, int clintPort) {
        this.workPath = workPath;
        this.clintPort = clintPort;
    }

    public void startListening() {
        // setup a server thread where we wait for incoming connections
        // to the server
        listining = true;
        log.debug("Start listening for FileTransfer");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHints hints = new ServerSocketHints();
                hints.acceptTimeout = 0;
                ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, null, clintPort, hints);

                SocketHints socketHints = new SocketHints();
                socketHints.connectTimeout = 0;
                socketHints.keepAlive = true;

                // wait for the next client connection
                Socket socket = server.accept(socketHints);

                InputStream in = socket.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in);
                DataInputStream dis = new DataInputStream(bis);

                OutputStream os = socket.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos);

                while (listining) {
                    try {


                        String message = dis.readUTF();

                        if (message.equals("Connect")) {
                            dos.writeUTF("Connected");
                            dos.flush();
                        } else if (message.equals("getFiles")) {
                            try {
                                ServerFile root = ServerFile.getDirectory(workPath);
                                BitStore writer = new BitStore();
                                root.serialize(writer);

                                byte[] data = writer.getArray();
                                dos.writeInt(data.length);
                                dos.write(data);
                                dos.flush();
                            } catch (NotImplementedException e) {
                                e.printStackTrace();
                            }
                        } else if (message.equals(FileBrowserClint.SENDFILE)) {
                            String path = dis.readUTF();
                            FileHandle outputFile = workPath.child(path);
                            outputFile.parent().mkdirs();


                            long fileLength = dis.readLong();

                            FileOutputStream fos = new FileOutputStream(outputFile.file());
                            BufferedOutputStream fbos = new BufferedOutputStream(fos);


                            for(int j = 0; j < fileLength; j++) {
                                fbos.write(bis.read());
                            }
                            fbos.close();

                            dos.writeUTF(TRANSFERRED);
                            dos.flush();
                        }
                    } catch (Exception e) {
                        if (e.getCause() instanceof SocketTimeoutException) {
                            //ignore, while waiting
                        } else {
                            log.error("an error occured", e);
                        }
                    }
                }
                server.dispose();
                server = null;
                log.debug(" listening stopped");
            }
        }).start();
    }

    public void stopListening() {
        listining = false;
        log.debug("Stop listening for FileTransfer");
    }

    private String getResponse(String message) {

        if (message.equals("Connect")) {
            return CONNECTED;
        } else if (message.equals(FileBrowserClint.SENDFILE)) {
            return TRANSFERRED;
        }

        return ERROR;
    }


}
