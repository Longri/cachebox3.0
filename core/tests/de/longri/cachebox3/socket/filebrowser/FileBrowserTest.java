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
import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by longri on 30.10.17.
 */
class FileBrowserTest {

    // for simple server/clint implementation show:
    // https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/net/PingPongSocketExample.java


    static String SERVER_ADRESS = "localhost";
    static int PORT = 9999;

    static FileHandle workpath;
    static FileBrowserServer server;
    static FileBrowserClint clint;


    @BeforeAll
    static void setUp() {
        TestUtils.initialGdx();

        workpath = TestUtils.getResourceFileHandle("testsResources");
        server = new FileBrowserServer(workpath, SERVER_ADRESS, PORT);
        clint = new FileBrowserClint(SERVER_ADRESS, PORT);
        server.startListening();
    }

    @AfterAll
    static void tearDown() {
    }


    @Test
    void connection() throws InterruptedException {
        assertThat("Connection must be established", clint.connect());
        assertThat("Connection must be established", clint.connect());
    }

    @Test
    void getRootDir() {
        ServerFile root = clint.getFiles();
        ServerFileTest.assertRecursiveDir(workpath, root);
    }

    @Test
    void excempleTest() {
        // setup a server thread where we wait for incoming connections
        // to the server
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHints hints = new ServerSocketHints();
                ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, "localhost", 9998, hints);
                // wait for the next client connection
                Socket client = server.accept(null);
                // read message and send it back
                try {
                    String message = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
                    Gdx.app.log("PingPongSocketExample", "got client message: " + message);
                    client.getOutputStream().write("PONG\n".getBytes());
                } catch (IOException e) {
                    Gdx.app.log("PingPongSocketExample", "an error occured", e);
                }

            }
        }).start();

        // create the client send a message, then wait for the
        // server to reply
        SocketHints hints = new SocketHints();
        Socket client = Gdx.net.newClientSocket(Net.Protocol.TCP, "localhost", 9998, hints);
        try {
            client.getOutputStream().write("PING\n".getBytes());
            String response = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
            Gdx.app.log("PingPongSocketExample", "got server message: " + response);
        } catch (IOException e) {
            Gdx.app.log("PingPongSocketExample", "an error occured", e);
        }
    }
}