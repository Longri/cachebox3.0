

//  Don't modify this file, it's created by tool 'extract_libgdx_test

/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.socket.filebrowser.*;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ByteArray;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.interfaces.ProgressHandler;
import de.longri.cachebox3.platform_test.AfterAll;
import de.longri.cachebox3.platform_test.BeforeAll;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;
import de.longri.cachebox3.platform_test.EXCLUDE_FROM_TRAVIS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by longri on 30.10.17.
 */
public class FileBrowserTest {

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

        workpath = TestUtils.getResourceFileHandle("testsResources", true);
        server = new FileBrowserServer(workpath, PORT, null);
        clint = new FileBrowserClint(SERVER_ADRESS, PORT);
        server.startListening();
    }

    @AfterAll
    static void tearDown() {
    }


    @Test
    public void getRootDir() throws InterruptedException, PlatformAssertionError {

        if (EXCLUDE_FROM_TRAVIS.VALUE) return;

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
        assertThat("File.length must charSequenceEquals", targetFile.length() == file.length());

        ByteArray a1 = new ByteArray(targetFile.readBytes());
        ByteArray a2 = new ByteArray(file.readBytes());


        assertThat("File Content must charSequenceEquals", a1.equals(a2));


        // delete test file
        targetFile.parent().parent().deleteDirectory();
        assertThat("transmitted File must delete", !targetFile.exists());

        Thread.sleep(100);
    }


    @Test
    public void getFileTest() throws InterruptedException, IOException, PlatformAssertionError {

        if (EXCLUDE_FROM_TRAVIS.VALUE) return;

        assertThat("Connection must be established", clint.connect());
        ServerFile root = clint.getFiles();
        ServerFileTest.assertRecursiveDir(workpath, root, workpath.parent().path());

        ServerFile serverFilePankowMap = root.getChild("pankow.map");
        assertThat("name must be 'pankow.map'", serverFilePankowMap != null && serverFilePankowMap.getName().equals("pankow.map"));

        File target = File.createTempFile("pankow", ".map");
        if (target.exists()) {
            target.delete();
        }

        final AtomicBoolean WAIT = new AtomicBoolean(true);

        final AtomicBoolean start = new AtomicBoolean(false);

        ProgressHandler progressHandler = new ProgressHandler() {
            @Override
            public void start() {
                start.set(true);
            }

            @Override
            public void updateProgress(CharSequence msg, long value, long maxValue) {

            }

            @Override
            public void success() {
                WAIT.set(false);
            }
        };

        clint.receiveFile(progressHandler, serverFilePankowMap, target);


        // wait for ready
        while (WAIT.get()) {
            Thread.sleep(10);
        }

        assertThat("Must started", start.get());

        FileHandle file = workpath.child("pankow.map");
        assertThat("File length must charSequenceEquals", target.length() == file.length());

        target.deleteOnExit();

    }


    @Test
    public void DeleteFileTest() throws InterruptedException, IOException, PlatformAssertionError {
        if (EXCLUDE_FROM_TRAVIS.VALUE) return;
        assertThat("Connection must be established", clint.connect());

        // create a File for delete test
        FileHandle deleteTestFile = workpath.child("DeleteTest.txt");
        if (deleteTestFile.exists()) {
            if (!deleteTestFile.delete()) {
                assertThat("Can't delete existing test file", false);
            }
        }

        deleteTestFile.writeString("Test String", false);

        ServerFile root = clint.getFiles();
        ServerFileTest.assertRecursiveDir(workpath, root, workpath.parent().path());

        ServerFile serverFileDeleteTest = root.getChild("DeleteTest.txt");
        assertThat("name must be 'DeleteTest.txt'", serverFileDeleteTest != null && serverFileDeleteTest.getName().equals("DeleteTest.txt"));
        assertThat("File must exist", deleteTestFile.exists());


        boolean success = clint.delete(serverFileDeleteTest);
        assertThat("Server response must be delete ServerFile==true", success);
        assertThat("File must not exist", !deleteTestFile.exists());
    }

}
