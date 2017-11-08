package de.longri.cachebox3.socket.filebrowser;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class ServerFileTest {


    private FileHandle workpath;

    @BeforeEach
    void setUp() {
        TestUtils.initialGdx();
        workpath = TestUtils.getResourceFileHandle("testsResources");
    }


    @Test
    void getDirectory() {
        ServerFile root = ServerFile.getDirectory(workpath);
        assertThat("Root must be a Directory", root.isDirectory());
        assertRecursiveDir(workpath, root, workpath.parent().path());
    }

    @Test
    void serialize() throws NotImplementedException {

        ServerFile root = ServerFile.getDirectory(workpath);

        BitStore writer = new BitStore();
        root.serialize(writer);


        ServerFile deserializeServerFile = new ServerFile();
        deserializeServerFile.deserialize(new BitStore(writer.getArray()));


        String rootPath = workpath.parent().path();

        assertRecursiveDir(workpath, root, rootPath);
        assertRecursiveDir(workpath, deserializeServerFile, rootPath);

    }


    public static void assertRecursiveDir(FileHandle fileHandle, ServerFile serverFile, String rootPath) {
        if (!fileHandle.isDirectory()) {
            assertThat("FileName must Equals", fileHandle.name().equals(serverFile.getName()));

            String handleAbsolut = fileHandle.path().replace(rootPath, "");
            String serverAbsolute = serverFile.getAbsolute();

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