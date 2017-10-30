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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserClint {

    private final String CONNECT = "Connect\n";
    final static String CONNECTED = "Connected";

    private final String serverAddress;
    private final int serverPort;


    public FileBrowserClint(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public boolean connect() {

        SocketHints hints = new SocketHints();
        Socket client = Gdx.net.newClientSocket(Net.Protocol.TCP, serverAddress, serverPort, hints);
        try {
            client.getOutputStream().write(CONNECT.getBytes());
            String response = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
            Gdx.app.log("PingPongSocketExample", "got server message: " + response);

            if (response.equals(CONNECTED)) {
                return true;
            }
        } catch (IOException e) {
            Gdx.app.log("PingPongSocketExample", "an error occured", e);
        }
        return false;
    }

    public ServerFile getFiles() {
        ServerFile root = new ServerFile();
        return root;
    }


}
