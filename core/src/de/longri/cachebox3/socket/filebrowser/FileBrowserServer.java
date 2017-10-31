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
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;

import java.io.*;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserServer {

    private final String GETFILES = "getFiles";
    final static String CONNECTED = "Connected";
    final static String ERROR = "ERROR";
    final static String TRANSFERRED = "Transferred";

    private final FileHandle workPath;
    private final String clintAddress;
    private final int clintPort;

    public FileBrowserServer(FileHandle workPath, String clintAddress, int clintPort) {
        this.workPath = workPath;
        this.clintAddress = clintAddress;
        this.clintPort = clintPort;
    }

    public void startListening() {
        // setup a server thread where we wait for incoming connections
        // to the server
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHints hints = new ServerSocketHints();
                ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, clintAddress, clintPort, hints);

                while (true) {
                    // wait for the next client connection
                    Socket client = server.accept(null);
                    // read message and send it back
                    try {
                        String message = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();

                        if (message.startsWith(FileBrowserClint.SENDFILE)) {
                            String path = message.replace(FileBrowserClint.SENDFILE, "");

                            FileHandle outputFile = workPath.child(path);
                            outputFile.parent().mkdirs();
                            outputFile.write(client.getInputStream(),false);


                            client.getOutputStream().write(getResponse(FileBrowserClint.SENDFILE));
                            client.getOutputStream().close();
                        } else {
                            client.getOutputStream().write(getResponse(message));
                            client.getOutputStream().close();
                        }
                    } catch (Exception e) {
                        Gdx.app.log("PingPongSocketExample", "an error occured", e);
                    }
                }
            }
        }).start();
    }


    private byte[] getResponse(String message) {

        if (message.equals("Connect")) {
            return CONNECTED.getBytes();
        } else if (message.equals(GETFILES)) {
            try {
                ServerFile root = ServerFile.getDirectory(workPath);
                BitStore writer = new BitStore();
                root.serialize(writer);
                return writer.getArray();
            } catch (NotImplementedException e) {
                e.printStackTrace();
            }
        } else if (message.equals(FileBrowserClint.SENDFILE)) {
            return TRANSFERRED.getBytes();
        }

        return ERROR.getBytes();
    }

}
