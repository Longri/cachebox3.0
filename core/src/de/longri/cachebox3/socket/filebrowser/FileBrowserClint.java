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
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;

import java.io.*;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserClint {

    private final String CONNECT = "Connect";
    private final String GETFILES = "getFiles";
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
            client.getOutputStream().write("\n".getBytes());
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

        SocketHints hints = new SocketHints();
        Socket client = Gdx.net.newClientSocket(Net.Protocol.TCP, serverAddress, serverPort, hints);
        try {
            client.getOutputStream().write(GETFILES.getBytes());
            client.getOutputStream().write("\n".getBytes());

            InputStream is = client.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[4096];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            ServerFile deserializeServerFile = new ServerFile();
            deserializeServerFile.deserialize(new BitStore(buffer.toByteArray()));

            return deserializeServerFile;
        } catch (IOException e) {
            Gdx.app.log("PingPongSocketExample", "an error occured", e);
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }
        return root;
    }


    public void sendFile(ServerFile file) {
        if (file.isDirectory()) return;

        String path = file.getAbsolute();

    }


}
