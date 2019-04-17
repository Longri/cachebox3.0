

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.socket.filebrowser.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.serializable.BitStore;
import de.longri.cachebox3.platform_test.BeforeAll;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 30.10.2017.
 */
public class ServerFileTest {


    private static FileHandle workpath;

    @BeforeAll
    public static void setUp() {
        TestUtils.initialGdx();
        workpath = TestUtils.getResourceFileHandle("testsResources", false);
    }


    @Test
    public void getDirectory() throws PlatformAssertionError {
        ServerFile root = ServerFile.getDirectory(workpath);
        assertThat("Root must be a Directory", root.isDirectory());
        assertRecursiveDir(workpath, root, Gdx.files.absolute(workpath.file().getAbsolutePath()).parent().file().getAbsolutePath());
    }

    @Test
    public void serialize() throws PlatformAssertionError {

        ServerFile root = ServerFile.getDirectory(workpath);

        BitStore writer = new BitStore();
        root.serialize(writer);


        ServerFile deserializeServerFile = new ServerFile();
        deserializeServerFile.deserialize(new BitStore(writer.getArray()));


        String rootPath = Gdx.files.absolute(workpath.file().getAbsolutePath()).parent().file().getAbsolutePath();

        assertRecursiveDir(workpath, root, rootPath);
        assertRecursiveDir(workpath, deserializeServerFile, rootPath);

    }


    public static void assertRecursiveDir(FileHandle fileHandle, ServerFile serverFile, String rootPath) throws PlatformAssertionError {
        if (!fileHandle.isDirectory()) {
            assertThat("FileName must Equals", fileHandle.name().equals(serverFile.getName()));

            String handleAbsolut = fileHandle.file().getAbsolutePath().replace(rootPath, "");
            String serverAbsolute = serverFile.getAbsolute();

            handleAbsolut = handleAbsolut.replace("\\", "/");
            serverAbsolute = serverAbsolute.replace("\\", "/");

            assertThat("FileAbsolute must Equals", handleAbsolut.equals(serverAbsolute));


            return;
        }
        FileHandle[] fileHandles = fileHandle.list();
        Array<ServerFile> serverFiles = serverFile.getFiles();
        assertThat("Dir size must be Equals", fileHandles.length == serverFiles.size);
        for (int i = 0, n = fileHandles.length; i < n; i++) {
            assertRecursiveDir(fileHandles[i], serverFiles.get(i), rootPath);
        }
    }

}
