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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserServer {

    final static String CONNECTED = "Connected\n";

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

                while (true){
                    // wait for the next client connection
                    Socket client = server.accept(null);
                    // read message and send it back
                    try {
                        String message = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
                        Gdx.app.log("PingPongSocketExample", "got client message: " + message);
                        client.getOutputStream().write(CONNECTED.getBytes());
                    } catch (Exception e) {
                        Gdx.app.log("PingPongSocketExample", "an error occured", e);
                    }
                }
            }
        }).start();

    }


}
