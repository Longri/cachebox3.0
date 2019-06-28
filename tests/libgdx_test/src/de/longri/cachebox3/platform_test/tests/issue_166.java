

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

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.gui.map.baseMap.BaseMapManager;
import de.longri.cachebox3.platform_test.AfterAll;
import de.longri.cachebox3.platform_test.BeforeAll;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.CreateCbDirectoryStructure;

import static de.longri.cachebox3.platform_test.Assert.assertEquals;
import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 10.11.2017.
 */
public class issue_166 {
//    Map selection shows no Map from default directory
//      /repository/maps
//      /repositories/xxxx/maps
//      /user-selected-directory


    private static FileHandle testWorkpath;
    private static String lastConfigValue = null;

    @BeforeAll
    public static void setUp() {
        TestUtils.initialGdx();


    }

    @AfterAll
    public static void tearDown() {
        testWorkpath.deleteDirectory();
        Config.MapPackFolder.setValue(lastConfigValue);
        Config.AcceptChanges();
    }

    @Test
    public void testMapCount() throws PlatformAssertionError {
        testWorkpath = TestUtils.getResourceFileHandle((TestUtils.isPlatformTest() ? CB.WorkPath : "") + "/testsResources", false).child("TestMapDirs");

        if (testWorkpath.exists()) testWorkpath.deleteDirectory();
        testWorkpath.mkdirs();

        //create CB3 folder struct on workPath
        new CreateCbDirectoryStructure(testWorkpath.file().getAbsolutePath(), true);

        // check created directory structure
        assertThat("Folder must exist '/user'", testWorkpath.child("user").exists());
        assertThat("Folder must exist '/repository'", testWorkpath.child("repository").exists());
        assertThat("Folder must exist '/repositories'", testWorkpath.child("repositories").exists());
        assertThat("Folder must exist '/data'", testWorkpath.child("data").exists());
        assertThat("Folder must exist '/user/temp'", testWorkpath.child("user").child("temp").exists());

        String PocketQueryFolder = Config.PocketQueryFolder.getDefaultValue().replace(CB.WorkPath, "");
        String TileCacheFolder = Config.TileCacheFolder.getDefaultValue().replace(CB.WorkPath, "");
        String TrackFolder = Config.TrackFolder.getDefaultValue().replace(CB.WorkPath, "");
        String UserImageFolder = Config.UserImageFolder.getDefaultValue().replace(CB.WorkPath, "");
        String DescriptionImageFolder = Config.DescriptionImageFolder.getDefaultValue().replace(CB.WorkPath, "");
        String MapPackFolder = Config.MapPackFolder.getDefaultValue().replace(CB.WorkPath, "");
        String SpoilerFolder = Config.SpoilerFolder.getDefaultValue().replace(CB.WorkPath, "");

        assertThat("Folder must exist '" + PocketQueryFolder + "'", testWorkpath.child(PocketQueryFolder).exists());
        assertThat("Folder must exist '" + TileCacheFolder + "'", testWorkpath.child(TileCacheFolder).exists());
        assertThat("Folder must exist '" + TrackFolder + "'", testWorkpath.child(TrackFolder).exists());
        assertThat("Folder must exist '" + UserImageFolder + "'", testWorkpath.child(UserImageFolder).exists());
        assertThat("Folder must exist '" + DescriptionImageFolder + "'", testWorkpath.child(DescriptionImageFolder).exists());
        assertThat("Folder must exist '" + MapPackFolder + "'", testWorkpath.child(MapPackFolder).exists());
        assertThat("Folder must exist '" + SpoilerFolder + "'", testWorkpath.child(SpoilerFolder).exists());


        //copy some test maps to test folder
        FileHandle pankowMap = TestUtils.getResourceFileHandle("testsResources/pankow.map", true);
        assertThat("File 'testsResources/pankow.map' must exist ", pankowMap.exists());

        FileHandle repositotyCopy = testWorkpath.child("repository").child("maps").child("reposetoryMap.map");
        repositotyCopy.parent().mkdirs();
        assertThat("Folder must exist", repositotyCopy.parent().exists());

        pankowMap.copyTo(repositotyCopy);

        FileHandle repositorieTestCopy = testWorkpath.child("Repositories").child("test").child("maps").child("reposetorieTestMap.map");
        repositorieTestCopy.parent().mkdirs();
        assertThat("Folder must exist", repositorieTestCopy.parent().exists());
        pankowMap.copyTo(repositorieTestCopy);

        Config.DatabaseName.setValue("test.db3");

        FileHandle myMapPacks = testWorkpath.child("MyMapPack");
        FileHandle myTestCopy = myMapPacks.child("MyTestMap.map");
        myTestCopy.parent().mkdirs();
        pankowMap.copyTo(myTestCopy);

        lastConfigValue = Config.MapPackFolder.getValue();
        Config.MapPackFolder.setValue(myMapPacks.file().getAbsolutePath());
        Config.AcceptChanges();

        BaseMapManager manager = new BaseMapManager();
        manager.refreshMaps();

        assertEquals(11, manager.size, "BasManager must found 11 maps");
        assertThat("Map 'Hike Bike' notFound", findMap(manager, "Hike Bike"));
        assertThat("Map 'Stamen Water Color' notFound", findMap(manager, "Stamen Water Color"));
        assertThat("Map 'Stamen Toner' notFound", findMap(manager, "Stamen Toner"));
        assertThat("Map 'opensciencemap' notFound", findMap(manager, "opensciencemap"));
        assertThat("Map 'Open Street Map Transport' notFound", findMap(manager, "Open Street Map Transport"));
        assertThat("Map 'Open Street Map' notFound", findMap(manager, "Open Street Map"));
        assertThat("Map 'Imagico Land Cover' notFound", findMap(manager, "Imagico Land Cover"));
        assertThat("Map 'Hike Bike Hill Shade' notFound", findMap(manager, "Hike Bike Hill Shade"));
        assertThat("Map 'reposetoryMap' notFound", findMap(manager, "reposetoryMap"));
        assertThat("Map 'reposetorieTestMap' notFound", findMap(manager, "reposetorieTestMap"));
        assertThat("Map 'MyTestMap' notFound", findMap(manager, "MyTestMap"));


    }

    private boolean findMap(BaseMapManager manager, String mapName) {
        int n = manager.size;
        while (n-- > 0) {
            if (manager.get(n).name.equals(mapName)) return true;
        }
        return false;
    }

}
