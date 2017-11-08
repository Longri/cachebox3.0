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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ByteArray;
import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by longri on 30.10.17.
 */
class FileBrowserTest {

    // for simple server/clint implementation show:
    // https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/net/PingPongSocketExample.java


    static String SERVER_ADRESS = "0.0.0.0";
    static int PORT = 6011;

    static FileHandle workpath;
    static FileBrowserServer server;
    static FileBrowserClint clint;


    @BeforeAll
    static void setUp() {
        TestUtils.initialGdx();

        workpath = TestUtils.getResourceFileHandle("testsResources");
        server = new FileBrowserServer(workpath, PORT, null);
        clint = new FileBrowserClint(SERVER_ADRESS, PORT);
        server.startListening();
    }

    @AfterAll
    static void tearDown() {
    }


    @Test
    void getRootDir() throws InterruptedException {

        assertThat("Connection must be established", clint.connect());
        assertThat("Connection must be established", clint.connect());
        ServerFile root = clint.getFiles();
        ServerFileTest.assertRecursiveDir(workpath, root, workpath.parent().path());


        FileHandle file = workpath.child("lang/de/strings.ini");
        String sendPath = "sendTest/de";
        ServerFile serverFile = new ServerFile("", sendPath, true);
        FileHandle targetFile = workpath.child(sendPath).child("strings.ini");

        if (targetFile.exists()) {
            // delete test file
            targetFile.parent().parent().deleteDirectory();
            assertThat("transmitted File must delete", !targetFile.exists());
        }

        Thread.sleep(100);

        ArrayList<File> fileList = new ArrayList<>();
        fileList.add(file.file());

        try {
            assertThat("sendFiles must return true", clint.sendFiles(null, serverFile, root, fileList));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(100);

        assertThat("transmitted File must exist", targetFile.exists());
        assertThat("File.length must equals", targetFile.length() == file.length());

        ByteArray a1 = new ByteArray(targetFile.readBytes());
        ByteArray a2 = new ByteArray(file.readBytes());


        assertThat("File Content must equals", a1.equals(a2));


        // delete test file
        targetFile.parent().parent().deleteDirectory();
        assertThat("transmitted File must delete", !targetFile.exists());

        Thread.sleep(100);
    }

//    @Test
//    void excempleTest() {
//        // setup a server thread where we wait for incoming connections
//        // to the server
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ServerSocketHints hints = new ServerSocketHints();
//                ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, "localhost", 9998, hints);
//                // wait for the next client connection
//                Socket client = server.accept(null);
//                // read message and send it back
//                try {
//                    String message = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
//                    Gdx.app.log("PingPongSocketExample", "got client message: " + message);
//                    client.getOutputStream().write("PONG".getBytes());
//                    client.getOutputStream().write("\n".getBytes());
//                } catch (IOException e) {
//                    Gdx.app.log("PingPongSocketExample", "an error occured", e);
//                }
//
//            }
//        }).start();
//
//        // create the client send a message, then wait for the
//        // server to reply
//        SocketHints hints = new SocketHints();
//        Socket client = Gdx.net.newClientSocket(Net.Protocol.TCP, "localhost", 9998, hints);
//        try {
//            client.getOutputStream().write("PING".getBytes());
//            client.getOutputStream().write("\n".getBytes());
//            String response = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
//            Gdx.app.log("PingPongSocketExample", "got server message: " + response);
//        } catch (IOException e) {
//            Gdx.app.log("PingPongSocketExample", "an error occured", e);
//        }
//    }
//
//
//    @Test
//    void excempleStreamTest() {
//        // setup a server thread where we wait for incoming connections
//        // to the server
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ServerSocketHints hints = new ServerSocketHints();
//                ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, "localhost", 9997, hints);
//                // wait for the next client connection
//                Socket client = server.accept(null);
//                // read message and send it back
//                try {
//                    String message = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
//                    Gdx.app.log("PingPongSocketExample", "got client message: " + message);
//                    client.getOutputStream().write("PONG".getBytes());
//                    client.getOutputStream().write("\n".getBytes());
//                    client.getOutputStream().close();
//                } catch (IOException e) {
//                    Gdx.app.log("PingPongSocketExample", "an error occured", e);
//                }
//
//            }
//        }).start();
//
//        // create the client send a message, then wait for the
//        // server to reply
//        SocketHints hints = new SocketHints();
//        Socket client = Gdx.net.newClientSocket(Net.Protocol.TCP, "localhost", 9997, hints);
//        try {
//            client.getOutputStream().write("PING".getBytes());
//            client.getOutputStream().write("\n".getBytes());
//
//            InputStream is = client.getInputStream();
//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//
//            int nRead;
//            byte[] data = new byte[50];
//
//            while ((nRead = is.read(data, 0, data.length)) != -1) {
//                buffer.write(data, 0, nRead);
//            }
//            buffer.flush();
//
//            byte[] response = buffer.toByteArray();
//
//            Gdx.app.log("butes", response.toString());
//
//        } catch (IOException e) {
//            Gdx.app.log("PingPongSocketExample", "an error occured", e);
//        }
//    }
}