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
import de.longri.cachebox3.interfaces.ProgressHandler;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserClint {

    public final static int BUFFER_SIZE = 512;

    private final Logger log = LoggerFactory.getLogger(FileBrowserServer.class);

    static final String CONNECT = "Connect";
    static final String SENDFILE = "sendFile";
    static final String GETFILES = "getFiles";
    final static String CONNECTED = "Connected";
    static final String CLOSE = "close";

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


    public boolean sendFile(ProgressHandler progressHandler, String path, FileHandle file) {
        if (file.isDirectory()) {
            if (progressHandler != null) progressHandler.sucess();
            return false;
        }

        try {

            dos.writeUTF(SENDFILE);
            dos.writeUTF(path);
            long length = file.length();
            dos.writeLong(length);
            dos.flush();

            FileInputStream fis = new FileInputStream(file.file());
            BufferedInputStream bis = new BufferedInputStream(fis);

            if (progressHandler != null) progressHandler.updateProgress("", 0, length);
            int theByte = 0;
            long sendet = 0;
            while ((theByte = bis.read()) != -1) {
                bos.write(theByte);
                if (progressHandler != null) {
                    progressHandler.updateProgress("", sendet++, length);
                }
            }
            bis.close();
            bos.flush();

            String response = dis.readUTF();

            if (response.equals(FileBrowserServer.TRANSFERRED)) {
                return true;
            }
        } catch (IOException e) {
            log.error("an error occured", e);
        } finally {
            if (progressHandler != null) progressHandler.sucess();
        }
        return false;
    }


    public void sendCloseEvent() {
        try {
            dos.writeUTF(CLOSE);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
