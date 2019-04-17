

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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.platform_test.AfterAll;
import de.longri.cachebox3.platform_test.BeforeAll;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import java.io.File;
import java.util.ArrayList;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 08.11.2017.
 */
public class FileBrowserClintTest {

    static FileHandle workpath;
    static FileBrowserClint clint;


    @BeforeAll
    public static void setUp() {
        TestUtils.initialGdx();
        workpath = TestUtils.getResourceFileHandle("testsResources", true).child("lang");
        if(!workpath.exists()){
            workpath = TestUtils.getResourceFileHandle("./", true).child("lang");
        }
        clint = new FileBrowserClint("", 0);
    }

    @AfterAll
    public static void tearDown() {
    }


    @Test
    public void mapFiles() throws PlatformAssertionError {

        ArrayList<File> files = new ArrayList<>();
        files.add(workpath.file());

        ServerFile path = new ServerFile("", "TestFolderTransfer", true);
        ServerFile workingDir = new ServerFile("", "testsResources", true);

        ObjectMap<String, FileHandle> fileMap = new ObjectMap<>();
        clint.addToFileList(fileMap, path, workingDir, files);

        assertThat("Map size must be 8, not " + fileMap.size, fileMap.size == 8);

    }

}
